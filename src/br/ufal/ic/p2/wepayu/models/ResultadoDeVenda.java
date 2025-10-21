package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.utils.FormatacaoMonetariaUtil;

public class ResultadoDeVenda {
    private String data;
    private Double valor;

    public ResultadoDeVenda() {
    }

    public ResultadoDeVenda(String data, String valor) {
        this.setData(data);
        this.setValor(FormatacaoMonetariaUtil.arredondarValor(valor.replace(',', '.')));
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
