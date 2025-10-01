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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoSplash = findViewById(R.id.videoSplash);
        layoutBotoes = findViewById(R.id.layoutBotoes);

        Log.d(TAG, "🚀 App Domus Iniciado");
        Log.d(TAG, "📊 Modo: SQLite Local + Supabase Cloud");


        // 🔥 INTEGRAÇÃO REAL AGORA
        integrarComSupabase();

        // Verificar SQLite local
        verificarSQLiteLocal();

        // Carrega o vídeo da pasta raw
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.domus);
        videoSplash.setVideoURI(videoUri);

        videoSplash.setOnCompletionListener(mp -> {
            showUserChoiceDialog();
        });

        videoSplash.start();
    }

    private void integrarComSupabase() {
        Log.d(TAG, "🚀 INICIANDO INTEGRAÇÃO SUPABASE");

        try {
            Class.forName("com.example.domus.SupabaseJavaManager");
            Log.d(TAG, "✅ SupabaseJavaManager encontrado");

            // 🔥 CORREÇÃO: Chamar o método de teste PRIMEIRO
            Log.d(TAG, "🎯 TESTANDO CONEXÃO BÁSICA...");

            // Chamar testarConexao() primeiro - método mais simples
            java.lang.reflect.Method testarConexao = Class.forName("com.example.domus.SupabaseJavaManager")
                    .getMethod("testarConexao");
            testarConexao.invoke(null);

            Log.d(TAG, "✅ Teste de conexão enviado para SupabaseJavaManager");

            // 🔥 AGUARDAR E DEPOIS TENTAR SINCRONIZAÇÃO
            new android.os.Handler().postDelayed(() -> {
                try {
                    Log.d(TAG, "🔄 INICIANDO SINCRONIZAÇÃO COMPLETA...");
                    java.lang.reflect.Method sincronizar = Class.forName("com.example.domus.SupabaseJavaManager")
                            .getMethod("sincronizarComSupabase", android.content.Context.class);
                    sincronizar.invoke(null, this);
                    Log.d(TAG, "🎉 SINCRONIZAÇÃO REAL INICIADA!");
                } catch (Exception e) {
                    Log.e(TAG, "❌ ERRO NA SINCRONIZAÇÃO: " + e.getMessage());
                }
            }, 2000); // Aguardar 2 segundos

        } catch (ClassNotFoundException e) {
            Log.e(TAG, "❌ SupabaseJavaManager NÃO encontrado");
        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO GRAVE na integração: " + e.getMessage());
            Log.e(TAG, "🔍 Stacktrace: " + android.util.Log.getStackTraceString(e));
            testeFinalSupabase();
        }
    }


    private void testeFinalSupabase() {
        Log.d("TESTE_FINAL", "🎯 TESTE FINAL - VERIFICANDO THREADS SUPABASE");

        new Thread(() -> {
            try {
                // Teste DIRETO do método testarConexao
                Log.d("TESTE_FINAL", "1. Chamando testarConexao() diretamente...");

                Class<?> managerClass = Class.forName("com.example.domus.SupabaseJavaManager");
                java.lang.reflect.Method testarConexao = managerClass.getMethod("testarConexao");
                testarConexao.invoke(null);

                Log.d("TESTE_FINAL", "✅ testarConexao() chamado com sucesso");

                // Aguardar 3 segundos para ver logs
                Thread.sleep(3000);

                // Teste DIRETO do método inserirDadoTeste
                Log.d("TESTE_FINAL", "2. Chamando inserirDadoTeste() diretamente...");

                java.lang.reflect.Method inserirTeste = managerClass.getMethod("inserirDadoTeste");
                inserirTeste.invoke(null);

                Log.d("TESTE_FINAL", "✅ inserirDadoTeste() chamado com sucesso");

                // Aguardar mais 3 segundos
                Thread.sleep(3000);

                Log.d("TESTE_FINAL", "🎉 TESTE FINAL CONCLUÍDO!");

            } catch (Exception e) {
                Log.e("TESTE_FINAL", "❌ ERRO NO TESTE FINAL: " + e.getMessage());
            }
        }).start();
    }



    private void debugForcadoSupabase() {
        Log.d("DEBUG_FORCADO", "🎯 DEBUG FORÇADO - SUPABASE JAVAMANAGER");

        new Thread(() -> {
            try {
                // 1. Verificar se a classe existe e carrega
                Class<?> managerClass = Class.forName("com.example.domus.SupabaseJavaManager");
                Log.d("DEBUG_FORCADO", "✅ CLASSE: Carregada com sucesso");

                // 2. Verificar métodos disponíveis
                java.lang.reflect.Method[] methods = managerClass.getDeclaredMethods();
                Log.d("DEBUG_FORCADO", "📋 MÉTODOS: " + methods.length + " encontrados");
                for (java.lang.reflect.Method method : methods) {
                    Log.d("DEBUG_FORCADO", "   - " + method.getName());
                }

                // 3. Testar conexão DIRETAMENTE
                Log.d("DEBUG_FORCADO", "🔗 TESTANDO CONEXÃO DIRETAMENTE...");
                java.lang.reflect.Method testarConexao = managerClass.getMethod("testarConexao");
                testarConexao.invoke(null);
                Log.d("DEBUG_FORCADO", "✅ TESTE DE CONEXÃO: Enviado com sucesso");

                // 4. Aguardar e testar inserção
                Thread.sleep(3000);
                Log.d("DEBUG_FORCADO", "📝 TESTANDO INSERÇÃO...");
                java.lang.reflect.Method inserirTeste = managerClass.getMethod("inserirDadoTeste");
                inserirTeste.invoke(null);
                Log.d("DEBUG_FORCADO", "✅ TESTE DE INSERÇÃO: Enviado com sucesso");

            } catch (Exception e) {
                Log.e("DEBUG_FORCADO", "💥 ERRO CRÍTICO: " + e.getMessage());
                Log.e("DEBUG_FORCADO", "📋 STACKTRACE: " + android.util.Log.getStackTraceString(e));
            }
        }).start();
    }

    private void verificarSQLiteLocal() {
        try {
            BDCondominioHelper dbHelper = new BDCondominioHelper(this);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            // Verificar se tabelas existem
            Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
            int tableCount = cursor.getCount();
            cursor.close();
            db.close();

            Log.d(TAG, "✅ SQLite Local: " + tableCount + " tabelas encontradas");
            Log.d(TAG, "📱 App funcionando perfeitamente com banco local");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro no SQLite local: " + e.getMessage());
        }
    }

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

    private void testeSupabaseSuperSimples() {
        Log.d("TESTE_SUPABASE", "🎯 TESTE SUPER SIMPLES INICIADO");

        new Thread(() -> {
            try {
                // Teste MUITO simples - só verificar se consegue chamar
                Class<?> managerClass = Class.forName("com.example.domus.SupabaseJavaManager");
                Log.d("TESTE_SUPABASE", "✅ Classe carregada: " + managerClass.getName());

                // Tentar criar uma instância (se necessário)
                Object manager = managerClass.newInstance();
                Log.d("TESTE_SUPABASE", "✅ Instância criada");

                // Chamar método estático diretamente
                managerClass.getMethod("testarConexao").invoke(null);
                Log.d("TESTE_SUPABASE", "✅ Método testarConexao chamado");

            } catch (Exception e) {
                Log.e("TESTE_SUPABASE", "❌ ERRO CRÍTICO: " + e.getMessage());
            }
        }).start();
    }

    private boolean isAdminRegistered() {
        BDCondominioHelper dbHelper = new BDCondominioHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(BDCondominioHelper.TABELA_USUARIOS_ADMIN,
                new String[]{BDCondominioHelper.COL_ADMIN_USUARIO},
                null, null, null, null, null);

        boolean existe = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        db.close();

        return existe;
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "✅ App totalmente funcional - SQLite + Supabase");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (videoSplash != null) {
            videoSplash.stopPlayback();
        }
    }
}