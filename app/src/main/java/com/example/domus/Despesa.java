package com.example.domus;

import java.util.ArrayList;
import java.util.List;

public class Despesa {

    private int id;
    private String dataHora;
    private String nome;
    private String descricao;
    private double valor;
    private List<String> anexos;

    public Despesa(String dataHora, String nome, String descricao, double valor, List<String> anexos) {
        this.dataHora = dataHora;
        this.nome = nome;
        this.descricao = descricao;
        this.valor = valor;
        this.anexos = anexos != null ? anexos : new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getDataHora() { return dataHora; }
    public String getNome() { return nome; }
    public String getDescricao() { return descricao; }
    public double getValor() { return valor; }
    public List<String> getAnexos() { return anexos; }
}
