package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.utils.*;

public class TaxaServico {
    private String data;
    private Double valor;

    public TaxaServico() {
    }

    public TaxaServico(String data, String valor) {
        this.data = data;
        this.valor = FormatacaoMonetariaUtil.arredondarValor(valor.replace(',', '.'));
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Double getValor() {
        return valor;
    }

    public void setValor(Double valor) {
        this.valor = valor;
    }
}
