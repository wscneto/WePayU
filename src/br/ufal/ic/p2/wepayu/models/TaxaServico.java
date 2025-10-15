package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.time.LocalDate;

public class TaxaServico implements Serializable {
    private String idEmpregado;
    private LocalDate data;
    private double valor;

    public TaxaServico() {}

    public TaxaServico(String idEmpregado, LocalDate data, double valor) {
        this.idEmpregado = idEmpregado;
        this.data = data;
        this.valor = valor;
    }

    public String getIdEmpregado() { return idEmpregado; }
    public void setIdEmpregado(String idEmpregado) { this.idEmpregado = idEmpregado; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
}
