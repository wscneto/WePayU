package br.ufal.ic.p2.wepayu.models;

import java.util.*;
import java.time.*;
import java.time.temporal.ChronoUnit;

public class AgendaDePags {
    private static final Set<String> PREDEFINIDAS = Set.of("semanal 5", "semanal 2 5", "mensal $");
    private static final Map<String, AgendaDePags> personalizadas = new HashMap<>();

    private final String descricao;
    private final String tipo;
    private final int p1;
    private final int p2;

    private AgendaDePags(String descricao, String tipo, int p1, int p2) {
        this.descricao = descricao;
        this.tipo = tipo;
        this.p1 = p1;
        this.p2 = p2;
    }

    // ========================================================
    // FACTORY PRINCIPAL
    // ========================================================

    public static AgendaDePags criarAgenda(String descritivo) {
        checarDescricao(descritivo);

        String[] partes = descritivo.trim().split("\\s+");
        String base = partes[0];

        if ("semanal".equals(base))
            return criarAgendaSemanal(descritivo, partes);
        if ("mensal".equals(base))
            return criarAgendaMensal(descritivo, partes);

        throw new IllegalArgumentException("Descricao de agenda invalida");
    }

    // ========================================================
    // VALIDAÇÕES
    // ========================================================

    private static void checarDescricao(String desc) {
        if (desc == null || desc.isBlank())
            throw new IllegalArgumentException("Descricao de agenda invalida");

        if (PREDEFINIDAS.contains(desc) || personalizadas.containsKey(desc))
            throw new IllegalArgumentException("Agenda de pagamentos ja existe");

        if (desc.trim().split("\\s+").length < 2)
            throw new IllegalArgumentException("Descricao de agenda invalida");
    }

    // ========================================================
    // CONSTRUÇÃO DE AGENDAS
    // ========================================================

    private static AgendaDePags criarAgendaSemanal(String texto, String[] partes) {
        if (partes.length == 2) {
            int dia = parseIntSeguro(partes[1]);
            validarIntervalo(dia, 1, 7);
            return registrarNova(texto, "semanal", dia, 0);
        } else if (partes.length == 3) {
            int sem = parseIntSeguro(partes[1]);
            int dia = parseIntSeguro(partes[2]);
            validarIntervalo(sem, 1, 52);
            validarIntervalo(dia, 1, 7);
            return registrarNova(texto, "semanal", sem, dia);
        }
        throw new IllegalArgumentException("Descricao de agenda invalida");
    }

    private static AgendaDePags criarAgendaMensal(String texto, String[] partes) {
        if (partes.length != 2)
            throw new IllegalArgumentException("Descricao de agenda invalida");

        String param = partes[1];
        if ("$".equals(param))
            return registrarNova(texto, "mensal", -1, 0);

        int diaMes = parseIntSeguro(param);
        validarIntervalo(diaMes, 1, 28);
        return registrarNova(texto, "mensal", diaMes, 0);
    }

    private static AgendaDePags registrarNova(String descricao, String tipo, int p1, int p2) {
        AgendaDePags nova = new AgendaDePags(descricao, tipo, p1, p2);
        personalizadas.put(descricao, nova);
        return nova;
    }

    private static int parseIntSeguro(String valor) {
        try {
            return Integer.parseInt(valor);
        } catch (Exception e) {
            throw new IllegalArgumentException("Descricao de agenda invalida");
        }
    }

    private static void validarIntervalo(int valor, int min, int max) {
        if (valor < min || valor > max)
            throw new IllegalArgumentException("Descricao de agenda invalida");
    }

    // ========================================================
    // CONSULTAS E ACESSO
    // ========================================================

    public static boolean validarAgendaExistente(String d) {
        return PREDEFINIDAS.contains(d) || personalizadas.containsKey(d);
    }

    public static AgendaDePags obterAgenda(String desc) {
        if (PREDEFINIDAS.contains(desc))
            return gerarPredefinida(desc);
        return personalizadas.get(desc);
    }

    private static AgendaDePags gerarPredefinida(String d) {
        String tipo = d.startsWith("semanal") ? "semanal" : "mensal";
        int a = switch (d) {
            case "semanal 5" -> 5;
            case "semanal 2 5" -> 2;
            case "mensal $" -> -1;
            default -> 5;
        };
        int b = "semanal 2 5".equals(d) ? 5 : 0;
        return new AgendaDePags(d, tipo, a, b);
    }

    // ========================================================
    // VERIFICAÇÃO DE PAGAMENTO POR DATA
    // ========================================================

