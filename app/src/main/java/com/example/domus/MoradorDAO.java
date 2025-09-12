package com.example.domus;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MoradorDAO {

    private static final String PREFS_NAME = "moradores";
    private static final String KEY_LISTA = "lista";

    private Context context;

    public MoradorDAO(Context context) {
        this.context = context;
    }

    // ----------------------------
    // MÉTODO ESTÁTICO
    // ----------------------------
    public static JSONArray getListaMoradores(Context context) {
        try {
            SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
            String json = prefs.getString(KEY_LISTA, "[]");
            return new JSONArray(json);
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }

    // ----------------------------
    // MÉTODO DE INSTÂNCIA (para uso interno)
    // ----------------------------
    public JSONArray getListaMoradores() {
        return getListaMoradores(context);
    }

    // ----------------------------
    // MÉTODOS DE INSTÂNCIA (CRUD)
    // ----------------------------
    public List<JSONObject> listarMoradoresJSON() {
        List<JSONObject> lista = new ArrayList<>();
        try {
            JSONArray jsonArray = getListaMoradores();
            for (int i = 0; i < jsonArray.length(); i++) {
                lista.add(jsonArray.getJSONObject(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public boolean inserirMorador(JSONObject morador) {
        try {
            JSONArray moradores = getListaMoradores();
            moradores.put(morador);
            salvarLista(moradores);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean atualizarMorador(String codigo, JSONObject moradorAtualizado) {
        try {
            JSONArray moradores = getListaMoradores();
            for (int i = 0; i < moradores.length(); i++) {
                JSONObject m = moradores.getJSONObject(i);
                if (m.optString("cod").equals(codigo)) {
                    moradores.put(i, moradorAtualizado);
                    salvarLista(moradores);
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Método original mantido para compatibilidade
    public void atualizarMorador(int index, JSONObject moradorAtualizado) {
        try {
            JSONArray moradores = getListaMoradores();
            moradores.put(index, moradorAtualizado);
            salvarLista(moradores);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean excluirMorador(int index) {
        try {
            JSONArray moradores = getListaMoradores();
            JSONArray novaLista = new JSONArray();
            for (int i = 0; i < moradores.length(); i++) {
                if (i != index) {
                    novaLista.put(moradores.getJSONObject(i));
                }
            }
            salvarLista(novaLista);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ----------------------------
    // Excluir morador pelo código
    // ----------------------------
    public boolean excluirMoradorPorCodigo(String cod) {
        try {
            JSONArray moradores = getListaMoradores();
            JSONArray novaLista = new JSONArray();
            boolean encontrado = false;

            for (int i = 0; i < moradores.length(); i++) {
                JSONObject m = moradores.getJSONObject(i);
                if (!m.optString("cod").equals(cod)) {
                    novaLista.put(m);
                } else {
                    encontrado = true;
                    // Excluir a imagem associada se existir
                    String imagePath = m.optString("foto", "");
                    if (!imagePath.isEmpty()) {
                        BackupUtil.deleteImageFile(imagePath);
                    }
                }
            }

            if (encontrado) {
                salvarLista(novaLista);
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // ----------------------------
    // VALIDAR LOGIN DO MORADOR (MÉTODO CORRIGIDO)
    // ----------------------------
    public JSONObject validarLoginMorador(String usuario, String senha) {
        try {
            JSONArray moradores = getListaMoradores();
            for (int i = 0; i < moradores.length(); i++) {
                JSONObject m = moradores.getJSONObject(i);

                // Verifica se o primeiro nome corresponde (case insensitive)
                String nomeCompleto = m.optString("nome", "");
                String primeiroNome = extrairPrimeiroNome(nomeCompleto);

                // Verifica se o CPF corresponde à senha
                String cpf = m.optString("cpf", "");

                if (primeiroNome.equalsIgnoreCase(usuario) && cpf.equals(senha)) {
                    return m;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // Método auxiliar para extrair o primeiro nome
    private String extrairPrimeiroNome(String nomeCompleto) {
        if (nomeCompleto == null || nomeCompleto.isEmpty()) {
            return "";
        }
        String[] partes = nomeCompleto.split(" ");
        return partes[0].trim();
    }

    // ----------------------------
    // Buscar morador por código
    // ----------------------------
    public JSONObject buscarPorCodigo(String codigo) {
        try {
            JSONArray moradores = getListaMoradores();
            for (int i = 0; i < moradores.length(); i++) {
                JSONObject m = moradores.getJSONObject(i);
                if (m.optString("cod").equals(codigo)) {
                    return m;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ----------------------------
    // Buscar morador por CPF
    // ----------------------------
    public JSONObject buscarPorCPF(String cpf) {
        try {
            JSONArray moradores = getListaMoradores();
            for (int i = 0; i < moradores.length(); i++) {
                JSONObject m = moradores.getJSONObject(i);
                if (m.optString("cpf").equals(cpf)) {
                    return m;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // ----------------------------
    // Verificar se CPF já existe
    // ----------------------------
    public boolean cpfExiste(String cpf) {
        try {
            JSONArray moradores = getListaMoradores();
            for (int i = 0; i < moradores.length(); i++) {
                JSONObject m = moradores.getJSONObject(i);
                if (m.optString("cpf").equals(cpf)) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    // ----------------------------
    // SALVAR LISTA
    // ----------------------------
    private void salvarLista(JSONArray lista) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_LISTA, lista.toString());
        editor.apply();
    }
}