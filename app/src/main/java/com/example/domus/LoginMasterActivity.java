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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
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

        android.util.Log.d("LOGIN_DEBUG", "=== LOGIN MASTER ACTIVITY INICIADA ===");

        dbHelper = new BDCondominioHelper(this);

        editUsuario = findViewById(R.id.editUsuario);
        editSenha = findViewById(R.id.editSenha);
        btnEntrar = findViewById(R.id.btnEntrar);

        btnEntrar.setOnClickListener(v -> validarLogin());

        // Criar apenas o admin master
        criarAdminMasterExclusivo();

        // Preencher automaticamente para teste
        editUsuario.setText("admin");
        editSenha.setText("master");
    }

    private String calcularSHA256(String input) {
        try {
            if (input == null) return null;

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            String resultado = hexString.toString();
            android.util.Log.d("LOGIN_DEBUG", "🔑 Hash calculado para '" + input + "': " + resultado);
            return resultado;

        } catch (NoSuchAlgorithmException e) {
            android.util.Log.e("LOGIN_DEBUG", "❌ Erro ao calcular SHA-256: " + e.getMessage());
            return null;
        }
    }

    private void criarAdminMasterExclusivo() {
        android.util.Log.d("LOGIN_DEBUG", "🔧 CONFIGURANDO ADMIN MASTER EXCLUSIVO...");

        SQLiteDatabase db = dbHelper.getWritableDatabase();

        try {
            // REMOVER TODOS OS USUÁRIOS EXISTENTES
            int deletados = db.delete(BDCondominioHelper.TABELA_USUARIOS_ADMIN, null, null);
            android.util.Log.d("LOGIN_DEBUG", "🗑️ " + deletados + " usuários anteriores removidos");

            // CRIAR APENAS O ADMIN MASTER
            String hashCorretoMaster = calcularSHA256("master");

            if (hashCorretoMaster == null) {
                android.util.Log.e("LOGIN_DEBUG", "❌ ERRO: Hash não pôde ser calculado");
                Toast.makeText(this, "Erro ao criar usuário master", Toast.LENGTH_SHORT).show();
                return;
            }

            android.util.Log.d("LOGIN_DEBUG", "🔑 Hash para 'master': " + hashCorretoMaster);

            ContentValues values = new ContentValues();
            values.put("usuario", "admin");
            values.put("senha_hash", hashCorretoMaster);
            values.put("tipo", "master");
            values.put("data_cadastro",
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

            long resultado = db.insert(BDCondominioHelper.TABELA_USUARIOS_ADMIN, null, values);

            if (resultado != -1) {
                android.util.Log.d("LOGIN_DEBUG", "🎉 Admin master criado com sucesso! ID: " + resultado);
                Toast.makeText(this, "Usuário master configurado: admin/master", Toast.LENGTH_LONG).show();
            } else {
                android.util.Log.e("LOGIN_DEBUG", "❌ Erro ao criar admin master");
                Toast.makeText(this, "Erro ao criar usuário master", Toast.LENGTH_SHORT).show();
            }

            // Mostrar usuários após criação
            mostrarUsuariosExistentes();

        } catch (Exception e) {
            android.util.Log.e("LOGIN_DEBUG", "💥 Erro crítico ao criar admin: " + e.getMessage());
            Toast.makeText(this, "Erro crítico: " + e.getMessage(), Toast.LENGTH_LONG).show();
        } finally {
            db.close();
        }
    }

    private void mostrarUsuariosExistentes() {
        android.util.Log.d("LOGIN_DEBUG", "📋 VERIFICANDO USUÁRIOS NO BANCO LOCAL:");

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            // Primeiro verificar se a tabela existe
            Cursor tableCursor = db.rawQuery(
                    "SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                    new String[]{BDCondominioHelper.TABELA_USUARIOS_ADMIN}
            );

            boolean tabelaExiste = tableCursor != null && tableCursor.moveToFirst();
            tableCursor.close();

            if (!tabelaExiste) {
                android.util.Log.e("LOGIN_DEBUG", "❌ TABELA NÃO EXISTE: " + BDCondominioHelper.TABELA_USUARIOS_ADMIN);
                return;
            }

            cursor = db.query(BDCondominioHelper.TABELA_USUARIOS_ADMIN, null, null, null, null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                android.util.Log.d("LOGIN_DEBUG", "✅ USUÁRIOS ENCONTRADOS:");
                int count = 0;
                do {
                    count++;
                    String usuario = cursor.getString(cursor.getColumnIndexOrThrow("usuario"));
                    String hash = cursor.getString(cursor.getColumnIndexOrThrow("senha_hash"));
                    String tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo"));
                    String data = "";

                    try {
                        data = cursor.getString(cursor.getColumnIndexOrThrow("data_cadastro"));
                    } catch (Exception e) {
                        // Coluna data_cadastro pode não existir
                    }

                    android.util.Log.d("LOGIN_DEBUG", "   " + count + ". 👤 " + usuario +
                            " | 🏷️ " + tipo +
                            " | 🔑 " + (hash != null ? hash.substring(0, 16) + "..." : "null") +
                            " | 📅 " + data);

                } while (cursor.moveToNext());

                android.util.Log.d("LOGIN_DEBUG", "📊 TOTAL: " + count + " usuário(s)");

            } else {
                android.util.Log.d("LOGIN_DEBUG", "   ❌ NENHUM USUÁRIO ENCONTRADO NA TABELA!");
            }

        } catch (Exception e) {
            android.util.Log.e("LOGIN_DEBUG", "💥 Erro ao verificar usuários: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
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

        String hashDigitado = calcularSHA256(senha);
        android.util.Log.d("LOGIN_DEBUG", "   🔑 Hash calculado da senha: " + hashDigitado);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(BDCondominioHelper.TABELA_USUARIOS_ADMIN,
                    new String[]{"usuario", "senha_hash", "tipo"},
                    "usuario = ?",
                    new String[]{usuario},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                String usuarioSalvo = cursor.getString(cursor.getColumnIndexOrThrow("usuario"));
                String hashSalvo = cursor.getString(cursor.getColumnIndexOrThrow("senha_hash"));
                String tipoSalvo = cursor.getString(cursor.getColumnIndexOrThrow("tipo"));

                android.util.Log.d("LOGIN_DEBUG", "📊 DADOS ENCONTRADOS NO BANCO:");
                android.util.Log.d("LOGIN_DEBUG", "   👤 Usuário salvo: " + usuarioSalvo);
                android.util.Log.d("LOGIN_DEBUG", "   🏷️ Tipo salvo: " + tipoSalvo);
                android.util.Log.d("LOGIN_DEBUG", "   🔑 Hash salvo: " + hashSalvo);
                android.util.Log.d("LOGIN_DEBUG", "   🔑 Hash digitado: " + hashDigitado);
                android.util.Log.d("LOGIN_DEBUG", "   ✅ Hash confere? " + (hashDigitado != null && hashDigitado.equals(hashSalvo)));
                android.util.Log.d("LOGIN_DEBUG", "   👑 É master? " + "master".equals(tipoSalvo));

                if ("master".equals(tipoSalvo) && hashDigitado != null && hashDigitado.equals(hashSalvo)) {
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
                    } else if (hashDigitado == null || !hashDigitado.equals(hashSalvo)) {
                        android.util.Log.d("LOGIN_DEBUG", "❌ FALHA: Senha incorreta");
                        Toast.makeText(this, "Senha incorreta!", Toast.LENGTH_SHORT).show();
                    } else {
                        android.util.Log.d("LOGIN_DEBUG", "❌ FALHA: Credenciais inválidas");
                        Toast.makeText(this, "Credenciais inválidas!", Toast.LENGTH_SHORT).show();
                    }
                }

            } else {
                android.util.Log.d("LOGIN_DEBUG", "❌ FALHA: Usuário não encontrado: " + usuario);
                Toast.makeText(this, "Usuário não encontrado!", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            android.util.Log.e("LOGIN_DEBUG", "💥 Erro durante validação: " + e.getMessage());
            Toast.makeText(this, "Erro durante login: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        android.util.Log.d("LOGIN_DEBUG", "=== TELA LOGIN MASTER VISÍVEL ===");

        // Verificar usuários sempre que a tela ficar visível
        new android.os.Handler().postDelayed(this::mostrarUsuariosExistentes, 500);
    }
}