package br.ufal.ic.p2.wepayu.Exception;

public class IdentificacaoDoSindicatoNulaException extends Exception {
    public IdentificacaoDoSindicatoNulaException() {
        super("Identificacao do sindicato nao pode ser nula.");
    }
}
