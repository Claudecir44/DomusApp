package com.example.domus;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.database.sqlite.SQLiteDatabase;

import com.example.domus.presentation.loginadmin.LoginAdminActivity;
import com.example.domus.presentation.loginmorador.LoginMoradorActivity;

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

    private void executarDiagnosticoCritico() {
        Log.d(TAG, "🔍🔍🔍 DIAGNÓSTICO CRÍTICO INICIADO 🔍🔍🔍");

        Log.d(TAG, "📋 DB Helper: " + (dbHelper != null ? "✅ OK" : "❌ NULL"));
        Log.d(TAG, "📋 Layout: " + (layoutBotoes != null ? "✅ OK" : "❌ NULL"));

        Log.d(TAG, "🔍🔍🔍 DIAGNÓSTICO CRÍTICO CONCLUÍDO 🔍🔍🔍");
    }

    private void showUserChoiceDialog() {
        Log.d(TAG, "💬 MOSTRANDO DIÁLOGO DE ESCOLHA DE USUÁRIO");

        try {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("🏠 Bem-vindo ao Domus")
                    .setMessage("Como você deseja acessar o sistema?")
                    .setPositiveButton("👑 Administrador", (dialog, which) -> {
                        Log.d(TAG, "➡️ Usuário escolheu: Administrador");
                        startActivity(new Intent(MainActivity.this, LoginAdminActivity.class));
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
            startActivity(new Intent(this, LoginAdminActivity.class));
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 MAIN ACTIVITY - ONRESUME");

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

    private void mostrarToast(String mensagem) {
        runOnUiThread(() -> Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show());
    }

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