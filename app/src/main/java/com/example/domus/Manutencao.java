package com.example.domus;

import java.util.List;

public class Manutencao {
    private int id;
    private String tipo;
    private String dataHora;
    private String local;
    private String servico;
    private String responsavel;
    private String valor;
    private String notas;
    private List<String> anexos;

    // Construtores
    public Manutencao() {}

    public Manutencao(String tipo, String dataHora, String local, String servico,
                      String responsavel, String valor, String notas, List<String> anexos) {
        this.tipo = tipo;
        this.dataHora = dataHora;
        this.local = local;
        this.servico = servico;
        this.responsavel = responsavel;
        this.valor = valor;
        this.notas = notas;
        this.anexos = anexos;
    }

    // Getters e Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getDataHora() { return dataHora; }
    public void setDataHora(String dataHora) { this.dataHora = dataHora; }

    public String getLocal() { return local; }
    public void setLocal(String local) { this.local = local; }

    public String getServico() { return servico; }
    public void setServico(String servico) { this.servico = servico; }

    public String getResponsavel() { return responsavel; }
    public void setResponsavel(String responsavel) { this.responsavel = responsavel; }

    public String getValor() { return valor; }
    public void setValor(String valor) { this.valor = valor; }

    public String getNotas() { return notas; }
    public void setNotas(String notas) { this.notas = notas; }

    public List<String> getAnexos() { return anexos; }
    public void setAnexos(List<String> anexos) { this.anexos = anexos; }
}