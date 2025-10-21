package br.ufal.ic.p2.wepayu.models;

import java.util.Set;
import java.util.Map;
import java.util.HashMap;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.DayOfWeek;

public class AgendaDePags {
    private static final Set<String> AGENDAS_PRE_DEFINIDAS = Set.of(
            "semanal 5", "semanal 2 5", "mensal $");

    private static final Map<String, AgendaDePags> agendasPersonalizadas = new HashMap<>();

    private String descritivo;
    private String periodicidade;
    private int valorParametro1;
    private int valorParametro2;

    private AgendaDePags(String descritivo, String periodicidade, int valorParametro1, int valorParametro2) {
        this.descritivo = descritivo;
        this.periodicidade = periodicidade;
        this.valorParametro1 = valorParametro1;
        this.valorParametro2 = valorParametro2;
    }

    public static AgendaDePags criarAgenda(String descritivo) {
        validarDescritivo(descritivo);

        String[] elementos = descritivo.trim().split("\\s+");
        String periodicidade = elementos[0];

        return switch (periodicidade) {
            case "semanal" -> construirAgendaSemanal(descritivo, elementos);
            case "mensal" -> construirAgendaMensal(descritivo, elementos);
            default -> throw new IllegalArgumentException("Descricao de agenda invalida");
        };
    }

    private static void validarDescritivo(String descritivo) {
        if (descritivo == null || descritivo.trim().isEmpty()) {
            throw new IllegalArgumentException("Descricao de agenda invalida");
        }

        if (AGENDAS_PRE_DEFINIDAS.contains(descritivo) || agendasPersonalizadas.containsKey(descritivo)) {
            throw new IllegalArgumentException("Agenda de pagamentos ja existe");
        }

        String[] elementos = descritivo.trim().split("\\s+");
        if (elementos.length < 2) {
            throw new IllegalArgumentException("Descricao de agenda invalida");
        }
    }

    private static AgendaDePags construirAgendaSemanal(String descritivo, String[] elementos) {
        if (elementos.length == 2) {
            return criarAgendaSemanalSimples(descritivo, elementos[1]);
        } else if (elementos.length == 3) {
            return criarAgendaSemanalComposta(descritivo, elementos[1], elementos[2]);
        } else {
            throw new IllegalArgumentException("Descricao de agenda invalida");
        }
    }

    private static AgendaDePags criarAgendaSemanalSimples(String descritivo, String diaSemanaStr) {
        int diaSemana = Integer.parseInt(diaSemanaStr);
        if (diaSemana < 1 || diaSemana > 7) {
            throw new IllegalArgumentException("Descricao de agenda invalida");
        }
        AgendaDePags agenda = new AgendaDePags(descritivo, "semanal", diaSemana, 0);
        agendasPersonalizadas.put(descritivo, agenda);
        return agenda;
    }

    private static AgendaDePags criarAgendaSemanalComposta(String descritivo, String semanasStr, String diaSemanaStr) {
        int semanas = Integer.parseInt(semanasStr);
        int diaSemana = Integer.parseInt(diaSemanaStr);
        if (semanas < 1 || semanas > 52 || diaSemana < 1 || diaSemana > 7) {
            throw new IllegalArgumentException("Descricao de agenda invalida");
        }
        AgendaDePags agenda = new AgendaDePags(descritivo, "semanal", semanas, diaSemana);
        agendasPersonalizadas.put(descritivo, agenda);
        return agenda;
    }

    private static AgendaDePags construirAgendaMensal(String descritivo, String[] elementos) {
        if (elementos.length != 2) {
            throw new IllegalArgumentException("Descricao de agenda invalida");
        }

        String parametro = elementos[1];
        if ("$".equals(parametro)) {
            AgendaDePags agenda = new AgendaDePags(descritivo, "mensal", -1, 0);
            agendasPersonalizadas.put(descritivo, agenda);
            return agenda;
        } else {
            int diaMes = Integer.parseInt(parametro);
            if (diaMes < 1 || diaMes > 28) {
                throw new IllegalArgumentException("Descricao de agenda invalida");
            }
            AgendaDePags agenda = new AgendaDePags(descritivo, "mensal", diaMes, 0);
            agendasPersonalizadas.put(descritivo, agenda);
            return agenda;
        }
    }

