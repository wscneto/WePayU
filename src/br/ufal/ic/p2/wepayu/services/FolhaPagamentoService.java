package br.ufal.ic.p2.wepayu.services;

import java.util.ArrayList;
import java.util.List;
import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.utils.*;
import java.util.Map;
import java.time.temporal.ChronoUnit;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.io.FileWriter;
import java.io.IOException;

public class FolhaPagamentoService {

    private Map<String, Empregado> emps;
    private Map<String, MembroSindicato> membsSind;

    public FolhaPagamentoService(Map<String, Empregado> emps, Map<String, MembroSindicato> membsSind) {
        this.emps = emps;
        this.membsSind = membsSind;
    }

    // ================================================================
    // Total Folha
    // ================================================================
    public String totalFolha(String data) throws DataInvalidaException {
        try {
            LocalDate dataFolha = LocalDate.parse(data, DateTimeFormatter.ofPattern("d/M/yyyy"));
            BigDecimal total = BigDecimal.ZERO;

            for (Empregado e : emps.values()) {
                if (deveReceberNaData(e, dataFolha)) {
                    total = total.add(calcularSalario(e, dataFolha));
                }
            }

            return FormatacaoMonetariaUtil.formatValor(total);
        } catch (Exception e) {
            return "0,00";
        }
    }

    // ================================================================
    // Rodar Folha
    // ================================================================
    public void rodaFolha(String data, String arquivo) throws DataInvalidaException {
        try {
            LocalDate dataFolha = LocalDate.parse(data, DateTimeFormatter.ofPattern("d/M/yyyy"));

            List<Empregado> empregadosHoristas = new ArrayList<>();
            List<Empregado> empregadosAssalariados = new ArrayList<>();
            List<Empregado> empregadosComissionados = new ArrayList<>();

            for (Empregado e : emps.values()) {
                if (deveReceberNaData(e, dataFolha)) {
                    switch (e.getTipo()) {
                        case "horista":
                            empregadosHoristas.add(e);
                            break;
                        case "assalariado":
                            empregadosAssalariados.add(e);
                            break;
                        case "comissionado":
                            empregadosComissionados.add(e);
                            break;
                    }
                }
            }

            empregadosHoristas.sort((e1, e2) -> e1.getNome().compareTo(e2.getNome()));
            empregadosAssalariados.sort((e1, e2) -> e1.getNome().compareTo(e2.getNome()));
            empregadosComissionados.sort((e1, e2) -> e1.getNome().compareTo(e2.getNome()));

            gerarArquivoFolha(dataFolha, arquivo, empregadosHoristas, empregadosAssalariados, empregadosComissionados);

        } catch (Exception e) {
            throw new DataInvalidaException("Data invalida.");
        }
    }

    private void gerarArquivoFolha(LocalDate dataFolha, String arquivo,
            List<Empregado> horistas, List<Empregado> assalariados,
            List<Empregado> comissionados) throws IOException {

        try (FileWriter writer = new FileWriter(arquivo)) {
            writer.write(String.format("FOLHA DE PAGAMENTO DO DIA %s\n",
                    dataFolha.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))));
            writer.write("====================================\n\n");

            writer.write(
                    "===============================================================================================================================\n");
            writer.write(
                    "===================== HORISTAS ================================================================================================\n");
            writer.write(
                    "===============================================================================================================================\n");
            writer.write(
                    "Nome                                 Horas Extra Salario Bruto Descontos Salario Liquido Metodo\n");
            writer.write(
                    "==================================== ===== ===== ============= ========= =============== ======================================\n");

            BigDecimal totalHoristasBruto = BigDecimal.ZERO;
            BigDecimal totalHoristasDescontos = BigDecimal.ZERO;
            BigDecimal totalHoristasLiquido = BigDecimal.ZERO;
            int totalHorasNormais = 0;
            int totalHorasExtras = 0;

