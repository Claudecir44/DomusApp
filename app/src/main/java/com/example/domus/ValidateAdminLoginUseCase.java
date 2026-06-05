package com.example.domus.domain.usercase;

import com.example.domus.domain.repository.AdminRepository;
import com.example.domus.utils.SecurityUtils;

public class ValidateAdminLoginUseCase {
    private final AdminRepository adminRepository;

    public ValidateAdminLoginUseCase(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public boolean execute(String usuario, String senha) {
        if (usuario == null || senha == null || usuario.isEmpty() || senha.isEmpty()) {
            return false;
        }

        String senhaHash = SecurityUtils.gerarHash(senha);
        if (senhaHash == null) {
            return false;
        }

        boolean isValid = adminRepository.validateLogin(usuario, senhaHash);

        if (isValid) {
            adminRepository.saveLoggedAdmin(usuario);
        }

        return isValid;
    }
}