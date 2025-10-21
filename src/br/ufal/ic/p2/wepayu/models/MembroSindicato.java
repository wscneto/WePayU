package br.ufal.ic.p2.wepayu.models;

import java.util.ArrayList;

public class MembroSindicato {
    private String idMembro;
    private double taxaSindical;
    private double dividaSindical = 0.0;
    private ArrayList<TaxaServico> taxasDeServicos = new ArrayList<>();

    public MembroSindicato() {
    }

    public MembroSindicato(String idMembro, String taxaSindical) {
        this.setIdMembro(idMembro);
        this.setTaxaSindical(Double.parseDouble(taxaSindical));
    }

    public String getIdMembro() {
        return idMembro;
    }

    public void setIdMembro(String idMembro) {
        this.idMembro = idMembro;
    }

    public double getTaxaSindical() {
        return taxaSindical;
    }

    public void setTaxaSindical(double taxaSindical) {
        this.taxaSindical = taxaSindical;
    }

    public ArrayList<TaxaServico> getTaxasDeServicos() {
        return taxasDeServicos;
    }

    public void setTaxasDeServicos(ArrayList<TaxaServico> taxasDeServicos) {
        this.taxasDeServicos = taxasDeServicos;
    }

    public void addTaxaServico(TaxaServico taxaServico) {
        this.taxasDeServicos.add(taxaServico);
    }

    public double getDividaSindical() {
        return dividaSindical;
    }

    public void setDividaSindical(double dividaSindical) {
        this.dividaSindical = dividaSindical;
    }
}
