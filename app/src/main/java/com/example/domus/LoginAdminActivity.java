package com.example.domus.presentation.loginadmin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.domus.R;
import com.example.domus.presentation.dashboard.DashBoardActivity;

public class LoginAdminActivity extends AppCompatActivity {

    private com.example.domus.presentation.loginadmin.LoginAdminViewModel viewModel;
    private EditText editLoginAdmin, editLoginSenha, editCadastroAdmin, editCadastroSenha;
    private Button btnEntrar, btnCadastrar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_admin);

        // Inicializa views
        editLoginAdmin = findViewById(R.id.editLoginAdmin);
        editLoginSenha = findViewById(R.id.editLoginSenha);
        editCadastroAdmin = findViewById(R.id.editCadastroAdmin);
        editCadastroSenha = findViewById(R.id.editCadastroSenha);
        btnEntrar = findViewById(R.id.btnEntrar);
        btnCadastrar = findViewById(R.id.btnCadastrar);

        // Inicializa ViewModel
        viewModel = new ViewModelProvider(this).get(com.example.domus.presentation.loginadmin.LoginAdminViewModel.class);

        // Observa estado da UI
        viewModel.getUiState().observe(this, this::updateUi);

        // Observa navegação
        viewModel.getNavigationToDashboard().observe(this, shouldNavigate -> {
            if (shouldNavigate) {
                navigateToDashboard();
            }
        });

        // Configura listeners
        btnEntrar.setOnClickListener(v -> {
            String usuario = editLoginAdmin.getText().toString().trim();
            String senha = editLoginSenha.getText().toString().trim();
            viewModel.onLoginClicked(usuario, senha);
        });

        btnCadastrar.setOnClickListener(v -> {
            String usuario = editCadastroAdmin.getText().toString().trim();
            String senha = editCadastroSenha.getText().toString().trim();

            if (usuario.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
                return;
            }

            // Mostra dialog para senha master
            showMasterPasswordDialog(usuario, senha);
        });
    }

    private void showMasterPasswordDialog(String usuario, String senha) {
        android.widget.EditText inputMaster = new android.widget.EditText(this);
        inputMaster.setHint("Digite a senha master");

        new AlertDialog.Builder(this)
                .setTitle("Autenticação Master")
                .setMessage("Digite a senha master para cadastrar um novo administrador:")
                .setView(inputMaster)
                .setPositiveButton("OK", (dialog, which) -> {
                    String masterSenha = inputMaster.getText().toString().trim();
                    viewModel.onRegisterClicked(usuario, senha, masterSenha);
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }

    private void updateUi(com.example.domus.presentation.loginadmin.LoginAdminUiState uiState) {
        if (uiState.isLoading()) {
            btnEntrar.setEnabled(false);
            btnCadastrar.setEnabled(false);
            btnEntrar.setText("Entrando...");
            return;
        }

        btnEntrar.setEnabled(true);
        btnCadastrar.setEnabled(true);
        btnEntrar.setText("Entrar");

        if (uiState.getSuccessMessage() != null) {
            Toast.makeText(this, uiState.getSuccessMessage(), Toast.LENGTH_LONG).show();
            viewModel.onMessageShown();

            // Limpa campos de cadastro em caso de sucesso
            if (editCadastroAdmin != null) editCadastroAdmin.setText("");
            if (editCadastroSenha != null) editCadastroSenha.setText("");
        }

        if (uiState.getErrorMessage() != null) {
            Toast.makeText(this, uiState.getErrorMessage(), Toast.LENGTH_SHORT).show();
            viewModel.onMessageShown();
        }

        // Controla visibilidade do formulário de cadastro
        if (!uiState.isShowCadastroForm()) {
            btnCadastrar.setVisibility(View.GONE);
            editCadastroAdmin.setVisibility(View.GONE);
            editCadastroSenha.setVisibility(View.GONE);

            // Ajusta margem do botão Entrar quando o cadastro está oculto
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) btnEntrar.getLayoutParams();
            params.topMargin = 0;
            btnEntrar.setLayoutParams(params);
        } else {
            btnCadastrar.setVisibility(View.VISIBLE);
            editCadastroAdmin.setVisibility(View.VISIBLE);
            editCadastroSenha.setVisibility(View.VISIBLE);
        }
    }

    private void navigateToDashboard() {
        Intent intent = new Intent(this, DashBoardActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}