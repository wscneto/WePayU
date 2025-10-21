package br.ufal.ic.p2.wepayu.cmds;

import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.Exception.*;
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
        try {
            membrosSindicato.put(membro.getIdMembro(), membro);
        } catch (Exception e) {
            throw new ErroCriacaoMembroSindicatoException("Erro ao criar membro do sindicato: " + e.getMessage(), e);
        }
    }

    @Override
    public void undo() {
        membrosSindicato.remove(membro.getIdMembro());
    }
}
