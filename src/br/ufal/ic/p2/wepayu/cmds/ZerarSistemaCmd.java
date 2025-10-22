package br.ufal.ic.p2.wepayu.cmds;

import br.ufal.ic.p2.wepayu.models.*;
import java.util.Map;
import java.util.HashMap;

public class ZerarSistemaCmd implements Cmd {
    private Map<String, Empregado> empregados;
    private Map<String, MembroSindicato> membrosSindicato;
    private Map<String, Empregado> empregadosBackup;
    private Map<String, MembroSindicato> membrosSindicatoBackup;

    public ZerarSistemaCmd(Map<String, Empregado> empregados, Map<String, MembroSindicato> membrosSindicato) {
        this.empregados = empregados;
        this.membrosSindicato = membrosSindicato;
    }

    @Override
    public void exec() {
        empregadosBackup = new HashMap<>(empregados);
        membrosSindicatoBackup = new HashMap<>(membrosSindicato);

        empregados.clear();
        membrosSindicato.clear();
        AgendaDePags.limparAgendasCustomizadas();
    }

    @Override
    public void undo() {
        empregados.clear();
        empregados.putAll(empregadosBackup);

        membrosSindicato.clear();
        membrosSindicato.putAll(membrosSindicatoBackup);
    }
}
