package br.ufal.ic.p2.wepayu.Exception;

public class IdentificacaoDoMembroNulaException extends Exception {
    public IdentificacaoDoMembroNulaException() {
        super("Identificacao do membro nao pode ser nula.");
    }
    
}