            for (Empregado e : horistas) {
                Horista h = (Horista) e;

                if (h.getSindicato() != null) {
                    MembroSindicato sindicato = h.getSindicato();
                    BigDecimal taxaSemanal = new BigDecimal(String.valueOf(sindicato.getTaxaSindical()))
                            .multiply(BigDecimal.valueOf(7))
                            .setScale(2, RoundingMode.DOWN);
                    BigDecimal dividaAtual = new BigDecimal(String.valueOf(sindicato.getDividaSindical()));
                    BigDecimal novaDivida = dividaAtual.add(taxaSemanal);
                    sindicato.setDividaSindical(novaDivida.doubleValue());
                }

                BigDecimal salarioBruto = calcularSalarioHorista(h, dataFolha);
                BigDecimal descontos = BigDecimal.ZERO;
                BigDecimal salarioLiquido = BigDecimal.ZERO;

                if (salarioBruto.compareTo(BigDecimal.ZERO) > 0) {
                    descontos = calcularDescontos(h, dataFolha);
                    salarioLiquido = salarioBruto.subtract(descontos);

                    if (salarioLiquido.compareTo(BigDecimal.ZERO) < 0) {
                        if (h.getSindicato() != null) {
                            MembroSindicato sindicato = h.getSindicato();
                            BigDecimal dividaRestante = descontos.subtract(salarioBruto);
                            sindicato.setDividaSindical(dividaRestante.doubleValue());
                            descontos = salarioBruto;
                            salarioLiquido = BigDecimal.ZERO;
                        }
                    } else {
                        if (h.getSindicato() != null) {
                            h.getSindicato().setDividaSindical(0.0);
                        }
                    }
                }

                int[] horas = calcularHorasHorista(h, dataFolha);
                totalHorasNormais += horas[0];
                totalHorasExtras += horas[1];
                String metodoPagamento = formatarMetodoPagamento(h.getMetodoPagamento(), h.getEndereco());

                writer.write(String.format("%-36s %5d %5d %13s %9s %15s %s\n",
                        h.getNome(),
                        horas[0],
                        horas[1],
                        FormatacaoMonetariaUtil.formatValor(salarioBruto),
                        FormatacaoMonetariaUtil.formatValor(descontos),
                        FormatacaoMonetariaUtil.formatValor(salarioLiquido),
                        metodoPagamento));

                totalHoristasBruto = totalHoristasBruto.add(salarioBruto);
                totalHoristasDescontos = totalHoristasDescontos.add(descontos);
                totalHoristasLiquido = totalHoristasLiquido.add(salarioLiquido);
            }

            writer.write(String.format("\nTOTAL HORISTAS                       %5d %5d %13s %9s %15s\n\n",
                    totalHorasNormais,
                    totalHorasExtras,
                    FormatacaoMonetariaUtil.formatValor(totalHoristasBruto),
                    FormatacaoMonetariaUtil.formatValor(totalHoristasDescontos),
                    FormatacaoMonetariaUtil.formatValor(totalHoristasLiquido)));

            writer.write(
                    "===============================================================================================================================\n");
            writer.write(
                    "===================== ASSALARIADOS ============================================================================================\n");
            writer.write(
                    "===============================================================================================================================\n");
            writer.write(
                    "Nome                                             Salario Bruto Descontos Salario Liquido Metodo\n");
            writer.write(
                    "================================================ ============= ========= =============== ======================================\n");

            BigDecimal totalAssalariadosBruto = BigDecimal.ZERO;
            BigDecimal totalAssalariadosDescontos = BigDecimal.ZERO;
            BigDecimal totalAssalariadosLiquido = BigDecimal.ZERO;

