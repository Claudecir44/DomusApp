package com.example.domus;

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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;

public class MainActivity extends AppCompatActivity {

    private LinearLayout layoutBotoes;
    private static final String TAG = "DomusMain";
    private SupabaseSyncManager syncManager;
    private BDCondominioHelper dbHelper;
    private ImageView imageSplash;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 🔥 LOG CRÍTICO - VERIFICAR SE CHEGA AQUI
        Log.d(TAG, "🔥🔥🔥 MAIN ACTIVITY - ONCREATE INICIADO 🔥🔥🔥");

        try {
            // Inicializa componentes básicos primeiro
            inicializarComponentesBasicos();

            // Log de sucesso na inicialização
            Log.d(TAG, "✅✅✅ COMPONENTES BÁSICOS INICIALIZADOS ✅✅✅");

            // Executar diagnóstico IMEDIATO
            executarDiagnosticoCritico();

            // Mostrar diálogo de escolha IMEDIATAMENTE (sem vídeo problemático)
            new Handler().postDelayed(() -> {
                showUserChoiceDialog();
            }, 1000);

            // Inicializar sincronização (se possível)
            inicializarSincronizacaoSegura();

        } catch (Exception e) {
            Log.e(TAG, "💥💥💥 ERRO CRÍTICO NO ONCREATE: " + e.getMessage(), e);
            mostrarToast("Erro ao iniciar app: " + e.getMessage());

            // Fallback: tentar mostrar diálogo mesmo com erro
            new Handler().postDelayed(() -> {
                showUserChoiceDialog();
            }, 2000);
        }
    }

    // 🔧 INICIALIZAÇÃO BÁSICA DOS COMPONENTES
    private void inicializarComponentesBasicos() {
        Log.d(TAG, "🔧 INICIALIZANDO COMPONENTES BÁSICOS");

        try {
            // REMOVIDO: VideoView problemático
            // ADICIONADO: ImageView simples como fallback
            imageSplash = findViewById(R.id.imageSplash); // Adicione este ID no layout
            layoutBotoes = findViewById(R.id.layoutBotoes);
            Log.d(TAG, "✅ Componentes de layout encontrados");
        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO ao encontrar componentes: " + e.getMessage());
            // Não lançar exceção - continuar sem componentes visuais
        }

        try {
            syncManager = new SupabaseSyncManager(this);
            Log.d(TAG, "✅ SupabaseSyncManager criado");
        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO ao criar SyncManager: " + e.getMessage());
            syncManager = null;
        }

        try {
            dbHelper = new BDCondominioHelper(this);
            Log.d(TAG, "✅ BDCondominioHelper criado");

            // Testar conexão com banco imediatamente
            testarConexaoBanco();
        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO ao criar DB Helper: " + e.getMessage());
            dbHelper = null;
        }
    }

    // 🔧 TESTAR CONEXÃO COM BANCO
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

    // 🔧 DIAGNÓSTICO CRÍTICO - EXECUTADO IMEDIATAMENTE
    private void executarDiagnosticoCritico() {
        Log.d(TAG, "🔍🔍🔍 DIAGNÓSTICO CRÍTICO INICIADO 🔍🔍🔍");

        // 1. Verificar se componentes foram inicializados
        Log.d(TAG, "📋 SyncManager: " + (syncManager != null ? "✅ OK" : "❌ NULL"));
        Log.d(TAG, "📋 DB Helper: " + (dbHelper != null ? "✅ OK" : "❌ NULL"));
        Log.d(TAG, "📋 Layout: " + (layoutBotoes != null ? "✅ OK" : "❌ NULL"));

        // 2. Verificar rede IMEDIATAMENTE
        verificarRedeImediata();

        // 3. Verificar banco de dados
        verificarBancoDados();

        // 4. Verificar autenticação
        verificarAutenticacao();

        // 5. Testar conexão Supabase
        testarConexaoSupabase();

        Log.d(TAG, "🔍🔍🔍 DIAGNÓSTICO CRÍTICO CONCLUÍDO 🔍🔍🔍");
    }

    // 🔧 TESTAR CONEXÃO COM SUPABASE
    private void testarConexaoSupabase() {
        if (syncManager == null) {
            Log.w(TAG, "⚠️ SyncManager null - pulando teste de conexão");
            return;
        }

        new Handler().postDelayed(() -> {
            Log.d(TAG, "🌐 TESTANDO CONEXÃO COM SUPABASE");
            syncManager.testarConexao();
        }, 500);
    }

    // 🔧 VERIFICAÇÃO IMEDIATA DE REDE
    private void verificarRedeImediata() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm != null ? cm.getActiveNetworkInfo() : null;

            if (activeNetwork != null && activeNetwork.isConnected()) {
                Log.d(TAG, "🌐✅ REDE DISPONÍVEL: " + activeNetwork.getTypeName());
                Log.d(TAG, "📶 Conectado: " + activeNetwork.isConnected());
                Log.d(TAG, "🔗 Tipo: " + getTipoRedeDetalhado(activeNetwork.getType()));
            } else {
                Log.e(TAG, "🌐❌ SEM CONEXÃO DE REDE");
                mostrarToast("⚠️ Sem conexão de internet - Sincronização limitada");
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

    // 🔧 VERIFICAÇÃO DE AUTENTICAÇÃO
    private void verificarAutenticacao() {
        if (syncManager == null) {
            Log.w(TAG, "⚠️ SyncManager null - pulando verificação de auth");
            return;
        }

        try {
            if (syncManager.isUsuarioLogado()) {
                Log.d(TAG, "🔐✅ USUÁRIO AUTENTICADO: " + syncManager.getUsuarioEmail());
                Log.d(TAG, "👤 Role: " + syncManager.getUserRole());
                Log.d(TAG, "👑 Admin: " + syncManager.isAdmin());
            } else {
                Log.w(TAG, "🔐⚠️ USUÁRIO NÃO AUTENTICADO");
                Log.d(TAG, "💡 Dica: Faça login para habilitar sincronização em tempo real");
            }
        } catch (Exception e) {
            Log.e(TAG, "💥 Erro ao verificar autenticação: " + e.getMessage());
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

    // 🔧 SINCRONIZAÇÃO SEGURA (COM TRATAMENTO DE ERROS)
    private void inicializarSincronizacaoSegura() {
        Log.d(TAG, "🔄 TENTANDO SINCRONIZAÇÃO SEGURA");

        if (syncManager == null) {
            Log.e(TAG, "❌ SyncManager null - pulando sincronização");
            return;
        }

        // Verificar condições antes de sincronizar
        if (!syncManager.isUsuarioLogado()) {
            Log.w(TAG, "⚠️ Usuário não autenticado - sincronização adiada");
            Log.d(TAG, "💡 Dica: Faça login como admin para habilitar sincronização automática");
            return;
        }

        if (!isRedeDisponivel()) {
            Log.w(TAG, "⚠️ Sem rede - sincronização adiada");
            return;
        }

        try {
            // Sincronizar após 3 segundos
            new Handler().postDelayed(() -> {
                Log.d(TAG, "🔄 INICIANDO SINCRONIZAÇÃO PRINCIPAL");
                syncManager.sincronizacaoRapida();

                // Verificar status após sincronização
                new Handler().postDelayed(this::verificarStatusAposSincronizacao, 3000);
            }, 3000);

        } catch (Exception e) {
            Log.e(TAG, "💥 Erro na sincronização: " + e.getMessage());
        }
    }

    // 🔧 VERIFICAR STATUS APÓS SINCRONIZAÇÃO
    private void verificarStatusAposSincronizacao() {
        Log.d(TAG, "📊 STATUS PÓS-SINCRONIZAÇÃO:");
        verificarStatusCompleto();
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

    // 🔧 VERIFICAR SE REDE ESTÁ DISPONÍVEL
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

    // 🔧 OBTER TIPO DE REDE DETALHADO
    private String getTipoRedeDetalhado(int tipo) {
        switch (tipo) {
            case ConnectivityManager.TYPE_WIFI:
                return "WiFi";
            case ConnectivityManager.TYPE_MOBILE:
                return "Dados Móveis";
            case ConnectivityManager.TYPE_ETHERNET:
                return "Ethernet";
            case ConnectivityManager.TYPE_VPN:
                return "VPN";
            default:
                return "Desconhecido (" + tipo + ")";
        }
    }

    // 🔧 MOSTRAR TOAST
    private void mostrarToast(String mensagem) {
        runOnUiThread(() -> Toast.makeText(this, mensagem, Toast.LENGTH_LONG).show());
    }

    // 🔧 FORÇAR SINCRONIZAÇÃO MANUAL
    private void forcarSincronizacaoManual() {
        Log.d(TAG, "🔄 SOLICITAÇÃO DE SINCRONIZAÇÃO MANUAL");

        if (syncManager == null) {
            Log.e(TAG, "❌ SyncManager null - não é possível sincronizar");
            mostrarToast("Erro: Sistema não inicializado corretamente");
            return;
        }

        if (!syncManager.isUsuarioLogado()) {
            Log.w(TAG, "⚠️ Usuário não autenticado");
            mostrarToast("🔐 Faça login como administrador primeiro");
            return;
        }

        if (!isRedeDisponivel()) {
            Log.w(TAG, "⚠️ Sem conexão de rede");
            mostrarToast("🌐 Sem conexão com a internet");
            return;
        }

        try {
            mostrarToast("🔄 Iniciando sincronização...");
            syncManager.sincronizacaoRapida();

            // Verificar resultado após 5 segundos
            new Handler().postDelayed(() -> {
                mostrarToast("📊 Sincronização em andamento...");
                verificarStatusCompleto();
            }, 5000);

        } catch (Exception e) {
            Log.e(TAG, "💥 Erro na sincronização manual: " + e.getMessage());
            mostrarToast("❌ Erro na sincronização");
        }
    }

    // 🔧 VERIFICAR STATUS COMPLETO
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

    // 🔧 MÉTODO PÚBLICO PARA ACTIVITIES FILHAS
    public void sincronizarAposInsercao() {
        Log.d(TAG, "🔄 SINCRONIZAÇÃO PÓS-INSERÇÃO SOLICITADA");

        if (syncManager != null && syncManager.isUsuarioLogado() && syncManager.isAdmin()) {
            new Handler().postDelayed(() -> {
                if (syncManager != null) {
                    syncManager.sincronizacaoRapida();
                    Log.d(TAG, "✅ Sincronização pós-inserção executada");
                }
            }, 1000);
        } else {
            Log.w(TAG, "⚠️ Sincronização pós-inserção ignorada - sem permissão");
        }
    }
}