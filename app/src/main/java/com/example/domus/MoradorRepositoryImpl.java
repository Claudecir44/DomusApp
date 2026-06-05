package com.example.domus.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.example.domus.BDCondominioHelper;
import com.example.domus.domain.model.Morador;
import com.example.domus.domain.repository.MoradorRepository;
import org.json.JSONObject;

public class MoradorRepositoryImpl implements MoradorRepository {

    private final Context context;
    private final BDCondominioHelper dbHelper;

    public MoradorRepositoryImpl(Context context) {
        this.context = context;
        this.dbHelper = new BDCondominioHelper(context);
    }

    @Override
    public JSONObject validateLogin(String usuario, String senha) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        // Assumindo que a tabela de moradores tem colunas USUARIO e SENHA
        Cursor cursor = db.query(BDCondominioHelper.TABELA_MORADORES,
                null,
                "usuario = ? AND senha = ?",
                new String[]{usuario, senha},
                null, null, null);

        JSONObject result = null;
        if (cursor != null && cursor.moveToFirst()) {
            result = new JSONObject();
            try {
                // Mapeia todas as colunas
                String[] columnNames = cursor.getColumnNames();
                for (int i = 0; i < columnNames.length; i++) {
                    String value = cursor.getString(i);
                    result.put(columnNames[i], value != null ? value : "");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (cursor != null) cursor.close();
        db.close();
        return result;
    }

    @Override
    public Morador getMoradorByUsuario(String usuario) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(BDCondominioHelper.TABELA_MORADORES,
                null,
                "usuario = ?",
                new String[]{usuario},
                null, null, null);

        Morador morador = null;
        if (cursor != null && cursor.moveToFirst()) {
            morador = new Morador();
            morador.setNome(cursor.getString(cursor.getColumnIndexOrThrow("nome")));
            morador.setCod(cursor.getString(cursor.getColumnIndexOrThrow("cod")));
            morador.setApartamento(cursor.getString(cursor.getColumnIndexOrThrow("apartamento")));
            morador.setBloco(cursor.getString(cursor.getColumnIndexOrThrow("bloco")));
            morador.setEmail(cursor.getString(cursor.getColumnIndexOrThrow("email")));
            morador.setTelefone(cursor.getString(cursor.getColumnIndexOrThrow("telefone")));
            morador.setUsuario(cursor.getString(cursor.getColumnIndexOrThrow("usuario")));
        }

        if (cursor != null) cursor.close();
        db.close();
        return morador;
    }

    @Override
    public boolean saveMorador(Morador morador) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nome", morador.getNome());
        values.put("cod", morador.getCod());
        values.put("apartamento", morador.getApartamento());
        values.put("bloco", morador.getBloco());
        values.put("email", morador.getEmail());
        values.put("telefone", morador.getTelefone());
        values.put("usuario", morador.getUsuario());
        values.put("senha", morador.getSenha());

        long result = db.insert(BDCondominioHelper.TABELA_MORADORES, null, values);
        db.close();
        return result != -1;
    }
}