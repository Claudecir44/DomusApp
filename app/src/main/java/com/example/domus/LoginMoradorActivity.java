package com.example.domus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONObject;

public class LoginMoradorActivity extends AppCompatActivity {

    private EditText editUsuario, editSenha;
    private Button btnEntrar;
    private MoradorDAO moradorDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_morador);

        // CORREÇÃO: Importada a classe View e usando a raiz do layout
        View rootView = findViewById(android.R.id.content);

        ViewCompat.setOnApplyWindowInsetsListener(rootView, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        editUsuario = findViewById(R.id.editUsuario);
        editSenha = findViewById(R.id.editSenha);
        btnEntrar = findViewById(R.id.btnEntrar);

        moradorDAO = new MoradorDAO(this);

        btnEntrar.setOnClickListener(v -> validarLogin());
    }

    private void validarLogin() {
        String usuario = editUsuario.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();

        if (usuario.isEmpty() || senha.isEmpty()) {
            Toast.makeText(this, "Preencha os campos usuário e senha!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Busca o morador no banco de dados
            JSONObject morador = moradorDAO.validarLoginMorador(usuario, senha);

            if (morador != null) {
                Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(LoginMoradorActivity.this, DashBoardActivity.class);

                // Passa dados do morador para o Dashboard
                intent.putExtra("tipo_usuario", "morador");
                intent.putExtra("morador_nome", morador.optString("nome"));
                intent.putExtra("morador_cod", morador.optString("cod"));
                intent.putExtra("morador_json", morador.toString());

                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Usuário ou senha incorretos!", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao fazer login. Tente novamente.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Limpar campos ao retornar para a tela de login
        if (editUsuario != null) editUsuario.setText("");
        if (editSenha != null) editSenha.setText("");
        if (editUsuario != null) editUsuario.requestFocus();
    }
}