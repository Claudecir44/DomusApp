package com.example.domus;

public class Morador {
    private int id;
    private String nome;
    private String cpf;
    private String email;
    private String rua;
    private String numero;
    private String telefone;
    private String quadra;
    private String lote;
    private String imagemUri;

    public Morador(int id, String nome, String cpf, String email,
                   String rua, String numero, String telefone,
                   String quadra, String lote, String imagemUri) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.email = email;
        this.rua = rua;
        this.numero = numero;
        this.telefone = telefone;
        this.quadra = quadra;
        this.lote = lote;
        this.imagemUri = imagemUri;
    }

    // Getters e Setters
    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getCpf() { return cpf; }
    public String getEmail() { return email; }
    public String getRua() { return rua; }
    public String getNumero() { return numero; }
    public String getTelefone() { return telefone; }
    public String getQuadra() { return quadra; }
    public String getLote() { return lote; }
    public String getImagemUri() { return imagemUri; }

    public void setNome(String nome) { this.nome = nome; }
    public void setCpf(String cpf) { this.cpf = cpf; }
    public void setEmail(String email) { this.email = email; }
    public void setRua(String rua) { this.rua = rua; }
    public void setNumero(String numero) { this.numero = numero; }
    public void setTelefone(String telefone) { this.telefone = telefone; }
    public void setQuadra(String quadra) { this.quadra = quadra; }
    public void setLote(String lote) { this.lote = lote; }
    public void setImagemUri(String imagemUri) { this.imagemUri = imagemUri; }
}
