package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.utils.FormatacaoMonetariaUtil;

public class CartaoPonto {
    private String data;
    private Double horas;

    public CartaoPonto() {
    }

    public CartaoPonto(String data, String horas) {
        this.data = data;
        this.horas = FormatacaoMonetariaUtil.arredondarValor(horas.replace(',', '.'));
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Double getHoras() {
        return horas;
    }

    public void setHoras(Double horas) {
        this.horas = horas;
    }
}
