package br.ufal.ic.p2.wepayu.factories;

import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.models.MembroSindicato;
import br.ufal.ic.p2.wepayu.utils.FormatacaoMonetariaUtil;

public class MembroSindicatoFactory {
    public static MembroSindicato criarMembro(String idMembro, String taxaSindical)
            throws Exception, RuntimeException {

        validarIdentificacaoMembro(idMembro);
        double taxaValidada = processarTaxaSindicalString(taxaSindical);

        return construirMembroSindicato(idMembro, taxaValidada);
    }

    public static MembroSindicato criarMembro(String idMembro, double taxaSindical)
            throws Exception, RuntimeException {

        validarIdentificacaoMembro(idMembro);
        double taxaValidada = processarTaxaSindicalDouble(taxaSindical);

        return construirMembroSindicato(idMembro, taxaValidada);
    }

    private static void validarIdentificacaoMembro(String idMembro) throws Exception {
        if (idMembro == null || idMembro.trim().isEmpty()) {
            throw new IdentificacaoDoSindicatoNulaException("Identificacao do membro nao pode ser nula.");
        }
    }

    private static double processarTaxaSindicalString(String taxaSindical) throws Exception {
        if (taxaSindical == null || taxaSindical.trim().isEmpty()) {
            throw new TaxaSindicalNulaException("Taxa sindical nao pode ser nula.");
        }

        double valorTaxa;
        try {
            valorTaxa = Double.parseDouble(taxaSindical.replace(",", "."));
        } catch (NumberFormatException e) {
            throw new ValorNaoNumericoException("Taxa sindical deve ser numerica.");
        }

        return validarEFormatarTaxa(valorTaxa);
    }

    private static double processarTaxaSindicalDouble(double taxaSindical) throws Exception {
        return validarEFormatarTaxa(taxaSindical);
    }

    private static double validarEFormatarTaxa(double taxa) throws Exception {
        if (taxa < 0) {
            throw new ValorNegativoException("Taxa sindical deve ser nao-negativa.");
        }

        return FormatacaoMonetariaUtil.arredondarValor(taxa);
    }

    private static MembroSindicato construirMembroSindicato(String idMembro, double taxaFormatada) {
        return new MembroSindicato(idMembro, String.valueOf(taxaFormatada));
    }
}