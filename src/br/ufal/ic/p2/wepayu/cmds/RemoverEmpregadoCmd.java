package br.ufal.ic.p2.wepayu.cmds;

import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.Exception.*;
import java.util.Map;

public class RemoverEmpregadoCmd implements Cmd {
    private String id;
    private Empregado empregadoRemovido;
    private Map<String, Empregado> empregados;

    public RemoverEmpregadoCmd(String id, Map<String, Empregado> empregados) {
        this.id = id;
        this.empregados = empregados;
    }

    @Override
    public void exec() {
        try {
            this.empregadoRemovido = empregados.get(id);
            if (empregadoRemovido == null)
                throw new EmpregadoNaoEncontradoException("Empregado não encontrado: " + id);
            empregados.remove(id);
        } catch (EmpregadoNaoEncontradoException e) {
            throw e;
        } catch (Exception e) {
            throw new ErroRemocaoEmpregadoException("Erro ao remover empregado: " + e.getMessage(), e);
        }
    }

    @Override
    public void undo() {
        if (empregadoRemovido != null)
            empregados.put(id, empregadoRemovido);
    }
}