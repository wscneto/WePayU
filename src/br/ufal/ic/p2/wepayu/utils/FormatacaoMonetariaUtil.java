package br.ufal.ic.p2.wepayu.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class FormatacaoMonetariaUtil {
    public static double arredondarValor(double valor) {
        BigDecimal bd = BigDecimal.valueOf(valor);
        bd = bd.setScale(2, RoundingMode.DOWN);
        return bd.doubleValue();
    }

    public static double arredondarValor(String valorStr) {
        if (valorStr == null || valorStr.isBlank())
            return 0.0;

        String valorCorrigido = valorStr.replace(",", ".");

        try {
            double valor = Double.parseDouble(valorCorrigido);
            return arredondarValor(valor);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    public static BigDecimal arredondarValor(BigDecimal valor) {
        if (valor == null)
            return BigDecimal.ZERO;
        return valor.setScale(2, RoundingMode.DOWN);
    }

    public static String formatValor(BigDecimal valor) {
        if (valor == null)
            return "0,00";

        BigDecimal formatado = arredondarValor(valor);

        return String.format("%.2f", formatado).replace(".", ",");
    }

    public static String formatValor(double valor) {
        double formatado = arredondarValor(valor);
        return String.format("%.2f", formatado).replace(".", ",");
    }

    public static String formatValor(String valorStr) {
        double valor = arredondarValor(valorStr);
        return formatValor(valor);
    }

    public static double convertToDouble(BigDecimal valor) {
        if (valor == null)
            return 0.0;
        return arredondarValor(valor).doubleValue();
    }
}
