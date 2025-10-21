package br.ufal.ic.p2.wepayu.cmds;

import br.ufal.ic.p2.wepayu.services.*;
import br.ufal.ic.p2.wepayu.Exception.*;

public class RodaFolhaCmd implements Cmd {
    private String data;
    private String arquivo;
    private FolhaPagamentoService folhaPagamentoService;

    public RodaFolhaCmd(String data, String arquivo, FolhaPagamentoService folhaPagamentoService) {
        this.data = data;
        this.arquivo = arquivo;
        this.folhaPagamentoService = folhaPagamentoService;
    }

    @Override
    public void exec() {
        try {
            folhaPagamentoService.rodaFolha(data, arquivo);
        } catch (DataInvalidaException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao rodar folha: " + e.getMessage(), e);
        }
    }

    @Override
    public void undo() {
    }
}
