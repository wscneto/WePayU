package br.ufal.ic.p2.wepayu.Exception;

public class SalarioNegativoException extends RuntimeException {
    public SalarioNegativoException(String msg) {
        super(msg);
    }
}
