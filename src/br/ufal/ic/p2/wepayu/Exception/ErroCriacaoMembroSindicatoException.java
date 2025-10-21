package br.ufal.ic.p2.wepayu.Exception;

public class ErroCriacaoMembroSindicatoException extends RuntimeException {
    public ErroCriacaoMembroSindicatoException(String msg) {
        super(msg);
    }

    public ErroCriacaoMembroSindicatoException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
