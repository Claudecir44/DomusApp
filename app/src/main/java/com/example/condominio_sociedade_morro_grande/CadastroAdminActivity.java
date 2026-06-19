package com.cjstudio.condominio_sociedade_morro_grande;

import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class CadastroAdminActivity extends AppCompatActivity {

    private EditText editEmail, editSenha, editNome, editTelefone;
    private Button btnCadastrar;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_admin);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editEmail = findViewById(R.id.editEmailAdmin);
        editSenha = findViewById(R.id.editSenhaAdmin);
        editNome = findViewById(R.id.editNomeAdmin);
        editTelefone = findViewById(R.id.editTelefoneAdmin);
        btnCadastrar = findViewById(R.id.btnCadastrarAdmin);

        btnCadastrar.setOnClickListener(v -> cadastrarAdmin());
    }

    private String extrairPrimeiroNome(String nomeCompleto) {
        if (nomeCompleto == null) return "";
        String[] partes = nomeCompleto.trim().split(" ");
        return partes.length > 0 ? partes[0] : nomeCompleto;
    }

    private void cadastrarAdmin() {
        String email = editEmail.getText().toString().trim();
        String senha = editSenha.getText().toString().trim();
        String nomeCompleto = editNome.getText().toString().trim();
        String telefone = editTelefone.getText().toString().trim();

        if (email.isEmpty() || senha.isEmpty() || nomeCompleto.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos obrigatórios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "E-mail inválido!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (senha.length() != 6 || !senha.matches("\\d+")) {
            Toast.makeText(this, "A senha deve ter 6 dígitos numéricos.", Toast.LENGTH_SHORT).show();
            return;
        }

        String primeiroNome = extrairPrimeiroNome(nomeCompleto);

        auth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = task.getResult().getUser();
                        if (user != null) {
                            String uid = user.getUid();

                            Map<String, Object> adminData = new HashMap<>();
                            adminData.put("nome", nomeCompleto);
                            adminData.put("nomeLowercase", nomeCompleto.toLowerCase());
                            adminData.put("primeiroNome", primeiroNome);
                            adminData.put("primeiroNomeLowercase", primeiroNome.toLowerCase());
                            adminData.put("email", email);
                            adminData.put("tipo", "admin");
                            adminData.put("telefone", telefone);

                            db.collection("administradores").document(uid).set(adminData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(CadastroAdminActivity.this,
                                                "Administrador cadastrado com sucesso!",
                                                Toast.LENGTH_SHORT).show();
                                        Toast.makeText(CadastroAdminActivity.this,
                                                "Email: " + email + " | Senha: " + senha,
                                                Toast.LENGTH_LONG).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(CadastroAdminActivity.this,
                                                "Erro ao salvar dados: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                        user.delete();
                                    });
                        }
                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(CadastroAdminActivity.this,
                                    "Este email já está cadastrado. Use outro ou faça login.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(CadastroAdminActivity.this,
                                    "Erro no cadastro: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}