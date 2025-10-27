package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.services.*;
import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.cmds.*;

import java.util.Map;
import java.util.HashMap;

public class Facade {

    private final Map<String, Empregado> emps;
    private final Map<String, MembroSindicato> membsSind;
    private int contadorId;
    private final CmdManager gerenciadorCmds;

    private final EmpregadoService empregadoSrv;
    private final SindicatoService sindicatoSrv;
    private final LancaCartaoService cartaoSrv;
    private final FolhaPagamentoService folhaSrv;
    private final ArmazenamentoService armazenamentoSrv;

    private boolean finalizar;

    public Facade() {
        this.emps = new HashMap<>();
        this.membsSind = new HashMap<>();
        this.contadorId = 0;
        this.gerenciadorCmds = new CmdManager();

        this.empregadoSrv = new EmpregadoService(emps, membsSind, contadorId, gerenciadorCmds);
        this.sindicatoSrv = new SindicatoService(membsSind, emps, gerenciadorCmds);
        this.cartaoSrv = new LancaCartaoService(emps, gerenciadorCmds);
        this.folhaSrv = new FolhaPagamentoService(emps, membsSind);
        this.armazenamentoSrv = new ArmazenamentoService(emps, membsSind, contadorId);

        this.armazenamentoSrv.carregarSistema();
        this.finalizar = false;
    }

    /*
     * =========================================================
     * ARMAZENAMENTO
     * =========================================================
     */

    public void salvarSistema() {
        armazenamentoSrv.salvarSistema();
    }

    public void carregarSistema() {
        armazenamentoSrv.carregarSistema();
    }

    public void zerarSistema() {
        ZerarSistemaCmd resetCmd = new ZerarSistemaCmd(emps, membsSind);
        gerenciadorCmds.exec(resetCmd);
    }

    public void encerrarSistema() {
        armazenamentoSrv.encerrarSistema();
        finalizar = true;
    }

    /*
     * =========================================================
     * CRUD DE EMPREGADOS
     * =========================================================
     */

    public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws Exception {
        String novoId = empregadoSrv.criarEmpregado(nome, endereco, tipo, salario);
        salvarSistema();
        return novoId;
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao)
            throws Exception {
        String novoId = empregadoSrv.criarEmpregado(nome, endereco, tipo, salario, comissao);
        salvarSistema();
        return novoId;
    }

    public void alteraEmpregado(String emp, String atributo, String valor) throws Exception {
        empregadoSrv.alteraEmpregado(emp, atributo, valor);
        salvarSistema();
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String comissao_salario) throws Exception {
        empregadoSrv.alteraEmpregado(emp, atributo, valor, comissao_salario);
        salvarSistema();
    }

    public void alteraEmpregado(String emp, String atributo, String valor1, String banco, String agencia,
            String contaCorrente) throws Exception {
        empregadoSrv.alteraEmpregado(emp, atributo, valor1, banco, agencia, contaCorrente);
        salvarSistema();
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String idSindicato, String taxaSindical)
            throws Exception {
        empregadoSrv.alteraEmpregado(emp, atributo, valor, idSindicato, taxaSindical);
        salvarSistema();
    }

    public void alteraEmpregado(String emp, String atributo, String valor1, String banco, String agencia,
            String contaCorrente, String comissao) throws Exception {
        empregadoSrv.alteraEmpregado(emp, atributo, valor1, banco, agencia, contaCorrente, comissao);
        salvarSistema();
    }

    public void removerEmpregado(String emp) throws Exception {
        empregadoSrv.removerEmpregado(emp);
        salvarSistema();
    }

    public String getEmpregadoPorNome(String emp, String indice) throws Exception {
        return empregadoSrv.getEmpregadoPorNome(emp, indice);
    }

    public String getAtributoEmpregado(String emp, String atributo) throws Exception {
        return empregadoSrv.getAtributoEmpregado(emp, atributo);
    }

    public int getNumeroDeEmpregados() {
        return emps.size();
    }

    /*
     * =========================================================
     * SINDICATO
     * =========================================================
     */

    public MembroSindicato criarMembro(String id, String taxa) throws Exception {
        MembroSindicato membro = sindicatoSrv.criarMembro(id, taxa);
        salvarSistema();
        return membro;
    }

    public void lancaTaxaServico(String membro, String data, String valor) throws Exception {
        sindicatoSrv.lancaTaxaServico(membro, data, valor);
        salvarSistema();
    }

    public String getTaxasServico(String empregado, String dataInicial, String dataFinal) throws Exception {
        return sindicatoSrv.getTaxasServico(empregado, dataInicial, dataFinal);
    }

    /*
     * =========================================================
     * LANÇAMENTO DE CARTÃO E VENDAS
     * =========================================================
     */

    public void lancaCartao(String emp, String data, String horas) throws Exception {
        cartaoSrv.lancaCartao(emp, data, horas);
        salvarSistema();
    }

    public void lancaVenda(String emp, String data, String valor) throws Exception {
        cartaoSrv.lancaVenda(emp, data, valor);
        salvarSistema();
    }

    public String getHorasNormaisTrabalhadas(String emp, String dataInicial, String dataFinal) throws Exception {
        return cartaoSrv.getHorasNormaisTrabalhadas(emp, dataInicial, dataFinal);
    }

    public String getHorasExtrasTrabalhadas(String emp, String dataInicial, String dataFinal) throws Exception {
        return cartaoSrv.getHorasExtrasTrabalhadas(emp, dataInicial, dataFinal);
    }

    public String getVendasRealizadas(String emp, String dataInicial, String dataFinal) throws Exception {
        return cartaoSrv.getVendasRealizadas(emp, dataInicial, dataFinal);
    }

    /*
     * =========================================================
     * FOLHA DE PAGAMENTO
     * =========================================================
     */

    public String totalFolha(String data) throws Exception {
        return folhaSrv.totalFolha(data);
    }

    public void rodaFolha(String data, String arquivo) throws Exception {
        RodaFolhaCmd cmd = new RodaFolhaCmd(data, arquivo, folhaSrv);
        gerenciadorCmds.exec(cmd);
    }

    /*
     * =========================================================
     * UNDO / REDO
     * =========================================================
     */

    public void undo() throws Exception {
        if (finalizar)
            throw new Exception("Nao pode dar comandos depois de encerrarSistema.");
        gerenciadorCmds.undo();
    }

    public void redo() throws Exception {
        gerenciadorCmds.redo();
    }

    /*
     * =========================================================
     * AGENDAS DE PAGAMENTO
     * =========================================================
     */

    public void criarAgendaDePagamentos(String descricao) throws Exception {
        AgendaDePags.criarAgenda(descricao);
        salvarSistema();
    }
}
