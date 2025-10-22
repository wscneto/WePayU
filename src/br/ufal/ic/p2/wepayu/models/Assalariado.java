package br.ufal.ic.p2.wepayu.models;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Assalariado extends Empregado {
    private double remuneracao;

    public Assalariado() {
    }

    public Assalariado(String nome, String endereco, double salario) {
        super(nome, endereco);
        this.remuneracao = salario;
        this.setAgendaPagamento(AgendaPag.getAgendaPadrao("assalariado"));
    }

    public double getSalarioMensal() {
        return this.remuneracao;
    }

    public void setSalarioMensal(double salarioMensal) {
        this.remuneracao = salarioMensal;
    }

    @Override
    public String getTipo() {
        return "assalariado";
    }

    @Override
    public String getSalario() {
        return formatarValor(this.remuneracao);
    }

    private String formatarValor(double valor) {
        BigDecimal decimal = BigDecimal.valueOf(valor);
        decimal = decimal.setScale(2, RoundingMode.DOWN);
        return decimal.toString().replace('.', ',');
    }

    public boolean validarValorSalarial() {
        return this.remuneracao >= 0;
    }

    public double calcularSalarioDiario() {
        return this.remuneracao / 30;
    }

    public String obterInformacaoSalarial() {
        return "Salário mensal: " + getSalario();
    }
}