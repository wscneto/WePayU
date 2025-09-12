package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;
import java.io.Serializable;

public class Empregado implements Serializable {
    private String id;
    private String nome;
    private String endereco;
    private String tipo; // horista, assalariado, comissionado
    private double salario;
    private Double comissao; // só se for comissionado
    private boolean sindicalizado = false;
    private String idSindicato;
    private double taxaSindical;

    public Empregado() { } // utilizado pelo XMLencoder

    public Empregado(String id, String nome, String endereco, String tipo, double salario, Double comissao) {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.tipo = tipo;
        this.salario = salario;
        this.comissao = comissao;
    }
    
    // setters
    public void setId(String id) { this.id = id; }
    public void setNome(String nome) { this.nome = nome; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    public void setSalario(double salario) { this.salario = salario; }
    public void setComissao(Double comissao) { this.comissao = comissao; }
    public void setSindicalizado(boolean sindicalizado) { this.sindicalizado = sindicalizado; }
    public void setIdSindicato(String idSindicato) { this.idSindicato = idSindicato; }
    public void setTaxaSindical(double taxaSindical) { this.taxaSindical = taxaSindical; }

    // getters
    public String getId() { return id; }
    public String getNome() { return nome; }
    public String getEndereco() { return endereco; }
    public String getTipo() { return tipo; }
    public double getSalario() { return salario; }
    public Double getComissao() { return comissao; }
    public boolean isSindicalizado() { return sindicalizado; }
    public String getIdSindicato() { return idSindicato; }
    public double getTaxaSindical() { return taxaSindical; }

    public String getAtributo(String atributo) throws Exception {
        switch (atributo) {
            case "nome": return nome;
            case "endereco": return endereco;
            case "tipo": return tipo;
            case "salario": return formatarValor(salario);
            case "comissao":
                if (comissao == null) throw new Exception("Atributo nao existe.");
                return formatarValor(comissao);
            case "sindicalizado": return Boolean.toString(sindicalizado);
            case "idSindicato": 
                if (idSindicato == null) throw new Exception("Atributo nao existe.");
                return idSindicato;
            case "taxaSindical":
                if (!sindicalizado) throw new Exception("Atributo nao existe.");
                return formatarValor(taxaSindical);
            default: throw new Exception("Atributo nao existe.");
        }
    }

    private String formatarValor(double valor) {
        return String.format("%.2f", valor).replace('.', ',');
    }
}
