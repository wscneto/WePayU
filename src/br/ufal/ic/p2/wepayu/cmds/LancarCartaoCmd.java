package br.ufal.ic.p2.wepayu.cmds;

import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.Exception.*;
import java.util.Map;

public class LancarCartaoCmd implements Cmd {
    private String empId;
    private String data;
    private String horas;
    private Map<String, Empregado> empregados;
    private CartaoMemoria memoria;

    public LancarCartaoCmd(String empId, String data, String horas, Map<String, Empregado> empregados) {
        this.empId = empId;
        this.data = data;
        this.horas = horas;
        this.empregados = empregados;
    }

    @Override
    public void exec() {
        try {
            Empregado empregado = empregados.get(empId);
            if (empregado == null)
                throw new EmpregadoNaoEncontradoException("Empregado não encontrado.");

            if (!(empregado instanceof Horista))
                throw new EmpregadoNaoEhHoristaException("Empregado nao eh horista.");

            Horista horista = (Horista) empregado;

            memoria = new CartaoMemoria(empId, empregados);

            CartaoPonto cartao = new CartaoPonto(data, horas);
            horista.getCartoes().add(cartao);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void undo() {
        if (memoria != null)
            memoria.restaurar();
    }
}
