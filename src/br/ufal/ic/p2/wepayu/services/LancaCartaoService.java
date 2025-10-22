package br.ufal.ic.p2.wepayu.services;

import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.cmds.*;
import br.ufal.ic.p2.wepayu.utils.*;

import java.util.Map;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

public class LancaCartaoService {

        private final Map<String, Empregado> empregados;
        private final CmdManager cmdManager;

        public LancaCartaoService(Map<String, Empregado> empregados, CmdManager cmdManager) {
                this.empregados = empregados;
                this.cmdManager = cmdManager;
        }

        // ================================================================
        // Lançamento de cartão de ponto
        // ================================================================
        public void lancaCartao(String emp, String data, String horas) throws Exception {
                validarCamposBasicos(emp, data, horas);
                validarEmpregadoExiste(emp);
                validarFormatoData(data);

                double totalHoras = converterHoras(horas);
                if (totalHoras <= 0)
                        throw new DataInvalidaException("Horas devem ser positivas.");

                cmdManager.exec(new LancarCartaoCmd(emp, data, horas, empregados));
        }

        // ================================================================
        // Lançamento de venda
        // ================================================================
        public void lancaVenda(String emp, String data, String valor) throws Exception {
                validarCamposBasicos(emp, data, valor);
                validarEmpregadoExiste(emp);
                validarFormatoData(data);

                double valorVenda = converterValorMonetario(valor);
                if (valorVenda <= 0)
                        throw new ValorNegativoException("Valor deve ser positivo.");

                cmdManager.exec(new LancarVendaCmd(emp, data, valor, empregados));
        }

        // ================================================================
        // Horas normais trabalhadas
        // ================================================================
        public String getHorasNormaisTrabalhadas(String emp, String dataInicial, String dataFinal) throws Exception {
                Empregado e = obterEmpregadoHorista(emp);
                LocalDate inicio = parseDataEstrita(dataInicial, "Data inicial");
                LocalDate fim = parseDataEstrita(dataFinal, "Data final");

                if (inicio.isAfter(fim))
                        throw new DataInvalidaException("Data inicial nao pode ser posterior aa data final.");

                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d/M/yyyy");
                double horas = e.getCartoes().stream()
                                .filter(c -> dentroDoPeriodo(c.getData(), fmt, inicio, fim))
                                .mapToDouble(c -> Math.min(c.getHoras(), 8.0))
                                .sum();

                return formatarHoras(horas);
        }

        // ================================================================
        // Horas extras trabalhadas
        // ================================================================
        public String getHorasExtrasTrabalhadas(String emp, String dataInicial, String dataFinal) throws Exception {
                Empregado e = obterEmpregadoHorista(emp);
                LocalDate inicio = parseDataEstrita(dataInicial, "Data inicial");
                LocalDate fim = parseDataEstrita(dataFinal, "Data final");

                if (inicio.isAfter(fim))
                        throw new DataInvalidaException("Data inicial nao pode ser posterior aa data final.");

                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d/M/yyyy");
                double horasExtras = e.getCartoes().stream()
                                .filter(c -> dentroDoPeriodo(c.getData(), fmt, inicio, fim))
                                .mapToDouble(c -> Math.max(0, c.getHoras() - 8.0))
                                .sum();

                return formatarHoras(horasExtras);
        }

        // ================================================================
        // Vendas realizadas
        // ================================================================
        public String getVendasRealizadas(String emp, String dataInicial, String dataFinal) throws Exception {
                Empregado e = obterEmpregadoComissionado(emp);
                LocalDate inicio = parseDataEstrita(dataInicial, "Data inicial");
                LocalDate fim = parseDataEstrita(dataFinal, "Data final");

                if (inicio.isAfter(fim))
                        throw new DataInvalidaException("Data inicial nao pode ser posterior aa data final.");

                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d/M/yyyy");
                double total = e.getResultadoDeVenda().stream()
                                .filter(v -> dentroDoPeriodo(v.getData(), fmt, inicio, fim))
                                .mapToDouble(ResultadoDeVenda::getValor)
                                .sum();

                return FormatacaoMonetariaUtil.formatValor(total);
        }

        // ================================================================
        // MÉTODOS AUXILIARES PRIVADOS
        // ================================================================

        private void validarCamposBasicos(String emp, String data, String valorOuHoras) throws Exception {
                if (emp == null || emp.isBlank())
                        throw new IdentificacaoDoMembroNulaException("Identificacao do empregado nao pode ser nula.");
                if (data == null || data.isBlank())
                        throw new DataNaoPodeSerNulaException("Data nao pode ser nula.");
                if (valorOuHoras == null || valorOuHoras.isBlank())
                        throw new ValorNuloException("Valor nao pode ser nulo.");
        }

        private void validarEmpregadoExiste(String emp) throws EmpregadoNaoExisteException {
                if (!empregados.containsKey(emp))
                        throw new EmpregadoNaoExisteException("Empregado nao existe.");
        }

        private void validarFormatoData(String data) throws DataInvalidaException {
                try {
                        LocalDate.parse(data, DateTimeFormatter.ofPattern("d/M/yyyy"));
                } catch (Exception e) {
                        throw new DataInvalidaException("Data invalida.");
                }
        }

        private double converterHoras(String horas) throws DataInvalidaException {
                try {
                        return Double.parseDouble(horas.replace(",", "."));
                } catch (NumberFormatException e) {
                        throw new DataInvalidaException("Horas devem ser numericas.");
                }
        }

        private double converterValorMonetario(String valor) throws ValorNaoNumericoException {
                try {
                        return Double.parseDouble(valor.replace(",", "."));
                } catch (NumberFormatException e) {
                        throw new ValorNaoNumericoException("Valor deve ser numerico.");
                }
        }

        private Empregado obterEmpregadoHorista(String emp) throws Exception {
                if (emp == null || emp.isBlank())
                        throw new IdentificacaoDoMembroNulaException("Identificacao do empregado nao pode ser nula.");
                validarEmpregadoExiste(emp);

                Empregado e = empregados.get(emp);
                if (!"horista".equals(e.getTipo()))
                        throw new EmpregadoNaoEhHoristaException("Empregado nao eh horista.");

                return e;
        }

        private Empregado obterEmpregadoComissionado(String emp) throws Exception {
                if (emp == null || emp.isBlank())
                        throw new IdentificacaoDoMembroNulaException("Identificacao do empregado nao pode ser nula.");
                validarEmpregadoExiste(emp);

                Empregado e = empregados.get(emp);
                if (!"comissionado".equals(e.getTipo()))
                        throw new TipoInvalidoException("Empregado nao eh comissionado.");

                return e;
        }

        private boolean dentroDoPeriodo(String data, DateTimeFormatter fmt, LocalDate inicio, LocalDate fim) {
                LocalDate d = LocalDate.parse(data, fmt);
                return (!d.isBefore(inicio)) && d.isBefore(fim);
        }

        private LocalDate parseDataEstrita(String data, String label) throws DataInvalidaException {
                try {
                        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d/M/uuuu")
                                        .withResolverStyle(ResolverStyle.STRICT);
                        return LocalDate.parse(data, fmt);
                } catch (Exception e) {
                        throw new DataInvalidaException(label + " invalida.");
                }
        }

        private String formatarHoras(double horas) {
                return (horas % 1 == 0)
                                ? String.format("%.0f", horas)
                                : String.format("%.1f", horas).replace(".", ",");
        }
}
