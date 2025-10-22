package br.ufal.ic.p2.wepayu.models;

import java.time.temporal.*;
import java.time.DayOfWeek;
import java.time.LocalDate;

public class AgendaPag {
    private String configuracao;

    public static final String SEMANAL_5 = "semanal 5";
    public static final String SEMANAL_2_5 = "semanal 2 5";
    public static final String MENSAL_DOLAR = "mensal $";

    public AgendaPag() {
        this.configuracao = SEMANAL_5;
    }

    public AgendaPag(String config) {
        this.configuracao = config;
    }

    public String getAgenda() {
        return this.configuracao;
    }

    public void setAgenda(String config) {
        this.configuracao = config;
    }

    public static boolean isAgendaValida(String config) {
        if (SEMANAL_5.equals(config))
            return true;
        if (SEMANAL_2_5.equals(config))
            return true;
        if (MENSAL_DOLAR.equals(config))
            return true;
        return AgendaDePags.validarAgendaExistente(config);
    }

    public static String getAgendaPadrao(String empregadoTipo) {
        if ("horista".equals(empregadoTipo))
            return SEMANAL_5;
        if ("assalariado".equals(empregadoTipo))
            return MENSAL_DOLAR;
        if ("comissionado".equals(empregadoTipo))
            return SEMANAL_2_5;
        return SEMANAL_5;
    }

    public boolean devePagarNaData(String dataPagamento) {
        try {
            if (ehAgendaPadrao()) {
                return processarDataPadrao(dataPagamento);
            } else {
                AgendaDePags agendaCustom = AgendaDePags.obterAgenda(configuracao);
                return agendaCustom != null && agendaCustom.verificarPagamentoData(dataPagamento);
            }
        } catch (Exception e) {
            return false;
        }
    }

    public double calcularValorPagamento(Empregado emp, String inicio, String fim) {
        if (ehAgendaPadrao()) {
            return processarPagamentoPadrao(emp, inicio, fim);
        } else {
            AgendaDePags agendaCustom = AgendaDePags.obterAgenda(configuracao);
            if (agendaCustom != null)
                return agendaCustom.computarValorPagamento(emp, inicio, fim);
            return 0.0;
        }
    }

    private boolean ehAgendaPadrao() {
        return SEMANAL_5.equals(configuracao) ||
                SEMANAL_2_5.equals(configuracao) ||
                MENSAL_DOLAR.equals(configuracao);
    }

    private boolean processarDataPadrao(String data) {
        try {
            LocalDate dataLocal = parseData(data);
            switch (configuracao) {
                case SEMANAL_5:
                    return dataLocal.getDayOfWeek() == DayOfWeek.FRIDAY;

                case SEMANAL_2_5:
                    LocalDate primeiroPagamento = LocalDate.of(2005, 1, 14);
                    if (dataLocal.isBefore(primeiroPagamento))
                        return false;
                    long diferencaDias = ChronoUnit.DAYS.between(primeiroPagamento, dataLocal);
                    return dataLocal.getDayOfWeek() == DayOfWeek.FRIDAY && diferencaDias % 14 == 0;

                case MENSAL_DOLAR:
                    return dataLocal.getDayOfMonth() == dataLocal.lengthOfMonth();

                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private double processarPagamentoPadrao(Empregado emp, String inicio, String fim) {
        String tipoEmpregado = emp.getTipo();

        if (SEMANAL_5.equals(configuracao)) {
            return calcularSemanal(emp, tipoEmpregado, inicio, fim);
        } else if (SEMANAL_2_5.equals(configuracao)) {
            return calcularBiSemanal(emp, tipoEmpregado, inicio, fim);
        } else if (MENSAL_DOLAR.equals(configuracao)) {
            return calcularMensal(emp, tipoEmpregado, inicio, fim);
        }
        return 0.0;
    }

    private double calcularSemanal(Empregado emp, String tipo, String inicio, String fim) {
        switch (tipo) {
            case "horista":
                return calcularHorista(emp, inicio, fim);
            case "assalariado":
                return ((Assalariado) emp).getSalarioMensal() * 12 / 52;
            case "comissionado":
                Comissionado com = (Comissionado) emp;
                return (com.getSalarioMensal() * 12 / 52) + calcularComissoesPeriodo(com, inicio, fim);
            default:
                return 0.0;
        }
    }

    private double calcularBiSemanal(Empregado emp, String tipo, String inicio, String fim) {
        switch (tipo) {
            case "horista":
                return calcularHorista(emp, inicio, fim);
            case "assalariado":
                return ((Assalariado) emp).getSalarioMensal() * 12 / 26;
            case "comissionado":
                Comissionado com = (Comissionado) emp;
                return (com.getSalarioMensal() * 12 / 26) + calcularComissoesPeriodo(com, inicio, fim);
            default:
                return 0.0;
        }
    }

    private double calcularMensal(Empregado emp, String tipo, String inicio, String fim) {
        switch (tipo) {
            case "horista":
                return calcularHorista(emp, inicio, fim);
            case "assalariado":
                return ((Assalariado) emp).getSalarioMensal();
            case "comissionado":
                Comissionado com = (Comissionado) emp;
                return com.getSalarioMensal() + calcularComissoesPeriodo(com, inicio, fim);
            default:
                return 0.0;
        }
    }

    private double calcularHorista(Empregado emp, String inicio, String fim) {
        if (!(emp instanceof Horista))
            return 0.0;
        Horista horista = (Horista) emp;
        double salarioHora = horista.getSalarioPorHora();
        double horasNormais = computarHorasNormais(horista, inicio, fim);
        double horasExtras = computarHorasExtras(horista, inicio, fim);
        return (horasNormais * salarioHora) + (horasExtras * salarioHora * 1.5);
    }

    private double computarHorasNormais(Horista horista, String inicio, String fim) {
        double total = 0.0;
        for (CartaoPonto cp : horista.getCartoes()) {
            if (dentroDoIntervalo(cp.getData(), inicio, fim)) {
                total += Math.min(cp.getHoras(), 8);
            }
        }
        return total;
    }

    private double computarHorasExtras(Horista horista, String inicio, String fim) {
        double total = 0.0;
        for (CartaoPonto cp : horista.getCartoes()) {
            if (dentroDoIntervalo(cp.getData(), inicio, fim)) {
                double horas = cp.getHoras();
                if (horas > 8)
                    total += horas - 8;
            }
        }
        return total;
    }

    private double calcularComissoesPeriodo(Comissionado com, String inicio, String fim) {
        double total = 0.0;
        double taxa = com.getTaxaDeComissao();
        for (ResultadoDeVenda venda : com.getResultadoDeVenda()) {
            if (dentroDoIntervalo(venda.getData(), inicio, fim)) {
                total += venda.getValor() * taxa;
            }
        }
        return total;
    }

    private boolean dentroDoIntervalo(String data, String inicio, String fim) {
        try {
            LocalDate dataRef = parseData(data);
            LocalDate dataInicio = parseData(inicio);
            LocalDate dataFim = parseData(fim);
            return !dataRef.isBefore(dataInicio) && !dataRef.isAfter(dataFim);
        } catch (Exception e) {
            return false;
        }
    }

    private LocalDate parseData(String data) {
        String[] partes = data.split("/");
        return LocalDate.of(
                Integer.parseInt(partes[2]),
                Integer.parseInt(partes[1]),
                Integer.parseInt(partes[0]));
    }
}