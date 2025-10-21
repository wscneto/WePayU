package br.ufal.ic.p2.wepayu.services;

import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.factories.*;
import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.cmds.*;

import java.util.List;
import java.util.Map;

public class EmpregadoService {

        private Map<String, Empregado> empregados;
        private Map<String, MembroSindicato> membrosSindicato;
        private int id;
        private CmdManager cm;

        public EmpregadoService(Map<String, Empregado> empregados,
                        Map<String, MembroSindicato> membrosSindicato,
                        int id,
                        CmdManager cm) {
                this.empregados = empregados;
                this.membrosSindicato = membrosSindicato;
                this.id = id;
                this.cm = cm;
        }

        public String criarEmpregado(String nome, String endereco, String tipo, String salario)
                        throws Exception, RuntimeException {

                if (nome == null || nome.isBlank())
                        throw new NomeNuloException("Nome nao pode ser nulo.");
                if (endereco == null || endereco.isBlank())
                        throw new EnderecoNuloException("Endereco nao pode ser nulo.");
                if (tipo == null || tipo.isBlank())
                        throw new TipoNaoPodeSerNuloException("Tipo nao pode ser nulo.");

                Empregado empregado = EmpregadoFactory.criarEmpregado(tipo, nome, endereco, salario);
                String idEmpregado = String.valueOf(id++);
                empregado.setId(idEmpregado);

                CriarEmpregadoCmd command = new CriarEmpregadoCmd(empregado, empregados);
                cm.exec(command);

                return idEmpregado;
        }

