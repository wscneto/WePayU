package br.ufal.ic.p2.wepayu.models;

import java.util.ArrayList;

public class Comissionado extends Empregado {
    private double salarioMensal;
    private double taxaDeComissao;
    private ArrayList<ResultadoDeVenda> resultadoDeVenda = new ArrayList<>();

    public Comissionado() {
    }

    public Comissionado(String nome, String endereco, double salario, double taxaDeComissao) {
        super(nome, endereco);
        this.salarioMensal = salario;
        this.taxaDeComissao = taxaDeComissao;
        this.setAgendaPagamento(AgendaPag.getAgendaPadrao("comissionado"));
    }

    public double getSalarioMensal() {
        return salarioMensal;
    }

    public void setSalarioMensal(double salarioMensal) {
        this.salarioMensal = salarioMensal;
    }

    @Override
    public double getTaxaDeComissao() {
        return taxaDeComissao;
    }

    @Override
    public void setTaxaDeComissao(double taxaDeComissao) {
        this.taxaDeComissao = taxaDeComissao;
    }

    @Override
    public ArrayList<ResultadoDeVenda> getResultadoDeVenda() {
        return resultadoDeVenda;
    }

    public void setResultadoDeVenda(ArrayList<ResultadoDeVenda> resultadoDeVenda) {
        this.resultadoDeVenda = resultadoDeVenda;
    }

    @Override
    public String getTipo() {
        return "comissionado";
    }

    @Override
    public String getSalario() {
        return truncarValorMonetario(this.salarioMensal);
    }

    private String truncarValorMonetario(double valor) {
        java.math.BigDecimal bd = java.math.BigDecimal.valueOf(valor);
        bd = bd.setScale(2, java.math.RoundingMode.DOWN);
        return bd.toString().replace('.', ',');
    }

    @Override
    public void lancarResultadoDeVenda(ResultadoDeVenda resultadoDeVenda) {
        this.resultadoDeVenda.add(resultadoDeVenda);
    }
}