    public boolean verificarPagamentoData(String data) {
        try {
            String[] pedacos = data.split("/");
            LocalDate d = LocalDate.of(
                    Integer.parseInt(pedacos[2]),
                    Integer.parseInt(pedacos[1]),
                    Integer.parseInt(pedacos[0]));

            return "semanal".equals(tipo) ? ehDataSemanal(d) : ehDataMensal(d);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean ehDataSemanal(LocalDate d) {
        int dow = d.getDayOfWeek().getValue();
        if (p2 == 0)
            return dow == p1;
        return ehDataSemanalComposta(d, dow);
    }

    private boolean ehDataSemanalComposta(LocalDate d, int dow) {
        LocalDate ref = (p1 == 52 && p2 == 1) ? LocalDate.of(2004, 12, 26) : LocalDate.of(2005, 1, 14);
        if (d.isBefore(ref))
            return false;

        long dias = ChronoUnit.DAYS.between(ref, d);
        long semanas = dias / 7;
        return dow == p2 && semanas % p1 == 0;
    }

    private boolean ehDataMensal(LocalDate d) {
        if (p1 == -1)
            return d.getDayOfMonth() == d.lengthOfMonth();
        return d.getDayOfMonth() == p1;
    }

    // ========================================================
    // CÁLCULO DE VALORES
    // ========================================================

    public double computarValorPagamento(Empregado emp, String inicio, String fim) {
        String cat = emp.getTipo();

        if ("semanal".equals(tipo)) {
            return (p2 == 0)
                    ? calcularSemanal(emp, cat)
                    : calcularSemanalComposto(emp, cat);
        }
        if ("mensal".equals(tipo))
            return calcularMensal(emp, cat);

        return 0.0;
    }

    private double calcularSemanal(Empregado e, String cat) {
        return switch (cat) {
            case "horista" -> pagarHorista(e, 7);
            case "assalariado" -> pagarAssalariadoSemanal(e);
            case "comissionado" -> pagarComissionado(e, 7);
            default -> 0.0;
        };
    }

    private double calcularSemanalComposto(Empregado e, String cat) {
        return switch (cat) {
            case "horista" -> pagarHorista(e, 14);
            case "assalariado" -> pagarAssalariadoBi(e);
            case "comissionado" -> pagarComissionado(e, p1 * 7);
            default -> 0.0;
        };
    }

    private double calcularMensal(Empregado e, String cat) {
        return switch (cat) {
            case "horista" -> pagarHorista(e, 30);
            case "assalariado" -> pagarAssalariadoMensal(e);
            case "comissionado" -> pagarComissionado(e, 30);
            default -> 0.0;
        };
    }

    // ========================================================
    // CÁLCULOS DE PAGAMENTO POR TIPO
    // ========================================================

    private double pagarHorista(Empregado e, int dias) {
        if (!(e instanceof Horista h))
            return 0.0;
        double vh = h.getSalarioPorHora();
        double normais = h.getCartoes().stream().limit(dias).mapToDouble(c -> Math.min(c.getHoras(), 8)).sum();
        double extras = h.getCartoes().stream().limit(dias).mapToDouble(c -> Math.max(c.getHoras() - 8, 0)).sum();
        return (normais * vh) + (extras * vh * 1.5);
    }

    private double pagarAssalariadoSemanal(Empregado e) {
        if (!(e instanceof Assalariado a))
            return 0.0;
        return (a.getSalarioMensal() * 12) / 52;
    }

    private double pagarAssalariadoBi(Empregado e) {
        if (!(e instanceof Assalariado a))
            return 0.0;
        return ((a.getSalarioMensal() * 12) / 52) * p1;
    }

    private double pagarAssalariadoMensal(Empregado e) {
        if (!(e instanceof Assalariado a))
            return 0.0;
        return a.getSalarioMensal();
    }

    private double pagarComissionado(Empregado e, int dias) {
        if (!(e instanceof Comissionado c))
            return 0.0;

        double base = (c.getSalarioMensal() * 12 / 52) * (dias / 7.0);
        double total = c.getResultadoDeVenda().stream()
                .limit(dias)
                .mapToDouble(v -> v.getValor() * c.getTaxaDeComissao())
                .sum();

        return base + total;
    }

    // ========================================================
    // GETTERS E METADADOS
    // ========================================================

    public String getDescricao() {
        return descricao;
    }

    public String getTipo() {
        return tipo;
    }

    public int getParametro1() {
        return p1;
    }

    public int getParametro2() {
        return p2;
    }

    public static Map<String, AgendaDePags> getAgendasCustomizadas() {
        return new HashMap<>(personalizadas);
    }

    public static void limparAgendasCustomizadas() {
        personalizadas.clear();
    }

    public static boolean agendaCustomizadaExiste(String d) {
        return personalizadas.containsKey(d);
    }
}
