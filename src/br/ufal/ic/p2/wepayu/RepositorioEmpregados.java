package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.models.Empregado;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.*;
import java.util.*;

public class RepositorioEmpregados {
    private List<Empregado> empregados = new ArrayList<>();
    private final String arquivo = "data/empregados.xml";

    public RepositorioEmpregados() {
        carregar();
    }

    public void adicionar(Empregado e) {
        empregados.add(e);
        salvar();
    }

    public void remover(Empregado e) {
        empregados.remove(e);
        salvar();
    }

    public Empregado getEmpregadoPorId(String id) {
        for (Empregado e : empregados) {
            if (e.getId().equals(id)) {
                return e;
            }
        }
        return null;
    }

    public List<Empregado> getEmpregadosPorNome(String nome) {
        List<Empregado> lista = new ArrayList<>();
        for (Empregado e : empregados) {
            if (e.getNome().equalsIgnoreCase(nome)) {
                lista.add(e);
            }
        }
        return lista;
    }

    public void limpar() {
        empregados.clear();
        salvar();
    }

    public void salvar() {
        try {
            new File("data").mkdirs();
            XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(arquivo)));
            encoder.writeObject(empregados);
            encoder.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void carregar() {
        File f = new File(arquivo);
        if (!f.exists()) return;

        try {
            XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(arquivo)));
            empregados = (List<Empregado>) decoder.readObject();
            decoder.close();
        } catch (Exception e) {
            empregados = new ArrayList<>();
        }
    }

    public List<Empregado> getEmpregados() {
        return empregados;
    }
}