            for (Empregado e : assalariados) {
                Assalariado a = (Assalariado) e;
                BigDecimal salarioBruto = calcularSalarioAssalariado(a, dataFolha);
                BigDecimal descontos = calcularDescontos(a, dataFolha);
                BigDecimal salarioLiquido = salarioBruto.subtract(descontos).max(BigDecimal.ZERO);
                String metodoPagamento = formatarMetodoPagamento(a.getMetodoPagamento(),
                        a.getEndereco());

                writer.write(String.format("%-48s %13s %9s %15s %s\n",
                        a.getNome(),
                        FormatacaoMonetariaUtil.formatValor(salarioBruto),
                        FormatacaoMonetariaUtil.formatValor(descontos),
                        FormatacaoMonetariaUtil.formatValor(salarioLiquido),
                        metodoPagamento));

                totalAssalariadosBruto = totalAssalariadosBruto.add(salarioBruto);
                totalAssalariadosDescontos = totalAssalariadosDescontos.add(descontos);
                totalAssalariadosLiquido = totalAssalariadosLiquido.add(salarioLiquido);
            }

            writer.write(String.format("\nTOTAL ASSALARIADOS                               %13s %9s %15s\n\n",
                    FormatacaoMonetariaUtil.formatValor(totalAssalariadosBruto),
                    FormatacaoMonetariaUtil.formatValor(totalAssalariadosDescontos),
                    FormatacaoMonetariaUtil.formatValor(totalAssalariadosLiquido)));

            writer.write(
                    "===============================================================================================================================\n");
            writer.write(
                    "===================== COMISSIONADOS ===========================================================================================\n");
            writer.write(
                    "===============================================================================================================================\n");
            writer.write(
                    "Nome                  Fixo     Vendas   Comissao Salario Bruto Descontos Salario Liquido Metodo\n");
            writer.write(
                    "===================== ======== ======== ======== ============= ========= =============== ======================================\n");

            BigDecimal totalComissionadosFixo = BigDecimal.ZERO;
            BigDecimal totalComissionadosVendas = BigDecimal.ZERO;
            BigDecimal totalComissionadosComissao = BigDecimal.ZERO;
            BigDecimal totalComissionadosBruto = BigDecimal.ZERO;
            BigDecimal totalComissionadosDescontos = BigDecimal.ZERO;
            BigDecimal totalComissionadosLiquido = BigDecimal.ZERO;

            for (Empregado e : comissionados) {
                Comissionado c = (Comissionado) e;
                BigDecimal[] valores = calcularValoresComissionado(c, dataFolha);
                BigDecimal salarioBruto = valores[0];
                BigDecimal vendas = valores[1];
                BigDecimal comissao = valores[2];
                BigDecimal salarioBase = valores[3];
                BigDecimal descontos = calcularDescontos(c, dataFolha);
                BigDecimal salarioLiquido = salarioBruto.subtract(descontos).max(BigDecimal.ZERO);
                String metodoPagamento = formatarMetodoPagamento(c.getMetodoPagamento(),
                        c.getEndereco());

                writer.write(String.format("%-21s %8s %8s %8s %13s %9s %15s %s\n",
                        c.getNome(),
                        FormatacaoMonetariaUtil.formatValor(salarioBase),
                        FormatacaoMonetariaUtil.formatValor(vendas),
                        FormatacaoMonetariaUtil.formatValor(comissao),
                        FormatacaoMonetariaUtil.formatValor(salarioBruto),
                        FormatacaoMonetariaUtil.formatValor(descontos),
                        FormatacaoMonetariaUtil.formatValor(salarioLiquido),
                        metodoPagamento));

                totalComissionadosFixo = totalComissionadosFixo.add(salarioBase);
                totalComissionadosVendas = totalComissionadosVendas.add(vendas);
                totalComissionadosComissao = totalComissionadosComissao.add(comissao);
                totalComissionadosBruto = totalComissionadosBruto.add(salarioBruto);
                totalComissionadosDescontos = totalComissionadosDescontos.add(descontos);
                totalComissionadosLiquido = totalComissionadosLiquido.add(salarioLiquido);
            }

