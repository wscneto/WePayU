package br.ufal.ic.p2.wepayu.services;

import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.cmds.*;
import br.ufal.ic.p2.wepayu.factories.*;
import br.ufal.ic.p2.wepayu.utils.*;

import java.util.Map;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;

public class SindicatoService {

        private final Map<String, MembroSindicato> membrosSindicato;
        private final Map<String, Empregado> empregados;
        private final CmdManager cmdManager;

        public SindicatoService(Map<String, MembroSindicato> membros, Map<String, Empregado> empregados,
                        CmdManager cmdManager) {
                this.membrosSindicato = membros;
                this.empregados = empregados;
                this.cmdManager = cmdManager;
        }

        // ==============================================================
        // Criação de membro sindical
        // ==============================================================
        public MembroSindicato criarMembro(String id, String taxa) throws Exception {
                MembroSindicato novo = MembroSindicatoFactory.criarMembro(id, taxa);
                cmdManager.exec(new CriarMembroSindicatoCmd(novo, membrosSindicato));
                return novo;
        }

        // ==============================================================
        // Lançamento de taxa de serviço
        // ==============================================================
        public void lancaTaxaServico(String membro, String data, String valor) throws Exception {
                verificarCamposTaxa(membro, data, valor);

                validarData(data, "Data");

                double valorNumerico = parseValor(valor);
                if (valorNumerico <= 0) {
                        throw new ValorNegativoException("Valor deve ser positivo.");
                }

                cmdManager.exec(new LancarTaxaServicoCmd(membro, data, valor, membrosSindicato));
        }

        // ==============================================================
        // Remoção de membro sindical
        // ==============================================================
        public void removerMembro(String id) throws Exception {
                if (id == null || id.isBlank()) {
                        throw new IdentificacaoDoSindicatoNulaException("Identificacao do membro nao pode ser nula.");
                }
                if (!membrosSindicato.containsKey(id)) {
                        throw new MembroNaoExisteException("Membro nao encontrado.");
                }
                membrosSindicato.remove(id);
        }

        // ==============================================================
        // Recuperar membro sindical
        // ==============================================================
        public MembroSindicato getMembro(String id) throws Exception {
                if (id == null || id.isBlank()) {
                        throw new IdentificacaoDoSindicatoNulaException("Identificacao do membro nao pode ser nula.");
                }
                MembroSindicato membro = membrosSindicato.get(id);
                if (membro == null) {
                        throw new MembroNaoExisteException("Membro nao encontrado.");
                }
                return membro;
        }

        // ==============================================================
        // Consulta de taxas de serviço de um empregado sindicalizado
        // ==============================================================
        public String getTaxasServico(String emp, String dataInicial, String dataFinal) throws Exception {
                validarParametrosEmpregado(emp, dataInicial, dataFinal);

                Empregado funcionario = empregados.get(emp);
                if (funcionario.getSindicato() == null) {
                        throw new TipoInvalidoException("Empregado nao eh sindicalizado.");
                }

                LocalDate inicio = validarData(dataInicial, "Data inicial");
                LocalDate fim = validarData(dataFinal, "Data final");

                if (inicio.isAfter(fim)) {
                        throw new DataInvalidaException("Data inicial nao pode ser posterior aa data final.");
                }

                ArrayList<TaxaServico> taxas = funcionario.getSindicato().getTaxasDeServicos();
                DateTimeFormatter formato = DateTimeFormatter.ofPattern("d/M/yyyy");

                double total = taxas.stream()
                                .filter(t -> dentroDoPeriodo(t.getData(), formato, inicio, fim))
                                .mapToDouble(TaxaServico::getValor)
                                .sum();

                return FormatacaoMonetariaUtil.formatValor(total);
        }

        // ==============================================================
        // MÉTODOS AUXILIARES PRIVADOS
        // ==============================================================

        private void verificarCamposTaxa(String membro, String data, String valor) throws Exception {
                if (membro == null || membro.isBlank()) {
                        throw new IdentificacaoDoSindicatoNulaException("Identificacao do membro nao pode ser nula.");
                }
                if (data == null || data.isBlank()) {
                        throw new DataNaoPodeSerNulaException("Data nao pode ser nula.");
                }
                if (valor == null || valor.isBlank()) {
                        throw new ValorNuloException("Valor nao pode ser nulo.");
                }
        }

        private void validarParametrosEmpregado(String emp, String inicio, String fim) throws Exception {
                if (emp == null || emp.isBlank()) {
                        throw new IdentificacaoDoMembroNulaException("Identificacao do empregado nao pode ser nula.");
                }
                if (inicio == null || inicio.isBlank()) {
                        throw new DataNaoPodeSerNulaException("Data inicial nao pode ser nula.");
                }
                if (fim == null || fim.isBlank()) {
                        throw new DataNaoPodeSerNulaException("Data final nao pode ser nula.");
                }
                if (!empregados.containsKey(emp)) {
                        throw new EmpregadoNaoExisteException("Empregado nao existe.");
                }
        }

        private double parseValor(String valor) throws ValorNaoNumericoException {
                try {
                        return Double.parseDouble(valor.replace(",", "."));
                } catch (NumberFormatException e) {
                        throw new ValorNaoNumericoException("Valor deve ser numerico.");
                }
        }

        private boolean dentroDoPeriodo(String data, DateTimeFormatter formato, LocalDate inicio, LocalDate fim) {
                LocalDate atual = LocalDate.parse(data, formato);
                return !atual.isBefore(inicio) && atual.isBefore(fim);
        }

        private LocalDate validarData(String data, String tipoData) throws DataInvalidaException {
                try {
                        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("d/M/uuuu")
                                        .withResolverStyle(ResolverStyle.STRICT);
                        return LocalDate.parse(data, fmt);
                } catch (Exception e) {
                        throw new DataInvalidaException(tipoData + " invalida.");
                }
        }
}
