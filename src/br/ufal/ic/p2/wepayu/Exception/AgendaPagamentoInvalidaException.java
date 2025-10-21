package br.ufal.ic.p2.wepayu.Exception;

public class AgendaPagamentoInvalidaException extends Exception {
    public AgendaPagamentoInvalidaException() {
        super();
    }

    public AgendaPagamentoInvalidaException(String message) {
        super(message);
    }

    public AgendaPagamentoInvalidaException(String message, Throwable cause) {
        super(message, cause);
    }
}
