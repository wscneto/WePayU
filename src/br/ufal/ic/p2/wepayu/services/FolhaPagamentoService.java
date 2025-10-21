package br.ufal.ic.p2.wepayu.services;

import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.utils.FormatacaoMonetariaUtil;
import java.util.Map;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class FolhaPagamentoService {

    private Map<String, Empregado> empregados;
    private Map<String, MembroSindicato> membrosSindicato;

    public FolhaPagamentoService(Map<String, Empregado> empregados, Map<String, MembroSindicato> membrosSindicato) {
        this.empregados = empregados;
        this.membrosSindicato = membrosSindicato;
    }

    public String totalFolha(String data) throws DataInvalidaException {
        try {
            LocalDate dataFolha = LocalDate.parse(data, DateTimeFormatter.ofPattern("d/M/yyyy"));
            BigDecimal total = BigDecimal.ZERO;

            for (Empregado empregado : empregados.values()) {
                if (deveReceberNaData(empregado, dataFolha)) {
                    total = total.add(calcularSalario(empregado, dataFolha)); // acumula com máxima precisão
                }
            }

            return FormatacaoMonetariaUtil.formatValor(total);
        } catch (Exception e) {
            return "0,00";
        }
    }

    public void rodaFolha(String data, String arquivo) throws DataInvalidaException {
        try {
            LocalDate dataFolha = LocalDate.parse(data, DateTimeFormatter.ofPattern("d/M/yyyy"));

            List<Empregado> empregadosHoristas = new ArrayList<>();
            List<Empregado> empregadosAssalariados = new ArrayList<>();
            List<Empregado> empregadosComissionados = new ArrayList<>();

            for (Empregado empregado : empregados.values()) {
                if (deveReceberNaData(empregado, dataFolha)) {
                    switch (empregado.getTipo()) {
                        case "horista":
                            empregadosHoristas.add(empregado);
                            break;
                        case "assalariado":
                            empregadosAssalariados.add(empregado);
                            break;
                        case "comissionado":
                            empregadosComissionados.add(empregado);
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

    private boolean deveReceberNaData(Empregado empregado, LocalDate data) {
        String dataString = data.format(DateTimeFormatter.ofPattern("d/M/yyyy"));
        return empregado.getAgendaPagamento().devePagarNaData(dataString);
    }

    private BigDecimal calcularSalario(Empregado empregado, LocalDate data) {
        String tipo = empregado.getTipo();
        String agenda = empregado.getAgendaPagamento().getAgenda();

        if (agenda.equals(br.ufal.ic.p2.wepayu.models.AgendaPag.getAgendaPadrao(tipo))) {
            switch (tipo) {
                case "horista":
                    return calcularSalarioHorista((Horista) empregado, data);
                case "assalariado":
                    return calcularSalarioAssalariado((Assalariado) empregado, data);
                case "comissionado":
                    return calcularSalarioComissionado((Comissionado) empregado, data);
                default:
                    return BigDecimal.ZERO;
            }
        } else {
            String dataString = data.format(DateTimeFormatter.ofPattern("d/M/yyyy"));
            String dataInicial = calcularDataInicialPeriodo(empregado, data);
            String dataFinal = dataString;

            double valorPagamento = empregado.getAgendaPagamento().calcularValorPagamento(empregado, dataInicial,
                    dataFinal);
            return BigDecimal.valueOf(valorPagamento);
        }
    }

    private String calcularDataInicialPeriodo(Empregado empregado, LocalDate data) {
        String agenda = empregado.getAgendaPagamento().getAgenda();

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

    private BigDecimal calcularSalarioHorista(Horista empregado, LocalDate data) {
        LocalDate inicioSemana = data.minusDays(6); // semana de 7 dias
        BigDecimal horasNormais = BigDecimal.ZERO;
        BigDecimal horasExtras = BigDecimal.ZERO;

        for (CartaoPonto cartao : empregado.getCartoes()) {
            LocalDate dataCartao = LocalDate.parse(cartao.getData(), DateTimeFormatter.ofPattern("d/M/yyyy"));
            if (!dataCartao.isBefore(inicioSemana) && !dataCartao.isAfter(data)) {
                BigDecimal horas = BigDecimal.valueOf(cartao.getHoras());
                horasNormais = horasNormais.add(horas.min(BigDecimal.valueOf(8.0)));
                horasExtras = horasExtras.add(horas.subtract(BigDecimal.valueOf(8.0)).max(BigDecimal.ZERO));
            }
        }

        BigDecimal salarioPorHora = BigDecimal.valueOf(empregado.getSalarioPorHora());
        BigDecimal salarioBruto = horasNormais.multiply(salarioPorHora)
                .add(horasExtras.multiply(salarioPorHora).multiply(BigDecimal.valueOf(1.5)));

        return salarioBruto;
    }

    private BigDecimal calcularSalarioAssalariado(Assalariado empregado, LocalDate data) {
        return BigDecimal.valueOf(empregado.getSalarioMensal());
    }

    private BigDecimal calcularSalarioComissionado(Comissionado empregado, LocalDate data) {
        BigDecimal salarioMensal = BigDecimal.valueOf(empregado.getSalarioMensal());
        BigDecimal salarioBase = salarioMensal.multiply(BigDecimal.valueOf(12))
                .divide(BigDecimal.valueOf(26), 10, RoundingMode.DOWN);
        salarioBase = salarioBase.setScale(2, RoundingMode.DOWN);

        LocalDate inicioPeriodo = data.minusDays(14);
        BigDecimal totalVendas = BigDecimal.ZERO;

        for (ResultadoDeVenda venda : empregado.getResultadoDeVenda()) {
            LocalDate dataVenda = LocalDate.parse(venda.getData(), DateTimeFormatter.ofPattern("d/M/yyyy"));
            if (!dataVenda.isBefore(inicioPeriodo) && !dataVenda.isAfter(data)) {
                totalVendas = totalVendas.add(BigDecimal.valueOf(venda.getValor()));
            }
        }

        BigDecimal comissao = totalVendas.multiply(BigDecimal.valueOf(empregado.getTaxaDeComissao()));
        comissao = comissao.setScale(2, RoundingMode.DOWN);
        BigDecimal salarioBruto = salarioBase.add(comissao);

        return salarioBruto;
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

            for (Empregado empregado : horistas) {
                Horista horista = (Horista) empregado;

                if (horista.getSindicato() != null) {
                    MembroSindicato sindicato = horista.getSindicato();
                    BigDecimal taxaSemanal = new BigDecimal(String.valueOf(sindicato.getTaxaSindical()))
                            .multiply(BigDecimal.valueOf(7))
                            .setScale(2, RoundingMode.DOWN);
                    BigDecimal dividaAtual = new BigDecimal(String.valueOf(sindicato.getDividaSindical()));
                    BigDecimal novaDivida = dividaAtual.add(taxaSemanal);
                    sindicato.setDividaSindical(novaDivida.doubleValue());
                }

                BigDecimal salarioBruto = calcularSalarioHorista(horista, dataFolha);
                BigDecimal descontos = BigDecimal.ZERO;
                BigDecimal salarioLiquido = BigDecimal.ZERO;

                if (salarioBruto.compareTo(BigDecimal.ZERO) > 0) {
                    descontos = calcularDescontos(horista, dataFolha);
                    salarioLiquido = salarioBruto.subtract(descontos);

                    if (salarioLiquido.compareTo(BigDecimal.ZERO) < 0) {
                        if (horista.getSindicato() != null) {
                            MembroSindicato sindicato = horista.getSindicato();
                            BigDecimal dividaRestante = descontos.subtract(salarioBruto);
                            sindicato.setDividaSindical(dividaRestante.doubleValue());
                            descontos = salarioBruto;
                            salarioLiquido = BigDecimal.ZERO;
                        }
                    } else {
                        if (horista.getSindicato() != null) {
                            horista.getSindicato().setDividaSindical(0.0);
                        }
                    }
                }

                int[] horas = calcularHorasHorista(horista, dataFolha);
                totalHorasNormais += horas[0];
                totalHorasExtras += horas[1];
                String metodoPagamento = formatarMetodoPagamento(horista.getMetodoPagamento(), horista.getEndereco());

                writer.write(String.format("%-36s %5d %5d %13s %9s %15s %s\n",
                        horista.getNome(),
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

            for (Empregado empregado : assalariados) {
                Assalariado assalariado = (Assalariado) empregado;
                BigDecimal salarioBruto = calcularSalarioAssalariado(assalariado, dataFolha);
                BigDecimal descontos = calcularDescontos(assalariado, dataFolha);
                BigDecimal salarioLiquido = salarioBruto.subtract(descontos).max(BigDecimal.ZERO);
                String metodoPagamento = formatarMetodoPagamento(assalariado.getMetodoPagamento(),
                        assalariado.getEndereco());

                writer.write(String.format("%-48s %13s %9s %15s %s\n",
                        assalariado.getNome(),
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

            for (Empregado empregado : comissionados) {
                Comissionado comissionado = (Comissionado) empregado;
                BigDecimal[] valores = calcularValoresComissionado(comissionado, dataFolha);
                BigDecimal salarioBruto = valores[0];
                BigDecimal vendas = valores[1];
                BigDecimal comissao = valores[2];
                BigDecimal salarioBase = valores[3];
                BigDecimal descontos = calcularDescontos(comissionado, dataFolha);
                BigDecimal salarioLiquido = salarioBruto.subtract(descontos).max(BigDecimal.ZERO);
                String metodoPagamento = formatarMetodoPagamento(comissionado.getMetodoPagamento(),
                        comissionado.getEndereco());

                writer.write(String.format("%-21s %8s %8s %8s %13s %9s %15s %s\n",
                        comissionado.getNome(),
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

    private BigDecimal calcularDescontos(Empregado empregado, LocalDate data) {
        if (empregado.getSindicato() == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.DOWN);
        }

        MembroSindicato sindicato = empregado.getSindicato();
        BigDecimal descontos = BigDecimal.ZERO;

        if (empregado.getTipo().equals("horista")) {
            return calcularDescontosHorista(empregado, sindicato, data);
        }

        int diasPeriodo = calcularDiasPeriodo(empregado, data);
        BigDecimal taxaDiaria = new BigDecimal(String.valueOf(sindicato.getTaxaSindical()));
        BigDecimal taxaSindicalTotal = taxaDiaria.multiply(BigDecimal.valueOf(diasPeriodo))
                .setScale(2, RoundingMode.DOWN);
        descontos = descontos.add(taxaSindicalTotal);

        LocalDate inicioPeriodo = data.minusDays(diasPeriodo);

        for (TaxaServico taxa : sindicato.getTaxasDeServicos()) {
            LocalDate dataTaxa = LocalDate.parse(taxa.getData(), DateTimeFormatter.ofPattern("d/M/yyyy"));
            if (!dataTaxa.isBefore(inicioPeriodo) && !dataTaxa.isAfter(data)) {
                BigDecimal valorTaxa = new BigDecimal(String.valueOf(taxa.getValor()))
                        .setScale(2, RoundingMode.DOWN);
                descontos = descontos.add(valorTaxa);
            }
        }

        return descontos.setScale(2, RoundingMode.DOWN);
    }

    private BigDecimal calcularDescontosHorista(Empregado empregado, MembroSindicato sindicato, LocalDate data) {
        LocalDate inicioSemana = data.minusDays(6);
        BigDecimal taxasServico = BigDecimal.ZERO;

        for (TaxaServico taxa : sindicato.getTaxasDeServicos()) {
            LocalDate dataTaxa = LocalDate.parse(taxa.getData(), DateTimeFormatter.ofPattern("d/M/yyyy"));
            if (!dataTaxa.isBefore(inicioSemana) && !dataTaxa.isAfter(data)) {
                BigDecimal valorTaxa = new BigDecimal(String.valueOf(taxa.getValor()))
                        .setScale(2, RoundingMode.DOWN);
                taxasServico = taxasServico.add(valorTaxa);
            }
        }

        BigDecimal dividaAtual = new BigDecimal(String.valueOf(sindicato.getDividaSindical()));
        BigDecimal totalDescontos = dividaAtual.add(taxasServico);

        return totalDescontos.setScale(2, RoundingMode.DOWN);
    }

    private int calcularDiasPeriodo(Empregado empregado, LocalDate data) {
        switch (empregado.getTipo()) {
            case "horista":
                return calcularDiasPeriodoHorista(empregado, data);
            case "assalariado":
                return data.lengthOfMonth();
            case "comissionado":
                return 14;
            default:
                return 1;
        }
    }

    private int calcularDiasPeriodoHorista(Empregado empregado, LocalDate data) {
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

    private int[] calcularHorasHorista(Horista empregado, LocalDate data) {
        LocalDate inicioSemana = data.minusDays(6);
        int horasNormais = 0;
        int horasExtras = 0;

        for (CartaoPonto cartao : empregado.getCartoes()) {
            LocalDate dataCartao = LocalDate.parse(cartao.getData(), DateTimeFormatter.ofPattern("d/M/yyyy"));
            if (!dataCartao.isBefore(inicioSemana) && !dataCartao.isAfter(data)) {
                int horas = cartao.getHoras().intValue();
                horasNormais += Math.min(horas, 8);
                horasExtras += Math.max(horas - 8, 0);
            }
        }

        return new int[] { horasNormais, horasExtras };
    }

    private BigDecimal[] calcularValoresComissionado(Comissionado empregado, LocalDate data) {
        BigDecimal salarioMensal = BigDecimal.valueOf(empregado.getSalarioMensal());
        BigDecimal salarioBase = salarioMensal.multiply(BigDecimal.valueOf(12))
                .divide(BigDecimal.valueOf(26), 10, RoundingMode.DOWN)
                .setScale(2, RoundingMode.DOWN);

        LocalDate inicioPeriodo = data.minusDays(14);
        BigDecimal totalVendas = BigDecimal.ZERO;

        for (ResultadoDeVenda venda : empregado.getResultadoDeVenda()) {
            LocalDate dataVenda = LocalDate.parse(venda.getData(), DateTimeFormatter.ofPattern("d/M/yyyy"));
            if (!dataVenda.isBefore(inicioPeriodo) && !dataVenda.isAfter(data)) {
                totalVendas = totalVendas.add(BigDecimal.valueOf(venda.getValor()));
            }
        }

        BigDecimal comissao = totalVendas.multiply(BigDecimal.valueOf(empregado.getTaxaDeComissao()))
                .setScale(2, RoundingMode.DOWN);
        BigDecimal salarioBruto = salarioBase.add(comissao);

        return new BigDecimal[] { salarioBruto, totalVendas, comissao, salarioBase };
    }

    private String formatarMetodoPagamento(MetodoPagamento metodo, String endereco) {
        if (metodo == null) {
            return "Em maos";
        }

        if (metodo instanceof EmMaos) {
            return "Em maos";
        } else if (metodo instanceof Banco) {
            Banco banco = (Banco) metodo;
            return String.format("Banco do Brasil, Ag. %s CC %s", banco.getAgencia(), banco.getContaCorrente());
        } else if (metodo instanceof Correios) {
            return String.format("Correios, %s", endereco);
        }

        return "Em maos";
    }
}