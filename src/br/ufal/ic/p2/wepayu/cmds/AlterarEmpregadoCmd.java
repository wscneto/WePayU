package br.ufal.ic.p2.wepayu.cmds;

import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.factories.*;
import java.util.Map;

public class AlterarEmpregadoCmd implements Cmd {
    private final String empId;
    private final String atributo;
    private final Map<String, String> valores;
    private final Map<String, Empregado> empregados;
    private final Map<String, MembroSindicato> membrosSindicato;

    private Object valorAnterior;

    public AlterarEmpregadoCmd(String empId, String atributo, Map<String, String> valores,
            Map<String, Empregado> empregados,
            Map<String, MembroSindicato> membrosSindicato) {
        this.empId = empId;
        this.atributo = atributo;
        this.valores = valores;
        this.empregados = empregados;
        this.membrosSindicato = membrosSindicato;
    }

    @Override
    public void exec() {
        try {
            Empregado empregado = empregados.get(empId);
            if (empregado == null)
                throw new EmpregadoNaoEncontradoException("Empregado não encontrado.");

            valorAnterior = obterValorAtual(empregado, atributo);

            aplicarAlteracao(empregado, atributo, valores);
        } catch (EmpregadoNaoEncontradoException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void undo() {
        Empregado empregado = empregados.get(empId);
        if (empregado != null) {
            if (valorAnterior != null)
                restaurarValorAnterior(empregado, atributo, valorAnterior);
            else
                restaurarValorAnterior(empregado, atributo, null);

        }
    }

    private Object obterValorAtual(Empregado empregado, String atributo) {
        switch (atributo) {
            case "nome":
                return empregado.getNome();
            case "endereco":
                return empregado.getEndereco();
            case "tipo":
                return empregado.getTipo();
            case "salario":
                return empregado.getSalario();
            case "comissao":
                if (empregado instanceof Comissionado)
                    return ((Comissionado) empregado).getTaxaDeComissao();

                return null;
            case "sindicalizado":
                return empregado.getSindicato();
            case "idSindicato":
                return empregado.getSindicato() != null ? empregado.getSindicato().getIdMembro() : null;
            case "taxaSindical":
                return empregado.getSindicato() != null ? empregado.getSindicato().getTaxaSindical() : null;
            case "metodoPagamento":
                return empregado.getMetodoPagamento();
            case "banco":
                if (empregado.getMetodoPagamento() instanceof Banco)
                    return ((Banco) empregado.getMetodoPagamento()).getBanco();

                return null;
            case "agencia":
                if (empregado.getMetodoPagamento() instanceof Banco)
                    return ((Banco) empregado.getMetodoPagamento()).getAgencia();

                return null;
            case "contaCorrente":
                if (empregado.getMetodoPagamento() instanceof Banco)
                    return ((Banco) empregado.getMetodoPagamento()).getContaCorrente();

                return null;
            case "agendaPagamento":
                return empregado.getAgendaPagamento();
            default:
                return null;
        }
    }

    private void aplicarAlteracao(Empregado empregado, String atributo, Map<String, String> valores)
            throws Exception, RuntimeException {
        String valor = valores.get("valor");
        String valor1 = valores.get("valor1");

        switch (atributo) {
            case "nome":
                if (valor == null || valor.isBlank())
                    throw new NomeNuloException("Nome nao pode ser nulo.");

                empregado.setNome(valor);
                break;
            case "endereco":
                if (valor == null || valor.isBlank())
                    throw new EnderecoNuloException("Endereco nao pode ser nulo.");

                empregado.setEndereco(valor);
                break;
            case "tipo":
                String novoSalario = valores.get("salario");
                if (novoSalario == null)
                    novoSalario = empregado.getSalario().replace(",", ".");

                String nome = empregado.getNome();
                String endereco = empregado.getEndereco();
                MembroSindicato sindicatoAnterior = empregado.getSindicato();
                MetodoPagamento metodoPagamentoAnterior = empregado.getMetodoPagamento();

                Empregado novoEmpregado;
                if ("comissionado".equals(valor)) {
                    String comissao = valores.get("comissao");
                    if (comissao == null)
                        throw new ComissaoNulaException("Comissao nao pode ser nula.");

                    novoEmpregado = EmpregadoFactory.criarEmpregado(valor, nome, endereco, novoSalario, comissao);
                } else
                    novoEmpregado = EmpregadoFactory.criarEmpregado(valor, nome, endereco, novoSalario);

                novoEmpregado.setId(empregado.getId());
                novoEmpregado.setSindicato(sindicatoAnterior);
                novoEmpregado.setMetodoPagamento(metodoPagamentoAnterior);

                empregados.put(empId, novoEmpregado);
                break;
            case "salario":
                if (valor == null || valor.isBlank())
                    throw new SalarioNuloException("Salario nao pode ser nulo.");

                double salarioNumerico;
                try {
                    salarioNumerico = Double.parseDouble(valor.replace(",", "."));
                } catch (NumberFormatException e) {
                    throw new SalarioNaoNumericoException("Salario deve ser numerico.");
                }

                if (salarioNumerico < 0)
                    throw new SalarioNegativoException("Salario deve ser nao-negativo.");

                String tipoAtualSalario = empregado.getTipo();
                String nomeSalario = empregado.getNome();
                String enderecoSalario = empregado.getEndereco();
                MembroSindicato sindicatoAnteriorSalario = empregado.getSindicato();
                MetodoPagamento metodoPagamentoAnteriorSalario = empregado.getMetodoPagamento();

                Empregado novoEmpregadoSalario;
                if ("comissionado".equals(tipoAtualSalario)) {
                    double comissaoAtual = ((Comissionado) empregado)
                            .getTaxaDeComissao();
                    novoEmpregadoSalario = EmpregadoFactory.criarEmpregado(tipoAtualSalario, nomeSalario,
                            enderecoSalario, valor, String.valueOf(comissaoAtual));
                } else
                    novoEmpregadoSalario = EmpregadoFactory.criarEmpregado(tipoAtualSalario, nomeSalario,
                            enderecoSalario, valor);

                novoEmpregadoSalario.setId(empregado.getId());
                novoEmpregadoSalario.setSindicato(sindicatoAnteriorSalario);
                novoEmpregadoSalario.setMetodoPagamento(metodoPagamentoAnteriorSalario);

                empregados.put(empId, novoEmpregadoSalario);
                break;
            case "comissao":
                if (valor == null || valor.isBlank())
                    throw new ComissaoNulaException("Comissao nao pode ser nula.");

                if (empregado instanceof Comissionado) {
                    double comissaoNumerica;
                    try {
                        comissaoNumerica = Double.parseDouble(valor.replace(",", "."));
                    } catch (NumberFormatException e) {
                        throw new ComissaoNaoNumericaException("Comissao deve ser numerica.");
                    }

                    if (comissaoNumerica < 0)
                        throw new ComissaoNegativaException("Comissao deve ser nao-negativa.");

                    ((Comissionado) empregado).setTaxaDeComissao(comissaoNumerica);
                } else {
                    throw new EmpregadoNaoEhComissionadoException(
                            "Empregado nao eh comissionado.");
                }
                break;
            case "sindicalizado":
                if (!"true".equals(valor) && !"false".equals(valor))
                    throw new ValorDeveSerTrueOuFalseException("Valor deve ser true ou false.");

                boolean sindicalizado = Boolean.parseBoolean(valor);
                if (sindicalizado) {
                    String idSindicato = valores.get("idSindicato");
                    String taxaSindical = valores.get("taxaSindical");

                    if (idSindicato == null || idSindicato.isBlank())
                        throw new IdentificacaoDoSindicatoNulaException(
                                "Identificacao do sindicato nao pode ser nula.");

                    if (taxaSindical == null || taxaSindical.isBlank())
                        throw new TaxaSindicalNulaException("Taxa sindical nao pode ser nula.");

                    taxaSindical = taxaSindical.replace(",", ".");
                    double taxa;
                    try {
                        taxa = Double.parseDouble(taxaSindical);
                    } catch (NumberFormatException e) {
                        throw new ValorNaoNumericoException(
                                "Taxa sindical deve ser numerica.");
                    }

                    if (taxa < 0)
                        throw new ValorNegativoException(
                                "Taxa sindical deve ser nao-negativa.");

                    if (membrosSindicato.containsKey(idSindicato))
                        throw new IdentificacaoSindicatoJaExisteException(
                                "Ha outro empregado com esta identificacao de sindicato");

                    if (idSindicato != null && taxaSindical != null) {
                        MembroSindicato membro = MembroSindicatoFactory
                                .criarMembro(idSindicato, taxaSindical);
                        membro.setTaxaSindical(taxa);
                        empregado.setSindicato(membro);
                        membrosSindicato.put(idSindicato, membro);
                    }
                } else
                    empregado.setSindicato(null);

                break;
            case "metodoPagamento":
                String metodoValor = (valor1 != null) ? valor1 : valor;

                if (!"banco".equals(metodoValor) && !"correios".equals(metodoValor) && !"emMaos".equals(metodoValor))
                    throw new MetodoPagamentoInvalido("Metodo de pagamento invalido.");

                if ("banco".equals(metodoValor)) {
                    String banco = valores.get("banco");
                    String agencia = valores.get("agencia");
                    String contaCorrente = valores.get("contaCorrente");

                    if (banco == null || banco.isBlank())
                        throw new BancoNaoPodeSerNuloException("Banco nao pode ser nulo.");

                    if (agencia == null || agencia.isBlank())
                        throw new AgenciaNaoPodeSerNuloException("Agencia nao pode ser nulo.");

                    if (contaCorrente == null || contaCorrente.isBlank())
                        throw new ContaCorrenteNaoPodeSerNulaException("Conta corrente nao pode ser nulo.");

                    Banco metodoBanco = new Banco(banco,
                            agencia, contaCorrente);
                    empregado.setMetodoPagamento(metodoBanco);
                } else if ("correios".equals(metodoValor)) {
                    Correios metodoCorreios = new Correios();
                    empregado.setMetodoPagamento(metodoCorreios);
                } else if ("emMaos".equals(metodoValor)) {
                    EmMaos metodoEmMaos = new EmMaos();
                    empregado.setMetodoPagamento(metodoEmMaos);
                }
                break;
            case "agendaPagamento":
                if (valor == null || valor.isBlank())
                    throw new AtributoNaoPodeSerNuloException(
                            "Agenda de pagamento nao pode ser nula.");

                if (!AgendaPag.isAgendaValida(valor))
                    throw new AgendaPagamentoInvalidaException("Agenda de pagamento nao esta disponivel");

                empregado.setAgendaPagamento(valor);
                break;
            default:
                throw new AtributoNaoExisteException("Atributo nao existe.");
        }
    }

    private void restaurarValorAnterior(Empregado empregado, String atributo, Object valorAnterior) {
        switch (atributo) {
            case "nome":
                empregado.setNome((String) valorAnterior);
                break;
            case "endereco":
                empregado.setEndereco((String) valorAnterior);
                break;
            case "tipo":
                break;
            case "salario":
                break;
            case "comissao":
                if (empregado instanceof Comissionado)
                    ((Comissionado) empregado)
                            .setTaxaDeComissao((Double) valorAnterior);
                break;
            case "sindicalizado":
                MembroSindicato sindicatoAnterior = (MembroSindicato) valorAnterior;
                if (sindicatoAnterior != null) {
                    empregado.setSindicato(sindicatoAnterior);
                    if (!membrosSindicato.containsKey(sindicatoAnterior.getIdMembro()))
                        membrosSindicato.put(sindicatoAnterior.getIdMembro(), sindicatoAnterior);
                } else {
                    if (empregado.getSindicato() != null)
                        membrosSindicato.remove(empregado.getSindicato().getIdMembro());

                    empregado.setSindicato(null);
                }
                break;
            case "idSindicato":
                if (empregado.getSindicato() != null)
                    empregado.getSindicato().setIdMembro((String) valorAnterior);
                break;
            case "taxaSindical":
                if (empregado.getSindicato() != null)
                    empregado.getSindicato().setTaxaSindical((Double) valorAnterior);
                break;
            case "metodoPagamento":
                empregado.setMetodoPagamento((MetodoPagamento) valorAnterior);
                break;
            case "banco":
                if (empregado.getMetodoPagamento() instanceof Banco)
                    ((Banco) empregado.getMetodoPagamento())
                            .setBanco((String) valorAnterior);
                break;
            case "agencia":
                if (empregado.getMetodoPagamento() instanceof Banco)
                    ((Banco) empregado.getMetodoPagamento())
                            .setAgencia((String) valorAnterior);
                break;
            case "contaCorrente":
                if (empregado.getMetodoPagamento() instanceof Banco)
                    ((Banco) empregado.getMetodoPagamento())
                            .setContaCorrente((String) valorAnterior);
                break;
            case "agendaPagamento":
                empregado.setAgendaPagamento((AgendaPag) valorAnterior);
                break;
        }
    }
}
