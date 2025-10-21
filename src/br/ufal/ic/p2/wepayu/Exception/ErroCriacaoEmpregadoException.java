package br.ufal.ic.p2.wepayu.Exception;

public class ErroCriacaoEmpregadoException extends RuntimeException {
    public ErroCriacaoEmpregadoException(String msg) {
        super(msg);
    }

    public ErroCriacaoEmpregadoException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
