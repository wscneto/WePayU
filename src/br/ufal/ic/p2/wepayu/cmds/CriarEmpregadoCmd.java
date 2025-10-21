package br.ufal.ic.p2.wepayu.cmds;

import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.Exception.*;
import java.util.Map;

public class CriarEmpregadoCmd implements Cmd {
    private String id;
    private Empregado empregado;
    private Map<String, Empregado> empregados;

    public CriarEmpregadoCmd(Empregado empregado, Map<String, Empregado> empregados) {
        this.empregado = empregado;
        this.empregados = empregados;
    }

    @Override
    public void exec() {
        try {
            this.id = empregado.getId();
            empregados.put(id, empregado);
        } catch (Exception e) {
            throw new ErroCriacaoEmpregadoException("Erro ao criar empregado: " + e.getMessage(), e);
        }
    }

    @Override
    public void undo() {
        if (id != null)
            empregados.remove(id);
    }

    public String getId() {
        return id;
    }
}