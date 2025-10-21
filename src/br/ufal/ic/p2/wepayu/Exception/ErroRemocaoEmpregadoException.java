package br.ufal.ic.p2.wepayu.Exception;

public class ErroRemocaoEmpregadoException extends RuntimeException {
    public ErroRemocaoEmpregadoException(String msg) {
        super(msg);
    }

    public ErroRemocaoEmpregadoException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
