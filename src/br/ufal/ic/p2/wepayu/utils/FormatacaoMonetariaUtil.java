package br.ufal.ic.p2.wepayu.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FormatacaoMonetariaUtil {

    // ==============================================================
    // ARREDONDAMENTO
    // ==============================================================

    public static double arredondarValor(double valor) {
        BigDecimal bd = BigDecimal.valueOf(valor);
        return bd.setScale(2, RoundingMode.DOWN).doubleValue();
    }

    public static double arredondarValor(String valorStr) {
        if (valorStr == null || valorStr.isBlank()) {
            return 0.0;
        }
        String valorNormalizado = valorStr.replace(",", ".");
        try {
            double valor = Double.parseDouble(valorNormalizado);
            return arredondarValor(valor);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public static BigDecimal arredondarValor(BigDecimal valor) {
        return (valor != null) ? valor.setScale(2, RoundingMode.DOWN) : BigDecimal.ZERO;
    }

    // ==============================================================
    // FORMATAÇÃO PARA STRING
    // ==============================================================

    public static String formatValor(double valor) {
        double arredondado = arredondarValor(valor);
        return String.format("%.2f", arredondado).replace(".", ",");
    }

    public static String formatValor(String valorStr) {
        double valor = arredondarValor(valorStr);
        return formatValor(valor);
    }

    public static String formatValor(BigDecimal valor) {
        BigDecimal v = arredondarValor(valor);
        return String.format("%.2f", v).replace(".", ",");
    }

    // ==============================================================
    // CONVERSÃO
    // ==============================================================

    public static double convertToDouble(BigDecimal valor) {
        if (valor == null)
            return 0.0;
        return arredondarValor(valor).doubleValue();
    }
}
