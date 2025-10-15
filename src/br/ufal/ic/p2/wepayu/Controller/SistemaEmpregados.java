package br.ufal.ic.p2.wepayu.Controller;

import br.ufal.ic.p2.wepayu.RepositorioEmpregados;
import br.ufal.ic.p2.wepayu.Exception.*;
import br.ufal.ic.p2.wepayu.models.CartaoPonto;
import br.ufal.ic.p2.wepayu.models.Empregado;
import br.ufal.ic.p2.wepayu.models.Venda;
import br.ufal.ic.p2.wepayu.util.Conversor;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.sql.Date;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class SistemaEmpregados {
    private RepositorioEmpregados repo = new RepositorioEmpregados();
    private int contadorId = 1;

    public SistemaEmpregados() {
        repo.carregar();
        int maxId = repo.getEmpregados().stream()
                        .mapToInt(e -> Integer.parseInt(e.getId()))
                        .max().orElse(0);
        contadorId = maxId + 1;
    }

    public String getAtributoEmpregado(String id, String atributo) throws Exception {
        if (id == null || id.isEmpty()) throw new IdentificacaoNulaException();

        Empregado e = getEmpregadoPorId(id);

        if (atributo.equals("nome")) return e.getNome();
        else if (atributo.equals("endereco")) return e.getEndereco();
        else if (atributo.equals("tipo")) return e.getTipo();
        else if (atributo.equals("salario"))
            return String.format("%.2f", e.getSalario()).replace('.', ',');
        else if (atributo.equals("comissao"))
            return String.format("%.2f", e.getComissao()).replace('.', ',');
        else if (atributo.equals("sindicalizado")) return String.valueOf(e.getSindicalizado());
        else throw new AtributoNaoExisteException();
    }


    public String criarEmpregado(String nome, String endereco, String tipo, String salarioStr) throws Exception {
        if (tipo.equals("comissionado")) throw new TipoNaoAplicavelException();
        return criarEmpregado(nome, endereco, tipo, salarioStr, null);
    }

    public void removerEmpregado(String id) throws Exception {
        Empregado e = getEmpregadoPorId(id);
        repo.remover(e);
    }

    public String criarEmpregado(String nome, String endereco, String tipo, String salarioStr, String comissaoStr) throws Exception {
        // Validações básicas
        if (nome == null || nome.trim().isEmpty()) throw new NomeNuloException();
        if (endereco == null || endereco.trim().isEmpty()) throw new EnderecoNuloException();
        if (!tipo.equals("horista") && !tipo.equals("assalariado") && !tipo.equals("comissionado")) throw new TipoInvalidoException();

        double salario = 0;
        if (salarioStr == null || salarioStr.trim().isEmpty()) throw new SalarioNuloException();
        try {
            salario = Conversor.parseDouble(salarioStr);
            if (salario < 0) throw new SalarioNegativoException();
        } catch (NumberFormatException e) {
            throw new SalarioNaoNumericoException();
        }

        double comissao = 0;
        if (tipo.equals("comissionado")) {
            if (comissaoStr == null || comissaoStr.trim().isEmpty()) throw new ComissaoNulaException();
            try {
                comissao = Conversor.parseDouble(comissaoStr);
                if (comissao < 0) throw new ComissaoNegativaException();
            } catch (NumberFormatException e) {
                throw new ComissaoNaoNumericaException();
            }
        } else if (comissaoStr != null)
            throw new TipoNaoAplicavelException();

        String id = String.valueOf(contadorId++);
        Empregado e = new Empregado(id, nome, endereco, tipo, salario, comissao);
        repo.adicionar(e);
        return id;
    }

    public Empregado getEmpregadoPorId(String id) throws Exception {
        if (id == null || id.isEmpty()) throw new IdentificacaoNulaException();
        Empregado e = repo.getEmpregadoPorId(id);
        if (e == null) throw new EmpregadoNaoExisteException();
        return e;
    }

    public Empregado getEmpregadoPorNome(String nome, int indice) throws Exception {
        List<Empregado> encontrados = repo.getEmpregadosPorNome(nome);
        if (encontrados.size() < indice) throw new NaoHaEmpregadoComNomeException();
        return encontrados.get(indice - 1);
    }

    public List<Empregado> getTodosEmpregados() {
        return repo.getEmpregados();
    }

    // *********************************************
    // LANCAR CARTAO
    // *********************************************

    public void lancaCartao(String id, String data, String horasStr) throws Exception {
        if (id == null || id.isEmpty()) throw new IdentificacaoNulaException();

        Empregado e = getEmpregadoPorId(id);
        if (!e.getTipo().equals("horista")) throw new EmpregadoNaoEhHoristaException();

        LocalDate dataLida;
        try {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("d/M/yyyy");
            try { 
                dataLida = LocalDate.parse(data, f);
            } catch (DateTimeParseException ex) {
                throw new DataInvalidaException();
            }
        } catch (NumberFormatException ex) {
            throw new DataInvalidaException();
        }

        double horas;
        try {
            horas = Conversor.parseDouble(horasStr);
        } catch (NumberFormatException ex) {
            throw new HorasNaoNumericasException();
        }

        if (horas <= 0) throw new HorasDevemSerPositivasException();

        e.adicionarCartao(new CartaoPonto(id, dataLida, horas));
        repo.salvar();
    }

    public String getHorasNormaisTrabalhadas(String emp, String dataInicial, String dataFinal) throws Exception {
        return calcularHoras(emp, dataInicial, dataFinal, false);
    }

    public String getHorasExtrasTrabalhadas(String emp, String dataInicial, String dataFinal) throws Exception {
        return calcularHoras(emp, dataInicial, dataFinal, true);
    }

    private String calcularHoras(String emp, String dataInicial, String dataFinal, boolean extras) throws Exception {
        if (emp == null || emp.trim().isEmpty()) throw new IdentificacaoNulaException();

        Empregado e = getEmpregadoPorId(emp);
        if (!e.getTipo().equals("horista")) throw new EmpregadoNaoEhHoristaException();
        if (e.getCartoes().isEmpty()) return "0";

        // Validação manual das datas
        LocalDate ini = parseData(dataInicial, true);
        LocalDate fim = parseData(dataFinal, false);

        if (ini.isAfter(fim)) throw new DataInicialPosteriorException();

        double soma = 0;
        for (CartaoPonto c : e.getCartoes()) {
            // Inclui dia inicial, exclui dia final
            if ((c.getData().isEqual(ini) || c.getData().isAfter(ini)) &&
                c.getData().isBefore(fim)) {
                double horas = c.getHoras();
                if (extras) {
                    soma += Math.max(0, horas - 8);
                } else {
                    soma += Math.min(8, horas);
                }
            }
        }

        return formatarNumero(soma);
    }

    private LocalDate parseData(String dataStr, boolean inicial) throws Exception {
        String[] partes = dataStr.split("/");
        if (partes.length != 3) throw inicial ? new DataInicialInvalidaException() : new DataFinalInvalidaException();

        int dia, mes, ano;
        try {
            dia = Integer.parseInt(partes[0]);
            mes = Integer.parseInt(partes[1]);
            ano = Integer.parseInt(partes[2]);
        } catch (NumberFormatException ex) {
            throw inicial ? new DataInicialInvalidaException() : new DataFinalInvalidaException();
        }

        // Valida mês
        if (mes < 1 || mes > 12) throw inicial ? new DataInicialInvalidaException() : new DataFinalInvalidaException();

        // Valida dia
        int maxDia = Month.of(mes).length(Year.isLeap(ano));
        if (dia < 1 || dia > maxDia) throw inicial ? new DataInicialInvalidaException() : new DataFinalInvalidaException();

        return LocalDate.of(ano, mes, dia);
    }

    private static String formatarNumero(double valor) {
        if (valor == (long) valor) {
            return String.format("%d", (long) valor);
        }
        String s = String.format("%.1f", valor).replace('.', ',');
        s = s.replaceAll(",00$", "");
        return s;
    }

    // *********************************************
    // LANCAR VENDAS
    // *********************************************

    public void lancaVenda(String id, String data, String valorStr) throws Exception {
        if (id == null || id.trim().isEmpty()) throw new IdentificacaoNulaException();

        Empregado e = getEmpregadoPorId(id);
        if (!e.getTipo().equals("comissionado")) throw new EmpregadoNaoEhComissionadoException();

        LocalDate dataLida;
        try {
            DateTimeFormatter f = DateTimeFormatter.ofPattern("d/M/yyyy");
            try { 
                dataLida = LocalDate.parse(data, f);
            } catch (DateTimeParseException ex) {
                throw new DataInvalidaException();
            }
        } catch (NumberFormatException ex) {
            throw new DataInvalidaException();
        }

        double valor;
        try {
            valor = Conversor.parseDouble(valorStr);
        } catch (NumberFormatException ex) {
            throw new ValorNaoNumericoException();
        }

        if (valor <= 0) throw new ValorDeveSerPositivoException();

        e.adicionarVenda(new Venda(id, dataLida, valor));
        repo.salvar();
    }

    public String getVendasRealizadas(String id, String dataInicial, String dataFinal) throws Exception {
        if (id == null || id.trim().isEmpty()) throw new IdentificacaoNulaException();

        Empregado e = getEmpregadoPorId(id);
        if (!e.getTipo().equals("comissionado")) throw new EmpregadoNaoEhComissionadoException();
        
        // Validação manual das datas
        LocalDate ini = parseData(dataInicial, true);
        LocalDate fim = parseData(dataFinal, false);

        if (ini.isAfter(fim)) throw new DataInicialPosteriorException();

        double total = 0.0;
        for (Venda v : e.getVendas()) {
            LocalDate dataVenda = LocalDate.of(v.getAno(), v.getMes(), v.getDia());
            if ((dataVenda.isAfter(ini) || dataVenda.isEqual(ini)) &&
                (dataVenda.isBefore(fim))) {
                total += v.getValor();
            }
        }

        return String.format(Locale.US, "%.2f", total).replace('.', ',');
    }
}