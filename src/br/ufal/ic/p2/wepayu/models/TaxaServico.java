package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.time.LocalDate;

public class TaxaServico implements Serializable {
    private String idEmpregado;
    private int dia = 1;
    private int mes = 1;
    private int ano = 2000;
    private double valor;

    public TaxaServico() {}

    public TaxaServico(String idEmpregado, LocalDate data, double valor) {
        this.idEmpregado = idEmpregado;
        this.dia = data.getDayOfMonth();
        this.mes = data.getMonthValue();
        this.ano = data.getYear();
        this.valor = valor;
    }

    public String getIdEmpregado() { return idEmpregado; }
    public void setIdEmpregado(String idEmpregado) { this.idEmpregado = idEmpregado; }

    public int getDia() { return dia; }
    public void setDia(int dia) { this.dia = dia; }

    public int getMes() { return mes; }
    public void setMes(int mes) { this.mes = mes; }
    
    public int getAno() { return ano; }
    public void setAno(int ano) { this.ano = ano; }

    public double getValor() { return valor; }
    public void setValor(double valor) { this.valor = valor; }
}
