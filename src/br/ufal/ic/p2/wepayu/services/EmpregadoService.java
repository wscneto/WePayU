package br.ufal.ic.p2.wepayu.services;

import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.factories.*;
import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.cmds.*;
import br.ufal.ic.p2.wepayu.utils.*;

import java.util.List;
import java.util.Map;

public class EmpregadoService {

        private final Map<String, Empregado> empregados;
        private final Map<String, MembroSindicato> membros;
        private int contadorId;
        private final CmdManager cmdManager;

        public EmpregadoService(Map<String, Empregado> empregados, Map<String, MembroSindicato> membrosSindicato,
                        int idInicial, CmdManager cmdManager) {
                this.empregados = empregados;
                this.membros = membrosSindicato;
                this.contadorId = idInicial;
                this.cmdManager = cmdManager;
        }

        // ---------------------------------------------------
        // MÉTODOS DE CRIAÇÃO
        // ---------------------------------------------------
        public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws Exception {
                validarCamposBasicos(nome, endereco, tipo);
                Empregado novo = EmpregadoFactory.criarEmpregado(tipo, nome, endereco, salario);
                String identificador = gerarNovoId();
                novo.setId(identificador);
                cmdManager.exec(new CriarEmpregadoCmd(novo, empregados));
                return identificador;
        }

