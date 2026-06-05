package com.example.domus.domain.model;

import org.json.JSONObject;

public class Morador {
    private String nome;
    private String cod;
    private String apartamento;
    private String bloco;
    private String email;
    private String telefone;
    private String usuario;
    private String senha;
    private UserType userType;  // Adicionado

    public Morador() {
        this.userType = UserType.MORADOR;  // Padrão é MORADOR
    }

    public Morador(String nome, String cod, String usuario, String senha) {
        this.nome = nome;
        this.cod = cod;
        this.usuario = usuario;
        this.senha = senha;
        this.userType = UserType.MORADOR;
    }

    public Morador(String nome, String cod, String usuario, String senha, UserType userType) {
        this.nome = nome;
        this.cod = cod;
        this.usuario = usuario;
        this.senha = senha;
        this.userType = userType;
    }

    // Getters e Setters
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getCod() { return cod; }
    public void setCod(String cod) { this.cod = cod; }

    public String getApartamento() { return apartamento; }
    public void setApartamento(String apartamento) { this.apartamento = apartamento; }

    public String getBloco() { return bloco; }
    public void setBloco(String bloco) { this.bloco = bloco; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getSenha() { return senha; }
    public void setSenha(String senha) { this.senha = senha; }

    public UserType getUserType() { return userType; }
    public void setUserType(UserType userType) { this.userType = userType; }

    // Métodos auxiliares
    public boolean isAdmin() {
        return userType != null && userType.isAdmin();
    }

    public boolean isMorador() {
        return userType != null && userType.isMorador();
    }

    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        try {
            json.put("nome", nome);
            json.put("cod", cod);
            json.put("apartamento", apartamento);
            json.put("bloco", bloco);
            json.put("email", email);
            json.put("telefone", telefone);
            json.put("usuario", usuario);
            json.put("userType", userType != null ? userType.name() : null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return json;
    }
}