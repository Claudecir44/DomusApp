package com.example.domus.presentation.loginmaster;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.domus.BuildConfig;
import com.example.domus.R;
import com.example.domus.presentation.loginadmin.LoginAdminActivity;

public class LoginMasterActivity extends AppCompatActivity {

    private static final String TAG = "LoginMasterActivity";

    private com.example.domus.presentation.loginmaster.LoginMasterViewModel viewModel;
    private EditText editUsuario, editSenha;
    private Button btnEntrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_master);

        Log.d(TAG, "=== LOGIN MASTER ACTIVITY INICIADA ===");

        // Inicializa views
        editUsuario = findViewById(R.id.editUsuario);
        editSenha = findViewById(R.id.editSenha);
        btnEntrar = findViewById(R.id.btnEntrar);

        // Inicializa ViewModel
        viewModel = new ViewModelProvider(this).get(com.example.domus.presentation.loginmaster.LoginMasterViewModel.class);

        // Observa estado da UI
        viewModel.getUiState().observe(this, this::updateUi);

        // Observa navegação
        viewModel.getNavigateToLoginAdmin().observe(this, shouldNavigate -> {
            if (shouldNavigate) {
                navigateToLoginAdmin();
            }
        });

        // Configura listener
        btnEntrar.setOnClickListener(v -> {
            String usuario = editUsuario.getText().toString().trim();
            String senha = editSenha.getText().toString().trim();
            viewModel.onLoginClicked(usuario, senha);
        });

        // Preenche automaticamente para teste (apenas em debug)
        if (BuildConfig.DEBUG) {
            editUsuario.setText("admin");
            editSenha.setText("master");
        }
    }

    private void updateUi(com.example.domus.presentation.loginmaster.LoginMasterUiState uiState) {
        if (uiState.isLoading()) {
            btnEntrar.setEnabled(false);
            btnEntrar.setText("Validando...");
            return;
        }

        btnEntrar.setEnabled(true);
        btnEntrar.setText("Entrar");

        if (uiState.getSuccessMessage() != null) {
            Log.d(TAG, "✅ " + uiState.getSuccessMessage());
            Toast.makeText(this, uiState.getSuccessMessage(), Toast.LENGTH_SHORT).show();
            viewModel.onMessageShown();
        }

        if (uiState.getErrorMessage() != null) {
            Log.d(TAG, "❌ " + uiState.getErrorMessage());
            Toast.makeText(this, uiState.getErrorMessage(), Toast.LENGTH_SHORT).show();
            viewModel.onMessageShown();
        }

        // Log das estatísticas (apenas para debug)
        if (uiState.getStats() != null) {
            Log.d(TAG, "📊 Estatísticas: " + uiState.getStats().getTotalAdmins() +
                    " admins, Master existe: " + uiState.getStats().hasMaster());
        }
    }

    private void navigateToLoginAdmin() {
        Log.d(TAG, "🎉 Navegando para LoginAdminActivity");
        Intent intent = new Intent(this, LoginAdminActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "=== TELA LOGIN MASTER VISÍVEL ===");

        // Recarrega estatísticas ao retornar
        viewModel.refreshStats();

        // Limpa campos de senha por segurança
        if (editSenha != null) {
            editSenha.setText("");
        }
    }
}