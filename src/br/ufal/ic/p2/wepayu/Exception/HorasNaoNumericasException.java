package br.ufal.ic.p2.wepayu.Exception;

public class HorasNaoNumericasException extends Exception {
    public HorasNaoNumericasException(){
        super("Horas devem ser numericas.");
    }
}