        public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao)
                        throws Exception {
                validarCamposBasicos(nome, endereco, tipo);
                Empregado novo = EmpregadoFactory.criarEmpregado(tipo, nome, endereco, salario, comissao);
                String identificador = gerarNovoId();
                novo.setId(identificador);
                cmdManager.exec(new CriarEmpregadoCmd(novo, empregados));
                return identificador;
        }

        // ---------------------------------------------------
        // MÉTODOS DE ALTERAÇÃO
        // ---------------------------------------------------
        public void alteraEmpregado(String emp, String atributo, String valor) throws Exception {
                verificarEmpregadoExistente(emp);
                Map<String, String> parametros = Map.of("valor", valor);
                cmdManager.exec(new AlterarEmpregadoCmd(emp, atributo, parametros, empregados, membros));
        }

        public void alteraEmpregado(String emp, String atributo, String valor, String comissaoOuSalario)
                        throws Exception {
                verificarEmpregadoExistente(emp);

                Map<String, String> parametros;
                if ("tipo".equals(atributo)) {
                        parametros = "comissionado".equals(valor)
                                        ? Map.of("valor", valor, "comissao", comissaoOuSalario)
                                        : Map.of("valor", valor, "salario", comissaoOuSalario);
                } else if ("comissao".equals(atributo)) {
                        parametros = Map.of("valor", valor, "comissao", comissaoOuSalario);
                } else {
                        parametros = Map.of("valor", valor);
                }

                cmdManager.exec(new AlterarEmpregadoCmd(emp, atributo, parametros, empregados, membros));
        }

        public void alteraEmpregado(String emp, String atributo, String valor1, String banco,
                        String agencia, String contaCorrente) throws Exception {
                verificarEmpregadoExistente(emp);
                Map<String, String> dados = Map.of(
                                "valor1", valor1,
                                "banco", banco,
                                "agencia", agencia,
                                "contaCorrente", contaCorrente);
                cmdManager.exec(new AlterarEmpregadoCmd(emp, atributo, dados, empregados, membros));
        }

        public void alteraEmpregado(String emp, String atributo, String valor, String idSindicato, String taxaSindical)
                        throws Exception {
                verificarEmpregadoExistente(emp);
                Map<String, String> dados = Map.of(
                                "valor", valor,
                                "idSindicato", idSindicato,
                                "taxaSindical", taxaSindical);
                cmdManager.exec(new AlterarEmpregadoCmd(emp, atributo, dados, empregados, membros));
        }

        public void alteraEmpregado(String emp, String atributo, String valor1, String banco,
                        String agencia, String conta, String comissao) throws Exception {
                verificarEmpregadoExistente(emp);
                Map<String, String> dados = Map.of(
                                "valor1", valor1,
                                "banco", banco,
                                "agencia", agencia,
                                "contaCorrente", conta,
                                "comissao", comissao);
                cmdManager.exec(new AlterarEmpregadoCmd(emp, atributo, dados, empregados, membros));
        }

        // ---------------------------------------------------
        // REMOÇÃO
        // ---------------------------------------------------
        public void removerEmpregado(String emp) throws Exception {
                verificarEmpregadoExistente(emp);
                cmdManager.exec(new RemoverEmpregadoCmd(emp, empregados));
        }

        // ---------------------------------------------------
        // CONSULTAS
        // ---------------------------------------------------
        public String getEmpregadoPorNome(String nome, String indiceStr) throws Exception {
                if (nome == null || nome.isBlank())
                        throw new IdentificacaoDoMembroNulaException("Identificacao do empregado nao pode ser nula.");

                int indice;
                try {
                        indice = Integer.parseInt(indiceStr);
                } catch (NumberFormatException e) {
                        throw new IndiceNaoPodeSerNuloException("Indice deve ser numerico.");
                }

                List<Empregado> encontrados = empregados.values()
                                .stream()
                                .filter(e -> e.getNome().equals(nome))
                                .toList();

                if (encontrados.isEmpty() || indice < 1 || indice > encontrados.size()) {
                        throw new EmpregadoNaoExisteException("Nao ha empregado com esse nome.");
                }

                return encontrados.get(indice - 1).getId();
        }

        public String getAtributoEmpregado(String emp, String atributo) throws Exception {
                if (emp == null || emp.isBlank())
                        throw new IdentificacaoDoMembroNulaException("Identificacao do empregado nao pode ser nula.");
                if (atributo == null || atributo.isBlank())
                        throw new AtributoNaoPodeSerNuloException("Atributo nao pode ser nulo.");
                if (!empregados.containsKey(emp))
                        throw new EmpregadoNaoExisteException("Empregado nao existe.");

                Empregado e = empregados.get(emp);

                if ("nome".equals(atributo))
                        return e.getNome();
                if ("endereco".equals(atributo))
                        return e.getEndereco();
                if ("tipo".equals(atributo))
                        return e.getTipo();
                if ("salario".equals(atributo))
                        return e.getSalario();
                if ("sindicalizado".equals(atributo))
                        return e.getSindicato() != null ? "true" : "false";

                if ("idSindicato".equals(atributo)) {
                        if (e.getSindicato() == null)
                                throw new EmpregadoNaoEhSindicalizadoException("Empregado nao eh sindicalizado.");
                        return e.getSindicato().getIdMembro();
                }

                if ("taxaSindical".equals(atributo)) {
                        if (e.getSindicato() == null)
                                throw new EmpregadoNaoEhSindicalizadoException("Empregado nao eh sindicalizado.");
                        return FormatacaoMonetariaUtil.formatValor(e.getSindicato().getTaxaSindical());
                }

                if ("metodoPagamento".equals(atributo))
                        return e.getMetodoPagamento().getMetodoPagamento();

                if ("banco".equals(atributo)) {
                        if (e.getMetodoPagamento() instanceof Banco b)
                                return b.getBanco();
                        throw new EmpregadoNaoRecebeEmBancoException("Empregado nao recebe em banco.");
                }

                if ("agencia".equals(atributo)) {
                        if (e.getMetodoPagamento() instanceof Banco b)
                                return b.getAgencia();
                        throw new EmpregadoNaoRecebeEmBancoException("Empregado nao recebe em banco.");
                }

                if ("contaCorrente".equals(atributo)) {
                        if (e.getMetodoPagamento() instanceof Banco b)
                                return b.getContaCorrente();
                        throw new EmpregadoNaoRecebeEmBancoException("Empregado nao recebe em banco.");
                }

                if ("comissao".equals(atributo)) {
                        if (e instanceof Comissionado c)
                                return String.valueOf(c.getTaxaDeComissao()).replace('.', ',');
                        throw new EmpregadoNaoEhComissionadoException("Empregado nao eh comissionado.");
                }

                if ("agendaPagamento".equals(atributo))
                        return e.getAgendaPagamento().getAgenda();

                throw new AtributoNaoExisteException("Atributo nao existe.");
        }

        // ---------------------------------------------------
        // MÉTODOS AUXILIARES PRIVADOS
        // ---------------------------------------------------
        private void validarCamposBasicos(String nome, String endereco, String tipo) throws Exception {
                if (nome == null || nome.isBlank())
                        throw new NomeNuloException("Nome nao pode ser nulo.");
                if (endereco == null || endereco.isBlank())
                        throw new EnderecoNuloException("Endereco nao pode ser nulo.");
                if (tipo == null || tipo.isBlank())
                        throw new TipoNaoPodeSerNuloException("Tipo nao pode ser nulo.");
        }

        private void verificarEmpregadoExistente(String emp) throws Exception {
                if (emp == null || emp.isBlank())
                        throw new IdentificacaoDoMembroNulaException("Identificacao do empregado nao pode ser nula.");
                if (!empregados.containsKey(emp))
                        throw new EmpregadoNaoExisteException("Empregado nao existe.");
        }

        private String gerarNovoId() {
                return String.valueOf(contadorId++);
        }
}
