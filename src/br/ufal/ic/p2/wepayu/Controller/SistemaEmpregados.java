package br.ufal.ic.p2.wepayu.Controller;

import br.ufal.ic.p2.wepayu.RepositorioEmpregados;
import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.models.Empregado;
import br.ufal.ic.p2.wepayu.util.Conversor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SistemaEmpregados {
    private RepositorioEmpregados repo = new RepositorioEmpregados();
    private int contadorId = 1;
    
    //public void zerarSistema()
    //{
    //    empregados.clear();
    //    contadorId = 1;
    //}

    public String getAtributoEmpregado(String id, String atributo) throws Exception {
        if (id == null || id.isEmpty()) throw new IdentificacaoNulaException();

        Empregado e = getEmpregadoPorId(id);

        if (atributo.equals("nome")) return e.getNome();
        else if (atributo.equals("endereco")) return e.getEndereco();
        else if (atributo.equals("tipo")) return e.getTipo();
        else if (atributo.equals("salario"))
            return String.format("%.2f", e.getSalario()).replace('.', ',');
        else if (atributo.equals("comissao"))
            return String.format("%.2f", e.getComissao()).replace('.', ',');
        else if (atributo.equals("sindicalizado")) return String.valueOf(e.getSindicalizado());
        else throw new AtributoNaoExisteException();
    }


    public String criarEmpregado(String nome, String endereco, String tipo, String salarioStr) throws Exception {
        if (tipo.equals("comissionado")) throw new TipoNaoAplicavelException();
        return criarEmpregado(nome, endereco, tipo, salarioStr, null);
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salarioStr, String comissaoStr) throws Exception {
        // Validações básicas
        if (nome == null || nome.trim().isEmpty()) throw new NomeNuloException();
        if (endereco == null || endereco.trim().isEmpty()) throw new EnderecoNuloException();
        if (!tipo.equals("horista") && !tipo.equals("assalariado") && !tipo.equals("comissionado")) throw new TipoInvalidoException();

        double salario = 0;
        if (salarioStr == null || salarioStr.trim().isEmpty()) throw new SalarioNuloException();
        try {
            salario = Conversor.parseDouble(salarioStr);
            if (salario < 0) throw new SalarioNegativoException();
        } catch (NumberFormatException e) {
            throw new SalarioNaoNumericoException();
        }

        double comissao = 0;
        if (tipo.equals("comissionado")) {
            if (comissaoStr == null || comissaoStr.trim().isEmpty()) throw new ComissaoNulaException();
            try {
                comissao = Conversor.parseDouble(comissaoStr);
                if (comissao < 0) throw new ComissaoNegativaException();
            } catch (NumberFormatException e) {
                throw new ComissaoNaoNumericaException();
            }
        } else if (comissaoStr != null)
            throw new TipoNaoAplicavelException();

        String id = String.valueOf(contadorId++);
        Empregado e = new Empregado(id, nome, endereco, tipo, salario, comissao);
        repo.adicionar(e);
        return id;
    }

    public Empregado getEmpregadoPorId(String id) throws Exception {
        if (id == null || id.isEmpty()) throw new EmpregadoNaoExisteException();
        Empregado e = repo.getEmpregadoPorId(id);
        if (e == null) throw new EmpregadoNaoExisteException();
        return e;
    }

    public Empregado getEmpregadoPorNome(String nome, int indice) throws Exception {
        List<Empregado> encontrados = repo.getEmpregadosPorNome(nome);
        if (encontrados.size() < indice) throw new NaoHaEmpregadoComNomeException();
        return encontrados.get(indice - 1);
    }

    public List<Empregado> getTodosEmpregados() {
        return repo.getEmpregados();
    }
}
