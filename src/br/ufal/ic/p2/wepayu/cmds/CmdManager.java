package br.ufal.ic.p2.wepayu.cmds;

import br.ufal.ic.p2.wepayu.Exception.*;
import java.util.Stack;

public class CmdManager {
    private Stack<Cmd> hist = new Stack<>();
    private Stack<Cmd> redoStack = new Stack<>();

    public void exec(Cmd command) {
        command.exec();
        hist.push(command);
        redoStack.clear();
    }

    public void undo() {
        if (!hist.isEmpty()) {
            Cmd command = hist.pop();
            command.undo();
            redoStack.push(command);
        } else
            throw new NaoHaComandoDesfazerException("Nao ha comando a desfazer.");
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            Cmd command = redoStack.pop();
            command.exec();
            hist.push(command);
        } else
            throw new NaoHaComandoDesfazerException("Nao ha comando a refazer.");
    }
}