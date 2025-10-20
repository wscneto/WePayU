package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.Controller.SistemaEmpregados;
import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.models.Empregado;

import java.io.File;
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
        sistema = new SistemaEmpregados();
    }
    
    public void zerarSistema() {
        File empregadosFile = new File("data/empregados.xml");
        empregadosFile.delete();
        sistema = new SistemaEmpregados();
    }

    public String getAtributoEmpregado(String id, String atributo) throws Exception {
        return sistema.getAtributoEmpregado(id, atributo);
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salarioStr) throws Exception {
        return sistema.criarEmpregado(nome, endereco, tipo, salarioStr);
    }

    public void removerEmpregado(String id) throws Exception {
        sistema.removerEmpregado(id);
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

    public void lancaCartao(String id, String data, String horas) throws Exception {
        sistema.lancaCartao(id, data, horas);
    }

    public String getHorasNormaisTrabalhadas(String emp, String dataInicial, String dataFinal) throws Exception {
        String valor = sistema.getHorasNormaisTrabalhadas(emp, dataInicial, dataFinal);
        return valor;
    }

    public String getHorasExtrasTrabalhadas(String emp, String dataInicial, String dataFinal) throws Exception {
        String valor = sistema.getHorasExtrasTrabalhadas(emp, dataInicial, dataFinal);
        return valor;
    }

    public void lancaVenda(String id, String data, String valor) throws Exception {
        sistema.lancaVenda(id, data, valor);
    }

    public String getVendasRealizadas(String id, String dataInicial, String dataFinal) throws Exception {
        String valor = sistema.getVendasRealizadas(id, dataInicial, dataFinal);
        return valor;
    }

    public void lancaTaxaServico(String membro, String data, String valorStr) throws Exception {
        sistema.lancaTaxaServico(membro, data, valorStr);
    }

    public String getTaxasServico(String empId, String dataInicial, String dataFinal) throws Exception {
        String valor = sistema.getTaxasServico(empId, dataInicial, dataFinal);
        return valor;
    }

    public void alteraEmpregado(String id, String atributo, String valor, String banco, String agencia, String contaCorrente) throws Exception {
        sistema.alteraEmpregado(id, atributo, valor, banco, agencia, contaCorrente);
    }

    public void alteraEmpregado(String id, String atributo, String valor, String idSindicato, String taxaSindicalStr) throws Exception {
        sistema.alteraEmpregado(id, atributo, valor, idSindicato, taxaSindicalStr);
    }

    public void alteraEmpregado(String id, String atributo, String valor1, String valor2) throws Exception {
        sistema.alteraEmpregado(id, atributo, valor1, valor2);
    }

    public void alteraEmpregado(String id, String atributo, String valor) throws Exception {
        sistema.alteraEmpregado(id, atributo, valor);
    }
}
