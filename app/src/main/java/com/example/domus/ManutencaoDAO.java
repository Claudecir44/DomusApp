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

    public ManutencaoDAO(Context context){
        dbHelper = new BDCondominioHelper(context);
    }

    public long inserirManutencao(Manutencao manutencao){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BDCondominioHelper.COL_MANU_TIPO, manutencao.getTipo());
        values.put(BDCondominioHelper.COL_MANU_DATAHORA, manutencao.getDataHora());
        values.put(BDCondominioHelper.COL_MANU_LOCAL, manutencao.getLocal());
        values.put(BDCondominioHelper.COL_MANU_SERVICO, manutencao.getServico());
        values.put(BDCondominioHelper.COL_MANU_RESPONSAVEL, manutencao.getResponsavel());
        values.put(BDCondominioHelper.COL_MANU_VALOR, manutencao.getValor());
        values.put(BDCondominioHelper.COL_MANU_NOTAS, manutencao.getNotas());

        JSONArray jsonArray = new JSONArray(manutencao.getAnexos());
        values.put(BDCondominioHelper.COL_MANU_DOCUMENTO, jsonArray.toString());

        long id = db.insert(BDCondominioHelper.TABELA_MANUTENCOES, null, values);
        db.close();
        return id;
    }

    public int atualizarManutencao(Manutencao manutencao){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BDCondominioHelper.COL_MANU_TIPO, manutencao.getTipo());
        values.put(BDCondominioHelper.COL_MANU_DATAHORA, manutencao.getDataHora());
        values.put(BDCondominioHelper.COL_MANU_LOCAL, manutencao.getLocal());
        values.put(BDCondominioHelper.COL_MANU_SERVICO, manutencao.getServico());
        values.put(BDCondominioHelper.COL_MANU_RESPONSAVEL, manutencao.getResponsavel());
        values.put(BDCondominioHelper.COL_MANU_VALOR, manutencao.getValor());
        values.put(BDCondominioHelper.COL_MANU_NOTAS, manutencao.getNotas());

        JSONArray jsonArray = new JSONArray(manutencao.getAnexos());
        values.put(BDCondominioHelper.COL_MANU_DOCUMENTO, jsonArray.toString());

        int linhas = db.update(BDCondominioHelper.TABELA_MANUTENCOES,
                values,
                BDCondominioHelper.COL_MANU_ID + "=?",
                new String[]{String.valueOf(manutencao.getId())});
        db.close();
        return linhas;
    }

    public Manutencao getManutencaoById(long id){
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(BDCondominioHelper.TABELA_MANUTENCOES,
                null,
                BDCondominioHelper.COL_MANU_ID + "=?",
                new String[]{String.valueOf(id)},
                null, null, null);

        Manutencao manutencao = null;
        if(cursor != null && cursor.moveToFirst()){
            manutencao = new Manutencao();
            manutencao.setId(cursor.getLong(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_ID)));
            manutencao.setTipo(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_TIPO)));
            manutencao.setDataHora(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_DATAHORA)));
            manutencao.setLocal(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_LOCAL)));
            manutencao.setServico(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_SERVICO)));
            manutencao.setResponsavel(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_RESPONSAVEL)));
            manutencao.setValor(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_VALOR)));
            manutencao.setNotas(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_NOTAS)));

            String anexosJson = cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_DOCUMENTO));
            List<String> anexos = new ArrayList<>();
            if(anexosJson != null && !anexosJson.isEmpty()){
                try {
                    JSONArray array = new JSONArray(anexosJson);
                    for(int i=0; i<array.length(); i++){
                        anexos.add(array.getString(i));
                    }
                } catch (Exception e){
                    e.printStackTrace();
                }
            }
            manutencao.setAnexos(anexos);

            cursor.close();
        }
        db.close();
        return manutencao;
    }

    public List<Manutencao> getTodasManutencoes(){
        List<Manutencao> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(BDCondominioHelper.TABELA_MANUTENCOES,
                null, null, null, null, null,
                BDCondominioHelper.COL_MANU_DATAHORA + " DESC");

        if(cursor != null){
            while(cursor.moveToNext()){
                Manutencao m = new Manutencao();
                m.setId(cursor.getLong(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_ID)));
                m.setTipo(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_TIPO)));
                m.setDataHora(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_DATAHORA)));
                m.setLocal(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_LOCAL)));
                m.setServico(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_SERVICO)));
                m.setResponsavel(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_RESPONSAVEL)));
                m.setValor(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_VALOR)));
                m.setNotas(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_NOTAS)));

                String anexosJson = cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_DOCUMENTO));
                List<String> anexos = new ArrayList<>();
                if(anexosJson != null && !anexosJson.isEmpty()){
                    try {
                        JSONArray array = new JSONArray(anexosJson);
                        for(int i=0; i<array.length(); i++){
                            anexos.add(array.getString(i));
                        }
                    } catch (Exception e){
                        e.printStackTrace();
                    }
                }
                m.setAnexos(anexos);

                lista.add(m);
            }
            cursor.close();
        }
        db.close();
        return lista;
    }

    public int excluirManutencao(long id){
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int linhas = db.delete(BDCondominioHelper.TABELA_MANUTENCOES,
                BDCondominioHelper.COL_MANU_ID + "=?",
                new String[]{String.valueOf(id)});
        db.close();
        return linhas;
    }
}
