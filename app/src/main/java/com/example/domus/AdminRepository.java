package com.example.domus.domain.repository;

import com.example.domus.domain.model.Admin;

public interface AdminRepository {
    boolean existsAnyAdmin();
    boolean validateLogin(String usuario, String senhaHash);
    boolean registerAdmin(String usuario, String senhaHash, String tipo, String dataCadastro);
    String getLoggedAdmin();
    void saveLoggedAdmin(String usuario);
    void clearLoggedAdmin();
}