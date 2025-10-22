package br.ufal.ic.p2.wepayu.models;

public class Correios extends MetodoPagamento {
    public Correios() {
    }

    @Override
    public void Pagamento() {
        System.out.println("No banco");
    }

    @Override
    public String getMetodoPagamento() {
        return "correios";
    }
}
