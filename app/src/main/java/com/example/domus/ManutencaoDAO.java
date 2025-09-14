package com.example.domus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

public class ManutencaoDAO {

    private BDCondominioHelper dbHelper;

    public ManutencaoDAO(Context context) {
        dbHelper = new BDCondominioHelper(context);
    }

    // Inserir ou atualizar
    public long salvarManutencao(Manutencao m) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(BDCondominioHelper.COL_MANU_TIPO, m.getTipo());
        cv.put(BDCondominioHelper.COL_MANU_DATAHORA, m.getDataHora());
        cv.put(BDCondominioHelper.COL_MANU_LOCAL, m.getLocal());
        cv.put(BDCondominioHelper.COL_MANU_SERVICO, m.getServico());
        cv.put(BDCondominioHelper.COL_MANU_RESPONSAVEL, m.getResponsavel());
        cv.put(BDCondominioHelper.COL_MANU_VALOR, m.getValor());
        cv.put(BDCondominioHelper.COL_MANU_NOTAS, m.getNotas());

        // Salvar anexos como JSON
        JSONArray jsonAnexos = new JSONArray();
        if (m.getAnexos() != null) {
            for (String uriStr : m.getAnexos()) {
                jsonAnexos.put(uriStr);
            }
        }
        cv.put(BDCondominioHelper.COL_MANU_ANEXOS, jsonAnexos.toString());

        if (m.getId() > 0) {
            return db.update(BDCondominioHelper.TABELA_MANUTENCOES, cv, "id=?", new String[]{String.valueOf(m.getId())});
        } else {
            return db.insert(BDCondominioHelper.TABELA_MANUTENCOES, null, cv);
        }
    }

    // Excluir
    public int excluirManutencao(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.delete(BDCondominioHelper.TABELA_MANUTENCOES, "id=?", new String[]{String.valueOf(id)});
    }

    // Buscar todas
    public List<Manutencao> getTodasManutencoes() {
        List<Manutencao> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(BDCondominioHelper.TABELA_MANUTENCOES,
                null, null, null, null, null, BDCondominioHelper.COL_MANU_DATAHORA + " DESC");

        if (c != null && c.moveToFirst()) {
            do {
                Manutencao m = cursorToManutencao(c);
                lista.add(m);
            } while (c.moveToNext());
            c.close();
        }
        return lista;
    }

    // Buscar por ID
    public Manutencao getManutencaoById(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor c = db.query(BDCondominioHelper.TABELA_MANUTENCOES,
                null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
        Manutencao m = null;
        if (c != null && c.moveToFirst()) {
            m = cursorToManutencao(c);
            c.close();
        }
        return m;
    }

    // Converte cursor para objeto Manutencao
    private Manutencao cursorToManutencao(Cursor c) {
        Manutencao m = new Manutencao();
        m.setId(c.getInt(c.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_ID)));
        m.setTipo(c.getString(c.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_TIPO)));
        m.setDataHora(c.getString(c.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_DATAHORA)));
        m.setLocal(c.getString(c.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_LOCAL)));
        m.setServico(c.getString(c.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_SERVICO)));
        m.setResponsavel(c.getString(c.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_RESPONSAVEL)));
        m.setValor(c.getString(c.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_VALOR)));
        m.setNotas(c.getString(c.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_NOTAS)));

        // Recuperar anexos do JSON
        String anexosJson = c.getString(c.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_ANEXOS));
        List<String> anexos = new ArrayList<>();
        try {
            if (anexosJson != null && !anexosJson.isEmpty()) {
                JSONArray arr = new JSONArray(anexosJson);
                for (int i = 0; i < arr.length(); i++) {
                    anexos.add(arr.getString(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        m.setAnexos(anexos);

        return m;
    }
}
