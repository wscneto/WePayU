package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.Controller.SistemaEmpregados;
import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.models.Empregado;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Facade {
    private SistemaEmpregados sistema;

    public Facade() {
        sistema = new SistemaEmpregados();
    }

    public void encerrarSistema() {
        zerarSistema();
    }
    
    public void zerarSistema() {
        sistema = new SistemaEmpregados();
    }

    public String getAtributoEmpregado(String id, String atributo) throws Exception {
        return sistema.getAtributoEmpregado(id, atributo);
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salarioStr) throws Exception {
        return sistema.criarEmpregado(nome, endereco, tipo, salarioStr);
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salarioStr, String comissaoStr) throws Exception {
        return sistema.criarEmpregado(nome, endereco, tipo, salarioStr, comissaoStr);
    }

    public Empregado getEmpregadoPorId(String id) throws Exception {
        return sistema.getEmpregadoPorId(id);
    }

    public String getEmpregadoPorNome(String nome, int indice) throws Exception {
        return sistema.getEmpregadoPorNome(nome, indice).getId();
    }

    public List<Empregado> getTodosEmpregados() {
        return sistema.getTodosEmpregados();
    }
}
