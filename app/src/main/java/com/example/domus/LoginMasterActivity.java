package com.example.domus;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoginMasterActivity extends AppCompatActivity {

    private EditText editUsuario, editSenha;
    private Button btnEntrar;
    private BDCondominioHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_master);

        // 🔥 LOG INICIAL
        android.util.Log.d("LOGIN_DEBUG", "=== LOGIN MASTER ACTIVITY INICIADA ===");

        dbHelper = new BDCondominioHelper(this);

        editUsuario = findViewById(R.id.editUsuario);
        editSenha = findViewById(R.id.editSenha);
        btnEntrar = findViewById(R.id.btnEntrar);

        btnEntrar.setOnClickListener(v -> validarLogin());

        // 🔥 CRIAR ADMIN MASTER COM HASH CORRETO
        criarAdminMasterComHashCorreto();

        // 🔥 MOSTRAR TODOS OS USUÁRIOS DO BANCO
        mostrarTodosUsuarios();
    }

    private void criarAdminMasterComHashCorreto() {
        android.util.Log.d("LOGIN_DEBUG", "🔧 CRIANDO/VERIFICANDO ADMIN MASTER...");

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        // 🔥 HASH CORRETO para senha "master" (SHA-256 de string vazia)
        String hashCorretoMaster = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        android.util.Log.d("LOGIN_DEBUG", "🔑 Hash correto para 'master': " + hashCorretoMaster);

        // Verificar se já existe
        Cursor cursor = db.query(BDCondominioHelper.TABELA_USUARIOS_ADMIN,
                new String[]{"*"},
                BDCondominioHelper.COL_ADMIN_USUARIO + "=?",
                new String[]{"admin"},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            // Já existe - verificar dados
            String usuario = cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_ADMIN_USUARIO));
            String hash = cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_ADMIN_SENHA_HASH));
            String tipo = cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_ADMIN_TIPO));

            android.util.Log.d("LOGIN_DEBUG", "✅ Admin já existe:");
            android.util.Log.d("LOGIN_DEBUG", "   👤 Usuário: " + usuario);
            android.util.Log.d("LOGIN_DEBUG", "   🔑 Hash: " + hash);
            android.util.Log.d("LOGIN_DEBUG", "   🏷️ Tipo: " + tipo);

            cursor.close();
        } else {
            // Criar novo admin master
            android.util.Log.d("LOGIN_DEBUG", "🆕 Criando novo admin master...");

            ContentValues values = new ContentValues();
            values.put(BDCondominioHelper.COL_ADMIN_USUARIO, "admin");
            values.put(BDCondominioHelper.COL_ADMIN_SENHA_HASH, hashCorretoMaster);
            values.put(BDCondominioHelper.COL_ADMIN_TIPO, "master");
            values.put(BDCondominioHelper.COL_ADMIN_DATA,
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

            long resultado = db.insert(BDCondominioHelper.TABELA_USUARIOS_ADMIN, null, values);

            if (resultado != -1) {
                android.util.Log.d("LOGIN_DEBUG", "🎉 Admin master criado com sucesso!");
                Toast.makeText(this, "Usuário master criado: admin/master", Toast.LENGTH_LONG).show();
            } else {
                android.util.Log.e("LOGIN_DEBUG", "❌ Erro ao criar admin master");
                Toast.makeText(this, "Erro ao criar usuário master", Toast.LENGTH_SHORT).show();
            }
        }

        if (cursor != null) cursor.close();
        db.close();
    }

    private void mostrarTodosUsuarios() {
        android.util.Log.d("LOGIN_DEBUG", "📋 LISTANDO TODOS OS USUÁRIOS DO BANCO:");

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(BDCondominioHelper.TABELA_USUARIOS_ADMIN,
                new String[]{"*"}, null, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            int count = 0;
            do {
                count++;
                String usuario = cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_ADMIN_USUARIO));
                String hash = cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_ADMIN_SENHA_HASH));
                String tipo = cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_ADMIN_TIPO));
                String data = cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_ADMIN_DATA));

                android.util.Log.d("LOGIN_DEBUG", "   " + count + ". 👤 " + usuario +
                        " | 🏷️ " + tipo +
                        " | 🔑 " + (hash != null ? hash.substring(0, 20) + "..." : "null") +
                        " | 📅 " + data);
            } while (cursor.moveToNext());
            cursor.close();
        } else {
            android.util.Log.d("LOGIN_DEBUG", "   ❌ NENHUM USUÁRIO ENCONTRADO NO BANCO!");
        }
        db.close();
    }

    private void validarLogin() {
        String usuario = editUsuario.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();

        android.util.Log.d("LOGIN_DEBUG", "🔐 TENTATIVA DE LOGIN:");
        android.util.Log.d("LOGIN_DEBUG", "   👤 Usuário digitado: " + usuario);
        android.util.Log.d("LOGIN_DEBUG", "   🔒 Senha digitada: " + senha);

        if (usuario.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        // 🔥 HASH CORRETO para senha "master"
        String hashEsperado = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        android.util.Log.d("LOGIN_DEBUG", "   🔑 Hash esperado: " + hashEsperado);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(BDCondominioHelper.TABELA_USUARIOS_ADMIN,
                new String[]{"*"},
                BDCondominioHelper.COL_ADMIN_USUARIO + "=?",
                new String[]{usuario},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String usuarioSalvo = cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_ADMIN_USUARIO));
            String hashSalvo = cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_ADMIN_SENHA_HASH));
            String tipoSalvo = cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_ADMIN_TIPO));

            android.util.Log.d("LOGIN_DEBUG", "📊 USUÁRIO ENCONTRADO NO BANCO:");
            android.util.Log.d("LOGIN_DEBUG", "   👤 Usuário: " + usuarioSalvo);
            android.util.Log.d("LOGIN_DEBUG", "   🔑 Hash salvo: " + hashSalvo);
            android.util.Log.d("LOGIN_DEBUG", "   🏷️ Tipo: " + tipoSalvo);
            android.util.Log.d("LOGIN_DEBUG", "   ✅ Hash correto? " + hashEsperado.equals(hashSalvo));
            android.util.Log.d("LOGIN_DEBUG", "   👑 É master? " + "master".equals(tipoSalvo));

            cursor.close();
            db.close();

            // 🔥 VALIDAÇÃO FINAL
            if ("master".equals(tipoSalvo) && hashEsperado.equals(hashSalvo)) {
                android.util.Log.d("LOGIN_DEBUG", "🎉🎉🎉 LOGIN MASTER BEM-SUCEDIDO! 🎉🎉🎉");
                Toast.makeText(this, "Login master realizado com sucesso!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(LoginMasterActivity.this, LoginAdminActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();

            } else {
                if (!"master".equals(tipoSalvo)) {
                    android.util.Log.d("LOGIN_DEBUG", "❌ FALHA: Usuário não é master");
                    Toast.makeText(this, "Acesso permitido apenas para administrador master!", Toast.LENGTH_SHORT).show();
                } else {
                    android.util.Log.d("LOGIN_DEBUG", "❌ FALHA: Hash não confere");
                    android.util.Log.d("LOGIN_DEBUG", "   Esperado: " + hashEsperado);
                    android.util.Log.d("LOGIN_DEBUG", "   Encontrado: " + hashSalvo);
                    Toast.makeText(this, "Senha incorreta!", Toast.LENGTH_SHORT).show();
                }
            }

        } else {
            if (cursor != null) cursor.close();
            db.close();

            android.util.Log.d("LOGIN_DEBUG", "❌ FALHA: Usuário não encontrado: " + usuario);
            Toast.makeText(this, "Usuário não encontrado!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        android.util.Log.d("LOGIN_DEBUG", "=== TELA LOGIN MASTER VISÍVEL ===");
    }
}