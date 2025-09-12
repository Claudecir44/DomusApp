package com.example.domus;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LoginAdminActivity extends AppCompatActivity {

    private EditText editLoginAdmin, editLoginSenha, editCadastroAdmin, editCadastroSenha;
    private Button btnEntrar, btnCadastrar;
    private BDCondominioHelper dbHelper;

    private static final String PREFS_NAME = "admin_prefs";
    private static final String KEY_ADMIN_LOGADO = "admin_logado";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new BDCondominioHelper(this);

        // Verifica se existe algum administrador cadastrado
        if (!existeAdministrador()) {
            // Nenhum admin → abrir cadastro inicial
            setContentView(R.layout.activity_login_admin); // ou layout de cadastro inicial se tiver
            Toast.makeText(this, "Nenhum administrador cadastrado. Cadastre o primeiro.", Toast.LENGTH_LONG).show();
        } else {
            // Já existe admin → abrir login normal
            setContentView(R.layout.activity_login_admin);
        }

        // Inicializa campos e botões
        editLoginAdmin = findViewById(R.id.editLoginAdmin);
        editLoginSenha = findViewById(R.id.editLoginSenha);
        editCadastroAdmin = findViewById(R.id.editCadastroAdmin);
        editCadastroSenha = findViewById(R.id.editCadastroSenha);
        btnEntrar = findViewById(R.id.btnEntrar);
        btnCadastrar = findViewById(R.id.btnCadastrar);

        btnEntrar.setOnClickListener(v -> validarLogin());
        btnCadastrar.setOnClickListener(v -> cadastrarAdmin());
    }

    // Verifica se já existe algum admin cadastrado
    private boolean existeAdministrador() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(BDCondominioHelper.TABELA_USUARIOS_ADMIN,
                new String[]{BDCondominioHelper.COL_ADMIN_USUARIO},
                null, null, null, null, null);
        boolean existe = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        return existe;
    }

    // Gera hash SHA-256
    private String gerarHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void validarLogin() {
        String usuario = editLoginAdmin.getText().toString().trim();
        String senha = editLoginSenha.getText().toString().trim();

        if (usuario.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        String senhaHash = gerarHash(senha);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(BDCondominioHelper.TABELA_USUARIOS_ADMIN,
                new String[]{BDCondominioHelper.COL_ADMIN_USUARIO},
                BDCondominioHelper.COL_ADMIN_USUARIO + "=? AND " + BDCondominioHelper.COL_ADMIN_SENHA_HASH + "=?",
                new String[]{usuario, senhaHash},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            cursor.close();

            // Salva admin logado
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putString(KEY_ADMIN_LOGADO, usuario).apply();

            Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(LoginAdminActivity.this, DashBoardActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Usuário ou senha incorretos!", Toast.LENGTH_SHORT).show();
        }
    }

    private void cadastrarAdmin() {
        EditText inputMaster = new EditText(this);
        inputMaster.setHint("Digite a senha master");

        new AlertDialog.Builder(this)
                .setTitle("Autenticação Master")
                .setMessage("Digite a senha master para cadastrar um novo administrador:")
                .setView(inputMaster)
                .setPositiveButton("OK", (dialog, which) -> {
                    String masterSenha = inputMaster.getText().toString().trim();
                    if (!"master".equals(masterSenha)) {
                        Toast.makeText(this, "Senha master incorreta! Cadastro cancelado.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String usuario = editCadastroAdmin.getText().toString().trim();
                    String senha = editCadastroSenha.getText().toString().trim();

                    if (usuario.isEmpty() || senha.isEmpty()) {
                        Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (senha.length() != 6 || !senha.matches("\\d{6}")) {
                        Toast.makeText(this, "A senha deve ter exatamente 6 dígitos numéricos!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String senhaHash = gerarHash(senha);
                    String dataCadastro = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                    SQLiteDatabase db = dbHelper.getWritableDatabase();
                    ContentValues values = new ContentValues();
                    values.put(BDCondominioHelper.COL_ADMIN_USUARIO, usuario);
                    values.put(BDCondominioHelper.COL_ADMIN_SENHA_HASH, senhaHash);
                    values.put(BDCondominioHelper.COL_ADMIN_TIPO, "admin");
                    values.put(BDCondominioHelper.COL_ADMIN_DATA, dataCadastro);

                    long resultado = db.insert(BDCondominioHelper.TABELA_USUARIOS_ADMIN, null, values);
                    if (resultado != -1) {
                        Toast.makeText(this, "Admin cadastrado com sucesso! Faça seu login.", Toast.LENGTH_LONG).show();
                        editCadastroAdmin.setText("");
                        editCadastroSenha.setText("");
                    } else {
                        Toast.makeText(this, "Erro ao cadastrar. Usuário já existe?", Toast.LENGTH_SHORT).show();
                    }

                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    // Recupera o admin logado em qualquer Activity
    public static String getAdminLogado(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        return prefs.getString(KEY_ADMIN_LOGADO, null);
    }
}
