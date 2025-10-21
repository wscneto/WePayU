package br.ufal.ic.p2.wepayu.Exception;

public class ErroLancamentoTaxaServicoException extends RuntimeException {
    public ErroLancamentoTaxaServicoException(String msg) {
        super(msg);
    }

    public ErroLancamentoTaxaServicoException(String msg, Throwable cause) {
        super(msg, cause);
    }
}
