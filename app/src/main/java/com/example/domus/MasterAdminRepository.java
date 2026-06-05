package com.example.domus.domain.repository;

import com.example.domus.domain.model.MasterAdmin;

public interface MasterAdminRepository {
    void setupMasterAdmin();
    MasterAdmin validateLogin(String usuario, String senhaHash);
    boolean existsMasterAdmin();
    void clearAllAdmins();
    int getAdminCount();
}