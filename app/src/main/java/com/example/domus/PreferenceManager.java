package com.example.domus.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    private static final String PREF_NAME = "domus_prefs";
    private static final String KEY_FIRST_RUN = "first_run";
    private static final String KEY_USER_TYPE = "user_type";
    private static final String KEY_MORADOR_NOME = "morador_nome";
    private static final String KEY_MORADOR_COD = "morador_cod";
    private static final String KEY_MORADOR_JSON = "morador_json";

    private final SharedPreferences prefs;
    private final SharedPreferences.Editor editor;

    public PreferenceManager(Context context) {
        prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        editor = prefs.edit();
    }

    // Verificar se é primeira execução
    public boolean isFirstRun() {
        return prefs.getBoolean(KEY_FIRST_RUN, true);
    }

    // Marcar que não é mais primeira execução
    public void setFirstRunComplete() {
        editor.putBoolean(KEY_FIRST_RUN, false);
        editor.apply();
    }

    // Salvar tipo de usuário
    public void setUserType(String tipoUsuario) {
        editor.putString(KEY_USER_TYPE, tipoUsuario);
        editor.apply();
    }

    // Obter tipo de usuário
    public String getUserType() {
        return prefs.getString(KEY_USER_TYPE, null);
    }

    // Verificar se é morador
    public boolean isMorador() {
        return "morador".equalsIgnoreCase(getUserType());
    }

    // Verificar se é administrador
    public boolean isAdmin() {
        return "admin".equalsIgnoreCase(getUserType());
    }

    // Salvar dados do morador
    public void setMoradorData(String nome, String cod, String json) {
        editor.putString(KEY_MORADOR_NOME, nome);
        editor.putString(KEY_MORADOR_COD, cod);
        editor.putString(KEY_MORADOR_JSON, json);
        editor.apply();
    }

    public String getMoradorNome() {
        return prefs.getString(KEY_MORADOR_NOME, null);
    }

    public String getMoradorCod() {
        return prefs.getString(KEY_MORADOR_COD, null);
    }

    public String getMoradorJson() {
        return prefs.getString(KEY_MORADOR_JSON, null);
    }

    // Limpar todos os dados (logout)
    public void clear() {
        editor.clear();
        editor.apply();
    }

    // Logout do morador
    public void logoutMorador() {
        editor.remove(KEY_MORADOR_NOME);
        editor.remove(KEY_MORADOR_COD);
        editor.remove(KEY_MORADOR_JSON);
        editor.remove(KEY_USER_TYPE);
        editor.apply();
    }
}