package com.example.domus.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SecurityUtils {

    // Constantes para credenciais master
    private static final String MASTER_USERNAME = "admin";
    private static final String MASTER_PASSWORD = "master";

    /**
     * Gera hash SHA-256 de uma string
     * @param input String a ser hasheada
     * @return Hash em hexadecimal ou null se erro
     */
    public static String gerarHash(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Valida se a senha de admin tem formato correto (6 dígitos numéricos)
     * @param senha Senha a ser validada
     * @return true se válida, false caso contrário
     */
    public static boolean isValidSenhaAdmin(String senha) {
        return senha != null && senha.length() == 6 && senha.matches("\\d{6}");
    }

    /**
     * Verifica se a senha é a senha master
     * @param senha Senha a ser verificada
     * @return true se for a senha master
     */
    public static boolean isMasterPassword(String senha) {
        return MASTER_PASSWORD.equals(senha);
    }

    /**
     * Verifica se as credenciais são do usuário master
     * @param usuario Nome de usuário
     * @param senha Senha
     * @return true se for o master
     */
    public static boolean isMasterCredentials(String usuario, String senha) {
        return MASTER_USERNAME.equals(usuario) && MASTER_PASSWORD.equals(senha);
    }

    /**
     * Obtém o nome de usuário master
     * @return "admin"
     */
    public static String getMasterUsername() {
        return MASTER_USERNAME;
    }

    /**
     * Obtém a senha master (apenas para uso interno)
     * @return "master"
     */
    public static String getMasterPassword() {
        return MASTER_PASSWORD;
    }

    /**
     * Valida se uma string é um hash SHA-256 válido
     * @param hash Hash a ser validado
     * @return true se parece um hash SHA-256 (64 caracteres hexadecimais)
     */
    public static boolean isValidSha256Hash(String hash) {
        return hash != null && hash.matches("^[a-fA-F0-9]{64}$");
    }

    /**
     * Gera um salt aleatório para hash (opcional - para segurança extra)
     * @return String com salt aleatório
     */
    public static String gerarSalt() {
        StringBuilder sb = new StringBuilder();
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*()";
        for (int i = 0; i < 32; i++) {
            int index = (int) (Math.random() * chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    /**
     * Gera hash com salt (para segurança adicional)
     * @param input String original
     * @param salt Salt a ser adicionado
     * @return Hash combinado
     */
    public static String gerarHashComSalt(String input, String salt) {
        if (input == null || salt == null) return null;
        return gerarHash(input + salt);
    }
}