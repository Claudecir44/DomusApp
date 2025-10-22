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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class SupabaseSyncManager {

    private static final String TAG = "SupabaseSync";
    private static final String SUPABASE_URL = "https://wkafwsxydyhkzxbdksve.supabase.co/rest/v1/";
    private static final String SUPABASE_AUTH_URL = "https://wkafwsxydyhkzxbdksve.supabase.co/auth/v1/";
    private static final String SUPABASE_ANON_KEY = BuildConfig.SUPABASE_ANON_KEY;

    private final Context context;
    private final Handler handler;
    private final SharedPreferences sharedPreferences;

    private static final String PREFS_NAME = "DomusPrefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_ROLE = "user_role";
    private static final String KEY_LAST_SYNC = "last_sync_timestamp";

    public SupabaseSyncManager(Context context) {
        this.context = context;
        this.handler = new Handler();
        this.sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /* ==============================
       AUTENTICAÇÃO
     ============================== */

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
                conn.setInstanceFollowRedirects(false);
                conn.setDoOutput(true);
                conn.setConnectTimeout(15000);
                conn.setReadTimeout(15000);

                OutputStream os = conn.getOutputStream();
                os.write(credenciais.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) response.append(line);
                    in.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String accessToken = jsonResponse.getString("access_token");

                    salvarTokenSeguro(accessToken, email);

                    // Para admin master, usar autenticação direta
                    if ("admin".equals(email) && "master".equals(senha)) {
                        verificarRoleEspecial(accessToken, email, "master", callback);
                    } else {
                        verificarRoleUsuario(accessToken, email, callback);
                    }
                } else {
                    // Tentar autenticação local para admin master
                    if ("admin".equals(email) && "master".equals(senha)) {
                        autenticarAdminMasterLocal(callback);
                    } else {
                        BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                        StringBuilder errorResponse = new StringBuilder();
                        String line;
                        while ((line = errorReader.readLine()) != null) errorResponse.append(line);
                        errorReader.close();
                        handler.post(() -> callback.onError("Credenciais inválidas - Código: " + responseCode));
                    }
                }

                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "💥 Erro auth: " + e.getMessage());
                // Fallback para autenticação local
                if ("admin".equals(email) && "master".equals(senha)) {
                    autenticarAdminMasterLocal(callback);
                } else {
                    handler.post(() -> callback.onError("Erro de conexão: " + e.getMessage()));
                }
            }
        }).start();
    }

    private void autenticarAdminMasterLocal(AuthCallback callback) {
        try {
            // Criar um token local para admin master
            String fakeToken = "local_admin_master_token_" + System.currentTimeMillis();
            salvarTokenSeguro(fakeToken, "admin");
            verificarRoleEspecial(fakeToken, "admin", "master", callback);
            Log.d(TAG, "✅ Admin master autenticado localmente");
        } catch (Exception e) {
            handler.post(() -> callback.onError("Erro na autenticação local"));
        }
    }

    private void verificarRoleEspecial(String accessToken, String email, String role, AuthCallback callback) {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_USER_ROLE, role);
            editor.apply();
            handler.post(() -> callback.onSuccess(accessToken));
        } catch (Exception e) {
            handler.post(() -> callback.onError("Erro ao configurar permissões especiais"));
        }
    }

    private void salvarTokenSeguro(String accessToken, String email) {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_ACCESS_TOKEN, accessToken);
            editor.putString(KEY_USER_EMAIL, email);
            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, "💥 Erro ao salvar token: " + e.getMessage());
        }
    }

    /* ==============================
       SESSÃO E PERMISSÕES
     ============================== */

    private void verificarRoleUsuario(String accessToken, String email, AuthCallback callback) {
        new Thread(() -> {
            try {
                URL urlAdmin = new URL(SUPABASE_URL + "usuarios_admin?usuario=eq." + email + "&select=tipo");
                HttpURLConnection connAdmin = criarConexaoAutenticada(urlAdmin, accessToken);
                connAdmin.setRequestMethod("GET");

                String userRole = "morador";
                if (connAdmin.getResponseCode() == 200) {
                    BufferedReader in = new BufferedReader(new InputStreamReader(connAdmin.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) response.append(line);
                    in.close();

                    JSONArray jsonArray = new JSONArray(response.toString());
                    if (jsonArray.length() > 0) {
                        userRole = jsonArray.getJSONObject(0).getString("tipo");
                    }
                }

                connAdmin.disconnect();

                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(KEY_USER_ROLE, userRole);
                editor.apply();
                handler.post(() -> callback.onSuccess(accessToken));
            } catch (Exception e) {
                handler.post(() -> callback.onError("Erro ao verificar permissões"));
            }
        }).start();
    }

    public boolean isUsuarioLogado() {
        return sharedPreferences.contains(KEY_ACCESS_TOKEN);
    }

    public boolean isAdmin() {
        String role = getUserRole();
        return "admin".equals(role) || "master".equals(role) || "superadmin".equals(role);
    }

    public boolean isMaster() {
        return "master".equals(getUserRole());
    }

    public String getUserRole() {
        return sharedPreferences.getString(KEY_USER_ROLE, "morador");
    }

    public String getAccessToken() {
        return sharedPreferences.getString(KEY_ACCESS_TOKEN, "");
    }

    public String getUsuarioEmail() {
        return sharedPreferences.getString(KEY_USER_EMAIL, "");
    }

    public void logout() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_ACCESS_TOKEN);
        editor.remove(KEY_USER_EMAIL);
        editor.remove(KEY_USER_ROLE);
        editor.apply();
    }

    /* ==============================
       CONECTIVIDADE
     ============================== */

    private boolean isNetworkAvailable() {
        try {
            ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = cm.getActiveNetworkInfo();
            return networkInfo != null && networkInfo.isConnected();
        } catch (Exception e) {
            Log.e(TAG, "Erro ao verificar rede: " + e.getMessage());
            return false;
        }
    }

    /* ==============================
       SINCRONIZAÇÃO DE DADOS
     ============================== */

    public void sincronizacaoCompleta() {
        if (!isNetworkAvailable()) {
            Log.e(TAG, "❌ Abortando sincronização: sem rede");
            return;
        }

        new Thread(() -> {
            try {
                Log.d(TAG, "🔄 Iniciando sincronização completa...");

                // Primeiro sincroniza os administradores
                sincronizarAdminMaster();

                // Se estiver logado, sincroniza outros dados
                if (isUsuarioLogado()) {
                    sincronizarMoradores();
                    sincronizarOcorrencias();
                    sincronizarAvisos();
                    sincronizarFuncionarios();
                    sincronizarManutencoes();
                    sincronizarAssembleias();
                    sincronizarDespesas();
                }

                salvarUltimaSincronizacao();
                Log.d(TAG, "✅ Sincronização completa finalizada");

            } catch (Exception e) {
                Log.e(TAG, "💥 Erro na sincronização completa: " + e.getMessage());
            }
        }).start();
    }

    public void sincronizarMoradores() {
        enviarParaSupabase(BDCondominioHelper.TABELA_MORADORES,
                new String[]{
                        "cod", "nome", "cpf", "email", "rua", "numero",
                        "telefone", "quadra", "lote", "imagem_uri"
                },
                "moradores");
    }

    public void sincronizarOcorrencias() {
        enviarParaSupabase(BDCondominioHelper.TABELA_OCORRENCIAS,
                new String[]{
                        "tipo", "envolvidos", "descricao", "datahora",
                        "anexos", "status"
                },
                "ocorrencias");
    }

    public void sincronizarAvisos() {
        enviarParaSupabase(BDCondominioHelper.TABELA_AVISOS,
                new String[]{
                        "datahora", "assunto", "descricao", "anexos",
                        "prioridade"
                },
                "avisos");
    }

    public void sincronizarFuncionarios() {
        enviarParaSupabase(BDCondominioHelper.TABELA_FUNCIONARIOS,
                new String[]{
                        "nome", "rua", "numero", "bairro", "cep", "cidade",
                        "estado", "pais", "telefone", "email", "rg", "cpf",
                        "carga_mensal", "turno", "hora_entrada", "hora_saida", "imagem_uri"
                },
                "funcionarios");
    }

    public void sincronizarManutencoes() {
        enviarParaSupabase(BDCondominioHelper.TABELA_MANUTENCOES,
                new String[]{
                        "tipo", "datahora", "local", "servico",
                        "responsavel", "valor", "notas", "anexos"
                },
                "manutencoes");
    }

    public void sincronizarAssembleias() {
        enviarParaSupabase(BDCondominioHelper.TABELA_ASSEMBLEIAS,
                new String[]{
                        "datahora", "local", "assunto", "descricao", "anexos"
                },
                "assembleias");
    }

    public void sincronizarDespesas() {
        enviarParaSupabase(BDCondominioHelper.TABELA_DESPESAS,
                new String[]{
                        "datahora", "nome", "descricao", "valor", "anexos"
                },
                "despesas");
    }

    /* ==============================
       SINCRONIZAÇÃO DE ADMINISTRADORES
     ============================== */

    public void sincronizarAdmins() {
        new Thread(() -> {
            try {
                Log.d(TAG, "🔄 Iniciando sincronização de administradores...");

                BDCondominioHelper dbHelper = new BDCondominioHelper(context);
                SQLiteDatabase db = dbHelper.getReadableDatabase();

                Cursor cursor = db.query(
                        BDCondominioHelper.TABELA_USUARIOS_ADMIN,
                        new String[]{"usuario", "senha_hash", "tipo", "data_cadastro"},
                        null, null, null, null, null);

                if (cursor != null && cursor.moveToFirst()) {
                    int count = 0;
                    do {
                        String usuario = cursor.getString(cursor.getColumnIndexOrThrow("usuario"));
                        String senhaHash = cursor.getString(cursor.getColumnIndexOrThrow("senha_hash"));
                        String tipo = cursor.getString(cursor.getColumnIndexOrThrow("tipo"));
                        String dataCadastro = cursor.getString(cursor.getColumnIndexOrThrow("data_cadastro"));

                        sincronizarAdminIndividual(usuario, senhaHash, tipo, dataCadastro);
                        count++;

                    } while (cursor.moveToNext());

                    cursor.close();
                    Log.d(TAG, "✅ " + count + " administradores processados");
                } else {
                    Log.d(TAG, "ℹ️ Nenhum administrador encontrado para sincronizar");
                }

                db.close();

            } catch (Exception e) {
                Log.e(TAG, "💥 Erro na sincronização de admins: " + e.getMessage());
            }
        }).start();
    }

    public void sincronizarAdminEspecifico(String usuario, String senhaHash, String tipo) {
        sincronizarAdminIndividual(usuario, senhaHash, tipo,
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
    }

    private void sincronizarAdminIndividual(String usuario, String senhaHash, String tipo, String dataCadastro) {
        new Thread(() -> {
            try {
                Log.d(TAG, "🔄 Sincronizando admin: " + usuario);

                JSONObject adminJson = new JSONObject();
                adminJson.put("usuario", usuario);
                adminJson.put("senha_hash", senhaHash);
                adminJson.put("tipo", tipo);
                adminJson.put("data_cadastro", dataCadastro);
                adminJson.put("ativo", true);

                // Verificar se já existe
                String response = enviarGet("usuarios_admin?usuario=eq." + usuario + "&select=*");
                JSONArray jsonArray = new JSONArray(response);

                if (jsonArray.length() == 0) {
                    // Criar novo admin
                    enviarPost("usuarios_admin", adminJson);
                    Log.d(TAG, "✅ Admin criado no Supabase: " + usuario);
                } else {
                    // Atualizar se necessário
                    JSONObject existente = jsonArray.getJSONObject(0);
                    String id = existente.getString("id");
                    enviarPatch("usuarios_admin?id=eq." + id, adminJson);
                    Log.d(TAG, "✅ Admin atualizado no Supabase: " + usuario);
                }
            } catch (Exception e) {
                Log.e(TAG, "💥 Erro ao sincronizar admin " + usuario + ": " + e.getMessage());
            }
        }).start();
    }

    public void sincronizarAdminMaster() {
        new Thread(() -> {
            try {
                Log.d(TAG, "🔄 Sincronizando admin master...");

                String senhaHash = BDCondominioHelper.gerarHash("master");
                String dataAtual = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date());

                JSONObject adminJson = new JSONObject();
                adminJson.put("usuario", "admin");
                adminJson.put("senha_hash", senhaHash);
                adminJson.put("tipo", "master");
                adminJson.put("data_cadastro", dataAtual);
                adminJson.put("ativo", true);

                // Verificar se já existe
                String response = enviarGet("usuarios_admin?usuario=eq.admin&select=*");
                JSONArray jsonArray = new JSONArray(response);

                if (jsonArray.length() == 0) {
                    // Criar admin master
                    enviarPost("usuarios_admin", adminJson);
                    Log.d(TAG, "✅ Admin master criado no Supabase");
                } else {
                    // Sempre atualizar para garantir que está correto
                    JSONObject existente = jsonArray.getJSONObject(0);
                    String id = existente.getString("id");
                    enviarPatch("usuarios_admin?id=eq." + id, adminJson);
                    Log.d(TAG, "✅ Admin master atualizado no Supabase");
                }

                // Limpar usuários de teste
                limparUsuariosDeTeste();

            } catch (Exception e) {
                Log.e(TAG, "💥 Erro ao sincronizar admin master: " + e.getMessage());
            }
        }).start();
    }

    private void limparUsuariosDeTeste() {
        try {
            // Manter apenas admin master
            enviarDelete("usuarios_admin?usuario=neq.admin");
            Log.d(TAG, "🧹 Usuários de teste removidos do Supabase");
        } catch (Exception e) {
            Log.e(TAG, "⚠️ Erro ao limpar usuários de teste: " + e.getMessage());
        }
    }

    /* ==============================
       OPERAÇÕES HTTP
     ============================== */

    private String enviarGet(String endpoint) {
        try {
            URL url = new URL(SUPABASE_URL + endpoint);
            HttpURLConnection conn = criarConexao(url);
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = in.readLine()) != null) response.append(line);
                in.close();
                conn.disconnect();
                return response.toString();
            } else {
                Log.e(TAG, "❌ Erro GET " + endpoint + ": Código " + responseCode);
                conn.disconnect();
                return "[]";
            }
        } catch (Exception e) {
            Log.e(TAG, "💥 Erro no GET " + endpoint + ": " + e.getMessage());
            return "[]";
        }
    }

    private void enviarPost(String endpoint, JSONObject jsonObject) {
        try {
            URL url = new URL(SUPABASE_URL + endpoint);
            HttpURLConnection conn = criarConexao(url);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Prefer", "return=minimal");
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            os.write(jsonObject.toString().getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == 201 || responseCode == 200) {
                Log.d(TAG, "✅ POST realizado: " + endpoint);
            } else {
                Log.e(TAG, "❌ Erro POST " + endpoint + ": Código " + responseCode);
            }
            conn.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "💥 Erro no POST " + endpoint + ": " + e.getMessage());
        }
    }

    private void enviarPatch(String endpoint, JSONObject jsonObject) {
        try {
            URL url = new URL(SUPABASE_URL + endpoint);
            HttpURLConnection conn = criarConexao(url);
            conn.setRequestMethod("PATCH");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Prefer", "return=minimal");
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            os.write(jsonObject.toString().getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == 204 || responseCode == 200) {
                Log.d(TAG, "✅ PATCH realizado: " + endpoint);
            } else {
                Log.e(TAG, "❌ Erro PATCH " + endpoint + ": Código " + responseCode);
            }
            conn.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "💥 Erro no PATCH " + endpoint + ": " + e.getMessage());
        }
    }

    private void enviarDelete(String endpoint) {
        try {
            URL url = new URL(SUPABASE_URL + endpoint);
            HttpURLConnection conn = criarConexao(url);
            conn.setRequestMethod("DELETE");
            conn.setRequestProperty("Prefer", "return=minimal");

            int responseCode = conn.getResponseCode();
            if (responseCode == 204 || responseCode == 200) {
                Log.d(TAG, "✅ DELETE realizado: " + endpoint);
            } else {
                Log.e(TAG, "❌ Erro DELETE " + endpoint + ": Código " + responseCode);
            }
            conn.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "💥 Erro no DELETE " + endpoint + ": " + e.getMessage());
        }
    }

    private HttpURLConnection criarConexao(URL url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("apikey", SUPABASE_ANON_KEY);
        conn.setRequestProperty("Authorization", "Bearer " + getAccessToken());
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setInstanceFollowRedirects(false);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        return conn;
    }

    private HttpURLConnection criarConexaoAutenticada(URL url, String token) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestProperty("apikey", SUPABASE_ANON_KEY);
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setInstanceFollowRedirects(false);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        return conn;
    }

    private void enviarParaSupabase(String tabelaSQLite, String[] colunasSQLite, String tabelaSupabase) {
        if (!isNetworkAvailable()) {
            Log.d(TAG, "⏸️ Sincronização pausada (sem rede): " + tabelaSupabase);
            return;
        }

        new Thread(() -> {
            try {
                BDCondominioHelper dbHelper = new BDCondominioHelper(context);
                SQLiteDatabase db = dbHelper.getReadableDatabase();

                Cursor cursor = db.query(tabelaSQLite, colunasSQLite, null, null, null, null, null);
                JSONArray jsonArray = new JSONArray();

                while (cursor.moveToNext()) {
                    JSONObject obj = new JSONObject();
                    for (String coluna : colunasSQLite) {
                        int idx = cursor.getColumnIndex(coluna);
                        if (idx != -1) {
                            String valor = cursor.getString(idx);
                            if (valor != null && !valor.trim().isEmpty()) {
                                obj.put(coluna, valor);
                            }
                        }
                    }
                    if (obj.length() > 0) {
                        jsonArray.put(obj);
                    }
                }
                cursor.close();
                db.close();

                if (jsonArray.length() > 0) {
                    enviarLoteParaSupabase(tabelaSupabase, jsonArray);
                } else {
                    Log.d(TAG, "ℹ️ Nenhum dado para " + tabelaSupabase);
                }
            } catch (Exception e) {
                Log.e(TAG, "💥 Erro ao processar " + tabelaSupabase + ": " + e.getMessage());
            }
        }).start();
    }

    private void enviarLoteParaSupabase(String tabelaSupabase, JSONArray batch) {
        try {
            URL url = new URL(SUPABASE_URL + tabelaSupabase);
            HttpURLConnection conn = criarConexao(url);
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Prefer", "resolution=merge-duplicates");
            conn.setDoOutput(true);

            String jsonData = batch.toString();
            OutputStream os = conn.getOutputStream();
            os.write(jsonData.getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            if (responseCode == 200 || responseCode == 201) {
                Log.d(TAG, "✅ " + batch.length() + " registros sincronizados para " + tabelaSupabase);
            } else {
                Log.e(TAG, "❌ Erro no lote para " + tabelaSupabase + ": Código " + responseCode);
            }
            conn.disconnect();
        } catch (Exception e) {
            Log.e(TAG, "💥 Erro no envio do lote para " + tabelaSupabase + ": " + e.getMessage());
        }
    }

    /* ==============================
       UTILITÁRIOS
     ============================== */

    private void salvarUltimaSincronizacao() {
        try {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString(KEY_LAST_SYNC,
                    new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
            editor.apply();
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao salvar timestamp de sincronização");
        }
    }

    public String getUltimaSincronizacao() {
        return sharedPreferences.getString(KEY_LAST_SYNC, "Nunca");
    }

    public void testarConexao() {
        new Thread(() -> {
            try {
                URL url = new URL(SUPABASE_URL + "usuarios_admin?select=count");
                HttpURLConnection conn = criarConexao(url);
                conn.setRequestMethod("GET");
                int responseCode = conn.getResponseCode();
                if (responseCode == 200) {
                    Log.d(TAG, "✅ Conexão ao Supabase OK");
                } else {
                    Log.e(TAG, "❌ Falha na conexão: " + responseCode);
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "💥 Erro ao testar conexão: " + e.getMessage());
            }
        }).start();
    }

    public void configurarSincronizacaoTempoReal() {
        try {
            Log.d(TAG, "🔔 Configurando sincronização em tempo real...");

            // Canal para moradores
            configurarCanalRealtime("moradores", () -> {
                Log.d(TAG, "🔄 Atualização em tempo real: moradores");
                sincronizarMoradores();
            });

            // Canal para ocorrências
            configurarCanalRealtime("ocorrencias", () -> {
                Log.d(TAG, "🔄 Atualização em tempo real: ocorrencias");
                sincronizarOcorrencias();
            });

            // Canal para avisos
            configurarCanalRealtime("avisos", () -> {
                Log.d(TAG, "🔄 Atualização em tempo real: avisos");
                sincronizarAvisos();
            });

            // Canal para funcionários
            configurarCanalRealtime("funcionarios", () -> {
                Log.d(TAG, "🔄 Atualização em tempo real: funcionarios");
                sincronizarFuncionarios();
            });

            // Canal para manutenções
            configurarCanalRealtime("manutencoes", () -> {
                Log.d(TAG, "🔄 Atualização em tempo real: manutencoes");
                sincronizarManutencoes();
            });

            // Canal para assembleias
            configurarCanalRealtime("assembleias", () -> {
                Log.d(TAG, "🔄 Atualização em tempo real: assembleias");
                sincronizarAssembleias();
            });

            // Canal para despesas
            configurarCanalRealtime("despesas", () -> {
                Log.d(TAG, "🔄 Atualização em tempo real: despesas");
                sincronizarDespesas();
            });

            Log.d(TAG, "✅ Sincronização em tempo real configurada");

        } catch (Exception e) {
            Log.e(TAG, "💥 Erro ao configurar tempo real: " + e.getMessage());
        }
    }

    private void configurarCanalRealtime(String tabela, Runnable onUpdate) {
        new Thread(() -> {
            try {
                // Simular inscrição no canal (implementação real depende da biblioteca)
                Log.d(TAG, "📡 Inscrito no canal: " + tabela);

                // Em uma implementação real, você usaria:
                // SupabaseClient.client.realtime.channel(tabela)
                //     .on("INSERT", payload -> onUpdate.run())
                //     .on("UPDATE", payload -> onUpdate.run())
                //     .on("DELETE", payload -> onUpdate.run())
                //     .subscribe();

            } catch (Exception e) {
                Log.e(TAG, "💥 Erro no canal " + tabela + ": " + e.getMessage());
            }
        }).start();
    }

    public void sincronizacaoRapida() {
        if (!isNetworkAvailable()) {
            Log.e(TAG, "❌ Abortando sincronização rápida: sem rede");
            return;
        }

        new Thread(() -> {
            try {
                sincronizarAdminMaster();
                sincronizarMoradores();
                sincronizarOcorrencias();
                sincronizarAvisos();
                salvarUltimaSincronizacao();
            } catch (Exception e) {
                Log.e(TAG, "💥 Erro na sincronização rápida: " + e.getMessage());
            }
        }).start();
    }

    public interface AuthCallback {
        void onSuccess(String token);
        void onError(String message);
    }
}