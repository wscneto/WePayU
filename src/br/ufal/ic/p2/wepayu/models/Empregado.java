package br.ufal.ic.p2.wepayu.models;

import br.ufal.ic.p2.wepayu.Exception.EmpregadoNaoExisteException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import br.ufal.ic.p2.wepayu.models.*;

public class Empregado implements Serializable{
    private String id;
    private String nome;
    private String endereco;
    private String tipo;
    private double salario;
    private double comissao;
    private boolean sindicalizado = false;
    private List<CartaoPonto> cartoes = new ArrayList<>();
    private List<Venda> vendas = new ArrayList<>();
    private String idSindicato;
    private double taxaSindical;
    private List<TaxaServico> taxasServico = new ArrayList<>();

    public Empregado() {
        this.cartoes = new ArrayList<>();
    }

    public Empregado(String id, String nome, String endereco, String tipo, double salario, double comissao) throws EmpregadoNaoExisteException {
        this.id = id;
        this.nome = nome;
        this.endereco = endereco;
        this.tipo = tipo;
        this.salario = salario;
        this.comissao = comissao;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public double getSalario() { return salario; }
    public void setSalario(double salario) { this.salario = salario; }

    public double getComissao() { return comissao; }
    public void setComissao(double comissao) { this.comissao = comissao; }

    public boolean getSindicalizado() { return sindicalizado; }
    public void setSindicalizado (boolean sindicalizado) { this.sindicalizado = sindicalizado; }

    public List<CartaoPonto> getCartoes() { return cartoes; }
    public void setCartoes(List<CartaoPonto> cartoes) { this.cartoes = cartoes; }
    
    public void adicionarCartao(CartaoPonto c) { cartoes.add(c); }

    public List<Venda> getVendas() { return vendas; }
    public void setVendas(List<Venda> vendas) { this.vendas = vendas; }

    public void adicionarVenda(Venda v) { vendas.add(v); }
}
