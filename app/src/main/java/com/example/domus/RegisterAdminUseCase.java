package com.example.domus.domain.usercase;

import com.example.domus.domain.repository.AdminRepository;
import com.example.domus.utils.SecurityUtils;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class RegisterAdminUseCase {
    private final AdminRepository adminRepository;

    public RegisterAdminUseCase(AdminRepository adminRepository) {
        this.adminRepository = adminRepository;
    }

    public Result execute(String usuario, String senha, String masterSenha) {
        // Validação da senha master
        if (!"master".equals(masterSenha)) {
            return new Result(false, "Senha master incorreta!");
        }

        // Validações de campo
        if (usuario == null || usuario.isEmpty()) {
            return new Result(false, "Preencha o usuário!");
        }

        if (senha == null || senha.isEmpty()) {
            return new Result(false, "Preencha a senha!");
        }

        if (senha.length() != 6 || !senha.matches("\\d{6}")) {
            return new Result(false, "A senha deve ter exatamente 6 dígitos numéricos!");
        }

        // Gera hash e data
        String senhaHash = SecurityUtils.gerarHash(senha);
        if (senhaHash == null) {
            return new Result(false, "Erro ao processar senha!");
        }

        String dataCadastro = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                .format(new Date());

        boolean success = adminRepository.registerAdmin(usuario, senhaHash, "admin", dataCadastro);

        if (success) {
            return new Result(true, "Admin cadastrado com sucesso!");
        } else {
            return new Result(false, "Erro ao cadastrar. Usuário já existe?");
        }
    }

    public static class Result {
        private final boolean success;
        private final String message;

        public Result(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}