package br.ufal.ic.p2.wepayu;

import br.ufal.ic.p2.wepayu.services.*;
import br.ufal.ic.p2.wepayu.models.*;
import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.cmds.*;

import java.util.Map;
import java.util.HashMap;

public class Facade {

    private final EmpregadoService empService;
    private final SindicatoService sindService;
    private final LancaCartaoService lancService;
    private final FolhaPagamentoService folhaPagService;
    private final ArmazenamentoService persistenceService;
    private final Map<String, Empregado> emps;
    private final Map<String, MembroSindicato> membsSind;
    private int id;
    private final CmdManager cm;
    private boolean sistemaEncerrado = false;

    public Facade() {
        this.emps = new HashMap<>();
        this.membsSind = new HashMap<>();
        this.id = 0;
        this.cm = new CmdManager();
        this.empService = new EmpregadoService(emps, membsSind, id, cm);
        this.sindService = new SindicatoService(membsSind, emps, cm);
        this.lancService = new LancaCartaoService(emps, cm);
        this.folhaPagService = new FolhaPagamentoService(emps, membsSind);
        this.persistenceService = new ArmazenamentoService(emps, membsSind, id);
        this.persistenceService.carregarSistema();
    }

    /*
     * ARMAZENAMENTO
     */

    public void salvarSistema() {
        persistenceService.salvarSistema();
    }

    public void carregarSistema() {
        persistenceService.carregarSistema();
    }

    public void zerarSistema() {
        ZerarSistemaCmd command = new ZerarSistemaCmd(emps, membsSind);
        cm.exec(command);
    }

    public void encerrarSistema() {
        persistenceService.encerrarSistema();
        sistemaEncerrado = true;
    }

    /*
     * CRIAÇÃO / ALTERAÇÃO / REMOÇÃO DE EMPREGADOS
     */

    public String criarEmpregado(String nome, String endereco, String tipo, String salario) throws Exception {
        String id = empService.criarEmpregado(nome, endereco, tipo, salario);
        salvarSistema();
        return id;
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salario, String comissao)
            throws Exception {
        String id = empService.criarEmpregado(nome, endereco, tipo, salario, comissao);
        salvarSistema();
        return id;
    }

    public void alteraEmpregado(String emp, String atributo, String valor) throws Exception {
        empService.alteraEmpregado(emp, atributo, valor);
        salvarSistema();
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String comissao_salario) throws Exception {
        empService.alteraEmpregado(emp, atributo, valor, comissao_salario);
        salvarSistema();
    }

    public void alteraEmpregado(String emp, String atributo, String valor1, String banco, String agencia,
            String contaCorrente) throws Exception {
        empService.alteraEmpregado(emp, atributo, valor1, banco, agencia, contaCorrente);
        salvarSistema();
    }

    public void alteraEmpregado(String emp, String atributo, String valor, String idSindicato, String taxaSindical)
            throws Exception {
        empService.alteraEmpregado(emp, atributo, valor, idSindicato, taxaSindical);
        salvarSistema();
    }

    public void alteraEmpregado(String emp, String atributo, String valor1, String banco, String agencia,
            String contaCorrente, String comissao) throws Exception {
        empService.alteraEmpregado(emp, atributo, valor1, banco, agencia, contaCorrente, comissao);
        salvarSistema();
    }

    public void removerEmpregado(String emp) throws Exception {
        empService.removerEmpregado(emp);
        salvarSistema();
    }

    public String getEmpregadoPorNome(String emp, String indice) throws Exception {
        return empService.getEmpregadoPorNome(emp, indice);
    }

    public String getAtributoEmpregado(String emp, String atributo) throws Exception {
        return empService.getAtributoEmpregado(emp, atributo);
    }

    public int getNumeroDeEmpregados() {
        return emps.size();
    }

    /*
     * SINDICATO
     */

    public MembroSindicato criarMembro(String id, String taxa) throws Exception {
        MembroSindicato membro = sindService.criarMembro(id, taxa);
        salvarSistema();
        return membro;
    }

    public void lancaTaxaServico(String membro, String data, String valor) throws Exception {
        sindService.lancaTaxaServico(membro, data, valor);
        salvarSistema();
    }

    public String getTaxasServico(String empregado, String dataInicial, String dataFinal) throws Exception {
        return sindService.getTaxasServico(empregado, dataInicial, dataFinal);
    }

    /*
     * LANÇA CARTÃO
     */

    public void lancaCartao(String emp, String data, String horas) throws Exception {
        lancService.lancaCartao(emp, data, horas);
        salvarSistema();
    }

    public void lancaVenda(String emp, String data, String valor) throws Exception {
        lancService.lancaVenda(emp, data, valor);
        salvarSistema();
    }

    public String getHorasNormaisTrabalhadas(String emp, String dataInicial, String dataFinal) throws Exception {
        return lancService.getHorasNormaisTrabalhadas(emp, dataInicial, dataFinal);
    }

    public String getHorasExtrasTrabalhadas(String emp, String dataInicial, String dataFinal) throws Exception {
        return lancService.getHorasExtrasTrabalhadas(emp, dataInicial, dataFinal);
    }

    public String getVendasRealizadas(String emp, String dataInicial, String dataFinal) throws Exception {
        return lancService.getVendasRealizadas(emp, dataInicial, dataFinal);
    }

    /*
     * FOLHA DE PAGAMENTO
     */

    public String totalFolha(String data) throws Exception {
        return folhaPagService.totalFolha(data);
    }

    public void rodaFolha(String data, String arquivo) throws Exception {
        RodaFolhaCmd command = new RodaFolhaCmd(data, arquivo, folhaPagService);
        cm.exec(command);
    }

    /*
     * UNDO/REDO
     */

    public void undo() throws Exception {
        if (sistemaEncerrado)
            throw new Exception("Nao pode dar comandos depois de encerrarSistema.");

        cm.undo();
    }

    public void redo() throws Exception {
        cm.redo();
    }

    /*
     * AGENDAS DE PAGAMENTO
     */

    public void criarAgendaDePagamentos(String descricao) throws Exception {
        br.ufal.ic.p2.wepayu.models.AgendaDePags.criarAgenda(descricao);
        salvarSistema();
    }
}
