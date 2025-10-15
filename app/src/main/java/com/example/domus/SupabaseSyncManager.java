package com.example.domus;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.OutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class SupabaseSyncManager {

    private static final String TAG = "SupabaseSync";
    private static final String SUPABASE_URL = "https://wkafwsxydyhkzxbdksve.supabase.co/rest/v1/";
    private static final String SUPABASE_AUTH_URL = "https://wkafwsxydyhkzxbdksve.supabase.co/auth/v1/";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndrYWZ3c3h5ZHloa3p4YmRrc3ZlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTc3MTUwOTIsImV4cCI6MjA3MzI5MTA5Mn0.jkX2ERr9AVasCLg2H_X6rXYEmdRXHlW81SdfH0Uohag";

    private final Context context;
    private final Handler handler;
    private final SharedPreferences sharedPreferences;

    // Chaves para SharedPreferences
    private static final String PREFS_NAME = "DomusPrefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ROLE = "user_role";

    public SupabaseSyncManager(Context context) {
        this.context = context;
        this.handler = new Handler();
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // 🔐 MÉTODOS DE AUTENTICAÇÃO SEGURA

    public void autenticarUsuario(String email, String senha, AuthCallback callback) {
        Log.d(TAG, "🔐 Tentando autenticar: " + email);

        new Thread(() -> {
            try {
                JSONObject credenciais = new JSONObject();
                credenciais.put("email", email);
                credenciais.put("password", senha);

                URL url = new URL(SUPABASE_AUTH_URL + "token?grant_type=password");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("apikey", SUPABASE_ANON_KEY);
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                OutputStream os = conn.getOutputStream();
                os.write(credenciais.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                Log.d(TAG, "🔐 Código de resposta auth: " + responseCode);

                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String accessToken = jsonResponse.getString("access_token");

                    salvarTokenSeguro(accessToken, email);
                    verificarRoleUsuario(accessToken, email, callback);

                } else {
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    errorReader.close();

                    Log.e(TAG, "❌ Erro auth: " + errorResponse.toString());
                    handler.post(() -> callback.onError("Credenciais inválidas - Código: " + responseCode));
                }

                conn.disconnect();

            } catch (Exception e) {
                Log.e(TAG, "💥 Erro auth: " + e.getMessage());
                handler.post(() -> callback.onError("Erro de conexão: " + e.getMessage()));
            }
        }).start();
    }

    // 🔒 SALVAR TOKEN SEGURO
    private void salvarTokenSeguro(String accessToken, String email) {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_ACCESS_TOKEN, accessToken);
            editor.putString(KEY_USER_EMAIL, email);
            editor.apply();

            Log.d(TAG, "✅ Token salvo para: " + email);
        } catch (Exception e) {
            Log.e(TAG, "💥 Erro ao salvar token: " + e.getMessage());
        }
    }

    // 🔍 VERIFICAR ROLE DO USUÁRIO
    private void verificarRoleUsuario(String accessToken, String email, AuthCallback callback) {
        new Thread(() -> {
            try {
                URL urlAdmin = new URL(SUPABASE_URL + "usuarios_admin?usuario=eq." + email + "&select=tipo");
                HttpURLConnection connAdmin = criarConexaoAutenticada(urlAdmin, accessToken);
                connAdmin.setRequestMethod("GET");

                int responseCodeAdmin = connAdmin.getResponseCode();
                String userRole = "morador";

                if (responseCodeAdmin == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connAdmin.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        response.append(line);
                    }
                    in.close();

                    JSONArray jsonArray = new JSONArray(response.toString());
                    if (jsonArray.length() > 0) {
                        userRole = jsonArray.getJSONObject(0).getString("tipo");
                        Log.d(TAG, "👤 Role encontrado: " + userRole);
                    }
                } else {
                    Log.w(TAG, "⚠️ Usuário não é admin no Supabase");
                }
                connAdmin.disconnect();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(KEY_USER_ROLE, userRole);
                editor.apply();

                Log.d(TAG, "✅ Autenticado: " + email + " | Role: " + userRole);
                handler.post(() -> callback.onSuccess(accessToken));

            } catch (Exception e) {
                Log.e(TAG, "💥 Erro ao verificar role: " + e.getMessage());
                handler.post(() -> callback.onError("Erro ao verificar permissões"));
            }
        }).start();
    }

    // 📱 GERENCIAMENTO DE SESSÃO
    public boolean isUsuarioLogado() {
        boolean logado = sharedPreferences.contains(KEY_ACCESS_TOKEN);
        Log.d(TAG, "🔐 Usuário logado: " + logado);
        return logado;
    }

    public String getUsuarioEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, "");
    }

    public String getUserRole() {
        return sharedPreferences.getString(KEY_USER_ROLE, "morador");
    }

    public boolean isAdmin() {
        String role = getUserRole();
        boolean admin = "admin".equals(role) || "superadmin".equals(role);
        Log.d(TAG, "👑 É admin: " + admin + " (" + role + ")");
        return admin;
    }

    public void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_ACCESS_TOKEN);
        editor.remove(KEY_USER_EMAIL);
        editor.remove(KEY_USER_ROLE);
        editor.apply();
        Log.d(TAG, "✅ Usuário deslogado");
    }

    public String getAccessToken() {
        String token = sharedPreferences.getString(KEY_ACCESS_TOKEN, "");
        Log.d(TAG, "🔑 Token disponível: " + (!token.isEmpty()));
        return token;
    }

    // 🌐 VERIFICAR CONECTIVIDADE
    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            boolean disponivel = networkInfo != null && networkInfo.isConnected();
            Log.d(TAG, "🌐 Rede disponível: " + disponivel);
            return disponivel;
        } catch (Exception e) {
            Log.e(TAG, "💥 Erro ao verificar rede: " + e.getMessage());
            return false;
        }
    }

    // 🐛 MÉTODO DE DEBUG DETALHADO
    public void debugSincronizacaoCompleta() {
        Log.d(TAG, "🐛 INICIANDO DEBUG DE SINCRONIZAÇÃO");

        new Thread(() -> {
            try {
                // 1. Verificar autenticação
                Log.d(TAG, "🔐 Status Autenticação: " + isUsuarioLogado());
                Log.d(TAG, "👤 Usuário: " + getUsuarioEmail());
                Log.d(TAG, "👑 Role: " + getUserRole());
                Log.d(TAG, "🔑 Token: " + (getAccessToken().isEmpty() ? "VAZIO" : "PRESENTE"));

                // 2. Verificar rede
                Log.d(TAG, "🌐 Rede disponível: " + isNetworkAvailable());

                // 3. Testar conexão com Supabase
                testarConexaoDetalhada();

                // 4. Verificar dados locais
                verificarDadosLocais();

                // 5. Testar inserção simples
                testarInsercaoSimples();

            } catch (Exception e) {
                Log.e(TAG, "💥 Erro no debug: " + e.getMessage());
            }
        }).start();
    }

    // 🧪 TESTE DE CONEXÃO DETALHADO
    private void testarConexaoDetalhada() {
        Log.d(TAG, "🧪 TESTANDO CONEXÃO DETALHADA");

        try {
            String urlCompleta = SUPABASE_URL + "moradores?select=count&limit=1";
            HttpURLConnection connection = criarConexaoGET(urlCompleta);

            int responseCode = connection.getResponseCode();
            Log.d(TAG, "📡 Código de resposta: " + responseCode);

            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                Log.d(TAG, "✅ Resposta do Supabase: " + response.toString());
            } else {
                // Ler erro detalhado
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                errorReader.close();
                Log.e(TAG, "❌ Erro detalhado: " + errorResponse.toString());
            }

            connection.disconnect();

        } catch (Exception e) {
            Log.e(TAG, "💥 Erro no teste de conexão: " + e.getMessage());
        }
    }

    // 📊 VERIFICAR DADOS LOCAIS
    private void verificarDadosLocais() {
        Log.d(TAG, "📊 VERIFICANDO DADOS LOCAIS");

        try {
            BDCondominioHelper dbHelper = new BDCondominioHelper(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            // Verificar moradores
            Cursor cursorMoradores = db.query(BDCondominioHelper.TABELA_MORADORES, null, null, null, null, null, null);
            int countMoradores = cursorMoradores.getCount();
            Log.d(TAG, "👤 Moradores locais: " + countMoradores);

            // Mostrar alguns dados de exemplo
            if (cursorMoradores.moveToFirst()) {
                do {
                    String nome = cursorMoradores.getString(cursorMoradores.getColumnIndex(BDCondominioHelper.COL_NOME));
                    String cpf = cursorMoradores.getString(cursorMoradores.getColumnIndex(BDCondominioHelper.COL_CPF));
                    Log.d(TAG, "   - " + nome + " | CPF: " + cpf);
                } while (cursorMoradores.moveToNext() && cursorMoradores.getPosition() < 3); // Mostrar apenas 3
            }
            cursorMoradores.close();

            db.close();

        } catch (Exception e) {
            Log.e(TAG, "💥 Erro ao verificar dados locais: " + e.getMessage());
        }
    }

    // 🧪 TESTAR INSERÇÃO SIMPLES
    private void testarInsercaoSimples() {
        Log.d(TAG, "🧪 TESTANDO INSERÇÃO SIMPLES");

        if (!isAdmin()) {
            Log.w(TAG, "⚠️ Pulando teste - usuário não é admin");
            return;
        }

        new Thread(() -> {
            try {
                // Criar um morador de teste
                JSONObject moradorTeste = new JSONObject();
                moradorTeste.put("nome", "TESTE_SINCRONIZACAO");
                moradorTeste.put("email", "teste@dominio.com");
                moradorTeste.put("telefone", "(11) 99999-9999");
                moradorTeste.put("cpf", "999.999.999-99");
                moradorTeste.put("rua", "Rua Teste");
                moradorTeste.put("numero", "123");
                moradorTeste.put("quadra", "A");
                moradorTeste.put("lote", "1");
                moradorTeste.put("created_at", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date()));

                boolean sucesso = inserirNoSupabase("moradores", moradorTeste);

                if (sucesso) {
                    Log.d(TAG, "✅✅✅ TESTE DE INSERÇÃO: SUCESSO!");
                    Log.d(TAG, "📝 Verifique no Supabase se apareceu um morador 'TESTE_SINCRONIZACAO'");
                } else {
                    Log.e(TAG, "❌❌❌ TESTE DE INSERÇÃO: FALHOU!");
                }

            } catch (Exception e) {
                Log.e(TAG, "💥 Erro no teste de inserção: " + e.getMessage());
            }
        }).start();
    }

    // 🔥 SINCRONIZAÇÃO PRINCIPAL - CORRIGIDA
    public void sincronizacaoRapida() {
        Log.d(TAG, "🔄 INICIANDO SINCRONIZAÇÃO RÁPIDA");

        if (!isUsuarioLogado()) {
            Log.e(TAG, "❌ ABORTANDO: Usuário não autenticado");
            return;
        }

        if (!isNetworkAvailable()) {
            Log.e(TAG, "❌ ABORTANDO: Sem conexão de rede");
            return;
        }

        if (!isAdmin()) {
            Log.w(TAG, "⚠️ AVISO: Apenas leitura - usuário não é admin");
            // Moradores podem sincronizar apenas dados públicos
            sincronizarDadosPublicos();
            return;
        }

        new Thread(() -> {
            try {
                Log.d(TAG, "🚀 SINCRONIZANDO COMO ADMIN...");

                sincronizarAdmins();
                Thread.sleep(1000);

                sincronizarMoradores();
                Thread.sleep(1000);

                sincronizarOcorrencias();
                Thread.sleep(1000);

                sincronizarFuncionarios();
                Thread.sleep(1000);

                sincronizarManutencoes();
                Thread.sleep(1000);

                sincronizarAssembleias();
                Thread.sleep(1000);

                sincronizarDespesas();
                Thread.sleep(1000);

                sincronizarAvisos();

                Log.d(TAG, "🎉 SINCRONIZAÇÃO CONCLUÍDA!");

            } catch (InterruptedException e) {
                Log.e(TAG, "💥 Sincronização interrompida: " + e.getMessage());
            }
        }).start();
    }

    // 👥 SINCRONIZAÇÃO PARA MORADORES (APENAS DADOS PÚBLICOS)
    private void sincronizarDadosPublicos() {
        Log.d(TAG, "📖 SINCRONIZANDO DADOS PÚBLICOS PARA MORADOR");

        new Thread(() -> {
            try {
                sincronizarOcorrencias();
                Thread.sleep(1000);

                sincronizarAssembleias();
                Thread.sleep(1000);

                sincronizarAvisos();

                Log.d(TAG, "✅ Dados públicos sincronizados");
            } catch (InterruptedException e) {
                Log.e(TAG, "💥 Sincronização pública interrompida");
            }
        }).start();
    }

    // 🔹 SINCRONIZAR ADMINS - CORRIGIDO
    public void sincronizarAdmins() {
        Log.d(TAG, "🔐 SINCRONIZANDO ADMINS");

        if (!"superadmin".equals(getUserRole())) {
            Log.w(TAG, "⚠️ Apenas superadmin pode sincronizar admins");
            return;
        }

        enviarParaSupabase(BDCondominioHelper.TABELA_USUARIOS_ADMIN,
                new String[]{
                        BDCondominioHelper.COL_ADMIN_USUARIO,
                        BDCondominioHelper.COL_ADMIN_SENHA_HASH,
                        BDCondominioHelper.COL_ADMIN_TIPO,
                        BDCondominioHelper.COL_ADMIN_DATA
                },
                "usuarios_admin");
    }

    // 🔹 SINCRONIZAR MORADORES - CORRIGIDO
    public void sincronizarMoradores() {
        Log.d(TAG, "👤 SINCRONIZANDO MORADORES");

        if (!isAdmin()) {
            Log.w(TAG, "⚠️ Apenas administradores podem sincronizar moradores");
            return;
        }

        enviarParaSupabase(BDCondominioHelper.TABELA_MORADORES,
                new String[]{
                        BDCondominioHelper.COL_COD,
                        BDCondominioHelper.COL_NOME,
                        BDCondominioHelper.COL_CPF,
                        BDCondominioHelper.COL_EMAIL,
                        BDCondominioHelper.COL_RUA,
                        BDCondominioHelper.COL_NUMERO,
                        BDCondominioHelper.COL_TELEFONE,
                        BDCondominioHelper.COL_QUADRA,
                        BDCondominioHelper.COL_LOTE,
                        BDCondominioHelper.COL_IMAGEM_URI
                },
                "moradores");
    }

    // 🔹 SINCRONIZAR OCORRÊNCIAS
    public void sincronizarOcorrencias() {
        Log.d(TAG, "📝 SINCRONIZANDO OCORRÊNCIAS");
        enviarParaSupabase(BDCondominioHelper.TABELA_OCORRENCIAS,
                new String[]{
                        BDCondominioHelper.COL_OCOR_TIPO,
                        BDCondominioHelper.COL_OCOR_ENVOLVIDOS,
                        BDCondominioHelper.COL_OCOR_DESCRICAO,
                        BDCondominioHelper.COL_OCOR_DATAHORA,
                        BDCondominioHelper.COL_OCOR_ANEXOS
                },
                "ocorrencias");
    }

    // 🔹 SINCRONIZAR FUNCIONÁRIOS
    public void sincronizarFuncionarios() {
        Log.d(TAG, "👷 SINCRONIZANDO FUNCIONÁRIOS");

        if (!isAdmin()) {
            Log.w(TAG, "⚠️ Apenas administradores podem sincronizar funcionários");
            return;
        }

        enviarParaSupabase(BDCondominioHelper.TABELA_FUNCIONARIOS,
                new String[]{
                        BDCondominioHelper.COL_FUNC_NOME,
                        BDCondominioHelper.COL_FUNC_RUA,
                        BDCondominioHelper.COL_FUNC_NUMERO,
                        BDCondominioHelper.COL_FUNC_BAIRRO,
                        BDCondominioHelper.COL_FUNC_CEP,
                        BDCondominioHelper.COL_FUNC_CIDADE,
                        BDCondominioHelper.COL_FUNC_ESTADO,
                        BDCondominioHelper.COL_FUNC_PAIS,
                        BDCondominioHelper.COL_FUNC_TELEFONE,
                        BDCondominioHelper.COL_FUNC_EMAIL,
                        BDCondominioHelper.COL_FUNC_RG,
                        BDCondominioHelper.COL_FUNC_CPF,
                        BDCondominioHelper.COL_FUNC_CARGA_MENSAL,
                        BDCondominioHelper.COL_FUNC_TURNO,
                        BDCondominioHelper.COL_FUNC_HORA_ENTRADA,
                        BDCondominioHelper.COL_FUNC_HORA_SAIDA,
                        BDCondominioHelper.COL_FUNC_IMAGEM_URI
                },
                "funcionarios");
    }

    // 🔹 SINCRONIZAR MANUTENÇÕES
    public void sincronizarManutencoes() {
        Log.d(TAG, "🔧 SINCRONIZANDO MANUTENÇÕES");

        if (!isAdmin()) {
            Log.w(TAG, "⚠️ Apenas administradores podem sincronizar manutenções");
            return;
        }

        enviarParaSupabase(BDCondominioHelper.TABELA_MANUTENCOES,
                new String[]{
                        BDCondominioHelper.COL_MANU_TIPO,
                        BDCondominioHelper.COL_MANU_DATAHORA,
                        BDCondominioHelper.COL_MANU_LOCAL,
                        BDCondominioHelper.COL_MANU_SERVICO,
                        BDCondominioHelper.COL_MANU_RESPONSAVEL,
                        BDCondominioHelper.COL_MANU_VALOR,
                        BDCondominioHelper.COL_MANU_NOTAS,
                        BDCondominioHelper.COL_MANU_ANEXOS
                },
                "manutencoes");
    }

    // 🔹 SINCRONIZAR ASSEMBLEIAS
    public void sincronizarAssembleias() {
        Log.d(TAG, "🏛️ SINCRONIZANDO ASSEMBLEIAS");
        enviarParaSupabase(BDCondominioHelper.TABELA_ASSEMBLEIAS,
                new String[]{
                        BDCondominioHelper.COL_ASS_DATAHORA,
                        BDCondominioHelper.COL_ASS_LOCAL,
                        BDCondominioHelper.COL_ASS_ASSUNTO,
                        BDCondominioHelper.COL_ASS_DESCRICAO,
                        BDCondominioHelper.COL_ASS_ANEXOS
                },
                "assembleias");
    }

    // 🔹 SINCRONIZAR DESPESAS
    public void sincronizarDespesas() {
        Log.d(TAG, "💰 SINCRONIZANDO DESPESAS");

        if (!isAdmin()) {
            Log.w(TAG, "⚠️ Apenas administradores podem sincronizar despesas");
            return;
        }

        enviarParaSupabase(BDCondominioHelper.TABELA_DESPESAS,
                new String[]{
                        BDCondominioHelper.COL_DESP_DATAHORA,
                        BDCondominioHelper.COL_DESP_NOME,
                        BDCondominioHelper.COL_DESP_DESCRICAO,
                        BDCondominioHelper.COL_DESP_VALOR,
                        BDCondominioHelper.COL_DESP_ANEXOS
                },
                "despesas");
    }

    // 🔹 SINCRONIZAR AVISOS
    public void sincronizarAvisos() {
        Log.d(TAG, "📢 SINCRONIZANDO AVISOS");
        enviarParaSupabase(BDCondominioHelper.TABELA_AVISOS,
                new String[]{
                        BDCondominioHelper.COL_AVISO_DATAHORA,
                        BDCondominioHelper.COL_AVISO_ASSUNTO,
                        BDCondominioHelper.COL_AVISO_DESCRICAO,
                        BDCondominioHelper.COL_AVISO_ANEXOS,
                        BDCondominioHelper.COL_AVISO_CRIADO_EM
                },
                "avisos");
    }

    // 🔥 MÉTODO PRINCIPAL DE ENVIO - VERSÃO CORRIGIDA
    private void enviarParaSupabase(String tabelaSQLite, String[] colunasSQLite, String tabelaSupabase) {
        Log.d(TAG, "📤 INICIANDO ENVIO: " + tabelaSQLite + " → " + tabelaSupabase);

        // Verificações críticas
        if (!isUsuarioLogado()) {
            Log.e(TAG, "❌ ABORTANDO: Usuário não autenticado");
            return;
        }

        if (!isNetworkAvailable()) {
            Log.e(TAG, "❌ ABORTANDO: Sem conexão de rede");
            return;
        }

        new Thread(() -> {
            try {
                BDCondominioHelper dbHelper = new BDCondominioHelper(context);
                SQLiteDatabase db = dbHelper.getReadableDatabase();

                Cursor cursor = db.query(tabelaSQLite, colunasSQLite, null, null, null, null, null);
                JSONArray jsonArray = new JSONArray();

                int totalRegistros = 0;
                int registrosProcessados = 0;

                Log.d(TAG, "📊 Lendo dados locais de: " + tabelaSQLite);

                while (cursor.moveToNext()) {
                    JSONObject obj = new JSONObject();
                    boolean temDadosValidos = false;

                    for (int i = 0; i < colunasSQLite.length; i++) {
                        String colunaSQLite = colunasSQLite[i];
                        int columnIndex = cursor.getColumnIndex(colunaSQLite);

                        if (columnIndex == -1) {
                            Log.w(TAG, "⚠️ Coluna não encontrada: " + colunaSQLite);
                            continue;
                        }

                        String valor = cursor.getString(columnIndex);

                        // Mapeamento correto de colunas SQLite → Supabase
                        String colunaSupabase = mapearColunaParaSupabase(colunaSQLite);

                        if (valor != null && !valor.trim().isEmpty()) {
                            obj.put(colunaSupabase, valor);
                            temDadosValidos = true;
                        } else {
                            // Para campos obrigatórios no Supabase, usar valor padrão
                            obj.put(colunaSupabase, obterValorPadrao(colunaSupabase));
                        }
                    }

                    if (temDadosValidos) {
                        jsonArray.put(obj);
                        registrosProcessados++;
                        Log.d(TAG, "📝 Registro " + registrosProcessados + ": " + obj.toString());
                    }
                    totalRegistros++;
                }

                cursor.close();
                db.close();

                Log.d(TAG, "📦 Dados preparados: " + registrosProcessados + "/" + totalRegistros + " registros válidos");

                if (jsonArray.length() == 0) {
                    Log.w(TAG, "⚠️ Nenhum registro válido para enviar: " + tabelaSQLite);
                    return;
                }

                // Enviar TODOS os registros de uma vez (Supabase aceita até 1MB)
                boolean sucesso = enviarLoteParaSupabase(tabelaSupabase, jsonArray);

                if (sucesso) {
                    Log.d(TAG, "✅✅✅ TODOS os " + jsonArray.length() + " registros enviados para: " + tabelaSupabase);
                } else {
                    Log.e(TAG, "❌❌❌ FALHA no envio para: " + tabelaSupabase);
                }

            } catch (Exception e) {
                Log.e(TAG, "💥 ERRO CRÍTICO ao enviar " + tabelaSQLite + ": " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    // 🎯 OBTER VALOR PADRÃO PARA CAMPOS OBRIGATÓRIOS
    private String obterValorPadrao(String colunaSupabase) {
        switch (colunaSupabase) {
            case "nome":
                return "Sem Nome";
            case "email":
                return "sem@email.com";
            case "cpf":
                return "000.000.000-00";
            case "created_at":
                return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date());
            default:
                return "";
        }
    }

    // 🗺️ MAPEAMENTO DE COLUNAS SQLite → Supabase
    private String mapearColunaParaSupabase(String colunaSQLite) {
        // Mapeamento específico para evitar problemas de nomenclatura
        switch (colunaSQLite) {
            case BDCondominioHelper.COL_ADMIN_USUARIO:
                return "usuario";
            case BDCondominioHelper.COL_ADMIN_SENHA_HASH:
                return "senha_hash";
            case BDCondominioHelper.COL_ADMIN_DATA:
                return "data_cadastro";
            case BDCondominioHelper.COL_COD:
                return "codigo";
            case BDCondominioHelper.COL_IMAGEM_URI:
                return "imagem_uri";
            case BDCondominioHelper.COL_AVISO_CRIADO_EM:
                return "criado_em";
            default:
                // Para a maioria das colunas, o nome é o mesmo
                return colunaSQLite;
        }
    }

    // 📦 ENVIO DE LOTE - CORRIGIDO (MÉTODO QUE ESTAVA FALTANDO)
    private boolean enviarLoteParaSupabase(String tabelaSupabase, JSONArray batch) {
        Log.d(TAG, "📦 Enviando lote de " + batch.length() + " registros para " + tabelaSupabase);

        try {
            URL url = new URL(SUPABASE_URL + tabelaSupabase);
            HttpURLConnection conn = criarConexaoAutenticada(url, getAccessToken());
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Prefer", "return=minimal");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000); // Aumentado timeout
            conn.setReadTimeout(30000);

            String jsonData = batch.toString();
            Log.d(TAG, "📄 JSON a ser enviado: " + jsonData);

            OutputStream os = conn.getOutputStream();
            os.write(jsonData.getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            Log.d(TAG, "📡 Resposta do Supabase: " + responseCode);

            if (responseCode == 201 || responseCode == 200) {
                Log.d(TAG, "✅ Lote enviado com sucesso!");
                conn.disconnect();
                return true;
            } else {
                Log.e(TAG, "❌ Falha no envio. Código: " + responseCode);

                // Ler detalhes do erro
                try {
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                    StringBuilder errorResponse = new StringBuilder();
                    String line;
                    while ((line = errorReader.readLine()) != null) {
                        errorResponse.append(line);
                    }
                    errorReader.close();
                    Log.e(TAG, "📋 Detalhes do erro: " + errorResponse.toString());
                } catch (Exception e) {
                    Log.e(TAG, "💥 Não foi possível ler erro detalhado: " + e.getMessage());
                }

                conn.disconnect();
                return false;
            }

        } catch (Exception e) {
            Log.e(TAG, "💥 Erro no envio do lote: " + e.getMessage());
            return false;
        }
    }

    // ➕ INSERIR INDIVIDUAL - VERSÃO CORRIGIDA
    private boolean inserirNoSupabase(String tabela, JSONObject dados) {
        Log.d(TAG, "➕ INSERINDO em " + tabela + ": " + dados.toString());

        try {
            // Adicionar timestamp se não existir
            if (!dados.has("created_at")) {
                dados.put("created_at", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date()));
            }

            URL url = new URL(SUPABASE_URL + tabela);
            HttpURLConnection conn = criarConexaoAutenticada(url, getAccessToken());
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Prefer", "return=representation"); // Mudado para ver resposta
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);

            String jsonData = dados.toString();
            Log.d(TAG, "📄 JSON enviado: " + jsonData);

            OutputStream os = conn.getOutputStream();
            os.write(jsonData.getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            Log.d(TAG, "📡 Resposta inserção: " + responseCode);

            // Ler resposta para debug
            if (responseCode == 201) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();
                Log.d(TAG, "✅ Resposta detalhada: " + response.toString());
            } else {
                BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                StringBuilder errorResponse = new StringBuilder();
                String line;
                while ((line = errorReader.readLine()) != null) {
                    errorResponse.append(line);
                }
                errorReader.close();
                Log.e(TAG, "❌ Erro detalhado: " + errorResponse.toString());
            }

            conn.disconnect();

            boolean sucesso = (responseCode == 201);
            Log.d(TAG, sucesso ? "✅ Inserção bem-sucedida" : "❌ Falha na inserção");

            return sucesso;

        } catch (Exception e) {
            Log.e(TAG, "💥 Erro na inserção: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // 🔧 MÉTODO ATUALIZAR MORADOR - ADICIONADO
    public void atualizarMoradorSupabase(int id, String nome, String email, String telefone,
                                         String cpf, String rua, String numero,
                                         String quadra, String lote) {
        Log.d(TAG, "🔄 ATUALIZANDO MORADOR NO SUPABASE - ID: " + id);

        if (!isAdmin()) {
            Log.w(TAG, "⚠️ Apenas administradores podem atualizar moradores");
            return;
        }

        new Thread(() -> {
            try {
                JSONObject moradorData = new JSONObject();
                moradorData.put("nome", nome);
                moradorData.put("email", email != null ? email : "");
                moradorData.put("telefone", telefone != null ? telefone : "");
                moradorData.put("cpf", cpf != null ? cpf : "");
                moradorData.put("rua", rua != null ? rua : "");
                moradorData.put("numero", numero != null ? numero : "");
                moradorData.put("quadra", quadra != null ? quadra : "");
                moradorData.put("lote", lote != null ? lote : "");
                moradorData.put("updated_at", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date()));

                boolean sucesso = atualizarNoSupabase("moradores", id, moradorData);
                if (sucesso) {
                    Log.d(TAG, "✅✅✅ MORADOR ATUALIZADO: " + nome + " (ID: " + id + ")");
                } else {
                    Log.e(TAG, "❌ FALHA ao atualizar morador: " + nome);
                }
            } catch (Exception e) {
                Log.e(TAG, "💥 Erro ao atualizar morador: " + e.getMessage());
            }
        }).start();
    }

    // 🗑️ MÉTODO EXCLUIR MORADOR - ADICIONADO
    public void excluirMoradorSupabase(String cpf) {
        Log.d(TAG, "🗑️ EXCLUINDO MORADOR NO SUPABASE - CPF: " + cpf);

        if (!isAdmin()) {
            Log.w(TAG, "⚠️ Apenas administradores podem excluir moradores");
            return;
        }

        new Thread(() -> {
            try {
                boolean sucesso = excluirNoSupabase("moradores", "cpf", cpf);
                if (sucesso) {
                    Log.d(TAG, "✅✅✅ MORADOR EXCLUÍDO: CPF " + cpf);
                } else {
                    Log.e(TAG, "❌ FALHA ao excluir morador: CPF " + cpf);
                }
            } catch (Exception e) {
                Log.e(TAG, "💥 Erro ao excluir morador: " + e.getMessage());
            }
        }).start();
    }

    // 🗑️ MÉTODO EXCLUIR POR ID - ADICIONADO
    public void excluirMoradorPorIdSupabase(int id) {
        Log.d(TAG, "🗑️ EXCLUINDO MORADOR NO SUPABASE - ID: " + id);

        if (!isAdmin()) {
            Log.w(TAG, "⚠️ Apenas administradores podem excluir moradores");
            return;
        }

        new Thread(() -> {
            try {
                boolean sucesso = excluirNoSupabase("moradores", "id", String.valueOf(id));
                if (sucesso) {
                    Log.d(TAG, "✅✅✅ MORADOR EXCLUÍDO: ID " + id);
                } else {
                    Log.e(TAG, "❌ FALHA ao excluir morador: ID " + id);
                }
            } catch (Exception e) {
                Log.e(TAG, "💥 Erro ao excluir morador: " + e.getMessage());
            }
        }).start();
    }

    // 🔧 MÉTODO ATUALIZAR NO SUPABASE - ADICIONADO
    private boolean atualizarNoSupabase(String tabela, int id, JSONObject dados) {
        Log.d(TAG, "🔄 ATUALIZANDO " + tabela + " ID: " + id);

        try {
            URL url = new URL(SUPABASE_URL + tabela + "?id=eq." + id);
            HttpURLConnection conn = criarConexaoAutenticada(url, getAccessToken());
            conn.setRequestMethod("PATCH");
            conn.setRequestProperty("Prefer", "return=minimal");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setDoOutput(true);
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);

            String jsonData = dados.toString();
            Log.d(TAG, "📄 JSON para atualização: " + jsonData);

            OutputStream os = conn.getOutputStream();
            os.write(jsonData.getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            Log.d(TAG, "📡 Resposta atualização: " + responseCode);

            boolean sucesso = (responseCode == 200 || responseCode == 204);
            Log.d(TAG, sucesso ? "✅ Atualização bem-sucedida" : "❌ Falha na atualização");

            conn.disconnect();
            return sucesso;

        } catch (Exception e) {
            Log.e(TAG, "💥 Erro na atualização: " + e.getMessage());
            return false;
        }
    }

    // 🗑️ MÉTODO EXCLUIR NO SUPABASE - ADICIONADO
    private boolean excluirNoSupabase(String tabela, String coluna, String valor) {
        Log.d(TAG, "🗑️ EXCLUINDO " + tabela + " WHERE " + coluna + " = " + valor);

        try {
            URL url = new URL(SUPABASE_URL + tabela + "?" + coluna + "=eq." + valor);
            HttpURLConnection conn = criarConexaoAutenticada(url, getAccessToken());
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Prefer", "return=minimal");
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);

            int responseCode = conn.getResponseCode();
            Log.d(TAG, "📡 Resposta exclusão: " + responseCode);

            boolean sucesso = (responseCode == 200 || responseCode == 204);
            Log.d(TAG, sucesso ? "✅ Exclusão bem-sucedida" : "❌ Falha na exclusão");

            conn.disconnect();
            return sucesso;

        } catch (Exception e) {
            Log.e(TAG, "💥 Erro na exclusão: " + e.getMessage());
            return false;
        }
    }

    public void sincronizarAdminEspecifico(String usuario, String senhaHash, String tipo) {
        Log.d(TAG, "🔐 SINCRONIZANDO ADMIN ESPECÍFICO: " + usuario);

        new Thread(() -> {
            try {
                JSONObject admin = new JSONObject();
                admin.put("usuario", usuario);
                admin.put("senha_hash", senhaHash);
                admin.put("tipo", tipo);
                admin.put("data_cadastro", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

                boolean sucesso = inserirNoSupabase("usuarios_admin", admin);
                if (sucesso) {
                    Log.d(TAG, "✅✅✅ ADMIN SINCRONIZADO: " + usuario);
                } else {
                    Log.e(TAG, "❌ FALHA ao sincronizar admin: " + usuario);
                }
            } catch (Exception e) {
                Log.e(TAG, "💥 Erro ao sincronizar admin: " + e.getMessage());
            }
        }).start();
    }

    // 🔍 MÉTODOS AUXILIARES
    private boolean moradorExisteNoSupabase(String cpf, String email) {
        try {
            String urlCompleta = SUPABASE_URL + "moradores?or=(cpf.eq." + cpf + ",email.eq." + email + ")&select=id";
            HttpURLConnection connection = criarConexaoGET(urlCompleta);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                JSONArray jsonArray = new JSONArray(response.toString());
                return jsonArray.length() > 0;
            }
            connection.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "💥 Erro ao verificar morador: " + e.getMessage());
        }
        return false;
    }

    private String buscarIdMoradorNoSupabase(String cpf) {
        try {
            String urlCompleta = SUPABASE_URL + "moradores?cpf=eq." + cpf + "&select=id";
            HttpURLConnection connection = criarConexaoGET(urlCompleta);

            int responseCode = connection.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) {
                    response.append(line);
                }
                in.close();

                JSONArray jsonArray = new JSONArray(response.toString());
                if (jsonArray.length() > 0) {
                    return jsonArray.getJSONObject(0).getString("id");
                }
            }
            connection.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "💥 Erro ao buscar ID: " + e.getMessage());
        }
        return null;
    }

    // 🔌 CONEXÕES
    private HttpURLConnection criarConexaoGET(String urlCompleta) throws Exception {
        URL url = new URL(urlCompleta);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("apikey", SUPABASE_ANON_KEY);
        connection.setRequestProperty("Authorization", "Bearer " + SUPABASE_ANON_KEY);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(15000);
        return connection;
    }

    private HttpURLConnection criarConexaoAutenticada(URL url, String accessToken) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("apikey", SUPABASE_ANON_KEY);
        conn.setRequestProperty("Authorization", "Bearer " + accessToken);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(30000);
        return conn;
    }

    // 🧪 TESTE DE CONEXÃO
    public void testarConexao() {
        Log.d(TAG, "🧪 TESTANDO CONEXÃO COM SUPABASE");

        new Thread(() -> {
            try {
                String urlCompleta = SUPABASE_URL + "moradores?select=count&limit=1";
                HttpURLConnection connection = criarConexaoGET(urlCompleta);

                int responseCode = connection.getResponseCode();
                if (responseCode == 200) {
                    Log.d(TAG, "✅✅✅ CONEXÃO COM SUPABASE: OK! ✅✅✅");
                } else {
                    Log.e(TAG, "❌❌❌ CONEXÃO COM SUPABASE: FALHA! Código: " + responseCode);
                }

                connection.disconnect();

            } catch (Exception e) {
                Log.e(TAG, "💥 ERRO NA CONEXÃO: " + e.getMessage());
            }
        }).start();
    }

    // 🔄 SINCRONIZAÇÃO COMPLETA
    public void sincronizarTudo() {
        Log.d(TAG, "🔄 INICIANDO SINCRONIZAÇÃO COMPLETA");
        sincronizacaoRapida();
    }

    // 🔧 SINCRONIZAÇÃO DE TABELA ESPECÍFICA
    public void sincronizarTabelaEspecifica(String tabelaSQLite, String tabelaSupabase) {
        Log.d(TAG, "🎯 SINCRONIZANDO TABELA ESPECÍFICA: " + tabelaSQLite);

        if (!isUsuarioLogado()) {
            Log.e(TAG, "❌ Usuário não autenticado");
            return;
        }

        switch (tabelaSQLite) {
            case BDCondominioHelper.TABELA_USUARIOS_ADMIN:
                sincronizarAdmins();
                break;
            case BDCondominioHelper.TABELA_MORADORES:
                sincronizarMoradores();
                break;
            case BDCondominioHelper.TABELA_OCORRENCIAS:
                sincronizarOcorrencias();
                break;
            case BDCondominioHelper.TABELA_FUNCIONARIOS:
                sincronizarFuncionarios();
                break;
            case BDCondominioHelper.TABELA_MANUTENCOES:
                sincronizarManutencoes();
                break;
            case BDCondominioHelper.TABELA_ASSEMBLEIAS:
                sincronizarAssembleias();
                break;
            case BDCondominioHelper.TABELA_DESPESAS:
                sincronizarDespesas();
                break;
            case BDCondominioHelper.TABELA_AVISOS:
                sincronizarAvisos();
                break;
            default:
                Log.e(TAG, "❌ Tabela não reconhecida: " + tabelaSQLite);
        }
    }

    // 👤 SINCRONIZAR MORADOR ESPECÍFICO - MÉTODO QUE ESTAVA FALTANDO
    public void sincronizarMoradorEspecifico(String nome, String email, String telefone, String cpf,
                                             String rua, String numero, String quadra, String lote) {
        Log.d(TAG, "👤 SINCRONIZANDO MORADOR ESPECÍFICO: " + nome);

        if (!isAdmin()) {
            Log.w(TAG, "⚠️ Apenas administradores podem adicionar moradores");
            return;
        }

        new Thread(() -> {
            try {
                JSONObject morador = new JSONObject();
                morador.put("nome", nome);
                morador.put("email", email != null ? email : "");
                morador.put("telefone", telefone != null ? telefone : "");
                morador.put("cpf", cpf != null ? cpf : "");
                morador.put("rua", rua != null ? rua : "");
                morador.put("numero", numero != null ? numero : "");
                morador.put("quadra", quadra != null ? quadra : "");
                morador.put("lote", lote != null ? lote : "");
                morador.put("created_at", new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).format(new Date()));

                boolean sucesso = inserirNoSupabase("moradores", morador);
                if (sucesso) {
                    Log.d(TAG, "✅✅✅ MORADOR SINCRONIZADO: " + nome);
                } else {
                    Log.e(TAG, "❌ FALHA ao sincronizar morador: " + nome);
                }
            } catch (Exception e) {
                Log.e(TAG, "💥 Erro ao sincronizar morador: " + e.getMessage());
            }
        }).start();
    }

    // 🧪 TESTE COMPLETO
    public void testeCompleto() {
        if (!isUsuarioLogado()) {
            Log.e(TAG, "❌ Usuário não autenticado para teste");
            return;
        }
        Log.d(TAG, "🧪 INICIANDO TESTE COMPLETO");
        sincronizacaoRapida();
    }

    // 📞 CALLBACK INTERFACE
    public interface AuthCallback {
        void onSuccess(String accessToken);
        void onError(String error);
    }
}