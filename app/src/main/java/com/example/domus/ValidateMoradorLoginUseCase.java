package com.example.domus.domain.usercase;

import com.example.domus.domain.repository.MoradorRepository;
import org.json.JSONObject;

public class ValidateMoradorLoginUseCase {
    private final MoradorRepository moradorRepository;

    public ValidateMoradorLoginUseCase(MoradorRepository moradorRepository) {
        this.moradorRepository = moradorRepository;
    }

    public Result execute(String usuario, String senha) {
        if (usuario == null || senha == null || usuario.isEmpty() || senha.isEmpty()) {
            return new Result(false, null, "Preencha os campos usuário e senha!");
        }

        try {
            JSONObject morador = moradorRepository.validateLogin(usuario, senha);

            if (morador != null) {
                return new Result(true, morador, "Login realizado com sucesso!");
            } else {
                return new Result(false, null, "Usuário ou senha incorretos!");
            }
        } catch (Exception e) {
            return new Result(false, null, "Erro ao fazer login. Tente novamente.");
        }
    }

    public static class Result {
        private final boolean success;
        private final JSONObject moradorData;
        private final String message;

        public Result(boolean success, JSONObject moradorData, String message) {
            this.success = success;
            this.moradorData = moradorData;
            this.message = message;
        }

        public boolean isSuccess() { return success; }
        public JSONObject getMoradorData() { return moradorData; }
        public String getMessage() { return message; }
    }
}