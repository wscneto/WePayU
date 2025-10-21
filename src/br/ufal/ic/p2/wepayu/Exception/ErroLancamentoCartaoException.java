package br.ufal.ic.p2.wepayu.Exception;

public class ErroLancamentoCartaoException extends RuntimeException {
    public ErroLancamentoCartaoException(String msg) {
        super(msg);
    }

    public ErroLancamentoCartaoException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
