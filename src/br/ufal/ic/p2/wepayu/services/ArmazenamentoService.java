package br.ufal.ic.p2.wepayu.services;

import br.ufal.ic.p2.wepayu.models.*;
import java.io.*;
import java.beans.*;
import java.util.*;

/**
 * Serviço responsável por armazenar e restaurar o estado do sistema
 * (empregados, sindicato e agendas personalizadas) usando serialização XML.
 */
public class ArmazenamentoService {

    private Map<String, Empregado> empregados;
    private Map<String, MembroSindicato> membrosSindicato;
    private int idAtual;

    private static final String CAMINHO_EMPREGADOS = "data/empregados.xml";
    private static final String CAMINHO_SINDICATO = "data/sindicato.xml";
    private static final String CAMINHO_AGENDAS = "data/agendas.xml";

    public ArmazenamentoService(Map<String, Empregado> empregados,
            Map<String, MembroSindicato> membrosSindicato,
            int id) {
        this.empregados = empregados;
        this.membrosSindicato = membrosSindicato;
        this.idAtual = id;
    }

    // ===========================================================
    // MÉTODOS PÚBLICOS
    // ===========================================================

    public void salvarSistema() {
        persistirEmpregados();
        persistirSindicato();
        persistirAgendas();
    }

    public void carregarSistema() {
        restaurarAgendas();
        restaurarEmpregados();
        restaurarSindicato();
    }

    public void zerarSistema() {
        empregados.clear();
        membrosSindicato.clear();
        AgendaDePags.limparAgendasCustomizadas();
        idAtual = 0;
    }

    public void encerrarSistema() {
        salvarSistema();
    }

    // ===========================================================
    // MÉTODOS PRIVADOS DE SALVAMENTO
    // ===========================================================

    private void persistirEmpregados() {
        try (XMLEncoder encoder = new XMLEncoder(
                new BufferedOutputStream(new FileOutputStream(CAMINHO_EMPREGADOS)))) {
            encoder.writeObject(empregados);
            encoder.writeObject(idAtual);
        } catch (IOException e) {
            System.err.println("Falha ao salvar empregados: " + e.getMessage());
        } catch (Exception ex) {
            System.err.println("Erro inesperado ao salvar empregados: " + ex.getMessage());
        }
    }

    private void persistirSindicato() {
        try (XMLEncoder encoder = new XMLEncoder(
                new BufferedOutputStream(new FileOutputStream(CAMINHO_SINDICATO)))) {
            encoder.writeObject(membrosSindicato);
        } catch (IOException e) {
            System.err.println("Falha ao salvar dados do sindicato: " + e.getMessage());
        } catch (Exception ex) {
            System.err.println("Erro inesperado ao salvar sindicato: " + ex.getMessage());
        }
    }

    private void persistirAgendas() {
        try (XMLEncoder encoder = new XMLEncoder(
                new BufferedOutputStream(new FileOutputStream(CAMINHO_AGENDAS)))) {

            Set<String> descricoes = new HashSet<>();
            Map<String, AgendaDePags> agendasCustom = AgendaDePags.getAgendasCustomizadas();
            for (AgendaDePags agenda : agendasCustom.values()) {
                descricoes.add(agenda.getDescricao());
            }

            encoder.writeObject(descricoes);

        } catch (IOException e) {
            System.err.println("Falha ao salvar agendas: " + e.getMessage());
        } catch (Exception ex) {
            System.err.println("Erro inesperado ao salvar agendas: " + ex.getMessage());
        }
    }

    // ===========================================================
    // MÉTODOS PRIVADOS DE RESTAURAÇÃO
    // ===========================================================

    @SuppressWarnings("unchecked")
    private void restaurarEmpregados() {
        File arquivo = new File(CAMINHO_EMPREGADOS);
        if (!arquivo.exists())
            return;

        try (XMLDecoder decoder = new XMLDecoder(
                new BufferedInputStream(new FileInputStream(arquivo)))) {

            Map<String, Empregado> recuperados = (Map<String, Empregado>) decoder.readObject();
            Integer novoId = (Integer) decoder.readObject();
            empregados.putAll(recuperados);
            idAtual = novoId;

        } catch (IOException e) {
            System.err.println("Falha ao carregar empregados: " + e.getMessage());
        } catch (Exception ex) {
            System.err.println("Erro inesperado ao carregar empregados: " + ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void restaurarSindicato() {
        File arquivo = new File(CAMINHO_SINDICATO);
        if (!arquivo.exists())
            return;

        try (XMLDecoder decoder = new XMLDecoder(
                new BufferedInputStream(new FileInputStream(arquivo)))) {

            Map<String, MembroSindicato> recuperados = (Map<String, MembroSindicato>) decoder.readObject();
            membrosSindicato.putAll(recuperados);

        } catch (IOException e) {
            System.err.println("Falha ao carregar sindicato: " + e.getMessage());
        } catch (Exception ex) {
            System.err.println("Erro inesperado ao carregar sindicato: " + ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void restaurarAgendas() {
        File arquivo = new File(CAMINHO_AGENDAS);
        if (!arquivo.exists())
            return;

        try (XMLDecoder decoder = new XMLDecoder(new BufferedInputStream(new FileInputStream(arquivo)))) {
            Set<String> descricoes = (Set<String>) decoder.readObject();
            for (String desc : descricoes) {
                try {
                    AgendaDePags.criarAgenda(desc);
                } catch (Exception ignored) {
                    // ignora agendas inválidas
                }
            }

        } catch (Exception ex) {
            System.err.println("Erro inesperado ao carregar agendas: " + ex.getMessage());
        }
    }
}
