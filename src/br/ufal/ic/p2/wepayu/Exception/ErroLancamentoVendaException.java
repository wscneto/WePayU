package br.ufal.ic.p2.wepayu.Exception;

public class ErroLancamentoVendaException extends RuntimeException {
    public ErroLancamentoVendaException(String msg) {
        super(msg);
    }

    public ErroLancamentoVendaException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
