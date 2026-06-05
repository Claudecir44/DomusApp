package com.example.domus.domain.model;

import java.util.Date;

public class MasterAdmin {
    private String usuario;
    private String senhaHash;
    private String tipo;
    private Date dataCadastro;

    public MasterAdmin(String usuario, String senhaHash, String tipo, Date dataCadastro) {
        this.usuario = usuario;
        this.senhaHash = senhaHash;
        this.tipo = tipo;
        this.dataCadastro = dataCadastro;
    }

    // Getters
    public String getUsuario() { return usuario; }
    public String getSenhaHash() { return senhaHash; }
    public String getTipo() { return tipo; }
    public Date getDataCadastro() { return dataCadastro; }

    public boolean isMaster() {
        return "master".equalsIgnoreCase(tipo);
    }

    public boolean validatePassword(String senha, String hashCalculado) {
        return senhaHash != null && senhaHash.equals(hashCalculado);
    }
}