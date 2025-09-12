package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.models.Empregado;
import br.ufal.ic.p2.wepayu.models.CartaoPonto;
import java.beans.XMLEncoder;
import java.beans.XMLDecoder;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

public class Facade {

    private Map<String, Empregado> empregados = new HashMap<>();
    private Map<String, List<CartaoPonto>> cartoes = new HashMap<>();
    private int nextId = 1;
    private static final String FILE = "empregados.xml";

    public Facade() {
        carregar();
    }

    public void zerarSistema() {
        empregados.clear();
        cartoes.clear();
        nextId = 1;
    }

    public void encerrarSistema() {
        salvar();
    }

    private void salvar() {
        try (XMLEncoder encoder = new XMLEncoder(
                new BufferedOutputStream(new FileOutputStream(FILE)))) {
            // Salvar empregados
            encoder.writeObject(new ArrayList<>(empregados.values()));
            // Salvar próximo ID
            encoder.writeObject(nextId);
            // Salvar cartões de ponto
            encoder.writeObject(new HashMap<>(cartoes));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private void carregar() {
        File f = new File(FILE);
        if (!f.exists()) return;
        try (XMLDecoder decoder = new XMLDecoder(
                new BufferedInputStream(new FileInputStream(FILE)))) {
            // Carregar empregados
            List<Empregado> lista = (List<Empregado>) decoder.readObject();
            // Carregar próximo ID
            nextId = (Integer) decoder.readObject();
            // Carregar cartões de ponto
            cartoes = (Map<String, List<CartaoPonto>>) decoder.readObject();
            
            // Reconstruir mapa de empregados
            for (Empregado e : lista) {
                empregados.put(e.getId(), e);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ---------------------
    // US1 - Criar empregado
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
    // US2 - Remover empregado
    public void removerEmpregado(String empId) throws Exception {
        if (empId == null || empId.isEmpty()) {
            throw new Exception("Identificacao do empregado nao pode ser nula.");
        }
        Empregado e = empregados.remove(empId);
        cartoes.remove(empId); // remove também os cartões de ponto do empregado
        if (e == null) {
            throw new Exception("Empregado nao existe.");
        }
    }

    // ---------------------
    // US3 - Cartão de ponto e horas
    public void lancaCartao(String empId, String dataStr, String horasStr) throws Exception {
        if (empId == null || empId.isEmpty())
            throw new Exception("Identificacao do empregado nao pode ser nula.");

        Empregado e = empregados.get(empId);
        if (e == null)
            throw new Exception("Empregado nao existe.");

        if (!e.getTipo().equals("horista"))
            throw new Exception("Empregado nao eh horista.");

        double horas = parseValor(horasStr, "Horas");
        if (horas <= 0)
            throw new Exception("Horas devem ser positivas.");

        Date data;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy");
            sdf.setLenient(false);
            data = sdf.parse(dataStr);
        } catch (Exception ex) {
            throw new Exception("Data invalida.");
        }

        cartoes.computeIfAbsent(empId, k -> new ArrayList<>())
                .add(new CartaoPonto(data, horas));
    }

    public String getHorasNormaisTrabalhadas(String empId, String dataInicialStr, String dataFinalStr) throws Exception {
        double horas = calcularHoras(empId, dataInicialStr, dataFinalStr, true);
        return formatarHoras(horas);
    }

    public String getHorasExtrasTrabalhadas(String empId, String dataInicialStr, String dataFinalStr) throws Exception {
        double horas = calcularHoras(empId, dataInicialStr, dataFinalStr, false);
        return formatarHoras(horas);
    }

    private double calcularHoras(String empId, String dataInicialStr, String dataFinalStr, boolean normais) throws Exception {
        if (empId == null || empId.isEmpty())
            throw new Exception("Identificacao do empregado nao pode ser nula.");

        Empregado e = empregados.get(empId);
        if (e == null)
            throw new Exception("Empregado nao existe.");

        if (!e.getTipo().equals("horista"))
            throw new Exception("Empregado nao eh horista.");

        SimpleDateFormat sdf = new SimpleDateFormat("d/M/yyyy");
        sdf.setLenient(false);
        Date dataInicial;
        Date dataFinal;
        try { 
            dataInicial = sdf.parse(dataInicialStr); 
        } catch (Exception ex) { 
            throw new Exception("Data inicial invalida."); 
        }
        try { 
            dataFinal = sdf.parse(dataFinalStr); 
        } catch (Exception ex) { 
            throw new Exception("Data final invalida."); 
        }

        if (dataInicial.after(dataFinal))
            throw new Exception("Data inicial nao pode ser posterior aa data final.");

        List<CartaoPonto> lista = cartoes.getOrDefault(empId, new ArrayList<>());
        double total = 0;

        // Agrupar horas por dia
        Map<String, Double> horasPorDia = new HashMap<>();
        for (CartaoPonto c : lista) {
            Date d = c.getData();
            // Verificar se a data está no intervalo [dataInicial, dataFinal)
            if (d.compareTo(dataInicial) >= 0 && d.compareTo(dataFinal) < 0) {
                String dia = sdf.format(d);
                horasPorDia.put(dia, horasPorDia.getOrDefault(dia, 0.0) + c.getHoras());
            }
        }

        // Calcular horas normais ou extras
        for (double hDia : horasPorDia.values()) {
            if (normais) {
                total += Math.min(hDia, 8);   // Até 8 horas normais por dia
            } else {
                total += Math.max(0, hDia - 8); // Horas extras além das 8
            }
        }

        return total;
    }

    private String formatarHoras(double horas) {
        // Se não tem decimais, retorna inteiro
        if (horas == (long) horas)
            return String.format("%d", (long) horas);
        // Caso contrário, manter apenas uma casa decimal para passar o teste "1,5"
        return String.format("%.1f", horas).replace('.', ',');
    }

    // ---------------------
    // Validação de criação de empregado
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
            if (comissao == null)
                throw new Exception("Tipo nao aplicavel.");
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
