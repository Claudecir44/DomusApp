package com.example.domus.domain.model;

public enum UserType {
    ADMIN("administrador"),
    MORADOR("morador"),
    NONE("nenhum");

    private final String description;

    UserType(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public boolean isAdmin() {
        return this == ADMIN;
    }

    public boolean isMorador() {
        return this == MORADOR;
    }
}