package br.ufal.ic.p2.wepayu.models;

import java.io.Serializable;
import java.time.LocalDate;

public class CartaoPonto implements Serializable {
    private String idEmpregado;
    private int dia = 1;
    private int mes = 1;
    private int ano = 2000;
    private double horas;

    public CartaoPonto() {}

    public CartaoPonto(String idEmpregado, LocalDate data, double horas) {
        this.idEmpregado = idEmpregado;
        this.dia = data.getDayOfMonth();
        this.mes = data.getMonthValue();
        this.ano = data.getYear();
        this.horas = horas;
    }

    public String getIdEmpregado() { return idEmpregado; }
    public void setIdEmpregado(String idEmpregado) { this.idEmpregado = idEmpregado; }

    public LocalDate getData() { 
        return LocalDate.of(ano, mes, dia); 
    }
    public void setData(LocalDate data) {
        this.dia = data.getDayOfMonth();
        this.mes = data.getMonthValue();
        this.ano = data.getYear();
    }

    public int getDia() { return dia; }
    public void setDia(int dia) { this.dia = dia; }

    public int getMes() { return mes; }
    public void setMes(int mes) { this.mes = mes; }

    public int getAno() { return ano; }
    public void setAno(int ano) { this.ano = ano; }

    public double getHoras() { return horas; }
    public void setHoras(double horas) { this.horas = horas; }
}
