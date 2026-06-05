package com.example.domus.data.repository;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.domus.BDCondominioHelper;
import com.example.domus.domain.model.MasterAdmin;
import com.example.domus.domain.repository.MasterAdminRepository;
import com.example.domus.utils.DatabaseConstants;
import com.example.domus.utils.SecurityUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class MasterAdminRepositoryImpl implements MasterAdminRepository {

    private static final String TAG = "MasterAdminRepo";
    private final Context context;
    private final BDCondominioHelper dbHelper;

    public MasterAdminRepositoryImpl(Context context) {
        this.context = context;
        this.dbHelper = new BDCondominioHelper(context);
    }

    @Override
    public void setupMasterAdmin() {
        Log.d(TAG, "🔧 Configurando admin master exclusivo...");

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            // Remove todos os usuários existentes
            int deleted = db.delete(DatabaseConstants.TABLE_ADMIN, null, null);
            Log.d(TAG, "🗑️ " + deleted + " usuários anteriores removidos");

            // Cria hash para senha master
            String hashMaster = SecurityUtils.gerarHash(DatabaseConstants.MASTER_PASSWORD);
            if (hashMaster == null) {
                Log.e(TAG, "❌ ERRO: Hash não pôde ser calculado");
                return;
            }

            Log.d(TAG, "🔑 Hash para 'master': " + hashMaster);

            // Insere admin master
            ContentValues values = new ContentValues();
            values.put(DatabaseConstants.COL_ADMIN_USUARIO, DatabaseConstants.MASTER_USERNAME);
            values.put(DatabaseConstants.COL_ADMIN_SENHA_HASH, hashMaster);
            values.put(DatabaseConstants.COL_ADMIN_TIPO, DatabaseConstants.ADMIN_TYPE_MASTER);
            values.put(DatabaseConstants.COL_ADMIN_DATA,
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

            long result = db.insert(DatabaseConstants.TABLE_ADMIN, null, values);

            if (result != -1) {
                Log.d(TAG, "🎉 Admin master criado com sucesso! ID: " + result);
            } else {
                Log.e(TAG, "❌ Erro ao criar admin master");
            }

        } catch (Exception e) {
            Log.e(TAG, "💥 Erro crítico ao criar admin: " + e.getMessage());
        } finally {
            db.close();
        }
    }

    @Override
    public MasterAdmin validateLogin(String usuario, String senhaHash) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(DatabaseConstants.TABLE_ADMIN,
                    new String[]{DatabaseConstants.COL_ADMIN_USUARIO,
                            DatabaseConstants.COL_ADMIN_SENHA_HASH,
                            DatabaseConstants.COL_ADMIN_TIPO,
                            DatabaseConstants.COL_ADMIN_DATA},
                    DatabaseConstants.COL_ADMIN_USUARIO + " = ?",
                    new String[]{usuario},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                String usuarioSalvo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.COL_ADMIN_USUARIO));
                String hashSalvo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.COL_ADMIN_SENHA_HASH));
                String tipoSalvo = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.COL_ADMIN_TIPO));
                String dataStr = cursor.getString(cursor.getColumnIndexOrThrow(DatabaseConstants.COL_ADMIN_DATA));

                Date dataCadastro = null;
                try {
                    dataCadastro = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(dataStr);
                } catch (Exception e) {
                    dataCadastro = new Date();
                }

                return new MasterAdmin(usuarioSalvo, hashSalvo, tipoSalvo, dataCadastro);
            }

            return null;

        } catch (Exception e) {
            Log.e(TAG, "Erro ao validar login: " + e.getMessage());
            return null;
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    @Override
    public boolean existsMasterAdmin() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(DatabaseConstants.TABLE_ADMIN,
                    new String[]{DatabaseConstants.COL_ADMIN_USUARIO},
                    DatabaseConstants.COL_ADMIN_TIPO + " = ?",
                    new String[]{DatabaseConstants.ADMIN_TYPE_MASTER},
                    null, null, null);

            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e(TAG, "Erro ao verificar master admin: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    @Override
    public void clearAllAdmins() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(DatabaseConstants.TABLE_ADMIN, null, null);
        db.close();
        Log.d(TAG, "Todos os admins foram removidos");
    }

    @Override
    public int getAdminCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(DatabaseConstants.TABLE_ADMIN,
                    new String[]{DatabaseConstants.COL_ADMIN_USUARIO},
                    null, null, null, null, null);

            return cursor != null ? cursor.getCount() : 0;
        } catch (Exception e) {
            Log.e(TAG, "Erro ao contar admins: " + e.getMessage());
            return 0;
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }
}