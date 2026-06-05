package com.example.domus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class MoradorDAO {

    private static final String TAG = "MoradorDAO";
    private BDCondominioHelper dbHelper;

    public MoradorDAO(Context context) {
        dbHelper = new BDCondominioHelper(context);
    }

    /**
     * Valida login do morador usando primeiro nome (usuario) e CPF (senha)
     * @param primeiroNome Primeiro nome do morador
     * @param cpf CPF do morador (usado como senha)
     * @return JSONObject com os dados do morador ou null
     */
    public JSONObject validarLoginMorador(String primeiroNome, String cpf) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        JSONObject result = null;

        try {
            // Busca pelo primeiro nome (usuario) e CPF (senha)
            String query = "SELECT * FROM " + BDCondominioHelper.TABELA_MORADORES +
                    " WHERE " + BDCondominioHelper.COL_USUARIO + " = ? AND " +
                    BDCondominioHelper.COL_SENHA + " = ?";
            cursor = db.rawQuery(query, new String[]{primeiroNome, cpf});

            if (cursor != null && cursor.moveToFirst()) {
                result = new JSONObject();
                String[] columnNames = cursor.getColumnNames();
                for (int i = 0; i < columnNames.length; i++) {
                    String value = cursor.getString(i);
                    result.put(columnNames[i], value != null ? value : "");
                }
                Log.d(TAG, "✅ Login válido para: " + primeiroNome);
            } else {
                Log.d(TAG, "❌ Login inválido para: " + primeiroNome);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao validar login: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        return result;
    }

    /**
     * Retorna lista de moradores em JSONArray
     */
    public JSONArray getListaMoradores() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        JSONArray lista = new JSONArray();

        try {
            cursor = db.query(BDCondominioHelper.TABELA_MORADORES,
                    null, null, null, null, null,
                    BDCondominioHelper.COL_NOME + " ASC");

            if (cursor != null && cursor.moveToFirst()) {
                do {
                    JSONObject morador = new JSONObject();
                    String[] columnNames = cursor.getColumnNames();
                    for (int i = 0; i < columnNames.length; i++) {
                        String value = cursor.getString(i);
                        morador.put(columnNames[i], value != null ? value : "");
                    }
                    lista.put(morador);
                } while (cursor.moveToNext());
            }
            Log.d(TAG, "📋 Lista de moradores carregada: " + lista.length() + " registros");
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao listar moradores: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        return lista;
    }

    /**
     * Extrai o primeiro nome do nome completo
     */
    private String getPrimeiroNome(String nomeCompleto) {
        if (nomeCompleto == null || nomeCompleto.isEmpty()) return "";
        String[] partes = nomeCompleto.trim().split(" ");
        return partes[0];
    }

    /**
     * Insere um novo morador com todos os campos
     * Define automaticamente usuario = primeiro nome e senha = cpf
     */
    public boolean inserirMoradorCompleto(String cod, String nome, String cpf, String email,
                                          String rua, String numero, String telefone,
                                          String quadra, String lote, String imagePath) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BDCondominioHelper.COL_COD, cod);
        values.put(BDCondominioHelper.COL_NOME, nome);
        values.put(BDCondominioHelper.COL_CPF, cpf);
        values.put(BDCondominioHelper.COL_EMAIL, email);
        values.put(BDCondominioHelper.COL_RUA, rua);
        values.put(BDCondominioHelper.COL_NUMERO, numero);
        values.put(BDCondominioHelper.COL_TELEFONE, telefone);
        values.put(BDCondominioHelper.COL_QUADRA, quadra);
        values.put(BDCondominioHelper.COL_LOTE, lote);
        values.put(BDCondominioHelper.COL_IMAGEM_URI, imagePath);

        // Define usuario = primeiro nome e senha = cpf (sem formatação)
        String primeiroNome = getPrimeiroNome(nome);
        String cpfLimpo = cpf.replaceAll("[^0-9]", ""); // Remove pontos e traços do CPF

        values.put(BDCondominioHelper.COL_USUARIO, primeiroNome.toLowerCase());
        values.put(BDCondominioHelper.COL_SENHA, cpfLimpo);

        long resultado = db.insert(BDCondominioHelper.TABELA_MORADORES, null, values);
        db.close();

        Log.d(TAG, resultado != -1 ? "✅ Morador inserido: " + nome + " (login: " + primeiroNome + ", senha: " + cpfLimpo + ")" : "❌ Erro ao inserir morador");
        return resultado != -1;
    }

    /**
     * Atualiza um morador existente com todos os campos
     */
    public boolean atualizarMoradorCompleto(String cod, String nome, String cpf, String email,
                                            String rua, String numero, String telefone,
                                            String quadra, String lote, String imagePath) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BDCondominioHelper.COL_NOME, nome);
        values.put(BDCondominioHelper.COL_CPF, cpf);
        values.put(BDCondominioHelper.COL_EMAIL, email);
        values.put(BDCondominioHelper.COL_RUA, rua);
        values.put(BDCondominioHelper.COL_NUMERO, numero);
        values.put(BDCondominioHelper.COL_TELEFONE, telefone);
        values.put(BDCondominioHelper.COL_QUADRA, quadra);
        values.put(BDCondominioHelper.COL_LOTE, lote);
        values.put(BDCondominioHelper.COL_IMAGEM_URI, imagePath);

        // Atualiza também usuario = primeiro nome e senha = cpf
        String primeiroNome = getPrimeiroNome(nome);
        String cpfLimpo = cpf.replaceAll("[^0-9]", "");

        values.put(BDCondominioHelper.COL_USUARIO, primeiroNome.toLowerCase());
        values.put(BDCondominioHelper.COL_SENHA, cpfLimpo);

        int rows = db.update(BDCondominioHelper.TABELA_MORADORES, values,
                BDCondominioHelper.COL_COD + " = ?",
                new String[]{cod});
        db.close();

        Log.d(TAG, rows > 0 ? "✅ Morador atualizado: " + nome : "❌ Erro ao atualizar morador");
        return rows > 0;
    }

    /**
     * Insere morador a partir de JSON
     */
    public boolean inserirMorador(JSONObject morador) {
        String cod = morador.optString("cod");
        String nome = morador.optString("nome");
        String cpf = morador.optString("cpf");
        String email = morador.optString("email");
        String rua = morador.optString("rua");
        String numero = morador.optString("numero");
        String telefone = morador.optString("telefone");
        String quadra = morador.optString("quadra");
        String lote = morador.optString("lote");
        String imagePath = morador.optString("foto");

        return inserirMoradorCompleto(cod, nome, cpf, email, rua, numero, telefone, quadra, lote, imagePath);
    }

    /**
     * Atualiza morador a partir de JSON
     */
    public boolean atualizarMorador(String cod, JSONObject morador) {
        String nome = morador.optString("nome");
        String cpf = morador.optString("cpf");
        String email = morador.optString("email");
        String rua = morador.optString("rua");
        String numero = morador.optString("numero");
        String telefone = morador.optString("telefone");
        String quadra = morador.optString("quadra");
        String lote = morador.optString("lote");
        String imagePath = morador.optString("foto");

        return atualizarMoradorCompleto(cod, nome, cpf, email, rua, numero, telefone, quadra, lote, imagePath);
    }

    /**
     * Busca morador por código
     */
    public JSONObject getMoradorPorCod(String cod) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        JSONObject result = null;

        try {
            cursor = db.query(BDCondominioHelper.TABELA_MORADORES,
                    null,
                    BDCondominioHelper.COL_COD + " = ?",
                    new String[]{cod},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                result = new JSONObject();
                String[] columnNames = cursor.getColumnNames();
                for (int i = 0; i < columnNames.length; i++) {
                    String value = cursor.getString(i);
                    result.put(columnNames[i], value != null ? value : "");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao buscar morador: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        return result;
    }

    /**
     * Remove um morador
     */
    public boolean removerMorador(String cod) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int rows = db.delete(BDCondominioHelper.TABELA_MORADORES,
                BDCondominioHelper.COL_COD + " = ?",
                new String[]{cod});
        db.close();
        Log.d(TAG, rows > 0 ? "🗑️ Morador removido: " + cod : "❌ Erro ao remover morador");
        return rows > 0;
    }

    /**
     * Conta quantos moradores existem
     */
    public int contarMoradores() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        int count = 0;

        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + BDCondominioHelper.TABELA_MORADORES, null);
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao contar moradores: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        return count;
    }

    /**
     * Verifica se já existe morador com o mesmo código ou CPF
     */
    public boolean existeMorador(String cod, String cpf) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        boolean existe = false;

        try {
            cursor = db.query(BDCondominioHelper.TABELA_MORADORES,
                    new String[]{BDCondominioHelper.COL_ID},
                    BDCondominioHelper.COL_COD + " = ? OR " + BDCondominioHelper.COL_CPF + " = ?",
                    new String[]{cod, cpf},
                    null, null, null);

            existe = cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao verificar existência: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        return existe;
    }
}