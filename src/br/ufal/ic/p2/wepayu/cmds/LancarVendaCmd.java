package br.ufal.ic.p2.wepayu.cmds;

import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.Exception.*;
import java.util.Map;

public class LancarVendaCmd implements Cmd {
    private String empId;
    private String data;
    private String valor;
    private Map<String, Empregado> empregados;
    private VendaMemoria memento;

    public LancarVendaCmd(String empId, String data, String valor, Map<String, Empregado> empregados) {
        this.empId = empId;
        this.data = data;
        this.valor = valor;
        this.empregados = empregados;
    }

    @Override
    public void exec() {
        try {
            Empregado empregado = empregados.get(empId);
            if (empregado == null) {
                throw new EmpregadoNaoEncontradoException("Empregado não encontrado.");
            }

            if (!(empregado instanceof Comissionado)) {
                throw new EmpregadoNaoEhComissionadoException("Empregado nao eh comissionado.");
            }

            Comissionado comissionado = (Comissionado) empregado;

            memento = new VendaMemoria(empId, empregados);

            ResultadoDeVenda resultadoDeVenda = new ResultadoDeVenda(data, valor);
            comissionado.getResultadoDeVenda().add(resultadoDeVenda);
        } catch (EmpregadoNaoEncontradoException | EmpregadoNaoEhComissionadoException e) {
            throw e;
        } catch (Exception e) {
            throw new ErroLancamentoVendaException("Erro ao lançar venda: " + e.getMessage(), e);
        }
    }

    @Override
    public void undo() {
        if (memento != null)
            memento.restaurar();
    }
}
