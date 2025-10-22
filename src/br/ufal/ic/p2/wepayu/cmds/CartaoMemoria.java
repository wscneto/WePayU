package br.ufal.ic.p2.wepayu.cmds;

import br.ufal.ic.p2.wepayu.models.*;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import br.ufal.ic.p2.wepayu.models.Empregado;

public class CartaoMemoria implements Memoria {
    private final String empId;
    private final Map<String, Empregado> empregados;
    private final List<CartaoPonto> cartoesAnteriores;

    public CartaoMemoria(String empId, Map<String, Empregado> empregados) {
        this.empId = empId;
        this.empregados = empregados;

        Empregado empregado = empregados.get(empId);
        if (empregado instanceof Horista) {
            Horista horista = (Horista) empregado;
            this.cartoesAnteriores = new ArrayList<>(horista.getCartoes());
        } else
            this.cartoesAnteriores = new ArrayList<>();
    }

    @Override
    public void restaurar() {
        Empregado empregado = empregados.get(empId);
        if (empregado instanceof Horista) {
            Horista horista = (Horista) empregado;
            horista.getCartoes().clear();
            horista.getCartoes().addAll(cartoesAnteriores);
        }
    }
}
