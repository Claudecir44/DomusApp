package com.example.domus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class OcorrenciaDAO {

    private BDCondominioHelper dbHelper;

    public OcorrenciaDAO(Context context) {
        dbHelper = new BDCondominioHelper(context);
    }

    public long inserirOcorrencia(JSONObject ocorrencia) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(BDCondominioHelper.COL_OCOR_TIPO, ocorrencia.optString("tipo"));
        values.put(BDCondominioHelper.COL_OCOR_ENVOLVIDOS, ocorrencia.optString("envolvidos"));
        values.put(BDCondominioHelper.COL_OCOR_DESCRICAO, ocorrencia.optString("descricao"));
        values.put(BDCondominioHelper.COL_OCOR_DATAHORA, ocorrencia.optString("datahora"));
        values.put(BDCondominioHelper.COL_OCOR_ANEXOS, ocorrencia.optString("anexos"));

        long id = db.insert(BDCondominioHelper.TABELA_OCORRENCIAS, null, values);
        db.close();
        return id;
    }

    public boolean atualizarOcorrencia(long id, JSONObject ocorrencia) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(BDCondominioHelper.COL_OCOR_TIPO, ocorrencia.optString("tipo"));
        values.put(BDCondominioHelper.COL_OCOR_ENVOLVIDOS, ocorrencia.optString("envolvidos"));
        values.put(BDCondominioHelper.COL_OCOR_DESCRICAO, ocorrencia.optString("descricao"));
        values.put(BDCondominioHelper.COL_OCOR_DATAHORA, ocorrencia.optString("datahora"));
        values.put(BDCondominioHelper.COL_OCOR_ANEXOS, ocorrencia.optString("anexos"));

        int linhasAfetadas = db.update(BDCondominioHelper.TABELA_OCORRENCIAS, values,
                BDCondominioHelper.COL_OCOR_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return linhasAfetadas > 0;
    }

    public boolean excluirOcorrencia(long id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int linhasAfetadas = db.delete(BDCondominioHelper.TABELA_OCORRENCIAS,
                BDCondominioHelper.COL_OCOR_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return linhasAfetadas > 0;
    }

    public List<JSONObject> listarOcorrencias() {
        List<JSONObject> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] colunas = {
                BDCondominioHelper.COL_OCOR_ID,
                BDCondominioHelper.COL_OCOR_TIPO,
                BDCondominioHelper.COL_OCOR_ENVOLVIDOS,
                BDCondominioHelper.COL_OCOR_DESCRICAO,
                BDCondominioHelper.COL_OCOR_DATAHORA,
                BDCondominioHelper.COL_OCOR_ANEXOS
        };

        Cursor cursor = db.query(BDCondominioHelper.TABELA_OCORRENCIAS,
                colunas, null, null, null, null,
                BDCondominioHelper.COL_OCOR_DATAHORA + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                try {
                    JSONObject ocorrencia = new JSONObject();
                    ocorrencia.put("id", cursor.getInt(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_OCOR_ID)));
                    ocorrencia.put("tipo", cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_OCOR_TIPO)));
                    ocorrencia.put("envolvidos", cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_OCOR_ENVOLVIDOS)));
                    ocorrencia.put("descricao", cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_OCOR_DESCRICAO)));
                    ocorrencia.put("datahora", cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_OCOR_DATAHORA)));

                    String anexosStr = cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_OCOR_ANEXOS));
                    if (anexosStr != null && !anexosStr.isEmpty()) {
                        ocorrencia.put("anexos", new JSONArray(anexosStr));
                    } else {
                        ocorrencia.put("anexos", new JSONArray());
                    }

                    lista.add(ocorrencia);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } while (cursor.moveToNext());
            cursor.close();
        }

        db.close();
        return lista;
    }
}
