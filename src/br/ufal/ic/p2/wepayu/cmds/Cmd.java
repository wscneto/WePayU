package br.ufal.ic.p2.wepayu.cmds;

public interface Cmd {
    void exec();

    void undo();
}
