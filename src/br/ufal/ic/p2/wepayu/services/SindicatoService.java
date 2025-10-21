package br.ufal.ic.p2.wepayu.services;

import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.cmds.*;
import br.ufal.ic.p2.wepayu.factories.MembroSindicatoFactory;
import br.ufal.ic.p2.wepayu.utils.FormatacaoMonetariaUtil;

import java.time.format.ResolverStyle;
import java.util.Map;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class SindicatoService {

        private Map<String, MembroSindicato> membrosSindicato;
        private Map<String, Empregado> empregados;
        private CmdManager cm;

        public SindicatoService(Map<String, MembroSindicato> membrosSindicato,
                        Map<String, Empregado> empregados,
                        CmdManager cm) {
                this.membrosSindicato = membrosSindicato;
                this.empregados = empregados;
                this.cm = cm;
        }

        public MembroSindicato criarMembro(String id, String taxa)
                        throws Exception, RuntimeException {

                MembroSindicato membro = MembroSindicatoFactory.criarMembro(id, taxa);
                CriarMembroSindicatoCmd command = new CriarMembroSindicatoCmd(membro, membrosSindicato);
                cm.exec(command);

                return membro;
        }

        public void lancaTaxaServico(String membro, String data, String valor)
                        throws Exception, RuntimeException {

                if (membro == null || membro.isBlank())
                        throw new IdentificacaoDoSindicatoNulaException("Identificacao do membro nao pode ser nula.");
                if (data == null || data.isBlank())
                        throw new DataNaoPodeSerNulaException("Data nao pode ser nula.");
                if (valor == null || valor.isBlank())
                        throw new ValorNuloException("Valor nao pode ser nulo.");

                try {
                        validarDataStrict(data, "Data");
                } catch (DataInvalidaException e) {
                        throw e;
                }

                double nmrValor;
                try {
                        nmrValor = Double.parseDouble(valor.replace(",", "."));
                        if (nmrValor <= 0)
                                throw new ValorNegativoException("Valor deve ser positivo.");
                } catch (NumberFormatException e) {
                        throw new ValorNaoNumericoException("Valor deve ser numerico.");
                }

                LancarTaxaServicoCmd command = new LancarTaxaServicoCmd(membro, data, valor, membrosSindicato);
                cm.exec(command);
        }

        public void removerMembro(String id)
                        throws Exception, RuntimeException {

                if (id == null || id.isBlank()) {
                        throw new IdentificacaoDoSindicatoNulaException("Identificacao do membro nao pode ser nula.");
                }

                if (!membrosSindicato.containsKey(id)) {
                        throw new MembroNaoExisteException("Membro nao encontrado.");
                }

                membrosSindicato.remove(id);
        }

        public MembroSindicato getMembro(String id)
                        throws Exception, RuntimeException {

                if (id == null || id.isBlank()) {
                        throw new IdentificacaoDoSindicatoNulaException("Identificacao do membro nao pode ser nula.");
                }

                MembroSindicato membro = membrosSindicato.get(id);
                if (membro == null) {
                        throw new MembroNaoExisteException("Membro nao encontrado.");
                }

                return membro;
        }

        public String getTaxasServico(String emp, String dataInicial, String dataFinal)
                        throws Exception, RuntimeException {
                if (emp == null || emp.isBlank())
                        throw new IdentificacaoDoMembroNulaException("Identificacao do empregado nao pode ser nula.");
                if (dataInicial == null || dataInicial.isBlank())
                        throw new DataNaoPodeSerNulaException("Data inicial nao pode ser nula.");
                if (dataFinal == null || dataFinal.isBlank())
                        throw new DataNaoPodeSerNulaException("Data final nao pode ser nula.");
                if (!empregados.containsKey(emp))
                        throw new EmpregadoNaoExisteException("Empregado nao existe.");

                Empregado empregado = empregados.get(emp);

                if (empregado.getSindicato() == null)
                        throw new TipoInvalidoException("Empregado nao eh sindicalizado.");

                LocalDate dtInicial, dtFinal;
                try {
                        dtInicial = validarDataStrict(dataInicial, "Data inicial");
                } catch (DataInvalidaException e) {
                        throw e;
                }

                try {
                        dtFinal = validarDataStrict(dataFinal, "Data final");
                } catch (DataInvalidaException e) {
                        throw e;
                }

                if (dtInicial.isAfter(dtFinal))
                        throw new DataInvalidaException("Data inicial nao pode ser posterior aa data final.");

                ArrayList<TaxaServico> taxas = empregado.getSindicato().getTaxasDeServicos();
                DateTimeFormatter formatador = DateTimeFormatter.ofPattern("d/M/yyyy");

                double valorTotalTaxas = taxas.stream()
                                .filter(taxa -> {
                                        LocalDate dataDaTaxa = LocalDate.parse(taxa.getData(), formatador);
                                        boolean aposOuIgualInicio = !dataDaTaxa.isBefore(dtInicial);
                                        boolean antesOuIgualFim = dataDaTaxa.isBefore(dtFinal);
                                        return aposOuIgualInicio && antesOuIgualFim;
                                })
                                .mapToDouble(TaxaServico::getValor)
                                .sum();

                return FormatacaoMonetariaUtil.formatValor(valorTotalTaxas);
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