package com.example.domus;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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

    private static final String TAG = "SupabaseSyncManager";
    private static final String SUPABASE_URL = "https://wkafwsxydyhkzxbdksve.supabase.co/rest/v1/";
    private static final String SUPABASE_ANON_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndrYWZ3c3h5ZHloa3p4YmRrc3ZlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTc3MTUwOTIsImV4cCI6MjA3MzI5MTA5Mn0.jkX2ERr9AVasCLg2H_X6rXYEmdRXHlW81SdfH0Uohag";

    private final Context context;
    private final Handler handler;

    public SupabaseSyncManager(Context context) {
        this.context = context;
        this.handler = new Handler();
    }

    // 🔥 MÉTODOS DE SINCRONIZAÇÃO EM MASSA
    public void sincronizacaoRapida() {
        Log.d(TAG, "🔄 Iniciando sincronização rápida de todas as tabelas");

        new Thread(() -> {
            try {
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

                Log.d(TAG, "🎉 SINCRONIZAÇÃO RÁPIDA CONCLUÍDA!");

            } catch (InterruptedException e) {
                Log.e(TAG, "💥 Sincronização interrompida: " + e.getMessage());
            }
        }).start();
    }

    public void sincronizarTudo() {
        Log.d(TAG, "🔄 INICIANDO SINCRONIZAÇÃO COMPLETA SQLite → Supabase");
        sincronizacaoRapida();
    }

    // 🔹 SINCRONIZAR ADMINS
    public void sincronizarAdmins() {
        enviarParaSupabase(BDCondominioHelper.TABELA_USUARIOS_ADMIN,
                new String[]{
                        BDCondominioHelper.COL_ADMIN_USUARIO,
                        BDCondominioHelper.COL_ADMIN_SENHA_HASH,
                        BDCondominioHelper.COL_ADMIN_TIPO,
                        BDCondominioHelper.COL_ADMIN_DATA
                },
                "usuarios_admin");
    }

    // 🔹 SINCRONIZAR MORADORES
    public void sincronizarMoradores() {
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
        enviarParaSupabase(BDCondominioHelper.TABELA_AVISOS,
                new String[]{
                        BDCondominioHelper.COL_AVISO_DATAHORA,
                        BDCondominioHelper.COL_AVISO_ASSUNTO,
                        BDCondominioHelper.COL_AVISO_DESCRICAO,
                        BDCondominioHelper.COL_AVISO_ANEXOS,
                        BDCondominioHelper.COL_AVISO_CRIADO_EM,
                        BDCondominioHelper.COL_AVISO_ATUALIZADO_EM
                },
                "avisos");
    }

    // 🔥 MÉTODOS PARA OPERAÇÕES INDIVIDUAIS

    // 🔹 SINCRONIZAR MORADOR ESPECÍFICO (para inserção)
    public void sincronizarMoradorEspecifico(String nome, String email, String telefone, String cpf,
                                             String rua, String numero, String quadra, String lote) {
        Log.d(TAG, "👤 Sincronizando morador específico: " + nome);

        new Thread(() -> {
            try {
                // Verificar se já existe no Supabase
                if (!moradorExisteNoSupabase(cpf, email)) {
                    JSONObject morador = new JSONObject();
                    morador.put("nome", nome);
                    morador.put("email", email);
                    morador.put("telefone", telefone);
                    morador.put("cpf", cpf);
                    morador.put("rua", rua);
                    morador.put("numero", numero);
                    morador.put("quadra", quadra);
                    morador.put("lote", lote);

                    boolean sucesso = inserirNoSupabase("moradores", morador);
                    if (sucesso) {
                        Log.d(TAG, "✅ Morador sincronizado com Supabase: " + nome);
                    } else {
                        Log.e(TAG, "❌ Falha ao sincronizar morador: " + nome);
                    }
                } else {
                    Log.d(TAG, "ℹ️ Morador já existe no Supabase: " + nome);
                }
            } catch (Exception e) {
                Log.e(TAG, "💥 Erro ao sincronizar morador específico: " + e.getMessage());
            }
        }).start();
    }

    // 🔹 ATUALIZAR MORADOR NO SUPABASE (para edição)
    public void atualizarMoradorSupabase(int idLocal, String nome, String email, String telefone, String cpf,
                                         String rua, String numero, String quadra, String lote) {
        Log.d(TAG, "✏️ Atualizando morador no Supabase: " + nome);

        new Thread(() -> {
            try {
                // Buscar ID no Supabase pelo CPF
                String idSupabase = buscarIdMoradorNoSupabase(cpf);

                if (idSupabase != null) {
                    JSONObject morador = new JSONObject();
                    morador.put("nome", nome);
                    morador.put("email", email);
                    morador.put("telefone", telefone);
                    morador.put("cpf", cpf);
                    morador.put("rua", rua);
                    morador.put("numero", numero);
                    morador.put("quadra", quadra);
                    morador.put("lote", lote);

                    boolean sucesso = atualizarNoSupabase("moradores", idSupabase, morador);
                    if (sucesso) {
                        Log.d(TAG, "✅ Morador atualizado no Supabase: " + nome);
                    } else {
                        Log.e(TAG, "❌ Falha ao atualizar morador: " + nome);
                    }
                } else {
                    Log.e(TAG, "❌ Morador não encontrado no Supabase para atualização: " + cpf);
                    // Se não encontrou, insere como novo
                    sincronizarMoradorEspecifico(nome, email, telefone, cpf, rua, numero, quadra, lote);
                }
            } catch (Exception e) {
                Log.e(TAG, "💥 Erro ao atualizar morador no Supabase: " + e.getMessage());
            }
        }).start();
    }

    // 🔹 EXCLUIR MORADOR DO SUPABASE
    public void excluirMoradorSupabase(String cpf) {
        Log.d(TAG, "🗑️ Excluindo morador do Supabase CPF: " + cpf);

        new Thread(() -> {
            try {
                String idSupabase = buscarIdMoradorNoSupabase(cpf);

                if (idSupabase != null) {
                    boolean sucesso = excluirNoSupabase("moradores", idSupabase);
                    if (sucesso) {
                        Log.d(TAG, "✅ Morador excluído do Supabase CPF: " + cpf);
                    } else {
                        Log.e(TAG, "❌ Falha ao excluir morador do Supabase CPF: " + cpf);
                    }
                } else {
                    Log.d(TAG, "ℹ️ Morador não encontrado no Supabase para exclusão CPF: " + cpf);
                }
            } catch (Exception e) {
                Log.e(TAG, "💥 Erro ao excluir morador do Supabase: " + e.getMessage());
            }
        }).start();
    }

    // 🔹 SINCRONIZAR ADMIN ESPECÍFICO
    public void sincronizarAdminEspecifico(String usuario, String senhaHash, String tipo) {
        Log.d(TAG, "🔐 Sincronizando admin específico: " + usuario);

        new Thread(() -> {
            try {
                if (!adminExisteNoSupabase(usuario)) {
                    JSONObject admin = new JSONObject();
                    admin.put("usuario", usuario);
                    admin.put("senha_hash", senhaHash);
                    admin.put("tipo", tipo);
                    admin.put("data_cadastro", new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

                    boolean sucesso = inserirNoSupabase("usuarios_admin", admin);
                    if (sucesso) {
                        Log.d(TAG, "✅ Admin sincronizado com Supabase: " + usuario);
                    } else {
                        Log.e(TAG, "❌ Falha ao sincronizar admin: " + usuario);
                    }
                } else {
                    Log.d(TAG, "ℹ️ Admin já existe no Supabase: " + usuario);
                }
            } catch (Exception e) {
                Log.e(TAG, "💥 Erro ao sincronizar admin específico: " + e.getMessage());
            }
        }).start();
    }

    // 🔹 MÉTODOS AUXILIARES PARA OPERAÇÕES INDIVIDUAIS

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

    private boolean adminExisteNoSupabase(String usuario) {
        try {
            String urlCompleta = SUPABASE_URL + "usuarios_admin?usuario=eq." + usuario + "&select=id";
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
            Log.e(TAG, "💥 Erro ao verificar admin: " + e.getMessage());
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
            Log.e(TAG, "💥 Erro ao buscar ID do morador: " + e.getMessage());
        }
        return null;
    }

    // 🔹 OPERAÇÕES CRUD NO SUPABASE

    private boolean inserirNoSupabase(String tabela, JSONObject dados) {
        try {
            URL url = new URL(SUPABASE_URL + tabela);
            HttpURLConnection conn = criarConexaoPOST(url);

            OutputStream os = conn.getOutputStream();
            os.write(dados.toString().getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            conn.disconnect();

            return responseCode == 201;
        } catch (Exception e) {
            Log.e(TAG, "💥 Erro ao inserir em " + tabela + ": " + e.getMessage());
            return false;
        }
    }

    private boolean atualizarNoSupabase(String tabela, String id, JSONObject dados) {
        try {
            URL url = new URL(SUPABASE_URL + tabela + "?id=eq." + id);
            HttpURLConnection conn = criarConexaoPATCH(url);

            OutputStream os = conn.getOutputStream();
            os.write(dados.toString().getBytes("UTF-8"));
            os.flush();
            os.close();

            int responseCode = conn.getResponseCode();
            conn.disconnect();

            return responseCode == 200 || responseCode == 204;
        } catch (Exception e) {
            Log.e(TAG, "💥 Erro ao atualizar em " + tabela + ": " + e.getMessage());
            return false;
        }
    }

    private boolean excluirNoSupabase(String tabela, String id) {
        try {
            URL url = new URL(SUPABASE_URL + tabela + "?id=eq." + id);
            HttpURLConnection conn = criarConexaoDELETE(url);

            int responseCode = conn.getResponseCode();
            conn.disconnect();

            return responseCode == 200 || responseCode == 204;
        } catch (Exception e) {
            Log.e(TAG, "💥 Erro ao excluir de " + tabela + ": " + e.getMessage());
            return false;
        }
    }

    // 🔹 MÉTODOS AUXILIARES PARA CONEXÕES

    private HttpURLConnection criarConexaoGET(String urlCompleta) throws Exception {
        URL url = new URL(urlCompleta);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("apikey", SUPABASE_ANON_KEY);
        connection.setRequestProperty("Authorization", "Bearer " + SUPABASE_ANON_KEY);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(10000);
        connection.setReadTimeout(10000);
        return connection;
    }

    private HttpURLConnection criarConexaoPOST(URL url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("apikey", SUPABASE_ANON_KEY);
        conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_ANON_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Prefer", "return=minimal");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        return conn;
    }

    private HttpURLConnection criarConexaoPATCH(URL url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("PATCH");
        conn.setRequestProperty("apikey", SUPABASE_ANON_KEY);
        conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_ANON_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestProperty("Prefer", "return=minimal");
        conn.setDoOutput(true);
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        return conn;
    }

    private HttpURLConnection criarConexaoDELETE(URL url) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("DELETE");
        conn.setRequestProperty("apikey", SUPABASE_ANON_KEY);
        conn.setRequestProperty("Authorization", "Bearer " + SUPABASE_ANON_KEY);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setConnectTimeout(10000);
        conn.setReadTimeout(10000);
        return conn;
    }

    // 🔹 MÉTODO GENÉRICO DE ENVIO EM MASSA
    private void enviarParaSupabase(String tabelaSQLite, String[] colunas, String tabelaSupabase) {
        new Thread(() -> {
            try {
                BDCondominioHelper dbHelper = new BDCondominioHelper(context);
                SQLiteDatabase db = dbHelper.getReadableDatabase();

                Cursor cursor = db.query(tabelaSQLite, colunas, null, null, null, null, null);
                JSONArray jsonArray = new JSONArray();

                while (cursor.moveToNext()) {
                    JSONObject obj = new JSONObject();
                    for (int i = 0; i < colunas.length; i++) {
                        String valor = cursor.getString(i);
                        if (valor != null) {
                            obj.put(colunas[i], valor);
                        }
                    }
                    jsonArray.put(obj);
                }

                cursor.close();
                db.close();

                if (jsonArray.length() == 0) {
                    Log.d(TAG, "📋 Nenhum registro para enviar da tabela: " + tabelaSQLite);
                    return;
                }

                Log.d(TAG, "📤 Enviando " + jsonArray.length() + " registros para Supabase [" + tabelaSupabase + "]");

                URL url = new URL(SUPABASE_URL + tabelaSupabase);
                HttpURLConnection conn = criarConexaoPOST(url);

                OutputStream os = conn.getOutputStream();
                os.write(jsonArray.toString().getBytes("UTF-8"));
                os.flush();
                os.close();

                int responseCode = conn.getResponseCode();
                if (responseCode == 201 || responseCode == 200) {
                    Log.d(TAG, "✅ Registros enviados com sucesso para " + tabelaSupabase);
                } else {
                    Log.e(TAG, "❌ Falha ao enviar para " + tabelaSupabase + " | Código: " + responseCode);
                    try (BufferedReader errorReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()))) {
                        StringBuilder errorResponse = new StringBuilder();
                        String line;
                        while ((line = errorReader.readLine()) != null) {
                            errorResponse.append(line);
                        }
                        Log.e(TAG, "📋 Detalhes do erro: " + errorResponse);
                    } catch (Exception e) {
                        Log.e(TAG, "💥 Não foi possível ler detalhes do erro");
                    }
                }

                conn.disconnect();

            } catch (Exception e) {
                Log.e(TAG, "💥 Erro ao enviar registros para Supabase: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    // 🔹 MÉTODO DE TESTE COMPLETO
    public void testeCompleto() {
        Log.d(TAG, "🧪 Iniciando teste completo de sincronização");
        sincronizacaoRapida();
    }

    // 🔹 MÉTODO PARA VERIFICAR CONEXÃO
    public void testarConexao() {
        new Thread(() -> {
            try {
                String urlCompleta = SUPABASE_URL + "moradores?select=count";
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
}