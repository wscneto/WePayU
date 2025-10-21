package br.ufal.ic.p2.wepayu.cmds;

import br.ufal.ic.p2.wepayu.models.*;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class CartaoMemoria implements Memoria {
    private final String empId;
    private final Map<String, br.ufal.ic.p2.wepayu.models.Empregado> empregados;
    private final List<CartaoPonto> cartoesAnteriores;

    public CartaoMemoria(String empId, Map<String, br.ufal.ic.p2.wepayu.models.Empregado> empregados) {
        this.empId = empId;
        this.empregados = empregados;

        br.ufal.ic.p2.wepayu.models.Empregado empregado = empregados.get(empId);
        if (empregado instanceof Horista) {
            Horista horista = (Horista) empregado;
            this.cartoesAnteriores = new ArrayList<>(horista.getCartoes());
        } else
            this.cartoesAnteriores = new ArrayList<>();
    }

    @Override
    public void restaurar() {
        br.ufal.ic.p2.wepayu.models.Empregado empregado = empregados.get(empId);
        if (empregado instanceof Horista) {
            Horista horista = (Horista) empregado;
            horista.getCartoes().clear();
            horista.getCartoes().addAll(cartoesAnteriores);
        }
    }
}
