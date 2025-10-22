package br.ufal.ic.p2.wepayu.factories;

import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.utils.*;

public class EmpregadoFactory {

    public static Empregado criarEmpregado(String tipo, String nome, String endereco, String salario) throws Exception {
        verificarSalarioValido(salario);
        double valorSal = parsearSalario(salario);
        return gerarEmpregadoBase(tipo, nome, endereco, valorSal);
    }

    public static Empregado criarEmpregado(String tipo, String nome, String endereco, String salario, String comissao)
            throws Exception {
        verificarSalarioValido(salario);
        double valorSal = parsearSalario(salario);

        verificarComissaoValida(comissao);
        double valorCom = parsearComissao(comissao);

        return gerarEmpregadoComissionado(tipo, nome, endereco, valorSal, valorCom);
    }

    // =============================================================
    // Métodos auxiliares de validação
    // =============================================================

    private static void verificarSalarioValido(String salario) throws Exception {
        if (salario == null || salario.trim().isEmpty()) {
            throw new SalarioNuloException("Salario nao pode ser nulo.");
        }
    }

    private static double parsearSalario(String salario) throws Exception {
        double valor;
        try {
            valor = Double.parseDouble(salario.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new SalarioNaoNumericoException("Salario deve ser numerico.");
        }

        if (valor < 0)
            throw new SalarioNegativoException("Salario deve ser nao-negativo.");

        return FormatacaoMonetariaUtil.arredondarValor(valor);
    }

    private static void verificarComissaoValida(String comissao) throws Exception {
        if (comissao == null || comissao.trim().isEmpty()) {
            throw new ComissaoNulaException("Comissao nao pode ser nula.");
        }
    }

    private static double parsearComissao(String comissao) throws Exception {
        double valor;
        try {
            valor = Double.parseDouble(comissao.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new ComissaoNaoNumericaException("Comissao deve ser numerica.");
        }

        if (valor < 0)
            throw new ComissaoNegativaException("Comissao deve ser nao-negativa.");

        return FormatacaoMonetariaUtil.arredondarValor(valor);
    }

    // =============================================================
    // Métodos de instanciação
    // =============================================================

    private static Empregado gerarEmpregadoBase(String tipo, String nome, String endereco, double salario)
            throws Exception {
        if ("assalariado".equals(tipo)) {
            return new Assalariado(nome, endereco, salario);
        } else if ("horista".equals(tipo)) {
            return new Horista(nome, endereco, salario);
        } else if ("comissionado".equals(tipo)) {
            throw new TipoInvalidoException("Tipo nao aplicavel.");
        } else {
            throw new TipoInvalidoException("Tipo invalido.");
        }
    }

    private static Empregado gerarEmpregadoComissionado(String tipo, String nome, String endereco,
            double salario, double comissao) throws Exception {
        if ("comissionado".equals(tipo)) {
            return new Comissionado(nome, endereco, salario, comissao);
        } else if ("assalariado".equals(tipo) || "horista".equals(tipo)) {
            throw new TipoInvalidoException("Tipo nao aplicavel.");
        } else {
            throw new TipoInvalidoException("Tipo invalido.");
        }
    }
}
