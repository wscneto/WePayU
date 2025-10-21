package br.ufal.ic.p2.wepayu.services;

import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.cmds.*;
import br.ufal.ic.p2.wepayu.utils.FormatacaoMonetariaUtil;

import java.util.ArrayList;
import java.util.Map;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

public class LancaCartaoService {

        private Map<String, Empregado> empregados;
        private CmdManager cm;

        public LancaCartaoService(Map<String, Empregado> empregados, CmdManager cm) {
                this.empregados = empregados;
                this.cm = cm;
        }

        public void lancaCartao(String emp, String data, String horas)
                        throws Exception, RuntimeException {

                if (emp == null || emp.isBlank())
                        throw new IdentificacaoDoMembroNulaException("Identificacao do empregado nao pode ser nula.");
                if (data == null || data.isBlank())
                        throw new DataNaoPodeSerNulaException("Data nao pode ser nula.");
                if (horas == null || horas.isBlank())
                        throw new HorasNaoNumericasException("Horas nao podem ser nulas.");
                if (!empregados.containsKey(emp))
                        throw new EmpregadoNaoExisteException("Empregado nao existe.");

                try {
                        LocalDate.parse(data, DateTimeFormatter.ofPattern("d/M/yyyy"));
                } catch (Exception e) {
                        throw new DataInvalidaException("Data invalida.");
                }

                double nmrHoras;
                try {
                        nmrHoras = Double.parseDouble(horas.replace(",", "."));
                        if (nmrHoras <= 0)
                                throw new DataInvalidaException("Horas devem ser positivas.");
                } catch (NumberFormatException e) {
                        throw new DataInvalidaException("Horas devem ser numericas.");
                }

                LancarCartaoCmd command = new LancarCartaoCmd(emp, data, horas, empregados);
                cm.exec(command);
        }

        public void lancaVenda(String emp, String data, String valor)
                        throws Exception, RuntimeException {

                if (emp == null || emp.isBlank())
                        throw new IdentificacaoDoMembroNulaException("Identificacao do empregado nao pode ser nula.");
                if (data == null || data.isBlank())
                        throw new DataNaoPodeSerNulaException("Data nao pode ser nula.");
                if (valor == null || valor.isBlank())
                        throw new ValorNuloException("Valor nao pode ser nulo.");
                if (!empregados.containsKey(emp))
                        throw new EmpregadoNaoExisteException("Empregado nao existe.");

                try {
                        LocalDate.parse(data, DateTimeFormatter.ofPattern("d/M/yyyy"));
                } catch (Exception e) {
                        throw new DataInvalidaException("Data invalida.");
                }

                double nmrValor;
                try {
                        nmrValor = Double.parseDouble(valor.replace(",", "."));
                        if (nmrValor <= 0)
                                throw new ValorNegativoException("Valor deve ser positivo.");
                } catch (NumberFormatException e) {
                        throw new ValorNaoNumericoException("Valor deve ser numerico.");
                }

                LancarVendaCmd command = new LancarVendaCmd(emp, data, valor, empregados);
                cm.exec(command);
        }

        public String getHorasNormaisTrabalhadas(String emp, String dataInicial, String dataFinal)
                        throws Exception, RuntimeException {
                if (emp == null || emp.isBlank())
                        throw new IdentificacaoDoMembroNulaException("Identificacao do empregado nao pode ser nula.");
                if (!empregados.containsKey(emp))
                        throw new EmpregadoNaoExisteException("Empregado nao existe.");

                Empregado empregado = empregados.get(emp);

                if (!empregado.getTipo().equals("horista"))
                        throw new EmpregadoNaoEhHoristaException("Empregado nao eh horista.");

                LocalDate dtInicial = validarDataStrict(dataInicial, "Data inicial");
                LocalDate dtFinal = validarDataStrict(dataFinal, "Data final");

                if (dtInicial.isAfter(dtFinal))
                        throw new DataInvalidaException("Data inicial nao pode ser posterior aa data final.");

                ArrayList<CartaoPonto> cartoes = empregado.getCartoes();
                DateTimeFormatter formatador = DateTimeFormatter.ofPattern("d/M/yyyy");

                double horas = cartoes.stream()
                                .filter(cartao -> {
                                        LocalDate dataDoCartao = LocalDate.parse(cartao.getData(), formatador);
                                        boolean aposOuIgualInicio = !dataDoCartao.isBefore(dtInicial);
                                        boolean antesOuIgualFim = dataDoCartao.isBefore(dtFinal);
                                        return aposOuIgualInicio && antesOuIgualFim;
                                })
                                .mapToDouble(cartao -> Math.min(cartao.getHoras(), 8.0))
                                .sum();

                if (horas % 1 == 0) {
                        return String.format("%.0f", horas);
                } else {
                        return String.format("%.1f", horas).replace(".", ",");
                }
        }

