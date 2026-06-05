package com.example.domus.presentation.loginmorador;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.domus.R;
import com.example.domus.presentation.dashboard.DashBoardActivity;

public class LoginMoradorActivity extends AppCompatActivity {

    private com.example.domus.presentation.loginmorador.LoginMoradorViewModel viewModel;
    private EditText editUsuario, editSenha;
    private Button btnEntrar;
    private TextView tvLembrete;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login_morador);

        // Window insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content),
                (v, insets) -> {
                    Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                    v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
                    return insets;
                });

        // Inicializa views
        editUsuario = findViewById(R.id.editUsuario);
        editSenha = findViewById(R.id.editSenha);
        btnEntrar = findViewById(R.id.btnEntrar);
        tvLembrete = findViewById(R.id.tvLembrete);

        // Configurar lembrete e conversão para minúsculo
        configurarCampoUsuario();

        // Inicializa ViewModel
        viewModel = new ViewModelProvider(this).get(com.example.domus.presentation.loginmorador.LoginMoradorViewModel.class);

        // Observa estado da UI
        viewModel.getUiState().observe(this, this::updateUi);

        // Observa navegação
        viewModel.getNavigationToDashboard().observe(this, this::navigateToDashboard);

        // Configura listener
        btnEntrar.setOnClickListener(v -> {
            String usuario = editUsuario.getText().toString().trim().toLowerCase();
            String senha = editSenha.getText().toString().trim();
            viewModel.onLoginClicked(usuario, senha);
        });
    }

    private void configurarCampoUsuario() {
        // Adicionar TextWatcher para converter automaticamente para minúsculo
        editUsuario.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // Não necessário
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Não necessário
            }

            @Override
            public void afterTextChanged(Editable s) {
                // Converter para minúsculo quando o usuário parar de digitar
                String text = s.toString();
                if (!text.equals(text.toLowerCase())) {
                    editUsuario.setText(text.toLowerCase());
                    editUsuario.setSelection(editUsuario.length());
                }
            }
        });

        // LEMBRETE SEMPRE VISÍVEL - sem timer para esconder
        tvLembrete.setVisibility(android.view.View.VISIBLE);
    }

    private void updateUi(com.example.domus.presentation.loginmorador.LoginMoradorUiState uiState) {
        if (uiState.isLoading()) {
            btnEntrar.setEnabled(false);
            btnEntrar.setText("Entrando...");
            return;
        }

        btnEntrar.setEnabled(true);
        btnEntrar.setText("Entrar");

        if (uiState.getErrorMessage() != null) {
            Toast.makeText(this, uiState.getErrorMessage(), Toast.LENGTH_SHORT).show();
            viewModel.onMessageShown();
        }
    }

    private void navigateToDashboard(com.example.domus.presentation.loginmorador.LoginMoradorViewModel.LoginSuccessData data) {
        if (data == null) return;

        Toast.makeText(this, "Login realizado com sucesso!", Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, DashBoardActivity.class);
        intent.putExtra("tipo_usuario", "morador");
        intent.putExtra("morador_nome", data.getMoradorNome());
        intent.putExtra("morador_cod", data.getMoradorCod());
        intent.putExtra("morador_json", data.getMoradorData().toString());

        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Limpa campos ao retornar
        if (editUsuario != null) editUsuario.setText("");
        if (editSenha != null) editSenha.setText("");
        if (editUsuario != null) editUsuario.requestFocus();
        viewModel.clearFields();

        // Garantir que o lembrete continue visível
        tvLembrete.setVisibility(android.view.View.VISIBLE);
    }
}