package br.ufal.ic.p2.wepayu.models;

public abstract class MetodoPagamento {
    public abstract void Pagamento();

    public MetodoPagamento() {
    }

    public abstract String getMetodoPagamento();
}
