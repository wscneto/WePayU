package br.ufal.ic.p2.wepayu.cmds;

import br.ufal.ic.p2.wepayu.models.*;
import java.util.Map;

public class CriarMembroSindicatoCmd implements Cmd {
    private MembroSindicato membro;
    private Map<String, MembroSindicato> membrosSindicato;

    public CriarMembroSindicatoCmd(MembroSindicato membro, Map<String, MembroSindicato> membrosSindicato) {
        this.membro = membro;
        this.membrosSindicato = membrosSindicato;
    }

    @Override
    public void exec() {
        membrosSindicato.put(membro.getIdMembro(), membro);
    }

    @Override
    public void undo() {
        membrosSindicato.remove(membro.getIdMembro());
    }
}
