package com.example.domus.domain.model;

import java.util.Date;

public class Admin {
    private String usuario;
    private String senhaHash;
    private String tipo;
    private Date dataCadastro;

    public Admin(String usuario, String senhaHash, String tipo, Date dataCadastro) {
        this.usuario = usuario;
        this.senhaHash = senhaHash;
        this.tipo = tipo;
        this.dataCadastro = dataCadastro;
    }

    // Getters e Setters
    public String getUsuario() { return usuario; }
    public void setUsuario(String usuario) { this.usuario = usuario; }

    public String getSenhaHash() { return senhaHash; }
    public void setSenhaHash(String senhaHash) { this.senhaHash = senhaHash; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public Date getDataCadastro() { return dataCadastro; }
    public void setDataCadastro(Date dataCadastro) { this.dataCadastro = dataCadastro; }

    public boolean isValid() {
        return usuario != null && !usuario.isEmpty() &&
                senhaHash != null && !senhaHash.isEmpty();
    }
}