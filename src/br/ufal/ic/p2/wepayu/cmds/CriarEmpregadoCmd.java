package br.ufal.ic.p2.wepayu.cmds;

import br.ufal.ic.p2.wepayu.models.*;
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
        this.id = empregado.getId();
        empregados.put(id, empregado);
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