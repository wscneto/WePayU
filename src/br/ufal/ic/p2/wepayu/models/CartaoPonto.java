package br.ufal.ic.p2.wepayu.models;

import java.util.Date;
import java.io.Serializable;

public class CartaoPonto implements Serializable {
    private Date data;
    private double horas;

    public CartaoPonto() { } // necessário para XMLEncoder

    public CartaoPonto(Date data, double horas) {
        this.data = data;
        this.horas = horas;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public double getHoras() {
        return horas;
    }

    public void setHoras(double horas) {
        this.horas = horas;
    }
}