        public String getHorasExtrasTrabalhadas(String emp, String dataInicial, String dataFinal)
                        throws Exception, RuntimeException {
                if (emp == null || emp.isBlank())
                        throw new IdentificacaoDoMembroNulaException("Identificacao do empregado nao pode ser nula.");
                if (!empregados.containsKey(emp))
                        throw new EmpregadoNaoExisteException("Empregado nao existe.");

                Empregado empregado = empregados.get(emp);

                if (!empregado.getTipo().equals("horista"))
                        throw new EmpregadoNaoEhHoristaException("Empregado nao eh horista.");

                LocalDate dtInicial = validarDataStrict(dataInicial, "Data inicial");
                LocalDate dtFinal = validarDataStrict(dataFinal, "Data final");

                if (dtInicial.isAfter(dtFinal)) {
                        throw new DataInvalidaException("Data inicial nao pode ser posterior aa data final.");
                }

                ArrayList<CartaoPonto> cartoes = empregado.getCartoes();
                DateTimeFormatter formatador = DateTimeFormatter.ofPattern("d/M/yyyy");

                double horas = cartoes.stream()
                                .filter(cartao -> {
                                        LocalDate dataDoCartao = LocalDate.parse(cartao.getData(), formatador);
                                        boolean aposOuIgualInicio = !dataDoCartao.isBefore(dtInicial);
                                        boolean antesOuIgualFim = dataDoCartao.isBefore(dtFinal);
                                        return aposOuIgualInicio && antesOuIgualFim;
                                })
                                .mapToDouble(cartao -> Math.max(0.0, cartao.getHoras() - 8.0))
                                .sum();

                if (horas % 1 == 0) {
                        return String.format("%.0f", horas);
                } else {
                        return String.format("%.1f", horas).replace(".", ",");
                }
        }

        public String getVendasRealizadas(String emp, String dataInicial, String dataFinal)
                        throws Exception, RuntimeException {
                if (emp == null || emp.isBlank())
                        throw new IdentificacaoDoMembroNulaException("Identificacao do empregado nao pode ser nula.");
                if (!empregados.containsKey(emp))
                        throw new EmpregadoNaoExisteException("Empregado nao existe.");

                Empregado empregado = empregados.get(emp);

                if (!empregado.getTipo().equals("comissionado"))
                        throw new TipoInvalidoException("Empregado nao eh comissionado.");

                LocalDate dtInicial = validarDataStrict(dataInicial, "Data inicial");
                LocalDate dtFinal = validarDataStrict(dataFinal, "Data final");

                if (dtInicial.isAfter(dtFinal)) {
                        throw new DataInvalidaException("Data inicial nao pode ser posterior aa data final.");
                }

                DateTimeFormatter formatador = DateTimeFormatter.ofPattern("d/M/yyyy");
                ArrayList<ResultadoDeVenda> vendas = empregado.getResultadoDeVenda();

                double valorTotalVendas = vendas.stream()
                                .filter(venda -> {
                                        LocalDate dataDaVenda = LocalDate.parse(venda.getData(), formatador);
                                        boolean aposOuIgualInicio = !dataDaVenda.isBefore(dtInicial);
                                        boolean antesOuIgualFim = dataDaVenda.isBefore(dtFinal);
                                        return aposOuIgualInicio && antesOuIgualFim;
                                })
                                .mapToDouble(ResultadoDeVenda::getValor)
                                .sum();

                return FormatacaoMonetariaUtil.formatValor(valorTotalVendas);
        }

        private LocalDate validarDataStrict(String data, String tipoData) throws Exception, RuntimeException {
                try {
                        DateTimeFormatter formatador = DateTimeFormatter.ofPattern("d/M/uuuu")
                                        .withResolverStyle(ResolverStyle.STRICT);
                        return LocalDate.parse(data, formatador);
                } catch (Exception e) {
                        throw new DataInvalidaException(tipoData + " invalida.");
                }
        }
}