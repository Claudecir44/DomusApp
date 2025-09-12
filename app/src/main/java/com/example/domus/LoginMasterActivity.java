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

        dbHelper = new BDCondominioHelper(this);

        editUsuario = findViewById(R.id.editUsuario);
        editSenha = findViewById(R.id.editSenha);
        btnEntrar = findViewById(R.id.btnEntrar);

        btnEntrar.setOnClickListener(v -> validarLogin());

        // Inicializa o master no banco caso ainda não exista
        criarMasterNoBanco();
    }

    private void criarMasterNoBanco() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Cursor cursor = db.query(BDCondominioHelper.TABELA_USUARIOS_ADMIN,
                new String[]{BDCondominioHelper.COL_ADMIN_USUARIO},
                BDCondominioHelper.COL_ADMIN_USUARIO + "=?",
                new String[]{"admin"},
                null, null, null);

        if (cursor == null || !cursor.moveToFirst()) {
            ContentValues values = new ContentValues();
            values.put(BDCondominioHelper.COL_ADMIN_USUARIO, "admin");
            values.put(BDCondominioHelper.COL_ADMIN_SENHA_HASH, BDCondominioHelper.gerarHash("master"));
            values.put(BDCondominioHelper.COL_ADMIN_TIPO, "MASTER");
            values.put(BDCondominioHelper.COL_ADMIN_DATA,
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
            db.insert(BDCondominioHelper.TABELA_USUARIOS_ADMIN, null, values);
            Toast.makeText(this, "Usuário master criado: admin/master", Toast.LENGTH_LONG).show();
        }

        if (cursor != null) cursor.close();
    }

    private void validarLogin() {
        String usuario = editUsuario.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();

        if (usuario.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
            return;
        }

        String senhaHash = BDCondominioHelper.gerarHash(senha);

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(BDCondominioHelper.TABELA_USUARIOS_ADMIN,
                new String[]{BDCondominioHelper.COL_ADMIN_USUARIO},
                BDCondominioHelper.COL_ADMIN_USUARIO + "=? AND " +
                        BDCondominioHelper.COL_ADMIN_SENHA_HASH + "=? AND " +
                        BDCondominioHelper.COL_ADMIN_TIPO + "=?",
                new String[]{usuario, senhaHash, "MASTER"},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            cursor.close();
            Toast.makeText(this, "Login master realizado com sucesso!", Toast.LENGTH_SHORT).show();

            // Vai para LoginAdminActivity
            Intent intent = new Intent(LoginMasterActivity.this, LoginAdminActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            Toast.makeText(this, "Usuário ou senha incorretos!", Toast.LENGTH_SHORT).show();
        }
    }
}