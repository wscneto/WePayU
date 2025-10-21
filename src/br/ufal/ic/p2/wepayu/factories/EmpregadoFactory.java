package br.ufal.ic.p2.wepayu.factories;

import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.utils.FormatacaoMonetariaUtil;

public class EmpregadoFactory {
    public static Empregado criarEmpregado(String tipo, String nome, String endereco, String salario)
            throws Exception, RuntimeException {

        validarSalario(salario);
        double salarioProcessado = converterSalario(salario);

        return instanciarEmpregadoBasico(tipo, nome, endereco, salarioProcessado);
    }

    public static Empregado criarEmpregado(String tipo, String nome, String endereco, String salario, String comissao)
            throws Exception, RuntimeException {

        validarSalario(salario);
        double salarioProcessado = converterSalario(salario);

        validarComissao(comissao);
        double comissaoProcessada = converterComissao(comissao);

        return instanciarEmpregadoComissionado(tipo, nome, endereco, salarioProcessado, comissaoProcessada);
    }

    private static void validarSalario(String salario) throws Exception {
        if (salario == null || salario.trim().isEmpty()) {
            throw new SalarioNuloException("Salario nao pode ser nulo.");
        }
    }

    private static double converterSalario(String salario) throws Exception {
        double valorSalario;
        try {
            valorSalario = Double.parseDouble(salario.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new SalarioNaoNumericoException("Salario deve ser numerico.");
        }

        if (valorSalario < 0) {
            throw new SalarioNegativoException("Salario deve ser nao-negativo.");
        }

        return FormatacaoMonetariaUtil.arredondarValor(valorSalario);
    }

    private static void validarComissao(String comissao) throws Exception {
        if (comissao == null || comissao.trim().isEmpty()) {
            throw new ComissaoNulaException("Comissao nao pode ser nula.");
        }
    }

    private static double converterComissao(String comissao) throws Exception {
        double valorComissao;
        try {
            valorComissao = Double.parseDouble(comissao.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new ComissaoNaoNumericaException("Comissao deve ser numerica.");
        }

        if (valorComissao < 0) {
            throw new ComissaoNegativaException("Comissao deve ser nao-negativa.");
        }

        return FormatacaoMonetariaUtil.arredondarValor(valorComissao);
    }

    private static Empregado instanciarEmpregadoBasico(String tipo, String nome, String endereco, double salario)
            throws Exception {
        switch (tipo) {
            case "assalariado":
                return new Assalariado(nome, endereco, salario);
            case "horista":
                return new Horista(nome, endereco, salario);
            case "comissionado":
                throw new TipoInvalidoException("Tipo nao aplicavel.");
            default:
                throw new TipoInvalidoException("Tipo invalido.");
        }
    }

    private static Empregado instanciarEmpregadoComissionado(String tipo, String nome, String endereco,
            double salario, double comissao) throws Exception {
        switch (tipo) {
            case "assalariado":
            case "horista":
                throw new TipoInvalidoException("Tipo nao aplicavel.");
            case "comissionado":
                return new Comissionado(nome, endereco, salario, comissao);
            default:
                throw new TipoInvalidoException("Tipo invalido.");
        }
    }
}