        public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao)
                        throws Exception, RuntimeException {

                if (nome == null || nome.isBlank())
                        throw new NomeNuloException("Nome nao pode ser nulo.");
                if (endereco == null || endereco.isBlank())
                        throw new EnderecoNuloException("Endereco nao pode ser nulo.");
                if (tipo == null || tipo.isBlank())
                        throw new TipoNaoPodeSerNuloException("Tipo nao pode ser nulo.");

                Empregado empregado = EmpregadoFactory.criarEmpregado(tipo, nome, endereco, salario, comissao);
                String idEmpregado = String.valueOf(id++);
                empregado.setId(idEmpregado);

                CriarEmpregadoCmd command = new CriarEmpregadoCmd(empregado, empregados);
                cm.exec(command);

                return idEmpregado;
        }

        public void alteraEmpregado(String emp, String atributo, String valor)
                        throws Exception, RuntimeException {

                if (emp == null || emp.isBlank())
                        throw new IdentificacaoDoMembroNulaException("Identificacao do empregado nao pode ser nula.");
                if (!empregados.containsKey(emp))
                        throw new EmpregadoNaoExisteException("Empregado nao existe.");

                Map<String, String> valores = Map.of("valor", valor);
                AlterarEmpregadoCmd command = new AlterarEmpregadoCmd(emp, atributo, valores, empregados,
                                membrosSindicato);
                cm.exec(command);
        }

        public void alteraEmpregado(String emp, String atributo, String valor, String comissao_salario)
                        throws Exception, RuntimeException {

                if (emp == null || emp.isBlank())
                        throw new IdentificacaoDoMembroNulaException("Identificacao do empregado nao pode ser nula.");
                if (!empregados.containsKey(emp))
                        throw new EmpregadoNaoExisteException("Empregado nao existe.");

                Map<String, String> valores;
                if ("tipo".equals(atributo)) {
                        if ("comissionado".equals(valor)) {
                                valores = Map.of("valor", valor, "comissao", comissao_salario);
                        } else {
                                valores = Map.of("valor", valor, "salario", comissao_salario);
                        }
                } else if ("comissao".equals(atributo)) {
                        valores = Map.of("valor", valor, "comissao", comissao_salario);
                } else {
                        valores = Map.of("valor", valor);
                }

                AlterarEmpregadoCmd command = new AlterarEmpregadoCmd(emp, atributo, valores, empregados,
                                membrosSindicato);
                cm.exec(command);
        }

        public void alteraEmpregado(String emp, String atributo, String valor1, String banco, String agencia,
                        String contaCorrente)
                        throws Exception, RuntimeException {

                if (emp == null || emp.isBlank())
                        throw new IdentificacaoDoMembroNulaException("Identificacao do empregado nao pode ser nula.");
                if (!empregados.containsKey(emp))
                        throw new EmpregadoNaoExisteException("Empregado nao existe.");

                Map<String, String> valores = Map.of("valor1", valor1, "banco", banco, "agencia", agencia,
                                "contaCorrente",
                                contaCorrente);
                AlterarEmpregadoCmd command = new AlterarEmpregadoCmd(emp, atributo, valores, empregados,
                                membrosSindicato);
                cm.exec(command);
        }

        public void alteraEmpregado(String emp, String atributo, String valor, String idSindicato, String taxaSindical)
                        throws Exception, RuntimeException {

                if (emp == null || emp.isBlank())
                        throw new IdentificacaoDoMembroNulaException("Identificacao do empregado nao pode ser nula.");
                if (!empregados.containsKey(emp))
                        throw new EmpregadoNaoExisteException("Empregado nao existe.");

                Map<String, String> valores = Map.of("valor", valor, "idSindicato", idSindicato, "taxaSindical",
                                taxaSindical);
                AlterarEmpregadoCmd command = new AlterarEmpregadoCmd(emp, atributo, valores, empregados,
                                membrosSindicato);
                cm.exec(command);
        }

        public void alteraEmpregado(String emp, String atributo, String valor1, String banco, String agencia,
                        String contaCorrente, String comissao)
                        throws Exception, RuntimeException {

                if (emp == null || emp.isBlank())
                        throw new IdentificacaoDoMembroNulaException("Identificacao do empregado nao pode ser nula.");
                if (!empregados.containsKey(emp))
                        throw new EmpregadoNaoExisteException("Empregado nao existe.");

                Map<String, String> valores = Map.of("valor1", valor1, "banco", banco, "agencia", agencia,
                                "contaCorrente",
                                contaCorrente, "comissao", comissao);
                AlterarEmpregadoCmd command = new AlterarEmpregadoCmd(emp, atributo, valores, empregados,
                                membrosSindicato);
                cm.exec(command);
        }

        public void removerEmpregado(String emp)
                        throws Exception, RuntimeException {
                if (emp == null || emp.isBlank())
                        throw new IdentificacaoDoMembroNulaException("Identificacao do empregado nao pode ser nula.");
                if (!empregados.containsKey(emp))
                        throw new EmpregadoNaoExisteException("Empregado nao existe.");

                RemoverEmpregadoCmd command = new RemoverEmpregadoCmd(emp, empregados);
                cm.exec(command);
        }

        public String getEmpregadoPorNome(String emp, String indice)
                        throws Exception, RuntimeException {
                if (emp == null || emp.isBlank())
                        throw new IdentificacaoDoMembroNulaException("Identificacao do empregado nao pode ser nula.");

                int indiceInt;
                try {
                        indiceInt = Integer.parseInt(indice);
                } catch (NumberFormatException e) {
                        throw new IndiceNaoPodeSerNuloException("Indice deve ser numerico.");
                }

                List<Empregado> encontrados = empregados.values().stream()
                                .filter(empregado -> empregado.getNome().equals(emp))
                                .toList();

                if (encontrados.isEmpty())
                        throw new EmpregadoNaoExisteException("Nao ha empregado com esse nome.");

                if (indiceInt < 1 || indiceInt > encontrados.size())
                        throw new EmpregadoNaoExisteException("Nao ha empregado com esse nome.");

                Empregado escolhido = encontrados.get(indiceInt - 1);

                return escolhido.getId();
        }

        public String getAtributoEmpregado(String emp, String atributo)
                        throws Exception, RuntimeException {

                if (emp == null || emp.isBlank())
                        throw new IdentificacaoDoMembroNulaException("Identificacao do empregado nao pode ser nula.");
                if (atributo == null || atributo.isBlank())
                        throw new AtributoNaoPodeSerNuloException("Atributo nao pode ser nulo.");
                if (!empregados.containsKey(emp))
                        throw new EmpregadoNaoExisteException("Empregado nao existe.");

                Empregado empregado = empregados.get(emp);

                switch (atributo) {
                        case "nome":
                                return empregado.getNome();
                        case "endereco":
                                return empregado.getEndereco();
                        case "tipo":
                                return empregado.getTipo();
                        case "salario":
                                return empregado.getSalario();
                        case "sindicalizado":
                                return empregado.getSindicato() != null ? "true" : "false";
                        case "idSindicato":
                                if (empregado.getSindicato() == null) {
                                        throw new br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoEhSindicalizadoException(
                                                        "Empregado nao eh sindicalizado.");
                                }
                                return empregado.getSindicato().getIdMembro();
                        case "taxaSindical":
                                if (empregado.getSindicato() == null) {
                                        throw new br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoEhSindicalizadoException(
                                                        "Empregado nao eh sindicalizado.");
                                }
                                return br.ufal.ic.p2.wepayu.utils.FormatacaoMonetariaUtil
                                                .formatValor(empregado.getSindicato().getTaxaSindical());
                        case "metodoPagamento":
                                return empregado.getMetodoPagamento().getMetodoPagamento();
                        case "banco":
                                if (empregado.getMetodoPagamento() instanceof Banco) {
                                        return ((Banco) empregado.getMetodoPagamento()).getBanco();
                                }
                                throw new br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoRecebeEmBancoException(
                                                "Empregado nao recebe em banco.");
                        case "agencia":
                                if (empregado.getMetodoPagamento() instanceof Banco) {
                                        return ((Banco) empregado.getMetodoPagamento()).getAgencia();
                                }
                                throw new br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoRecebeEmBancoException(
                                                "Empregado nao recebe em banco.");
                        case "contaCorrente":
                                if (empregado.getMetodoPagamento() instanceof Banco) {
                                        return ((Banco) empregado.getMetodoPagamento()).getContaCorrente();
                                }
                                throw new br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoRecebeEmBancoException(
                                                "Empregado nao recebe em banco.");
                        case "comissao":
                                if (empregado instanceof Comissionado) {
                                        return String.valueOf(((Comissionado) empregado).getTaxaDeComissao())
                                                        .replace('.', ',');
                                }
                                throw new br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoEhComissionadoException(
                                                "Empregado nao eh comissionado.");
                        case "agendaPagamento":
                                return empregado.getAgendaPagamento().getAgenda();
                        default:
                                throw new AtributoNaoExisteException("Atributo nao existe.");
                }
        }
}