    public static boolean validarAgendaExistente(String descritivo) {
        return AGENDAS_PRE_DEFINIDAS.contains(descritivo) || agendasPersonalizadas.containsKey(descritivo);
    }

    public static AgendaDePags obterAgenda(String descritivo) {
        if (AGENDAS_PRE_DEFINIDAS.contains(descritivo)) {
            return new AgendaDePags(descritivo, obterPeriodicidadePadrao(descritivo),
                    obterParametro1Padrao(descritivo), obterParametro2Padrao(descritivo));
        }
        return agendasPersonalizadas.get(descritivo);
    }

    private static String obterPeriodicidadePadrao(String descritivo) {
        return descritivo.startsWith("semanal") ? "semanal" : "mensal";
    }

    private static int obterParametro1Padrao(String descritivo) {
        return switch (descritivo) {
            case "semanal 5" -> 5;
            case "semanal 2 5" -> 2;
            case "mensal $" -> -1;
            default -> 5;
        };
    }

    private static int obterParametro2Padrao(String descritivo) {
        return "semanal 2 5".equals(descritivo) ? 5 : 0;
    }

    public boolean verificarPagamentoData(String data) {
        try {
            String[] partesData = data.split("/");
            int dia = Integer.parseInt(partesData[0]);
            int mes = Integer.parseInt(partesData[1]);
            int ano = Integer.parseInt(partesData[2]);

            LocalDate dataConsulta = LocalDate.of(ano, mes, dia);

            return "semanal".equals(periodicidade) ? verificarPagamentoSemanal(dataConsulta)
                    : verificarPagamentoMensal(dataConsulta);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean verificarPagamentoSemanal(LocalDate dataConsulta) {
        DayOfWeek diaDaSemana = dataConsulta.getDayOfWeek();
        int numeroDiaSemana = diaDaSemana.getValue();

        if (valorParametro2 == 0) {
            return numeroDiaSemana == valorParametro1;
        } else {
            return verificarPagamentoSemanalComposto(dataConsulta, numeroDiaSemana);
        }
    }

    private boolean verificarPagamentoSemanalComposto(LocalDate dataConsulta, int numeroDiaSemana) {
        LocalDate dataReferencia = calcularDataReferencia();

        if (dataConsulta.isBefore(dataReferencia)) {
            return false;
        }

        long diferencaDias = ChronoUnit.DAYS.between(dataReferencia, dataConsulta);
        long diferencaSemanas = diferencaDias / 7;

        return numeroDiaSemana == valorParametro2 && diferencaSemanas % valorParametro1 == 0;
    }

    private LocalDate calcularDataReferencia() {
        if (valorParametro1 == 52 && valorParametro2 == 1) {
            return LocalDate.of(2004, 12, 26);
        } else {
            return LocalDate.of(2005, 1, 14);
        }
    }

    private boolean verificarPagamentoMensal(LocalDate dataConsulta) {
        if (valorParametro1 == -1) {
            return dataConsulta.equals(dataConsulta.withDayOfMonth(dataConsulta.lengthOfMonth()));
        } else {
            return dataConsulta.getDayOfMonth() == valorParametro1;
        }
    }

    public double computarValorPagamento(Empregado empregado, String inicioPeriodo, String fimPeriodo) {
        String categoriaEmpregado = empregado.getTipo();

        if ("semanal".equals(periodicidade)) {
            return valorParametro2 == 0 ? computarPagamentoSemanal(empregado, categoriaEmpregado)
                    : computarPagamentoSemanalComposto(empregado, categoriaEmpregado);
        } else if ("mensal".equals(periodicidade)) {
            return computarPagamentoMensal(empregado, categoriaEmpregado);
        }

        return 0.0;
    }

    private double computarPagamentoSemanal(Empregado empregado, String categoria) {
        return switch (categoria) {
            case "horista" -> computarPagamentoHorista(empregado, 7);
            case "assalariado" -> computarPagamentoAssalariadoSemanal(empregado);
            case "comissionado" -> computarPagamentoComissionado(empregado, 7);
            default -> 0.0;
        };
    }

    private double computarPagamentoSemanalComposto(Empregado empregado, String categoria) {
        return switch (categoria) {
            case "horista" -> computarPagamentoHorista(empregado, 14);
            case "assalariado" -> computarPagamentoAssalariadoBiSemanal(empregado);
            case "comissionado" -> computarPagamentoComissionado(empregado, valorParametro1 * 7);
            default -> 0.0;
        };
    }

    private double computarPagamentoMensal(Empregado empregado, String categoria) {
        return switch (categoria) {
            case "horista" -> computarPagamentoHorista(empregado, 30);
            case "assalariado" -> computarPagamentoAssalariadoMensal(empregado);
            case "comissionado" -> computarPagamentoComissionado(empregado, 30);
            default -> 0.0;
        };
    }

    private double computarPagamentoHorista(Empregado empregado, int dias) {
        if (!(empregado instanceof Horista))
            return 0.0;

        Horista horista = (Horista) empregado;
        double valorHora = horista.getSalarioPorHora();

        double horasNormais = calcularTotalHorasNormais(horista, dias);
        double horasExtra = calcularTotalHorasExtra(horista, dias);

        return (horasNormais * valorHora) + (horasExtra * valorHora * 1.5);
    }

    private double computarPagamentoAssalariadoSemanal(Empregado empregado) {
        if (!(empregado instanceof Assalariado))
            return 0.0;

        Assalariado assalariado = (Assalariado) empregado;
        double salarioAnual = assalariado.getSalarioMensal() * 12;
        return salarioAnual / 52;
    }

    private double computarPagamentoAssalariadoBiSemanal(Empregado empregado) {
        if (!(empregado instanceof Assalariado))
            return 0.0;

        Assalariado assalariado = (Assalariado) empregado;
        double salarioAnual = assalariado.getSalarioMensal() * 12;
        return (salarioAnual / 52) * valorParametro1;
    }

    private double computarPagamentoAssalariadoMensal(Empregado empregado) {
        if (!(empregado instanceof Assalariado))
            return 0.0;

        Assalariado assalariado = (Assalariado) empregado;
        return assalariado.getSalarioMensal();
    }

    private double computarPagamentoComissionado(Empregado empregado, int dias) {
        if (!(empregado instanceof Comissionado))
            return 0.0;

        Comissionado comissionado = (Comissionado) empregado;
        double salarioBase = calcularSalarioBaseComissionado(comissionado, dias);
        double comissoes = calcularTotalComissoes(comissionado, dias);

        return salarioBase + comissoes;
    }

    private double calcularSalarioBaseComissionado(Comissionado comissionado, int dias) {
        double salarioAnual = comissionado.getSalarioMensal() * 12;
        return (salarioAnual / 52) * (dias / 7.0);
    }

    private double calcularTotalHorasNormais(Horista horista, int dias) {
        return horista.getCartoes().stream()
                .limit(dias)
                .mapToDouble(cartao -> Math.min(cartao.getHoras(), 8))
                .sum();
    }

    private double calcularTotalHorasExtra(Horista horista, int dias) {
        return horista.getCartoes().stream()
                .limit(dias)
                .mapToDouble(cartao -> Math.max(cartao.getHoras() - 8, 0))
                .sum();
    }

    private double calcularTotalComissoes(Comissionado comissionado, int dias) {
        double taxaComissao = comissionado.getTaxaDeComissao();
        return comissionado.getResultadoDeVenda().stream()
                .limit(dias)
                .mapToDouble(venda -> venda.getValor() * taxaComissao)
                .sum();
    }

    public String getDescricao() {
        return descritivo;
    }

    public String getTipo() {
        return periodicidade;
    }

    public int getParametro1() {
        return valorParametro1;
    }

    public int getParametro2() {
        return valorParametro2;
    }

    public static Map<String, AgendaDePags> getAgendasCustomizadas() {
        return new HashMap<>(agendasPersonalizadas);
    }

    public static void limparAgendasCustomizadas() {
        agendasPersonalizadas.clear();
    }

    public static boolean agendaCustomizadaExiste(String descricao) {
        return agendasPersonalizadas.containsKey(descricao);
    }
}