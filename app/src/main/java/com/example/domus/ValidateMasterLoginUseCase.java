package com.example.domus.domain.usercase;

import com.example.domus.domain.model.MasterAdmin;
import com.example.domus.domain.repository.MasterAdminRepository;
import com.example.domus.utils.SecurityUtils;

public class ValidateMasterLoginUseCase {
    private final MasterAdminRepository repository;

    public ValidateMasterLoginUseCase(MasterAdminRepository repository) {
        this.repository = repository;
    }

    public Result execute(String usuario, String senha) {
        // Validações básicas
        if (usuario == null || senha == null || usuario.isEmpty() || senha.isEmpty()) {
            return new Result(false, "Preencha todos os campos!", null);
        }

        // Calcula hash da senha
        String senhaHash = SecurityUtils.gerarHash(senha);
        if (senhaHash == null) {
            return new Result(false, "Erro ao processar senha!", null);
        }

        // Busca no repositório
        MasterAdmin admin = repository.validateLogin(usuario, senhaHash);

        if (admin == null) {
            return new Result(false, "Usuário não encontrado!", null);
        }

        if (!admin.isMaster()) {
            return new Result(false, "Acesso permitido apenas para administrador master!", null);
        }

        if (!admin.validatePassword(senha, senhaHash)) {
            return new Result(false, "Senha incorreta!", null);
        }

        return new Result(true, "Login master realizado com sucesso!", admin);
    }

    public static class Result {
        private final boolean success;
        private final String message;
        private final MasterAdmin admin;

        public Result(boolean success, String message, MasterAdmin admin) {
            this.success = success;
            this.message = message;
            this.admin = admin;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public MasterAdmin getAdmin() { return admin; }
    }
}