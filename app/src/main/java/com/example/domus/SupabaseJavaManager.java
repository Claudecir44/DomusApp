package com.example.domus;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class SupabaseJavaManager {
    private static final String TAG = "SUPABASE_JAVA";
    private static final String SUPABASE_URL = "https://wkafwsxydyhkzxbdksve.supabase.co";
    private static final String API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndrYWZ3c3h5ZHloa3p4YmRrc3ZlIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NTc3MTUwOTIsImV4cCI6MjA3MzI5MTA5Mn0.jkX2ERr9AVasCLg2H_X6rXYEmdRXHlW81SdfH0Uohag";

    // 🔥 MÉTODO TESTAR CONEXÃO COM LOGS FORÇADOS
    public static void testarConexao() {
        Log.d(TAG, "🎯🎯🎯 testarConexao() INICIADO! 🎯🎯🎯");

        new Thread(() -> {
            try {
                Log.d(TAG, "🌐 [THREAD] Iniciando teste de conexão...");

                URL url = new URL(SUPABASE_URL + "/rest/v1/moradores?select=count&limit=1");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                connection.setRequestMethod("GET");
                connection.setRequestProperty("apikey", API_KEY);
                connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
                connection.setRequestProperty("Content-Type", "application/json");

                Log.d(TAG, "📡 Conectando com Supabase...");
                int responseCode = connection.getResponseCode();
                Log.d(TAG, "📊 Código de resposta: " + responseCode);

                connection.disconnect();

                if (responseCode == 200) {
                    Log.d(TAG, "🎉🎉🎉 CONEXÃO SUPABASE FUNCIONANDO! 🎉🎉🎉");
                    Log.d(TAG, "✅ AGORA SIM: SQLite ↔ Supabase INTEGRADO!");
                } else {
                    Log.e(TAG, "❌ CONEXÃO FALHOU: Código " + responseCode);
                }

            } catch (Exception e) {
                Log.e(TAG, "💥 ERRO NA CONEXÃO: " + e.getMessage());
                Log.e(TAG, "🔍 Stacktrace: " + Log.getStackTraceString(e));
            }
        }).start();

        Log.d(TAG, "✅ testarConexao() FINALIZADO - Thread iniciada");
    }

    // 🔥 MÉTODO PRINCIPAL DE SINCRONIZAÇÃO
    public static void sincronizarComSupabase(android.content.Context context) {
        Log.d(TAG, "🔄 🔄 🔄 INICIANDO SINCRONIZAÇÃO COMPLETA 🔄 🔄 🔄");

        new Thread(() -> {
            try {
                // 1. TESTAR CONEXÃO
                Log.d(TAG, "1. Testando conexão com Supabase...");
                if (!testarConexaoSupabase()) {
                    Log.e(TAG, "❌ Conexão falhou - sincronização cancelada");
                    return;
                }

                // Pequena pausa para logs
                Thread.sleep(1000);

                // 2. SINCRONIZAR DADOS LOCAIS → NUVEM
                Log.d(TAG, "2. Sincronizando SQLite → Supabase...");
                sincronizarSQLiteParaSupabase(context);

                // Pequena pausa para logs
                Thread.sleep(1000);

                // 3. SINCRONIZAR DADOS NUVEM → LOCAIS
                Log.d(TAG, "3. Sincronizando Supabase → SQLite...");
                sincronizarSupabaseParaSQLite(context);

                Log.d(TAG, "🎉 🎉 🎉 SINCRONIZAÇÃO CONCLUÍDA! 🎉 🎉 🎉");

            } catch (Exception e) {
                Log.e(TAG, "❌ ERRO NA SINCRONIZAÇÃO: " + e.getMessage());
                Log.e(TAG, "🔍 Stacktrace: " + Log.getStackTraceString(e));
            }
        }).start();
    }

    // 🔥 TESTAR CONEXÃO (versão que retorna boolean)
    private static boolean testarConexaoSupabase() {
        try {
            Log.d(TAG, "🌐 TESTANDO CONEXÃO COM SUPABASE...");

            URL url = new URL(SUPABASE_URL + "/rest/v1/moradores?select=count&limit=1");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("apikey", API_KEY);
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");

            int responseCode = connection.getResponseCode();
            Log.d(TAG, "📊 Código de resposta: " + responseCode);

            connection.disconnect();

            if (responseCode == 200) {
                Log.d(TAG, "✅ CONEXÃO SUPABASE: OK");
                return true;
            } else {
                Log.e(TAG, "❌ CONEXÃO SUPABASE: FALHA");
                return false;
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ ERRO NA CONEXÃO: " + e.getMessage());
            return false;
        }
    }

    // 🔥 SINCRONIZAR SQLite → Supabase (Upload)
    private static void sincronizarSQLiteParaSupabase(android.content.Context context) {
        try {
            Log.d(TAG, "📤 SINCRONIZANDO SQLite → Supabase");

            BDCondominioHelper dbHelper = new BDCondominioHelper(context);
            SQLiteDatabase db = dbHelper.getReadableDatabase();

            // SINCRONIZAR MORADORES
            Cursor cursorMoradores = db.query("moradores", null, null, null, null, null, null);
            if (cursorMoradores != null && cursorMoradores.moveToFirst()) {
                int count = 0;
                do {
                    // Buscar dados do morador
                    String nome = cursorMoradores.getString(cursorMoradores.getColumnIndexOrThrow("nome"));
                    String email = cursorMoradores.getString(cursorMoradores.getColumnIndexOrThrow("email"));
                    String telefone = cursorMoradores.getString(cursorMoradores.getColumnIndexOrThrow("telefone"));
                    String cpf = cursorMoradores.getString(cursorMoradores.getColumnIndexOrThrow("cpf"));

                    // Verificar se já existe no Supabase
                    if (!existeMoradorNoSupabase(cpf)) {
                        // Inserir no Supabase
                        inserirMoradorNoSupabase(nome, email, telefone, cpf);
                        Log.d(TAG, "⬆️ Morador enviado para nuvem: " + nome);
                        count++;
                    }

                } while (cursorMoradores.moveToNext());
                cursorMoradores.close();
                Log.d(TAG, "✅ " + count + " moradores sincronizados para nuvem");
            } else {
                Log.d(TAG, "ℹ️ Nenhum morador local para sincronizar");
            }

            db.close();
            Log.d(TAG, "✅ Upload SQLite → Supabase concluído");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro no upload: " + e.getMessage());
        }
    }

    // 🔥 SINCRONIZAR Supabase → SQLite (Download)
    private static void sincronizarSupabaseParaSQLite(android.content.Context context) {
        try {
            Log.d(TAG, "📥 SINCRONIZANDO Supabase → SQLite");

            // Buscar moradores do Supabase
            List<JSONObject> moradoresSupabase = buscarMoradoresDoSupabase();
            Log.d(TAG, "📊 " + moradoresSupabase.size() + " moradores encontrados na nuvem");

            BDCondominioHelper dbHelper = new BDCondominioHelper(context);
            SQLiteDatabase db = dbHelper.getWritableDatabase();

            int count = 0;
            for (JSONObject morador : moradoresSupabase) {
                String cpf = morador.getString("cpf");

                // Verificar se não existe localmente
                if (!existeMoradorNoSQLite(db, cpf)) {
                    // Inserir no SQLite
                    ContentValues values = new ContentValues();
                    values.put("nome", morador.getString("nome"));
                    values.put("email", morador.getString("email"));
                    values.put("telefone", morador.getString("telefone"));
                    values.put("cpf", cpf);

                    db.insert("moradores", null, values);
                    Log.d(TAG, "⬇️ Morador baixado da nuvem: " + morador.getString("nome"));
                    count++;
                }
            }

            db.close();
            Log.d(TAG, "✅ " + count + " moradores baixados da nuvem");
            Log.d(TAG, "✅ Download Supabase → SQLite concluído");

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro no download: " + e.getMessage());
        }
    }

    // 🔥 VERIFICAR SE MORADOR EXISTE NO SUPABASE
    private static boolean existeMoradorNoSupabase(String cpf) {
        try {
            URL url = new URL(SUPABASE_URL + "/rest/v1/moradores?cpf=eq." + cpf);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("apikey", API_KEY);
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);

            int responseCode = connection.getResponseCode();

            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String response = in.readLine();
                in.close();

                // Se retornar dados, o morador existe
                return response != null && response.length() > 2 && !response.equals("[]");
            }

            connection.disconnect();
            return false;

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao verificar morador: " + e.getMessage());
            return false;
        }
    }

    // 🔥 BUSCAR MORADORES DO SUPABASE
    private static List<JSONObject> buscarMoradoresDoSupabase() {
        List<JSONObject> moradores = new ArrayList<>();

        try {
            URL url = new URL(SUPABASE_URL + "/rest/v1/moradores");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("GET");
            connection.setRequestProperty("apikey", API_KEY);
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);

            if (connection.getResponseCode() == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();

                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                JSONArray jsonArray = new JSONArray(response.toString());
                for (int i = 0; i < jsonArray.length(); i++) {
                    moradores.add(jsonArray.getJSONObject(i));
                }
            }

            connection.disconnect();

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao buscar moradores: " + e.getMessage());
        }

        return moradores;
    }

    // 🔥 INSERIR MORADOR NO SUPABASE
    private static void inserirMoradorNoSupabase(String nome, String email, String telefone, String cpf) {
        try {
            URL url = new URL(SUPABASE_URL + "/rest/v1/moradores");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            connection.setRequestMethod("POST");
            connection.setRequestProperty("apikey", API_KEY);
            connection.setRequestProperty("Authorization", "Bearer " + API_KEY);
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Prefer", "return=minimal");
            connection.setDoOutput(true);

            JSONObject morador = new JSONObject();
            morador.put("nome", nome);
            morador.put("email", email);
            morador.put("telefone", telefone);
            morador.put("cpf", cpf);

            OutputStream os = connection.getOutputStream();
            os.write(morador.toString().getBytes());
            os.flush();
            os.close();

            int responseCode = connection.getResponseCode(); // Executar a inserção
            Log.d(TAG, "📝 Inserção no Supabase - Código: " + responseCode);
            connection.disconnect();

        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao inserir morador: " + e.getMessage());
        }
    }

    // 🔥 VERIFICAR SE MORADOR EXISTE NO SQLITE
    private static boolean existeMoradorNoSQLite(SQLiteDatabase db, String cpf) {
        Cursor cursor = db.query("moradores", new String[]{"cpf"}, "cpf = ?", new String[]{cpf}, null, null, null);
        boolean existe = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        return existe;
    }

    // 🔥 MÉTODO PÚBLICO PARA INSERIR DADO DE TESTE
    public static void inserirDadoTeste() {
        new Thread(() -> {
            try {
                Log.d(TAG, "📝 INSERINDO DADO DE TESTE...");
                inserirMoradorNoSupabase("Teste Java", "teste@java.com", "11999998888", "99988877766");
                Log.d(TAG, "✅ Dado de teste inserido!");
            } catch (Exception e) {
                Log.e(TAG, "❌ Erro no teste: " + e.getMessage());
            }
        }).start();
    }
}