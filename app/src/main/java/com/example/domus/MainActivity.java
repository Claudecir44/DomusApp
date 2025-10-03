package com.example.domus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.VideoView;
import android.util.Log;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class MainActivity extends AppCompatActivity {

    private VideoView videoSplash;
    private LinearLayout layoutBotoes;
    private static final String TAG = "MainActivity";
    private SupabaseSyncManager syncManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "🎯 APP DOMUS INICIANDO");

        // Inicializa o SyncManager
        syncManager = new SupabaseSyncManager(this);

        videoSplash = findViewById(R.id.videoSplash);
        layoutBotoes = findViewById(R.id.layoutBotoes);

        BDCondominioHelper dbHelper = new BDCondominioHelper(this);
        dbHelper.verificarCompatibilidadeSupabase();


        inicializarSincronizacao();

        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.domus);
        videoSplash.setVideoURI(videoUri);

        videoSplash.setOnCompletionListener(mp -> showUserChoiceDialog());

        videoSplash.start();
    }

    // Inicializa sincronização automática de todas as tabelas
    private void inicializarSincronizacao() {
        Log.d(TAG, "🔄 INICIANDO SINCRONIZAÇÃO AUTOMÁTICA DE TODAS AS TABELAS");

        // 1. Sincroniza imediatamente todas as tabelas
        syncManager.sincronizacaoRapida();

        // 2. Reforça sincronização após 2 segundos
        new android.os.Handler().postDelayed(() -> {
            Log.d(TAG, "🔄 REFORÇANDO SINCRONIZAÇÃO DE TODAS AS TABELAS");
            syncManager.sincronizacaoRapida();
        }, 2000);
    }

    // Diálogo para escolha de usuário
    private void showUserChoiceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Escolha o tipo de usuário")
                .setMessage("Você é Administrador ou Morador?")
                .setPositiveButton("Administrador", (dialog, which) -> {
                    if (!isAdminRegistered()) {
                        startActivity(new Intent(MainActivity.this, LoginMasterActivity.class));
                    } else {
                        startActivity(new Intent(MainActivity.this, LoginAdminActivity.class));
                    }
                })
                .setNegativeButton("Morador", (dialog, which) -> {
                    startActivity(new Intent(MainActivity.this, LoginMoradorActivity.class));
                })
                .setCancelable(false)
                .show();
    }

    // Verifica se existe admin registrado no SQLite e sincroniza
    private boolean isAdminRegistered() {
        try {
            BDCondominioHelper dbHelper = new BDCondominioHelper(this);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            Cursor cursor = db.query(BDCondominioHelper.TABELA_USUARIOS_ADMIN,
                    new String[]{BDCondominioHelper.COL_ADMIN_USUARIO},
                    null, null, null, null, null);

            boolean existe = cursor != null && cursor.getCount() > 0;
            if (cursor != null) cursor.close();
            db.close();

            Log.d(TAG, "🔐 Admin registrado localmente: " + existe);

            if (existe) {
                // Sincroniza todas as tabelas, incluindo admins
                syncManager.sincronizacaoRapida();
            }

            return existe;
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao verificar admin: " + e.getMessage());
            return false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "✅ App em primeiro plano");
        // Sincroniza todas as tabelas ao retornar
        syncManager.sincronizacaoRapida();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoSplash != null) {
            videoSplash.stopPlayback();
        }
        Log.d(TAG, "🛑 Activity sendo destruída");
    }

    // Chamado manualmente após salvar qualquer dado local
    public void forcarSincronizacao() {
        Log.d(TAG, "🔄 FORÇANDO SINCRONIZAÇÃO MANUAL DE TODAS AS TABELAS");
        syncManager.sincronizacaoRapida();
    }

    // Verifica quantidade de registros em todas as tabelas
    public void verificarStatusSincronizacao() {
        Log.d(TAG, "📊 STATUS DA SINCRONIZAÇÃO DE TODAS AS TABELAS:");

        BDCondominioHelper dbHelper = new BDCondominioHelper(this);

        Log.d(TAG, "📋 Admins: " + dbHelper.contarRegistros(BDCondominioHelper.TABELA_USUARIOS_ADMIN));
        Log.d(TAG, "📋 Moradores: " + dbHelper.contarRegistros(BDCondominioHelper.TABELA_MORADORES));
        Log.d(TAG, "📋 Ocorrências: " + dbHelper.contarRegistros(BDCondominioHelper.TABELA_OCORRENCIAS));
        Log.d(TAG, "📋 Funcionários: " + dbHelper.contarRegistros(BDCondominioHelper.TABELA_FUNCIONARIOS));
        Log.d(TAG, "📋 Manutenções: " + dbHelper.contarRegistros(BDCondominioHelper.TABELA_MANUTENCOES));
        Log.d(TAG, "📋 Assembleias: " + dbHelper.contarRegistros(BDCondominioHelper.TABELA_ASSEMBLEIAS));
        Log.d(TAG, "📋 Despesas: " + dbHelper.contarRegistros(BDCondominioHelper.TABELA_DESPESAS));
        Log.d(TAG, "📋 Avisos: " + dbHelper.contarRegistros(BDCondominioHelper.TABELA_AVISOS));
    }
}
