package com.example.domus.data.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import com.example.domus.BDCondominioHelper;
import com.example.domus.domain.repository.AdminRepository;

public class AdminRepositoryImpl implements AdminRepository {

    private static final String PREFS_NAME = "admin_prefs";
    private static final String KEY_ADMIN_LOGADO = "admin_logado";

    private final Context context;
    private final BDCondominioHelper dbHelper;

    public AdminRepositoryImpl(Context context) {
        this.context = context;
        this.dbHelper = new BDCondominioHelper(context);
    }

    @Override
    public boolean existsAnyAdmin() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(BDCondominioHelper.TABELA_USUARIOS_ADMIN,
                new String[]{BDCondominioHelper.COL_ADMIN_USUARIO},
                null, null, null, null, null);
        boolean exists = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        db.close();
        return exists;
    }

    @Override
    public boolean validateLogin(String usuario, String senhaHash) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(BDCondominioHelper.TABELA_USUARIOS_ADMIN,
                new String[]{BDCondominioHelper.COL_ADMIN_USUARIO},
                BDCondominioHelper.COL_ADMIN_USUARIO + "=? AND " +
                        BDCondominioHelper.COL_ADMIN_SENHA_HASH + "=?",
                new String[]{usuario, senhaHash},
                null, null, null);

        boolean isValid = cursor != null && cursor.moveToFirst();
        if (cursor != null) cursor.close();
        db.close();
        return isValid;
    }

    @Override
    public boolean registerAdmin(String usuario, String senhaHash, String tipo, String dataCadastro) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BDCondominioHelper.COL_ADMIN_USUARIO, usuario);
        values.put(BDCondominioHelper.COL_ADMIN_SENHA_HASH, senhaHash);
        values.put(BDCondominioHelper.COL_ADMIN_TIPO, tipo);
        values.put(BDCondominioHelper.COL_ADMIN_DATA, dataCadastro);

        long result = db.insert(BDCondominioHelper.TABELA_USUARIOS_ADMIN, null, values);
        db.close();
        return result != -1;
    }

    @Override
    public String getLoggedAdmin() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_ADMIN_LOGADO, null);
    }

    @Override
    public void saveLoggedAdmin(String usuario) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_ADMIN_LOGADO, usuario).apply();
    }

    @Override
    public void clearLoggedAdmin() {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_ADMIN_LOGADO).apply();
    }
}