package br.ufal.ic.p2.wepayu.models;

import java.time.temporal.*;
import java.time.DayOfWeek;
import java.time.LocalDate;

public class AgendaPag {
    private String agenda;

    public static final String SEMANAL_5 = "semanal 5";
    public static final String SEMANAL_2_5 = "semanal 2 5";
    public static final String MENSAL_DOLAR = "mensal $";

    public AgendaPag() {
        this.agenda = SEMANAL_5;
    }

    public AgendaPag(String agenda) {
        this.agenda = agenda;
    }

    public String getAgenda() {
        return agenda;
    }

    public void setAgenda(String agenda) {
        this.agenda = agenda;
    }

    public static boolean isAgendaValida(String agenda) {
        return SEMANAL_5.equals(agenda) ||
                SEMANAL_2_5.equals(agenda) ||
                MENSAL_DOLAR.equals(agenda) ||
                AgendaDePags.validarAgendaExistente(agenda);
    }

    public static String getAgendaPadrao(String tipo) {
        switch (tipo) {
            case "horista":
                return SEMANAL_5;
            case "assalariado":
                return MENSAL_DOLAR;
            case "comissionado":
                return SEMANAL_2_5;
            default:
                return SEMANAL_5;
        }
    }

    public boolean devePagarNaData(String data) {
        try {
            if (SEMANAL_5.equals(agenda) || SEMANAL_2_5.equals(agenda) || MENSAL_DOLAR.equals(agenda)) {
                return devePagarNaDataPadrao(data);
            } else {
                AgendaDePags agendaCustomizada = AgendaDePags.obterAgenda(agenda);
                if (agendaCustomizada != null) {
                    return agendaCustomizada.verificarPagamentoData(data);
                }
                return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    private boolean devePagarNaDataPadrao(String data) {
        try {
            String[] partes = data.split("/");
            int dia = Integer.parseInt(partes[0]);
            int mes = Integer.parseInt(partes[1]);
            int ano = Integer.parseInt(partes[2]);

            LocalDate localDate = LocalDate.of(ano, mes, dia);
            DayOfWeek diaSemana = localDate.getDayOfWeek();

            switch (agenda) {
                case SEMANAL_5:
                    return diaSemana == DayOfWeek.FRIDAY;

                case SEMANAL_2_5:
                    LocalDate primeiroPagamento = LocalDate.of(2005, 1, 14);

                    if (localDate.isBefore(primeiroPagamento)) {
                        return false;
                    }

                    long diasEntre = ChronoUnit.DAYS.between(primeiroPagamento, localDate);
                    return diaSemana == DayOfWeek.FRIDAY && diasEntre % 14 == 0;

                case MENSAL_DOLAR:
                    return localDate.equals(localDate.withDayOfMonth(localDate.lengthOfMonth()));

                default:
                    return false;
            }
        } catch (Exception e) {
            return false;
        }
    }

    public double calcularValorPagamento(Empregado empregado, String dataInicial, String dataFinal) {
        if (SEMANAL_5.equals(agenda) || SEMANAL_2_5.equals(agenda) || MENSAL_DOLAR.equals(agenda))
            return calcularValorPagamentoPadrao(empregado, dataInicial, dataFinal);
        else {
            AgendaDePags agendaCustomizada = AgendaDePags.obterAgenda(agenda);
            if (agendaCustomizada != null)
                return agendaCustomizada.computarValorPagamento(empregado, dataInicial, dataFinal);

            return 0.0;
        }
    }

    private double calcularValorPagamentoPadrao(Empregado empregado, String dataInicial, String dataFinal) {
        String tipo = empregado.getTipo();

        switch (agenda) {
            case SEMANAL_5:

                if ("horista".equals(tipo)) {
                    return calcularPagamentoHoristaSemanal(empregado, dataInicial, dataFinal);
                } else if ("assalariado".equals(tipo)) {
                    return calcularPagamentoAssalariadoSemanal(empregado);
                } else if ("comissionado".equals(tipo)) {
                    return calcularPagamentoComissionadoSemanal(empregado, dataInicial, dataFinal);
                }
                break;

            case SEMANAL_2_5:

                if ("horista".equals(tipo)) {
                    return calcularPagamentoHoristaBiSemanal(empregado, dataInicial, dataFinal);
                } else if ("assalariado".equals(tipo)) {
                    return calcularPagamentoAssalariadoBiSemanal(empregado);
                } else if ("comissionado".equals(tipo)) {
                    return calcularPagamentoComissionadoBiSemanal(empregado, dataInicial, dataFinal);
                }
                break;

            case MENSAL_DOLAR:

                if ("horista".equals(tipo)) {
                    return calcularPagamentoHoristaMensal(empregado, dataInicial, dataFinal);
                } else if ("assalariado".equals(tipo)) {
                    return calcularPagamentoAssalariadoMensal(empregado);
                } else if ("comissionado".equals(tipo)) {
                    return calcularPagamentoComissionadoMensal(empregado, dataInicial, dataFinal);
                }
                break;
        }

        return 0.0;
    }

    private double calcularPagamentoHoristaSemanal(Empregado empregado, String dataInicial, String dataFinal) {
        if (!(empregado instanceof Horista))
            return 0.0;

        Horista horista = (Horista) empregado;
        double salarioHora = horista.getSalarioPorHora();

        double horasNormais = calcularHorasNormais(horista, dataInicial, dataFinal);
        double horasExtras = calcularHorasExtras(horista, dataInicial, dataFinal);

        return (horasNormais * salarioHora) + (horasExtras * salarioHora * 1.5);
    }

    private double calcularPagamentoHoristaBiSemanal(Empregado empregado, String dataInicial, String dataFinal) {
        if (!(empregado instanceof Horista))
            return 0.0;

        Horista horista = (Horista) empregado;
        double salarioHora = horista.getSalarioPorHora();

        double horasNormais = calcularHorasNormais(horista, dataInicial, dataFinal);
        double horasExtras = calcularHorasExtras(horista, dataInicial, dataFinal);

        return (horasNormais * salarioHora) + (horasExtras * salarioHora * 1.5);
    }

    private double calcularPagamentoHoristaMensal(Empregado empregado, String dataInicial, String dataFinal) {
        if (!(empregado instanceof Horista))
            return 0.0;

        Horista horista = (Horista) empregado;
        double salarioHora = horista.getSalarioPorHora();

        double horasNormais = calcularHorasNormais(horista, dataInicial, dataFinal);
        double horasExtras = calcularHorasExtras(horista, dataInicial, dataFinal);

        return (horasNormais * salarioHora) + (horasExtras * salarioHora * 1.5);
    }

    private double calcularPagamentoAssalariadoSemanal(Empregado empregado) {
        if (!(empregado instanceof Assalariado))
            return 0.0;

        Assalariado assalariado = (Assalariado) empregado;
        double salarioAnual = assalariado.getSalarioMensal() * 12;
        return salarioAnual / 52;
    }

    private double calcularPagamentoAssalariadoBiSemanal(Empregado empregado) {
        if (!(empregado instanceof Assalariado))
            return 0.0;

        Assalariado assalariado = (Assalariado) empregado;
        double salarioAnual = assalariado.getSalarioMensal() * 12;
        return salarioAnual / 26;
    }

    private double calcularPagamentoAssalariadoMensal(Empregado empregado) {
        if (!(empregado instanceof Assalariado))
            return 0.0;

        Assalariado assalariado = (Assalariado) empregado;
        return assalariado.getSalarioMensal();
    }

    private double calcularPagamentoComissionadoSemanal(Empregado empregado, String dataInicial, String dataFinal) {
        if (!(empregado instanceof Comissionado))
            return 0.0;

        Comissionado comissionado = (Comissionado) empregado;
        double salarioAnual = comissionado.getSalarioMensal() * 12;
        double salarioSemanal = salarioAnual / 52;

        double comissoes = calcularComissoes(comissionado, dataInicial, dataFinal);

        return salarioSemanal + comissoes;
    }

    private double calcularPagamentoComissionadoBiSemanal(Empregado empregado, String dataInicial, String dataFinal) {
        if (!(empregado instanceof Comissionado))
            return 0.0;

        Comissionado comissionado = (Comissionado) empregado;
        double salarioAnual = comissionado.getSalarioMensal() * 12;
        double salarioBiSemanal = salarioAnual / 26;

        double comissoes = calcularComissoes(comissionado, dataInicial, dataFinal);

        return salarioBiSemanal + comissoes;
    }

    private double calcularPagamentoComissionadoMensal(Empregado empregado, String dataInicial, String dataFinal) {
        if (!(empregado instanceof Comissionado))
            return 0.0;

        Comissionado comissionado = (Comissionado) empregado;
        double salarioMensal = comissionado.getSalarioMensal();

        double comissoes = calcularComissoes(comissionado, dataInicial, dataFinal);

        return salarioMensal + comissoes;
    }

    private double calcularHorasNormais(Horista horista, String dataInicial, String dataFinal) {
        double totalHoras = 0.0;

        for (CartaoPonto cartao : horista.getCartoes()) {
            if (estaNoPeriodo(cartao.getData(), dataInicial, dataFinal)) {
                double horas = cartao.getHoras();
                if (horas <= 8) {
                    totalHoras += horas;
                } else {
                    totalHoras += 8;
                }
            }
        }

        return totalHoras;
    }

    private double calcularHorasExtras(Horista horista, String dataInicial, String dataFinal) {
        double totalHoras = 0.0;

        for (CartaoPonto cartao : horista.getCartoes()) {
            if (estaNoPeriodo(cartao.getData(), dataInicial, dataFinal)) {
                double horas = cartao.getHoras();
                if (horas > 8) {
                    totalHoras += (horas - 8);
                }
            }
        }

        return totalHoras;
    }

    private double calcularComissoes(Comissionado comissionado, String dataInicial, String dataFinal) {
        double totalComissoes = 0.0;
        double taxaComissao = comissionado.getTaxaDeComissao();

        for (ResultadoDeVenda venda : comissionado.getResultadoDeVenda()) {
            if (estaNoPeriodo(venda.getData(), dataInicial, dataFinal)) {
                totalComissoes += venda.getValor() * taxaComissao;
            }
        }

        return totalComissoes;
    }

    private boolean estaNoPeriodo(String data, String dataInicial, String dataFinal) {
        try {
            String[] partesData = data.split("/");
            String[] partesInicial = dataInicial.split("/");
            String[] partesFinal = dataFinal.split("/");

            int dia = Integer.parseInt(partesData[0]);
            int mes = Integer.parseInt(partesData[1]);
            int ano = Integer.parseInt(partesData[2]);

            int diaInicial = Integer.parseInt(partesInicial[0]);
            int mesInicial = Integer.parseInt(partesInicial[1]);
            int anoInicial = Integer.parseInt(partesInicial[2]);

            int diaFinal = Integer.parseInt(partesFinal[0]);
            int mesFinal = Integer.parseInt(partesFinal[1]);
            int anoFinal = Integer.parseInt(partesFinal[2]);

            LocalDate localDate = LocalDate.of(ano, mes, dia);
            LocalDate dataInicialLocal = LocalDate.of(anoInicial, mesInicial, diaInicial);
            LocalDate dataFinalLocal = LocalDate.of(anoFinal, mesFinal, diaFinal);

            return !localDate.isBefore(dataInicialLocal) && !localDate.isAfter(dataFinalLocal);
        } catch (Exception e) {
            return false;
        }
    }
}
