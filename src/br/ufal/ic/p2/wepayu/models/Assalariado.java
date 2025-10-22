package br.ufal.ic.p2.wepayu.models;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Assalariado extends Empregado {
    private double salarioBase;

    public Assalariado() {
    }

    public Assalariado(String nomeCompleto, String enderecoCompleto, double salarioBase) {
        super(nomeCompleto, enderecoCompleto);
        this.salarioBase = salarioBase;
        this.setAgendaPagamento(AgendaPag.getAgendaPadrao("assalariado"));
    }

    public double getSalarioMensal() {
        return this.salarioBase;
    }

    public void setSalarioMensal(double novoSalario) {
        this.salarioBase = novoSalario;
    }

    @Override
    public String getTipo() {
        return "assalariado";
    }

    @Override
    public String getSalario() {
        return converterParaFormatoBrasileiro(this.salarioBase);
    }

    public boolean validarValorSalarial() {
        return this.salarioBase >= 0;
    }

    public double calcularSalarioDiario() {
        return this.salarioBase / 30.0;
    }

    public String obterInformacaoSalarial() {
        return "Salário mensal: " + getSalario();
    }

    private String converterParaFormatoBrasileiro(double valorNumerico) {
        BigDecimal valorDecimal = BigDecimal.valueOf(valorNumerico);
        valorDecimal = valorDecimal.setScale(2, RoundingMode.DOWN);
        String valorFormatado = valorDecimal.toString();
        return valorFormatado.replace('.', ',');
    }

    public double obterSalarioAnual() {
        return this.salarioBase * 12;
    }

    public boolean possuiSalarioValido() {
        return validarValorSalarial();
    }
}