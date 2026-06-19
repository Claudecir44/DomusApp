package com.cjstudio.condominio_sociedade_morro_grande.presentation.loginadmin;

import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.cjstudio.condominio_sociedade_morro_grande.R;
import com.cjstudio.condominio_sociedade_morro_grande.presentation.dashboard.DashBoardActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.HashMap;
import java.util.Map;

public class LoginAdminActivity extends AppCompatActivity {

    private EditText editLoginAdmin, editLoginSenha;
    private EditText editCadastroEmail, editCadastroAdmin, editCadastroSenha;
    private Button btnEntrar, btnCadastrar;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_admin);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        editLoginAdmin = findViewById(R.id.editLoginAdmin);
        editLoginSenha = findViewById(R.id.editLoginSenha);
        btnEntrar = findViewById(R.id.btnEntrar);

        editCadastroEmail = findViewById(R.id.editCadastroEmail);
        editCadastroAdmin = findViewById(R.id.editCadastroAdmin);
        editCadastroSenha = findViewById(R.id.editCadastroSenha);
        btnCadastrar = findViewById(R.id.btnCadastrar);

        // 🔥 REMOVIDO: verificação automática de usuário logado

        btnEntrar.setOnClickListener(v -> {
            String nomeDigitado = editLoginAdmin.getText().toString().trim();
            String senha = editLoginSenha.getText().toString().trim();
            if (nomeDigitado.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha nome e senha", Toast.LENGTH_SHORT).show();
                return;
            }
            String primeiroNome = extrairPrimeiroNome(nomeDigitado);
            loginPorPrimeiroNome(primeiroNome, senha);
        });

        btnCadastrar.setOnClickListener(v -> {
            String email = editCadastroEmail.getText().toString().trim();
            String nomeCompleto = editCadastroAdmin.getText().toString().trim();
            String senha = editCadastroSenha.getText().toString().trim();

            if (email.isEmpty() || nomeCompleto.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show();
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
            cadastrarAdmin(email, nomeCompleto, senha);
        });
    }

    // ============================================================
    // UTILITÁRIO – extrai o primeiro nome
    // ============================================================
    private String extrairPrimeiroNome(String nomeCompleto) {
        if (nomeCompleto == null) return "";
        String[] partes = nomeCompleto.trim().split(" ");
        return partes.length > 0 ? partes[0] : nomeCompleto;
    }

    // ============================================================
    // LOGIN – busca pelo PRIMEIRO NOME na coleção "administradores"
    // ============================================================
    private void loginPorPrimeiroNome(String primeiroNome, String senha) {
        String primeiroNomeLower = primeiroNome.toLowerCase();

        db.collection("administradores")
                .whereEqualTo("primeiroNomeLowercase", primeiroNomeLower)
                .get()
                .addOnSuccessListener((QuerySnapshot query) -> {
                    if (!query.isEmpty()) {
                        DocumentSnapshot doc = query.getDocuments().get(0);
                        autenticarComEmail(doc.getString("email"), senha);
                        return;
                    }
                    // Fallback: busca pelo campo "nome" (completo) – compatibilidade
                    loginPorNomeOriginal(primeiroNome, senha);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginAdminActivity.this,
                            "Erro ao buscar administrador: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void loginPorNomeOriginal(String nome, String senha) {
        db.collection("administradores")
                .whereEqualTo("nome", nome)
                .get()
                .addOnSuccessListener(query -> {
                    if (query.isEmpty()) {
                        Toast.makeText(LoginAdminActivity.this,
                                "Administrador não encontrado! Verifique o nome.",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    DocumentSnapshot doc = query.getDocuments().get(0);
                    autenticarComEmail(doc.getString("email"), senha);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginAdminActivity.this,
                            "Erro ao buscar administrador: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void autenticarComEmail(String email, String senha) {
        if (email == null || email.isEmpty()) {
            Toast.makeText(LoginAdminActivity.this,
                    "Email não cadastrado para este administrador.",
                    Toast.LENGTH_LONG).show();
            return;
        }

        auth.signInWithEmailAndPassword(email, senha)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            verificarTipoAdmin(user.getUid());
                        }
                    } else {
                        Toast.makeText(LoginAdminActivity.this,
                                "Erro no login: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void verificarTipoAdmin(String uid) {
        db.collection("administradores").document(uid).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String tipo = doc.getString("tipo");
                        if ("admin".equals(tipo)) {
                            Intent intent = new Intent(LoginAdminActivity.this, DashBoardActivity.class);
                            intent.putExtra("tipo_usuario", "admin");
                            intent.putExtra("morador_nome", doc.getString("nome"));
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(LoginAdminActivity.this,
                                    "Acesso restrito a administradores.",
                                    Toast.LENGTH_SHORT).show();
                            auth.signOut();
                        }
                    } else {
                        Toast.makeText(LoginAdminActivity.this,
                                "Usuário não cadastrado no sistema.",
                                Toast.LENGTH_SHORT).show();
                        auth.signOut();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(LoginAdminActivity.this,
                            "Erro ao verificar permissão: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show();
                });
    }

    // ============================================================
    // CADASTRO – salva primeiroNome e primeiroNomeLowercase
    // ============================================================
    private void cadastrarAdmin(String email, String nomeCompleto, String senha) {
        String primeiroNome = extrairPrimeiroNome(nomeCompleto);

        auth.createUserWithEmailAndPassword(email, senha)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            String uid = user.getUid();

                            Map<String, Object> adminData = new HashMap<>();
                            adminData.put("nome", nomeCompleto);
                            adminData.put("nomeLowercase", nomeCompleto.toLowerCase());
                            adminData.put("primeiroNome", primeiroNome);
                            adminData.put("primeiroNomeLowercase", primeiroNome.toLowerCase());
                            adminData.put("email", email);
                            adminData.put("tipo", "admin");
                            adminData.put("telefone", "");

                            db.collection("administradores").document(uid).set(adminData)
                                    .addOnSuccessListener(aVoid -> {
                                        Toast.makeText(LoginAdminActivity.this,
                                                "Administrador cadastrado com sucesso!",
                                                Toast.LENGTH_SHORT).show();
                                        Toast.makeText(LoginAdminActivity.this,
                                                "Email: " + email + " | Senha: " + senha,
                                                Toast.LENGTH_LONG).show();
                                        editCadastroEmail.setText("");
                                        editCadastroAdmin.setText("");
                                        editCadastroSenha.setText("");
                                        auth.signOut();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(LoginAdminActivity.this,
                                                "Erro ao salvar dados: " + e.getMessage(),
                                                Toast.LENGTH_SHORT).show();
                                        user.delete();
                                    });
                        }
                    } else {
                        if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                            Toast.makeText(LoginAdminActivity.this,
                                    "Este email já está cadastrado. Use outro ou faça login.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(LoginAdminActivity.this,
                                    "Erro ao cadastrar: " + task.getException().getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }
}