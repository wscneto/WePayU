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

    public AlterarEmpregadoCmd(String empId, String atributo,
            Map<String, String> valores,
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
        Empregado emp = empregados.get(empId);
        if (emp == null)
            throw new EmpregadoNaoEncontradoException("Empregado não encontrado.");

        try {
            valorAnterior = obterValorAtual(emp, atributo);
            aplicarAlteracao(emp, atributo, valores);
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    public void undo() {
        Empregado emp = empregados.get(empId);
        if (emp == null)
            return;
        restaurarValorAnterior(emp, atributo, valorAnterior);
    }

    private Object obterValorAtual(Empregado e, String att) {
        if ("nome".equals(att))
            return e.getNome();
        if ("endereco".equals(att))
            return e.getEndereco();
        if ("tipo".equals(att))
            return e.getTipo();
        if ("salario".equals(att))
            return e.getSalario();
        if ("comissao".equals(att) && e instanceof Comissionado c)
            return c.getTaxaDeComissao();
        if ("sindicalizado".equals(att))
            return e.getSindicato();
        if ("idSindicato".equals(att))
            return (e.getSindicato() != null) ? e.getSindicato().getIdMembro() : null;
        if ("taxaSindical".equals(att))
            return (e.getSindicato() != null) ? e.getSindicato().getTaxaSindical() : null;
        if ("metodoPagamento".equals(att))
            return e.getMetodoPagamento();
        if ("banco".equals(att) && e.getMetodoPagamento() instanceof Banco b)
            return b.getBanco();
        if ("agencia".equals(att) && e.getMetodoPagamento() instanceof Banco b)
            return b.getAgencia();
        if ("contaCorrente".equals(att) && e.getMetodoPagamento() instanceof Banco b)
            return b.getContaCorrente();
        if ("agendaPagamento".equals(att))
            return e.getAgendaPagamento();
        return null;
    }

    private void aplicarAlteracao(Empregado emp, String att, Map<String, String> vals) throws Exception {
        String v = vals.get("valor");
        String v1 = vals.get("valor1");

        if ("nome".equals(att)) {
            alterarNome(emp, v);
        } else if ("endereco".equals(att)) {
            alterarEndereco(emp, v);
        } else if ("tipo".equals(att)) {
            alterarTipo(emp, v, vals);
        } else if ("salario".equals(att)) {
            alterarSalario(emp, v);
        } else if ("comissao".equals(att)) {
            alterarComissao(emp, v);
        } else if ("sindicalizado".equals(att)) {
            alterarSindicato(emp, v, vals);
        } else if ("metodoPagamento".equals(att)) {
            alterarMetodoPagamento(emp, v, v1, vals);
        } else if ("agendaPagamento".equals(att)) {
            alterarAgenda(emp, v);
        } else {
            throw new AtributoNaoExisteException("Atributo nao existe.");
        }
    }

    private void alterarNome(Empregado e, String v) {
        if (v == null || v.isBlank())
            throw new NomeNuloException("Nome nao pode ser nulo.");
        e.setNome(v);
    }

    private void alterarEndereco(Empregado e, String v) {
        if (v == null || v.isBlank())
            throw new EnderecoNuloException("Endereco nao pode ser nulo.");
        e.setEndereco(v);
    }

    private void alterarTipo(Empregado e, String novoTipo, Map<String, String> vals) throws Exception {
        String novoSalario = vals.getOrDefault("salario", e.getSalario().replace(",", "."));
        MembroSindicato sindicatoAnt = e.getSindicato();
        MetodoPagamento metodoAnt = e.getMetodoPagamento();
        String nome = e.getNome();
        String end = e.getEndereco();

        Empregado novoEmp;
        if ("comissionado".equals(novoTipo)) {
            String comissao = vals.get("comissao");
            if (comissao == null)
                throw new ComissaoNulaException("Comissao nao pode ser nula.");
            novoEmp = EmpregadoFactory.criarEmpregado(novoTipo, nome, end, novoSalario, comissao);
        } else {
            novoEmp = EmpregadoFactory.criarEmpregado(novoTipo, nome, end, novoSalario);
        }

        novoEmp.setId(e.getId());
        novoEmp.setSindicato(sindicatoAnt);
        novoEmp.setMetodoPagamento(metodoAnt);
        empregados.put(empId, novoEmp);
    }

    private void alterarSalario(Empregado e, String v) throws Exception {
        if (v == null || v.isBlank())
            throw new SalarioNuloException("Salario nao pode ser nulo.");

        double sal;
        try {
            sal = Double.parseDouble(v.replace(",", "."));
        } catch (NumberFormatException ex) {
            throw new SalarioNaoNumericoException("Salario deve ser numerico.");
        }
        if (sal < 0)
            throw new SalarioNegativoException("Salario deve ser nao-negativo.");

        String tipo = e.getTipo();
        String nome = e.getNome();
        String end = e.getEndereco();
        MembroSindicato sind = e.getSindicato();
        MetodoPagamento met = e.getMetodoPagamento();

        Empregado novo;
        if ("comissionado".equals(tipo)) {
            double com = ((Comissionado) e).getTaxaDeComissao();
            novo = EmpregadoFactory.criarEmpregado(tipo, nome, end, v, String.valueOf(com));
        } else {
            novo = EmpregadoFactory.criarEmpregado(tipo, nome, end, v);
        }

        novo.setId(e.getId());
        novo.setSindicato(sind);
        novo.setMetodoPagamento(met);
        empregados.put(empId, novo);
    }

    private void alterarComissao(Empregado e, String v) {
        if (v == null || v.isBlank())
            throw new ComissaoNulaException("Comissao nao pode ser nula.");
        if (!(e instanceof Comissionado c))
            throw new EmpregadoNaoEhComissionadoException("Empregado nao eh comissionado.");

        double taxa;
        try {
            taxa = Double.parseDouble(v.replace(",", "."));
        } catch (NumberFormatException ex) {
            throw new ComissaoNaoNumericaException("Comissao deve ser numerica.");
        }
        if (taxa < 0)
            throw new ComissaoNegativaException("Comissao deve ser nao-negativa.");

        c.setTaxaDeComissao(taxa);
    }

    private void alterarSindicato(Empregado e, String v, Map<String, String> vals) throws Exception {
        if (!"true".equals(v) && !"false".equals(v))
            throw new ValorDeveSerTrueOuFalseException("Valor deve ser true ou false.");

        boolean ativar = Boolean.parseBoolean(v);
        if (ativar) {
            String idS = vals.get("idSindicato");
            String taxaS = vals.get("taxaSindical");
            if (idS == null || idS.isBlank())
                throw new IdentificacaoDoSindicatoNulaException("Identificacao do sindicato nao pode ser nula.");
            if (taxaS == null || taxaS.isBlank())
                throw new TaxaSindicalNulaException("Taxa sindical nao pode ser nula.");

            double taxa;
            try {
                taxa = Double.parseDouble(taxaS.replace(",", "."));
            } catch (NumberFormatException ex) {
                throw new ValorNaoNumericoException("Taxa sindical deve ser numerica.");
            }
            if (taxa < 0)
                throw new ValorNegativoException("Taxa sindical deve ser nao-negativa.");
            if (membrosSindicato.containsKey(idS))
                throw new IdentificacaoSindicatoJaExisteException(
                        "Ha outro empregado com esta identificacao de sindicato");

            MembroSindicato novoM = MembroSindicatoFactory.criarMembro(idS, taxaS);
            novoM.setTaxaSindical(taxa);
            e.setSindicato(novoM);
            membrosSindicato.put(idS, novoM);
        } else {
            if (e.getSindicato() != null)
                membrosSindicato.remove(e.getSindicato().getIdMembro());
            e.setSindicato(null);
        }
    }

    private void alterarMetodoPagamento(Empregado e, String v, String v1, Map<String, String> vals) {
        String tipo = (v1 != null) ? v1 : v;

        if (!"banco".equals(tipo) && !"correios".equals(tipo) && !"emMaos".equals(tipo))
            throw new MetodoPagamentoInvalido("Metodo de pagamento invalido.");

        if ("banco".equals(tipo)) {
            String banco = vals.get("banco");
            String ag = vals.get("agencia");
            String conta = vals.get("contaCorrente");

            if (banco == null || banco.isBlank())
                throw new BancoNaoPodeSerNuloException("Banco nao pode ser nulo.");
            if (ag == null || ag.isBlank())
                throw new AgenciaNaoPodeSerNuloException("Agencia nao pode ser nulo.");
            if (conta == null || conta.isBlank())
                throw new ContaCorrenteNaoPodeSerNulaException("Conta corrente nao pode ser nulo.");

            e.setMetodoPagamento(new Banco(banco, ag, conta));
        } else if ("correios".equals(tipo)) {
            e.setMetodoPagamento(new Correios());
        } else {
            e.setMetodoPagamento(new EmMaos());
        }
    }

    private void alterarAgenda(Empregado e, String v) throws Exception {
        if (v == null || v.isBlank())
            throw new AtributoNaoPodeSerNuloException("Agenda de pagamento nao pode ser nula.");
        if (!AgendaPag.isAgendaValida(v))
            throw new AgendaPagamentoInvalidaException("Agenda de pagamento nao esta disponivel");
        e.setAgendaPagamento(v);
    }

    private void restaurarValorAnterior(Empregado e, String att, Object anterior) {
        if ("nome".equals(att))
            e.setNome((String) anterior);
        else if ("endereco".equals(att))
            e.setEndereco((String) anterior);
        else if ("comissao".equals(att) && e instanceof Comissionado c)
            c.setTaxaDeComissao((Double) anterior);
        else if ("sindicalizado".equals(att)) {
            MembroSindicato m = (MembroSindicato) anterior;
            if (m != null) {
                e.setSindicato(m);
                membrosSindicato.putIfAbsent(m.getIdMembro(), m);
            } else {
                if (e.getSindicato() != null)
                    membrosSindicato.remove(e.getSindicato().getIdMembro());
                e.setSindicato(null);
            }
        } else if ("idSindicato".equals(att) && e.getSindicato() != null)
            e.getSindicato().setIdMembro((String) anterior);
        else if ("taxaSindical".equals(att) && e.getSindicato() != null)
            e.getSindicato().setTaxaSindical((Double) anterior);
        else if ("metodoPagamento".equals(att))
            e.setMetodoPagamento((MetodoPagamento) anterior);
        else if ("banco".equals(att) && e.getMetodoPagamento() instanceof Banco b)
            b.setBanco((String) anterior);
        else if ("agencia".equals(att) && e.getMetodoPagamento() instanceof Banco b)
            b.setAgencia((String) anterior);
        else if ("contaCorrente".equals(att) && e.getMetodoPagamento() instanceof Banco b)
            b.setContaCorrente((String) anterior);
        else if ("agendaPagamento".equals(att))
            e.setAgendaPagamento((AgendaPag) anterior);
    }
}
