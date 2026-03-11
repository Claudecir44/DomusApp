package com.example.domus;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.widget.LinearLayout;
import android.util.Log;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.database.sqlite.SQLiteDatabase;

public class MainActivity extends AppCompatActivity {

    private LinearLayout layoutBotoes;
    private static final String TAG = "DomusMain";
    private BDCondominioHelper dbHelper;
    private ImageView imageSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "🔥🔥🔥 MAIN ACTIVITY - ONCREATE INICIADO 🔥🔥🔥");

        try {
            inicializarComponentesBasicos();
            Log.d(TAG, "✅✅✅ COMPONENTES BÁSICOS INICIALIZADOS ✅✅✅");

            executarDiagnosticoCritico();
            new Handler().postDelayed(this::showUserChoiceDialog, 1000);

        } catch (Exception e) {
            Log.e(TAG, "💥💥💥 ERRO CRÍTICO NO ONCREATE: " + e.getMessage(), e);
            mostrarToast("Erro ao iniciar app: " + e.getMessage());

            new Handler().postDelayed(this::showUserChoiceDialog, 2000);
        }
    }

    // Método que inicializa membros principais
    private void inicializarComponentesBasicos() {
        Log.d(TAG, "🔧 INICIALIZANDO COMPONENTES BÁSICOS");

        try {
            imageSplash = findViewById(R.id.imageSplash);
            layoutBotoes = findViewById(R.id.layoutBotoes);
            Log.d(TAG, "✅ Componentes de layout encontrados");
        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO ao encontrar componentes: " + e.getMessage());
        }

        try {
            dbHelper = new BDCondominioHelper(this);
            Log.d(TAG, "✅ BDCondominioHelper criado");
            testarConexaoBanco();
        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO ao criar DB Helper: " + e.getMessage());
            dbHelper = null;
        }
    }

    // Método para testar conexão com banco local SQLite
    private void testarConexaoBanco() {
        if (dbHelper == null) return;

        try {
            SQLiteDatabase db = dbHelper.getReadableDatabase();
            boolean isOpen = db.isOpen();
            db.close();
            Log.d(TAG, "🗃️ Conexão com banco: " + (isOpen ? "✅ OK" : "❌ FALHA"));
        } catch (Exception e) {
            Log.e(TAG, "💥 Erro ao testar banco: " + e.getMessage());
        }
    }

    // Método que executa diagnose
    private void executarDiagnosticoCritico() {
        Log.d(TAG, "🔍🔍🔍 DIAGNÓSTICO CRÍTICO INICIADO 🔍🔍🔍");

        Log.d(TAG, "📋 DB Helper: " + (dbHelper != null ? "✅ OK" : "❌ NULL"));
        Log.d(TAG, "📋 Layout: " + (layoutBotoes != null ? "✅ OK" : "❌ NULL"));

        Log.d(TAG, "🔍🔍🔍 DIAGNÓSTICO CRÍTICO CONCLUÍDO 🔍🔍🔍");
    }

    // 🔧 VERIFICAÇÃO IMEDIATA DE REDE (apenas para verificar conectividade local)
    private void verificarRedeImediata() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm != null ? cm.getActiveNetworkInfo() : null;

            if (activeNetwork != null && activeNetwork.isConnected()) {
                Log.d(TAG, "🌐✅ REDE DISPONÍVEL: " + activeNetwork.getTypeName());
                Log.d(TAG, "📶 Conectado: " + activeNetwork.isConnected());
            } else {
                Log.e(TAG, "🌐❌ SEM CONEXÃO DE REDE");
                mostrarToast("⚠️ Sem conexão de internet");
            }
        } catch (Exception e) {
            Log.e(TAG, "💥 Erro ao verificar rede: " + e.getMessage());
        }
    }

    // 🔧 VERIFICAÇÃO DO BANCO DE DADOS
    private void verificarBancoDados() {
        if (dbHelper == null) {
            Log.e(TAG, "❌ DB Helper é null - não é possível verificar banco");
            return;
        }

        try {
            // Verificar se tabelas existem
            String[] tabelas = {
                    BDCondominioHelper.TABELA_MORADORES,
                    BDCondominioHelper.TABELA_USUARIOS_ADMIN,
                    BDCondominioHelper.TABELA_OCORRENCIAS
            };

            for (String tabela : tabelas) {
                boolean existe = dbHelper.tabelaExiste(tabela);
                int registros = dbHelper.contarRegistros(tabela);
                Log.d(TAG, "🗃️ " + tabela + ": " + (existe ? "✅ EXISTE" : "❌ AUSENTE") +
                        " | Registros: " + registros);
            }

        } catch (Exception e) {
            Log.e(TAG, "💥 Erro ao verificar banco: " + e.getMessage());
        }
    }

    // Diálogo para escolha de usuário
    private void showUserChoiceDialog() {
        Log.d(TAG, "💬 MOSTRANDO DIÁLOGO DE ESCOLHA DE USUÁRIO");

        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("🏠 Bem-vindo ao Domus")
                    .setMessage("Como você deseja acessar o sistema?")
                    .setPositiveButton("👑 Administrador", (dialog, which) -> {
                        Log.d(TAG, "➡️ Usuário escolheu: Administrador");
                        verificarEIrParaAdmin();
                    })
                    .setNegativeButton("👥 Morador", (dialog, which) -> {
                        Log.d(TAG, "➡️ Usuário escolheu: Morador");
                        startActivity(new Intent(MainActivity.this, LoginMoradorActivity.class));
                    })
                    .setCancelable(false)
                    .show();

            Log.d(TAG, "✅ Diálogo de escolha exibido com sucesso");

        } catch (Exception e) {
            Log.e(TAG, "💥 Erro ao mostrar diálogo: " + e.getMessage());
            // Fallback: ir direto para login de admin
            startActivity(new Intent(this, LoginAdminActivity.class));
        }
    }

    // 🔧 VERIFICAR E IR PARA TELA DE ADMIN CORRETA
    private void verificarEIrParaAdmin() {
        try {
            boolean adminExiste = isAdminRegistered();
            Log.d(TAG, "🔐 Admin registrado localmente: " + adminExiste);

            if (!adminExiste) {
                Log.d(TAG, "➡️ Indo para LoginMasterActivity (primeiro admin)");
                startActivity(new Intent(MainActivity.this, LoginMasterActivity.class));
            } else {
                Log.d(TAG, "➡️ Indo para LoginAdminActivity (admin existente)");
                startActivity(new Intent(MainActivity.this, LoginAdminActivity.class));
            }
        } catch (Exception e) {
            Log.e(TAG, "💥 Erro ao verificar admin: " + e.getMessage());
            // Fallback para tela principal de admin
            startActivity(new Intent(this, LoginAdminActivity.class));
        }
    }

    // Verifica se existe admin registrado
    private boolean isAdminRegistered() {
        if (dbHelper == null) {
            Log.e(TAG, "❌ DB Helper null - não é possível verificar admin");
            return false;
        }

        try {
            boolean existe = dbHelper.existeAdmin();
            Log.d(TAG, "🔐 Verificação de admin: " + existe);
            return existe;
        } catch (Exception e) {
            Log.e(TAG, "💥 Erro ao verificar admin: " + e.getMessage());
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 MAIN ACTIVITY - ONRESUME");

        // Atualizar diagnóstico ao retornar
        new Handler().postDelayed(() -> {
            Log.d(TAG, "🔍 ATUALIZANDO DIAGNÓSTICO AO RETORNAR");
            verificarStatusCompleto();
        }, 1000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "🛑 MAIN ACTIVITY - ONDESTROY");

        if (dbHelper != null) {
            dbHelper.close();
        }
    }

    // 🔧 VERIFICAR SE REDE ESTÁ DISPONÍVEL (apenas para info)
    private boolean isRedeDisponivel() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm != null ? cm.getActiveNetworkInfo() : null;
            return networkInfo != null && networkInfo.isConnected();
        } catch (Exception e) {
            Log.e(TAG, "💥 Erro ao verificar rede: " + e.getMessage());
            return false;
        }
    }

    // 🔧 MOSTRAR TOAST
    private void mostrarToast(String mensagem) {
        runOnUiThread(() -> Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show());
    }

    // 🔧 VERIFICAR STATUS COMPLETO (apenas local)
    private void verificarStatusCompleto() {
        Log.d(TAG, "📊 VERIFICAÇÃO DE STATUS COMPLETA");

        if (dbHelper == null) {
            Log.e(TAG, "❌ DB Helper null - não é possível verificar status");
            return;
        }

        try {
            String[] tabelas = {
                    BDCondominioHelper.TABELA_USUARIOS_ADMIN,
                    BDCondominioHelper.TABELA_MORADORES,
                    BDCondominioHelper.TABELA_OCORRENCIAS,
                    BDCondominioHelper.TABELA_FUNCIONARIOS
            };

            String[] nomes = {"Admins", "Moradores", "Ocorrências", "Funcionários"};

            for (int i = 0; i < tabelas.length; i++) {
                int count = dbHelper.contarRegistros(tabelas[i]);
                Log.d(TAG, "📋 " + nomes[i] + ": " + count + " registros locais");
            }

        } catch (Exception e) {
            Log.e(TAG, "💥 Erro ao verificar status: " + e.getMessage());
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null) {
            return false;
        }

        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}