            writer.write(String.format("\nTOTAL COMISSIONADOS   %8s %8s %8s %13s %9s %15s\n",
                    FormatacaoMonetariaUtil.formatValor(totalComissionadosFixo),
                    FormatacaoMonetariaUtil.formatValor(totalComissionadosVendas),
                    FormatacaoMonetariaUtil.formatValor(totalComissionadosComissao),
                    FormatacaoMonetariaUtil.formatValor(totalComissionadosBruto),
                    FormatacaoMonetariaUtil.formatValor(totalComissionadosDescontos),
                    FormatacaoMonetariaUtil.formatValor(totalComissionadosLiquido)));

            BigDecimal totalFolha = totalHoristasBruto.add(totalAssalariadosBruto).add(totalComissionadosBruto);
            writer.write(String.format("\nTOTAL FOLHA: %s\n", FormatacaoMonetariaUtil.formatValor(totalFolha)));
        }
    }

    private boolean deveReceberNaData(Empregado e, LocalDate data) {
        String dataString = data.format(DateTimeFormatter.ofPattern("d/M/yyyy"));
        return e.getAgendaPagamento().devePagarNaData(dataString);
    }

    private BigDecimal calcularSalario(Empregado e, LocalDate data) {
        String tipo = e.getTipo();
        String agenda = e.getAgendaPagamento().getAgenda();

        if (agenda.equals(AgendaPag.getAgendaPadrao(tipo))) {
            switch (tipo) {
                case "horista":
                    return calcularSalarioHorista((Horista) e, data);
                case "assalariado":
                    return calcularSalarioAssalariado((Assalariado) e, data);
                case "comissionado":
                    return calcularSalarioComissionado((Comissionado) e, data);
                default:
                    return BigDecimal.ZERO;
            }
        } else {
            String dataString = data.format(DateTimeFormatter.ofPattern("d/M/yyyy"));
            String dataInicial = calcularDataInicialPeriodo(e, data);
            String dataFinal = dataString;

            double valorPagamento = e.getAgendaPagamento().calcularValorPagamento(e, dataInicial,
                    dataFinal);
            return BigDecimal.valueOf(valorPagamento);
        }
    }

    private String calcularDataInicialPeriodo(Empregado e, LocalDate data) {
        String agenda = e.getAgendaPagamento().getAgenda();

        switch (agenda) {
            case "semanal 5":
                return data.minusDays(6).format(DateTimeFormatter.ofPattern("d/M/yyyy"));
            case "semanal 2 5":
                return data.minusDays(13).format(DateTimeFormatter.ofPattern("d/M/yyyy"));
            case "mensal $":
                return data.withDayOfMonth(1).format(DateTimeFormatter.ofPattern("d/M/yyyy"));
            default:
                return data.minusDays(6).format(DateTimeFormatter.ofPattern("d/M/yyyy"));
        }
    }

    private BigDecimal calcularSalarioHorista(Horista e, LocalDate data) {
        LocalDate inicioSemana = data.minusDays(6);
        BigDecimal horasNormais = BigDecimal.ZERO;
        BigDecimal horasExtras = BigDecimal.ZERO;

        for (CartaoPonto c : e.getCartoes()) {
            LocalDate dataCartao = LocalDate.parse(c.getData(), DateTimeFormatter.ofPattern("d/M/yyyy"));
            if (!dataCartao.isBefore(inicioSemana) && !dataCartao.isAfter(data)) {
                BigDecimal horas = BigDecimal.valueOf(c.getHoras());
                horasNormais = horasNormais.add(horas.min(BigDecimal.valueOf(8.0)));
                horasExtras = horasExtras.add(horas.subtract(BigDecimal.valueOf(8.0)).max(BigDecimal.ZERO));
            }
        }

        BigDecimal salarioPorHora = BigDecimal.valueOf(e.getSalarioPorHora());
        BigDecimal salarioBruto = horasNormais.multiply(salarioPorHora)
                .add(horasExtras.multiply(salarioPorHora).multiply(BigDecimal.valueOf(1.5)));

        return salarioBruto;
    }

    private BigDecimal calcularSalarioAssalariado(Assalariado e, LocalDate data) {
        return BigDecimal.valueOf(e.getSalarioMensal());
    }

    private BigDecimal calcularSalarioComissionado(Comissionado e, LocalDate data) {
        BigDecimal salarioMensal = BigDecimal.valueOf(e.getSalarioMensal());
        BigDecimal salarioBase = salarioMensal.multiply(BigDecimal.valueOf(12))
                .divide(BigDecimal.valueOf(26), 10, RoundingMode.DOWN);
        salarioBase = salarioBase.setScale(2, RoundingMode.DOWN);

        LocalDate inicioPeriodo = data.minusDays(14);
        BigDecimal totalVendas = BigDecimal.ZERO;

        for (ResultadoDeVenda v : e.getResultadoDeVenda()) {
            LocalDate dataVenda = LocalDate.parse(v.getData(), DateTimeFormatter.ofPattern("d/M/yyyy"));
            if (!dataVenda.isBefore(inicioPeriodo) && !dataVenda.isAfter(data)) {
                totalVendas = totalVendas.add(BigDecimal.valueOf(v.getValor()));
            }
        }

        BigDecimal comissao = totalVendas.multiply(BigDecimal.valueOf(e.getTaxaDeComissao()));
        comissao = comissao.setScale(2, RoundingMode.DOWN);
        BigDecimal salarioBruto = salarioBase.add(comissao);

        return salarioBruto;
    }

    private BigDecimal calcularDescontos(Empregado e, LocalDate data) {
        if (e.getSindicato() == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.DOWN);
        }

        MembroSindicato sindicato = e.getSindicato();
        BigDecimal descontos = BigDecimal.ZERO;

        if (e.getTipo().equals("horista")) {
            return calcularDescontosHorista(e, sindicato, data);
        }

        int diasPeriodo = calcularDiasPeriodo(e, data);
        BigDecimal taxaDiaria = new BigDecimal(String.valueOf(sindicato.getTaxaSindical()));
        BigDecimal taxaSindicalTotal = taxaDiaria.multiply(BigDecimal.valueOf(diasPeriodo))
                .setScale(2, RoundingMode.DOWN);
        descontos = descontos.add(taxaSindicalTotal);

        LocalDate inicioPeriodo = data.minusDays(diasPeriodo);

        for (TaxaServico t : sindicato.getTaxasDeServicos()) {
            LocalDate dataTaxa = LocalDate.parse(t.getData(), DateTimeFormatter.ofPattern("d/M/yyyy"));
            if (!dataTaxa.isBefore(inicioPeriodo) && !dataTaxa.isAfter(data)) {
                BigDecimal valorTaxa = new BigDecimal(String.valueOf(t.getValor()))
                        .setScale(2, RoundingMode.DOWN);
                descontos = descontos.add(valorTaxa);
            }
        }

        return descontos.setScale(2, RoundingMode.DOWN);
    }

    private BigDecimal calcularDescontosHorista(Empregado e, MembroSindicato sindicato, LocalDate data) {
        LocalDate inicioSemana = data.minusDays(6);
        BigDecimal taxasServico = BigDecimal.ZERO;

        for (TaxaServico t : sindicato.getTaxasDeServicos()) {
            LocalDate dataTaxa = LocalDate.parse(t.getData(), DateTimeFormatter.ofPattern("d/M/yyyy"));
            if (!dataTaxa.isBefore(inicioSemana) && !dataTaxa.isAfter(data)) {
                BigDecimal valorTaxa = new BigDecimal(String.valueOf(t.getValor()))
                        .setScale(2, RoundingMode.DOWN);
                taxasServico = taxasServico.add(valorTaxa);
            }
        }

        BigDecimal dividaAtual = new BigDecimal(String.valueOf(sindicato.getDividaSindical()));
        BigDecimal totalDescontos = dividaAtual.add(taxasServico);

        return totalDescontos.setScale(2, RoundingMode.DOWN);
    }

    private int calcularDiasPeriodo(Empregado e, LocalDate data) {
        switch (e.getTipo()) {
            case "horista":
                return calcularDiasPeriodoHorista(e, data);
            case "assalariado":
                return data.lengthOfMonth();
            case "comissionado":
                return 14;
            default:
                return 1;
        }
    }

    private int calcularDiasPeriodoHorista(Empregado e, LocalDate data) {
        LocalDate primeiroPagamento = LocalDate.of(2005, 1, 7);

        if (data.isBefore(primeiroPagamento))
            return 0;

        LocalDate ultimoPagamento = calcularUltimoPagamentoHorista(data);

        long diasEntre = ChronoUnit.DAYS.between(ultimoPagamento, data);

        int semanas = (int) (diasEntre / 7);

        return semanas * 7;
    }

    private LocalDate calcularUltimoPagamentoHorista(LocalDate data) {
        LocalDate primeiroPagamento = LocalDate.of(2005, 1, 7);

        if (data.isBefore(primeiroPagamento))
            return primeiroPagamento;

        long diasEntre = ChronoUnit.DAYS.between(primeiroPagamento, data);
        int semanas = (int) (diasEntre / 7);

        return primeiroPagamento.plusDays(semanas * 7);
    }

    private int[] calcularHorasHorista(Horista e, LocalDate data) {
        LocalDate inicioSemana = data.minusDays(6);
        int horasNormais = 0;
        int horasExtras = 0;

        for (CartaoPonto c : e.getCartoes()) {
            LocalDate dataCartao = LocalDate.parse(c.getData(), DateTimeFormatter.ofPattern("d/M/yyyy"));
            if (!dataCartao.isBefore(inicioSemana) && !dataCartao.isAfter(data)) {
                int horas = c.getHoras().intValue();
                horasNormais += Math.min(horas, 8);
                horasExtras += Math.max(horas - 8, 0);
            }
        }

        return new int[] { horasNormais, horasExtras };
    }

    private BigDecimal[] calcularValoresComissionado(Comissionado e, LocalDate data) {
        BigDecimal salarioMensal = BigDecimal.valueOf(e.getSalarioMensal());
        BigDecimal salarioBase = salarioMensal.multiply(BigDecimal.valueOf(12))
                .divide(BigDecimal.valueOf(26), 10, RoundingMode.DOWN)
                .setScale(2, RoundingMode.DOWN);

        LocalDate inicioPeriodo = data.minusDays(14);
        BigDecimal totalVendas = BigDecimal.ZERO;

        for (ResultadoDeVenda v : e.getResultadoDeVenda()) {
            LocalDate dataVenda = LocalDate.parse(v.getData(), DateTimeFormatter.ofPattern("d/M/yyyy"));
            if (!dataVenda.isBefore(inicioPeriodo) && !dataVenda.isAfter(data)) {
                totalVendas = totalVendas.add(BigDecimal.valueOf(v.getValor()));
            }
        }

        BigDecimal comissao = totalVendas.multiply(BigDecimal.valueOf(e.getTaxaDeComissao()))
                .setScale(2, RoundingMode.DOWN);
        BigDecimal salarioBruto = salarioBase.add(comissao);

        return new BigDecimal[] { salarioBruto, totalVendas, comissao, salarioBase };
    }

    private String formatarMetodoPagamento(MetodoPagamento metodoPag, String endereco) {
        if (metodoPag == null) {
            return "Em maos";
        }

        if (metodoPag instanceof EmMaos) {
            return "Em maos";
        } else if (metodoPag instanceof Banco) {
            Banco banco = (Banco) metodoPag;
            return String.format("Banco do Brasil, Ag. %s CC %s", banco.getAgencia(), banco.getContaCorrente());
        } else if (metodoPag instanceof Correios) {
            return String.format("Correios, %s", endereco);
        }

        return "Em maos";
    }
}