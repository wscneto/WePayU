package br.ufal.ic.p2.wepayu;
import br.ufal.ic.p2.wepayu.models.Empregado;
import java.beans.XMLEncoder;
import java.beans.XMLDecoder;
import java.io.*;
import java.util.*;

public class Facade {

    private Map<String, Empregado> empregados = new HashMap<>();
    private int nextId = 1;
    private static final String FILE = "empregados.xml";

    public Facade() {
        carregar();
    }

    public void zerarSistema() {
        empregados.clear();
        nextId = 1;
    }

    public void encerrarSistema() {
        salvar();
    }

    private void salvar() {
        try (XMLEncoder encoder = new XMLEncoder(new BufferedOutputStream(new FileOutputStream(FILE)))) {
            encoder.writeObject(new ArrayList<>(empregados.values()));
            encoder.writeObject(nextId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void carregar() {
        File f = new File(FILE);
        if (!f.exists()) return;
        try (XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(FILE)))) {
            List<Empregado> lista = (List<Empregado>) decoder.readObject();
            nextId = (Integer) decoder.readObject();
            for (Empregado e : lista) {
                empregados.put(e.getId(), e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao) throws Exception {
        validarCriacao(nome, endereco, tipo, salario, comissao);

        double sal = parseValor(salario, "Salario");
        Double com = comissao != null ? parseValor(comissao, "Comissao") : null;

        String id = "E" + nextId++;
        Empregado e = new Empregado(id, nome, endereco, tipo, sal, com);
        empregados.put(id, e);
        return id;
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws Exception {
        return criarEmpregado(nome, endereco, tipo, salario, null);
    }

    public String getAtributoEmpregado(String emp, String atributo) throws Exception {
        if (emp == null || emp.isEmpty())
            throw new Exception("Identificacao do empregado nao pode ser nula.");
        Empregado e = empregados.get(emp);
        if (e == null) throw new Exception("Empregado nao existe.");
        return e.getAtributo(atributo);
    }

    public String getEmpregadoPorNome(String nome, int indice) throws Exception {
        List<Empregado> encontrados = new ArrayList<>();
        for (Empregado e : empregados.values()) {
            if (e.getNome().contains(nome)) {
                encontrados.add(e);
            }
        }
        if (encontrados.isEmpty()) throw new Exception("Nao ha empregado com esse nome.");
        if (indice < 1 || indice > encontrados.size()) throw new Exception("Nao ha empregado com esse nome.");
        return encontrados.get(indice - 1).getId();
    }

    // ---------------------

    private void validarCriacao(String nome, String endereco, String tipo, String salario, String comissao) throws Exception {
        if (nome == null || nome.isEmpty())
            throw new Exception("Nome nao pode ser nulo.");
        if (endereco == null || endereco.isEmpty())
            throw new Exception("Endereco nao pode ser nulo.");
        if (salario == null || salario.isEmpty())
            throw new Exception("Salario nao pode ser nulo.");

        if (!tipo.equals("horista") && !tipo.equals("assalariado") && !tipo.equals("comissionado"))
            throw new Exception("Tipo invalido.");

        if (!isNumeroValido(salario))
            throw new Exception("Salario deve ser numerico.");
        if (parseValor(salario, "Salario") < 0)
            throw new Exception("Salario deve ser nao-negativo.");

        if (tipo.equals("comissionado")) {
            // ⚠️ Se comissao não foi passada (null), então é tipo errado
            if (comissao == null)
                throw new Exception("Tipo nao aplicavel.");
            // ⚠️ Se comissao foi passada mas está vazia
            if (comissao.isEmpty())
                throw new Exception("Comissao nao pode ser nula.");
            if (!isNumeroValido(comissao))
                throw new Exception("Comissao deve ser numerica.");
            if (parseValor(comissao, "Comissao") < 0)
                throw new Exception("Comissao deve ser nao-negativa.");
        } else {
            if (comissao != null)
                throw new Exception("Tipo nao aplicavel.");
        }
    }

    private boolean isNumeroValido(String valor) {
        return valor.matches("-?\\d+(,\\d+)?");
    }

    private double parseValor(String valor, String campo) throws Exception {
        try {
            return Double.parseDouble(valor.replace(',', '.'));
        } catch (NumberFormatException e) {
            throw new Exception(campo + " deve ser numerico.");
        }
    }
}

