package br.ufal.ic.p2.wepayu.Exception;

public class ErroAlteracaoEmpregadoException extends RuntimeException {
    public ErroAlteracaoEmpregadoException(String msg) {
        super(msg);
    }

    public ErroAlteracaoEmpregadoException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
