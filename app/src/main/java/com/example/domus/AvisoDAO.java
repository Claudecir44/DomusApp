package com.example.domus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class AvisoDAO {

    private static final String NOME_BANCO = "bd";
    private static final int VERSAO_BANCO = 1;
    private static final String TABELA_AVISO = "avisos";

    private SQLiteDatabase db;
    private DBHelper dbHelper;

    public AvisoDAO(Context context) {
        dbHelper = new DBHelper(context);
        db = dbHelper.getWritableDatabase();
    }

    // Inserir aviso
    public long inserirAviso(JSONObject aviso) {
        try {
            ContentValues cv = new ContentValues();
            cv.put("datahora", aviso.optString("datahora", ""));
            cv.put("assunto", aviso.optString("assunto", ""));
            cv.put("descricao", aviso.optString("descricao", ""));
            cv.put("anexos", aviso.optString("anexos", "[]"));
            return db.insert(TABELA_AVISO, null, cv);
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    // Atualizar aviso por id
    public boolean atualizarAviso(long id, JSONObject aviso) {
        try {
            ContentValues cv = new ContentValues();
            cv.put("datahora", aviso.optString("datahora", ""));
            cv.put("assunto", aviso.optString("assunto", ""));
            cv.put("descricao", aviso.optString("descricao", ""));
            cv.put("anexos", aviso.optString("anexos", "[]"));
            int linhas = db.update(TABELA_AVISO, cv, "id = ?", new String[]{String.valueOf(id)});
            return linhas > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    // Listar todos os avisos
    public List<JSONObject> listarAvisos() {
        List<JSONObject> lista = new ArrayList<>();
        Cursor cursor = db.query(TABELA_AVISO, null, null, null, null, null, "datahora DESC");
        if (cursor != null) {
            while (cursor.moveToNext()) {
                try {
                    JSONObject aviso = new JSONObject();
                    aviso.put("id", cursor.getLong(cursor.getColumnIndexOrThrow("id")));
                    aviso.put("datahora", cursor.getString(cursor.getColumnIndexOrThrow("datahora")));
                    aviso.put("assunto", cursor.getString(cursor.getColumnIndexOrThrow("assunto")));
                    aviso.put("descricao", cursor.getString(cursor.getColumnIndexOrThrow("descricao")));
                    aviso.put("anexos", cursor.getString(cursor.getColumnIndexOrThrow("anexos")));
                    lista.add(aviso);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            cursor.close();
        }
        return lista;
    }

    // Excluir aviso por id
    public boolean excluirAviso(long id) {
        int linhas = db.delete(TABELA_AVISO, "id = ?", new String[]{String.valueOf(id)});
        return linhas > 0;
    }

    // Fechar banco
    public void fechar() {
        if (db != null && db.isOpen()) db.close();
    }

    // Helper para criação do banco e tabela
    private static class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context) {
            super(context, NOME_BANCO, null, VERSAO_BANCO);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            String sql = "CREATE TABLE IF NOT EXISTS " + TABELA_AVISO + " (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                    "datahora TEXT NOT NULL," +
                    "assunto TEXT NOT NULL," +
                    "descricao TEXT NOT NULL," +
                    "anexos TEXT" +
                    ");";
            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + TABELA_AVISO);
            onCreate(db);
        }
    }
}
