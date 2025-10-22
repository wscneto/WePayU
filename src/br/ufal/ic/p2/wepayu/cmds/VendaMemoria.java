package br.ufal.ic.p2.wepayu.cmds;

import br.ufal.ic.p2.wepayu.models.*;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class VendaMemoria implements Memoria {
    private final String empId;
    private final Map<String, Empregado> empregados;
    private final List<ResultadoDeVenda> vendasAnteriores;

    public VendaMemoria(String empId, Map<String, Empregado> empregados) {
        this.empId = empId;
        this.empregados = empregados;

        Empregado empregado = empregados.get(empId);
        if (empregado instanceof Comissionado) {
            Comissionado comissionado = (Comissionado) empregado;
            this.vendasAnteriores = new ArrayList<>(comissionado.getResultadoDeVenda());
        } else
            this.vendasAnteriores = new ArrayList<>();
    }

    @Override
    public void restaurar() {
        Empregado empregado = empregados.get(empId);
        if (empregado instanceof Comissionado) {
            Comissionado comissionado = (Comissionado) empregado;
            comissionado.getResultadoDeVenda().clear();
            comissionado.getResultadoDeVenda().addAll(vendasAnteriores);
        }
    }
}
