package br.ufal.ic.p2.wepayu.Exception;

public class NaoHaEmpregadoComNomeException extends Exception{
    public NaoHaEmpregadoComNomeException(){
        super("Nao ha empregado com esse nome.");
    }
}
