package br.ufal.ic.p2.wepayu.models;

import java.util.ArrayList;

public class Horista extends Empregado {
    private double salarioPorHora;
    private ArrayList<CartaoPonto> cartoes = new ArrayList<>();

    public Horista() {
    }

    public Horista(String nome, String endereco, double salario) {
        super(nome, endereco);
        this.salarioPorHora = salario;
        this.setAgendaPagamento(AgendaPag.getAgendaPadrao("horista"));
    }

    public double getSalarioPorHora() {
        return salarioPorHora;
    }

    public void setSalarioPorHora(double salarioPorHora) {
        this.salarioPorHora = salarioPorHora;
    }

    @Override
    public ArrayList<CartaoPonto> getCartoes() {
        return this.cartoes;
    }

    public void setCartoes(ArrayList<CartaoPonto> cartoes) {
        this.cartoes = cartoes;
    }

    @Override
    public void lancarCartao(CartaoPonto cartao) {
        this.cartoes.add(cartao);
    }

    @Override
    public String getTipo() {
        return "horista";
    }

    @Override
    public String getSalario() {
        return truncarValorMonetario(this.salarioPorHora);
    }

    private String truncarValorMonetario(double valor) {
        java.math.BigDecimal bd = java.math.BigDecimal.valueOf(valor);
        bd = bd.setScale(2, java.math.RoundingMode.DOWN);
        return bd.toString().replace('.', ',');
    }
}
