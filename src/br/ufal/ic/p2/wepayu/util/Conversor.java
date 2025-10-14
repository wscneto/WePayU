package br.ufal.ic.p2.wepayu.util;

public class Conversor {
    public static double parseDouble(String valor) {
        valor = valor.replace(',', '.');
        return Double.parseDouble(valor);
    }
}
