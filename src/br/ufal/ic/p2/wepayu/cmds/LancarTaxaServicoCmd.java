package br.ufal.ic.p2.wepayu.cmds;

import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.Exception.*;
import java.util.Map;

public class LancarTaxaServicoCmd implements Cmd {
    private String membroId;
    private String data;
    private String valor;
    private Map<String, MembroSindicato> membrosSindicato;
    private TaxaServicoMemoria memoria;

    public LancarTaxaServicoCmd(String membroId, String data, String valor,
            Map<String, MembroSindicato> membrosSindicato) {
        this.membroId = membroId;
        this.data = data;
        this.valor = valor;
        this.membrosSindicato = membrosSindicato;
    }

    @Override
    public void exec() {
        try {
            MembroSindicato membro = membrosSindicato.get(membroId);
            if (membro == null)
                throw new MembroNaoExisteException("Membro nao existe.");

            memoria = new TaxaServicoMemoria(membroId, membrosSindicato);

            TaxaServico taxaServico = new TaxaServico(data, valor);
            membro.getTaxasDeServicos().add(taxaServico);
        } catch (MembroNaoExisteException e) {
            throw e;
        } catch (Exception e) {
            throw new ErroLancamentoTaxaServicoException("Erro ao lançar taxa de serviço: " + e.getMessage(), e);
        }
    }

    @Override
    public void undo() {
        if (memoria != null)
            memoria.restaurar();
    }
}
