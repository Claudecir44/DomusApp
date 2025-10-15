package com.example.domus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BDCondominioHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "bdcondominio.db";
    private static final int DATABASE_VERSION = 19; // 🔥 ATUALIZADO PARA FORÇAR RECRIAÇÃO

    // Tabelas
    public static final String TABELA_MORADORES = "moradores";
    public static final String TABELA_OCORRENCIAS = "ocorrencias";
    public static final String TABELA_FUNCIONARIOS = "funcionarios";
    public static final String TABELA_MANUTENCOES = "manutencoes";
    public static final String TABELA_ASSEMBLEIAS = "assembleias";
    public static final String TABELA_DESPESAS = "despesas";
    public static final String TABELA_AVISOS = "avisos";
    public static final String TABELA_USUARIOS_ADMIN = "usuarios_admin";

    // Colunas Admin
    public static final String COL_ADMIN_ID = "id";
    public static final String COL_ADMIN_USUARIO = "usuario";
    public static final String COL_ADMIN_SENHA_HASH = "senha_hash";
    public static final String COL_ADMIN_TIPO = "tipo";
    public static final String COL_ADMIN_DATA = "data_cadastro";

    // Colunas Moradores
    public static final String COL_ID = "id";
    public static final String COL_COD = "cod";
    public static final String COL_NOME = "nome";
    public static final String COL_CPF = "cpf";
    public static final String COL_EMAIL = "email";
    public static final String COL_RUA = "rua";
    public static final String COL_NUMERO = "numero";
    public static final String COL_TELEFONE = "telefone";
    public static final String COL_QUADRA = "quadra";
    public static final String COL_LOTE = "lote";
    public static final String COL_IMAGEM_URI = "imagem_uri";

    // Colunas Ocorrencias
    public static final String COL_OCOR_ID = "id";
    public static final String COL_OCOR_TIPO = "tipo";
    public static final String COL_OCOR_ENVOLVIDOS = "envolvidos";
    public static final String COL_OCOR_DESCRICAO = "descricao";
    public static final String COL_OCOR_DATAHORA = "datahora";
    public static final String COL_OCOR_ANEXOS = "anexos";

    // Colunas Funcionários
    public static final String COL_FUNC_ID = "id";
    public static final String COL_FUNC_NOME = "nome";
    public static final String COL_FUNC_RUA = "rua";
    public static final String COL_FUNC_NUMERO = "numero";
    public static final String COL_FUNC_BAIRRO = "bairro";
    public static final String COL_FUNC_CEP = "cep";
    public static final String COL_FUNC_CIDADE = "cidade";
    public static final String COL_FUNC_ESTADO = "estado";
    public static final String COL_FUNC_PAIS = "pais";
    public static final String COL_FUNC_TELEFONE = "telefone";
    public static final String COL_FUNC_EMAIL = "email";
    public static final String COL_FUNC_RG = "rg";
    public static final String COL_FUNC_CPF = "cpf";
    public static final String COL_FUNC_CARGA_MENSAL = "carga_mensal";
    public static final String COL_FUNC_TURNO = "turno";
    public static final String COL_FUNC_HORA_ENTRADA = "hora_entrada";
    public static final String COL_FUNC_HORA_SAIDA = "hora_saida";
    public static final String COL_FUNC_IMAGEM_URI = "imagem_uri";

    // Colunas Manutenções
    public static final String COL_MANU_ID = "id";
    public static final String COL_MANU_TIPO = "tipo";
    public static final String COL_MANU_DATAHORA = "datahora";
    public static final String COL_MANU_LOCAL = "local";
    public static final String COL_MANU_SERVICO = "servico";
    public static final String COL_MANU_RESPONSAVEL = "responsavel";
    public static final String COL_MANU_VALOR = "valor";
    public static final String COL_MANU_NOTAS = "notas";
    public static final String COL_MANU_ANEXOS = "anexos";

    // Colunas Assembleias
    public static final String COL_ASS_ID = "id";
    public static final String COL_ASS_DATAHORA = "datahora";
    public static final String COL_ASS_LOCAL = "local";
    public static final String COL_ASS_ASSUNTO = "assunto";
    public static final String COL_ASS_DESCRICAO = "descricao";
    public static final String COL_ASS_ANEXOS = "anexos";

    // Colunas Despesas
    public static final String COL_DESP_ID = "id";
    public static final String COL_DESP_DATAHORA = "datahora";
    public static final String COL_DESP_NOME = "nome";
    public static final String COL_DESP_DESCRICAO = "descricao";
    public static final String COL_DESP_VALOR = "valor";
    public static final String COL_DESP_ANEXOS = "anexos";

    // Colunas Avisos
    public static final String COL_AVISO_ID = "id";
    public static final String COL_AVISO_DATAHORA = "datahora";
    public static final String COL_AVISO_ASSUNTO = "assunto";
    public static final String COL_AVISO_DESCRICAO = "descricao";
    public static final String COL_AVISO_ANEXOS = "anexos";
    public static final String COL_AVISO_CRIADO_EM = "criado_em";
    public static final String COL_AVISO_ATUALIZADO_EM = "atualizado_em";

    private Context context;
    private SupabaseSyncManager syncManager;

    public BDCondominioHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
        this.syncManager = new SupabaseSyncManager(context);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        criarTodasTabelas(db);
        criarAdminMasterCorrigido(db); // 🔥 AGORA USA O MÉTODO CORRIGIDO
        Log.d("DB_HELPER", "✅ Banco SQLite criado com admin 'admin/master'");

        // 🔥 SINCRONIZAR ADMINS COM SUPABASE APÓS CRIAÇÃO
        sincronizarAdminsComSupabase();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d("DB_HELPER", "🔄 Atualizando banco da versão " + oldVersion + " para " + newVersion);
        recriarTodasTabelas(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    // --- CRIAÇÃO DE TABELAS ---
    private void recriarTodasTabelas(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + TABELA_MORADORES);
        db.execSQL("DROP TABLE IF EXISTS " + TABELA_OCORRENCIAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABELA_FUNCIONARIOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABELA_MANUTENCOES);
        db.execSQL("DROP TABLE IF EXISTS " + TABELA_ASSEMBLEIAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABELA_DESPESAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABELA_AVISOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABELA_USUARIOS_ADMIN);

        criarTodasTabelas(db);
        criarAdminMasterCorrigido(db); // 🔥 AGORA USA O MÉTODO CORRIGIDO
    }

    private void criarTodasTabelas(SQLiteDatabase db) {
        criarTabelaMoradores(db);
        criarTabelaOcorrencias(db);
        criarTabelaFuncionarios(db);
        criarTabelaManutencoes(db);
        criarTabelaAssembleias(db);
        criarTabelaDespesas(db);
        criarTabelaAvisos(db);
        criarTabelaUsuariosAdmin(db);
    }

    // --- MÉTODOS PARA CRIAR CADA TABELA ---
    private void criarTabelaMoradores(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABELA_MORADORES + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_COD + " TEXT UNIQUE, " +
                COL_NOME + " TEXT, " +
                COL_CPF + " TEXT, " +
                COL_EMAIL + " TEXT, " +
                COL_RUA + " TEXT, " +
                COL_NUMERO + " TEXT, " +
                COL_TELEFONE + " TEXT, " +
                COL_QUADRA + " TEXT, " +
                COL_LOTE + " TEXT, " +
                COL_IMAGEM_URI + " TEXT);");
    }

    private void criarTabelaOcorrencias(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABELA_OCORRENCIAS + " (" +
                COL_OCOR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_OCOR_TIPO + " TEXT, " +
                COL_OCOR_ENVOLVIDOS + " TEXT, " +
                COL_OCOR_DESCRICAO + " TEXT NOT NULL, " +
                COL_OCOR_DATAHORA + " TEXT NOT NULL, " +
                COL_OCOR_ANEXOS + " TEXT);");
    }

    private void criarTabelaFuncionarios(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABELA_FUNCIONARIOS + " (" +
                COL_FUNC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_FUNC_NOME + " TEXT NOT NULL, " +
                COL_FUNC_RUA + " TEXT, " +
                COL_FUNC_NUMERO + " TEXT, " +
                COL_FUNC_BAIRRO + " TEXT, " +
                COL_FUNC_CEP + " TEXT, " +
                COL_FUNC_CIDADE + " TEXT, " +
                COL_FUNC_ESTADO + " TEXT, " +
                COL_FUNC_PAIS + " TEXT, " +
                COL_FUNC_TELEFONE + " TEXT, " +
                COL_FUNC_EMAIL + " TEXT, " +
                COL_FUNC_RG + " TEXT, " +
                COL_FUNC_CPF + " TEXT, " +
                COL_FUNC_CARGA_MENSAL + " TEXT, " +
                COL_FUNC_TURNO + " TEXT, " +
                COL_FUNC_HORA_ENTRADA + " TEXT, " +
                COL_FUNC_HORA_SAIDA + " TEXT, " +
                COL_FUNC_IMAGEM_URI + " TEXT);");
    }

    private void criarTabelaManutencoes(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABELA_MANUTENCOES + " (" +
                COL_MANU_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_MANU_TIPO + " TEXT NOT NULL, " +
                COL_MANU_DATAHORA + " TEXT NOT NULL, " +
                COL_MANU_LOCAL + " TEXT, " +
                COL_MANU_SERVICO + " TEXT, " +
                COL_MANU_RESPONSAVEL + " TEXT, " +
                COL_MANU_VALOR + " TEXT, " +
                COL_MANU_NOTAS + " TEXT, " +
                COL_MANU_ANEXOS + " TEXT);");
    }

    private void criarTabelaAssembleias(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABELA_ASSEMBLEIAS + " (" +
                COL_ASS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ASS_DATAHORA + " TEXT NOT NULL, " +
                COL_ASS_LOCAL + " TEXT, " +
                COL_ASS_ASSUNTO + " TEXT NOT NULL, " +
                COL_ASS_DESCRICAO + " TEXT, " +
                COL_ASS_ANEXOS + " TEXT);");
    }

    private void criarTabelaDespesas(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABELA_DESPESAS + " (" +
                COL_DESP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DESP_DATAHORA + " TEXT NOT NULL, " +
                COL_DESP_NOME + " TEXT NOT NULL, " +
                COL_DESP_DESCRICAO + " TEXT, " +
                COL_DESP_VALOR + " REAL, " +
                COL_DESP_ANEXOS + " TEXT);");
    }

    private void criarTabelaAvisos(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABELA_AVISOS + " (" +
                COL_AVISO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_AVISO_DATAHORA + " TEXT NOT NULL, " +
                COL_AVISO_ASSUNTO + " TEXT NOT NULL, " +
                COL_AVISO_DESCRICAO + " TEXT, " +
                COL_AVISO_ANEXOS + " TEXT, " +
                COL_AVISO_CRIADO_EM + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                COL_AVISO_ATUALIZADO_EM + " DATETIME DEFAULT CURRENT_TIMESTAMP);");
    }

    private void criarTabelaUsuariosAdmin(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABELA_USUARIOS_ADMIN + " (" +
                COL_ADMIN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ADMIN_USUARIO + " TEXT UNIQUE NOT NULL, " +
                COL_ADMIN_SENHA_HASH + " TEXT NOT NULL, " +
                COL_ADMIN_TIPO + " TEXT NOT NULL, " +
                COL_ADMIN_DATA + " TEXT);");
    }

    // 🔥 MÉTODO CORRIGIDO: CRIAR APENAS ADMIN "admin" COM SENHA "master"
    private void criarAdminMasterCorrigido(SQLiteDatabase db) {
        try {
            // 🔥 LIMPAR TODOS OS USUÁRIOS EXISTENTES PRIMEIRO
            db.delete(TABELA_USUARIOS_ADMIN, null, null);
            Log.d("DB_HELPER", "🗑️ Todos os usuários anteriores removidos");

            // 🔥 CALCULAR HASH CORRETO PARA SENHA "master"
            String hashCorretoMaster = calcularSHA256("master");
            Log.d("DB_HELPER", "🔑 Hash calculado para 'master': " + hashCorretoMaster);

            // 🔥 CRIAR APENAS O USUÁRIO "admin" COM SENHA "master"
            ContentValues cv = new ContentValues();
            cv.put(COL_ADMIN_USUARIO, "admin"); // 🔥 USUÁRIO: admin
            cv.put(COL_ADMIN_SENHA_HASH, hashCorretoMaster); // 🔥 SENHA: master
            cv.put(COL_ADMIN_TIPO, "master");
            cv.put(COL_ADMIN_DATA, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

            long resultado = db.insert(TABELA_USUARIOS_ADMIN, null, cv);

            if (resultado != -1) {
                Log.d("DB_HELPER", "✅ ADMIN MASTER CRIADO COM SUCESSO:");
                Log.d("DB_HELPER", "   👤 Usuário: admin");
                Log.d("DB_HELPER", "   🔑 Senha: master");
                Log.d("DB_HELPER", "   🔐 Hash: " + hashCorretoMaster);
                Log.d("DB_HELPER", "   🏷️ Tipo: master");
            } else {
                Log.e("DB_HELPER", "❌ ERRO AO CRIAR ADMIN MASTER");
            }

        } catch (Exception e) {
            Log.e("DB_HELPER", "💥 ERRO CRÍTICO ao criar admin master: " + e.getMessage());
        }
    }

    // 🔧 MÉTODO PARA CALCULAR SHA-256 CORRETAMENTE
    private String calcularSHA256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            Log.e("DB_HELPER", "❌ Erro ao calcular SHA-256: " + e.getMessage());
            return null;
        }
    }

    // 🔥 NOVO MÉTODO: SINCRONIZAR ADMINS COM SUPABASE
    private void sincronizarAdminsComSupabase() {
        Log.d("DB_HELPER", "🔥 SINCRONIZANDO ADMINS COM SUPABASE");

        new Thread(() -> {
            try {
                // Aguardar um pouco para garantir que o banco está pronto
                Thread.sleep(2000);

                // Sincronizar tabela de admins
                if (syncManager != null) {
                    syncManager.sincronizarTabelaEspecifica(TABELA_USUARIOS_ADMIN, "usuarios_admin");
                }
            } catch (Exception e) {
                Log.e("DB_HELPER", "💥 Erro ao sincronizar admins: " + e.getMessage());
            }
        }).start();
    }

    // 🔹 INSERIR MORADOR COM SINCRONIZAÇÃO MELHORADA
    public long inserirMorador(String cod, String nome, String cpf, String email,
                               String rua, String numero, String telefone,
                               String quadra, String lote, String imagemUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = -1;

        try {
            ContentValues cv = new ContentValues();
            cv.put(COL_COD, cod);
            cv.put(COL_NOME, nome);
            cv.put(COL_CPF, cpf);
            cv.put(COL_EMAIL, email);
            cv.put(COL_RUA, rua);
            cv.put(COL_NUMERO, numero);
            cv.put(COL_TELEFONE, telefone);
            cv.put(COL_QUADRA, quadra);
            cv.put(COL_LOTE, lote);
            cv.put(COL_IMAGEM_URI, imagemUri);

            id = db.insert(TABELA_MORADORES, null, cv);

            if (id != -1) {
                Log.d("DB_HELPER", "✅ Morador inserido localmente - ID: " + id);

                // 🔥 SINCRONIZAR COM SUPABASE COM VERIFICAÇÃO
                if (syncManager != null) {
                    // Sincronizar em background
                    new Thread(() -> {
                        try {
                            syncManager.sincronizarMoradorEspecifico(nome, email, telefone, cpf,
                                    rua, numero, quadra, lote);
                        } catch (Exception e) {
                            Log.e("DB_HELPER", "💥 Erro na sincronização em background: " + e.getMessage());
                        }
                    }).start();
                } else {
                    Log.e("DB_HELPER", "❌ SyncManager é null");
                }
            }

        } catch (Exception e) {
            Log.e("DB_HELPER", "❌ Erro ao inserir morador: " + e.getMessage());
        } finally {
            db.close();
        }

        return id;
    }

    // 🔹 ATUALIZAR MORADOR COM SINCRONIZAÇÃO
    public boolean atualizarMorador(int id, String cod, String nome, String cpf, String email,
                                    String rua, String numero, String telefone,
                                    String quadra, String lote, String imagemUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;

        try {
            ContentValues cv = new ContentValues();
            cv.put(COL_COD, cod);
            cv.put(COL_NOME, nome);
            cv.put(COL_CPF, cpf);
            cv.put(COL_EMAIL, email);
            cv.put(COL_RUA, rua);
            cv.put(COL_NUMERO, numero);
            cv.put(COL_TELEFONE, telefone);
            cv.put(COL_QUADRA, quadra);
            cv.put(COL_LOTE, lote);
            cv.put(COL_IMAGEM_URI, imagemUri);

            int rowsAffected = db.update(TABELA_MORADORES, cv, COL_ID + " = ?",
                    new String[]{String.valueOf(id)});
            success = rowsAffected > 0;

            if (success) {
                Log.d("DB_HELPER", "✅ Morador atualizado localmente - ID: " + id);
                // 🔥 SINCRONIZAR ATUALIZAÇÃO COM SUPABASE
                if (syncManager != null) {
                    syncManager.atualizarMoradorSupabase(id, nome, email, telefone, cpf, rua, numero, quadra, lote);
                }
            }

        } catch (Exception e) {
            Log.e("DB_HELPER", "❌ Erro ao atualizar morador: " + e.getMessage());
        } finally {
            db.close();
        }

        return success;
    }

    // 🔹 EXCLUIR MORADOR COM SINCRONIZAÇÃO
    public boolean excluirMorador(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean success = false;

        try {
            // Primeiro buscar dados para sincronização
            String cpf = obterCpfMorador(id);

            int rowsAffected = db.delete(TABELA_MORADORES, COL_ID + " = ?",
                    new String[]{String.valueOf(id)});
            success = rowsAffected > 0;

            if (success) {
                Log.d("DB_HELPER", "✅ Morador excluído localmente - ID: " + id);
                // 🔥 SINCRONIZAR EXCLUSÃO COM SUPABASE
                if (syncManager != null && cpf != null) {
                    syncManager.excluirMoradorSupabase(cpf);
                }
            }

        } catch (Exception e) {
            Log.e("DB_HELPER", "❌ Erro ao excluir morador: " + e.getMessage());
        } finally {
            db.close();
        }

        return success;
    }

    // 🔹 INSERIR ADMIN COM SINCRONIZAÇÃO CORRIGIDA
    public long inserirAdmin(String usuario, String senha, String tipo) {
        SQLiteDatabase db = this.getWritableDatabase();
        long id = -1;

        try {
            String senhaHash = calcularSHA256(senha);

            ContentValues cv = new ContentValues();
            cv.put(COL_ADMIN_USUARIO, usuario);
            cv.put(COL_ADMIN_SENHA_HASH, senhaHash);
            cv.put(COL_ADMIN_TIPO, tipo);
            cv.put(COL_ADMIN_DATA, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

            id = db.insert(TABELA_USUARIOS_ADMIN, null, cv);

            if (id != -1) {
                Log.d("DB_HELPER", "✅ Admin inserido localmente - Usuário: " + usuario);

                // 🔥 SINCRONIZAR COM SUPABASE COM VERIFICAÇÃO
                if (syncManager != null) {
                    // Verificar se usuário está autenticado
                    if (!syncManager.isUsuarioLogado()) {
                        Log.w("DB_HELPER", "⚠️ Usuário não autenticado. Admin salvo apenas localmente.");
                        return id;
                    }

                    // Verificar se é superadmin (apenas superadmins podem criar outros admins)
                    if (!"superadmin".equals(syncManager.getUserRole())) {
                        Log.w("DB_HELPER", "⚠️ Apenas superadmins podem criar outros administradores");
                        return id;
                    }

                    // Sincronizar em background
                    new Thread(() -> {
                        try {
                            syncManager.sincronizarAdminEspecifico(usuario, senhaHash, tipo);
                            Log.d("DB_HELPER", "🔄 Admin enviado para sincronização: " + usuario);
                        } catch (Exception e) {
                            Log.e("DB_HELPER", "💥 Erro na sincronização do admin: " + e.getMessage());
                        }
                    }).start();
                } else {
                    Log.e("DB_HELPER", "❌ SyncManager é null");
                }
            }

        } catch (Exception e) {
            Log.e("DB_HELPER", "❌ Erro ao inserir admin: " + e.getMessage());
        } finally {
            db.close();
        }

        return id;
    }

    // --- MÉTODOS AUXILIARES ---
    private String obterCpfMorador(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;

        try {
            cursor = db.query(TABELA_MORADORES, new String[]{COL_CPF},
                    COL_ID + " = ?", new String[]{String.valueOf(id)},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(cursor.getColumnIndexOrThrow(COL_CPF));
            }
            return null;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    // --- MÉTODOS DE SINCRONIZAÇÃO ---
    public void sincronizarTudo() {
        Log.d("DB_HELPER", "🔄 INICIANDO SINCRONIZAÇÃO COMPLETA");
        if (syncManager != null) {
            syncManager.sincronizarTudo();
        }
    }

    public void sincronizacaoRapida() {
        Log.d("DB_HELPER", "⚡ SINCRONIZAÇÃO RÁPIDA");
        if (syncManager != null) {
            syncManager.sincronizacaoRapida();
        }
    }

    // 🔥 NOVO MÉTODO: FORÇAR SINCRONIZAÇÃO DE ADMINS
    public void forcarSincronizacaoAdmins() {
        Log.d("DB_HELPER", "🔥 FORÇANDO SINCRONIZAÇÃO DE ADMINS");
        if (syncManager != null) {
            syncManager.sincronizarAdmins();
        }
    }

    // --- MÉTODOS DE VERIFICAÇÃO ---
    public void verificarCompatibilidadeSupabase() {
        Log.d("DB_HELPER", "🔍 VERIFICANDO COMPATIBILIDADE COM SUPABASE:");
        String[] tabelas = {TABELA_MORADORES, TABELA_OCORRENCIAS, TABELA_FUNCIONARIOS,
                TABELA_MANUTENCOES, TABELA_ASSEMBLEIAS, TABELA_DESPESAS,
                TABELA_AVISOS, TABELA_USUARIOS_ADMIN};

        for (String tabela : tabelas) {
            if (tabelaExiste(tabela)) {
                int registros = contarRegistros(tabela);
                Log.d("DB_HELPER", "   ✅ " + tabela + ": " + registros + " registros");
            } else {
                Log.d("DB_HELPER", "   ❌ " + tabela + ": NÃO EXISTE");
            }
        }
    }

    public boolean tabelaExiste(String tabelaNome) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                    new String[]{tabelaNome});
            return cursor != null && cursor.getCount() > 0;
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    public int contarRegistros(String tabelaNome) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + tabelaNome, null);
            if (cursor != null && cursor.moveToFirst()) return cursor.getInt(0);
        } finally {
            if (cursor != null) cursor.close();
        }
        return 0;
    }

    public static String gerarHash(String senha) {
        try {
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(senha.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public boolean existeAdmin() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = null;
        try {
            c = db.rawQuery("SELECT COUNT(*) FROM " + TABELA_USUARIOS_ADMIN, null);
            if (c != null && c.moveToFirst()) return c.getInt(0) > 0;
        } finally {
            if (c != null) c.close();
        }
        return false;
    }

    // 🔹 MÉTODO PARA SINCRONIZAR ADMINS MANUALMENTE (NOVO)
    public void sincronizarAdminsManualmente() {
        Log.d("DB_HELPER", "🔄 SINCRONIZANDO ADMINS MANUALMENTE");

        if (syncManager == null) {
            Log.e("DB_HELPER", "❌ SyncManager não disponível");
            return;
        }

        if (!syncManager.isUsuarioLogado()) {
            Log.w("DB_HELPER", "⚠️ Usuário não autenticado para sincronização");
            return;
        }

        new Thread(() -> {
            try {
                // Buscar todos os admins locais
                SQLiteDatabase db = getReadableDatabase();
                Cursor cursor = db.query(TABELA_USUARIOS_ADMIN,
                        new String[]{COL_ADMIN_USUARIO, COL_ADMIN_SENHA_HASH, COL_ADMIN_TIPO, COL_ADMIN_DATA},
                        null, null, null, null, null);

                int adminsSincronizados = 0;

                while (cursor.moveToNext()) {
                    String usuario = cursor.getString(cursor.getColumnIndexOrThrow(COL_ADMIN_USUARIO));
                    String senhaHash = cursor.getString(cursor.getColumnIndexOrThrow(COL_ADMIN_SENHA_HASH));
                    String tipo = cursor.getString(cursor.getColumnIndexOrThrow(COL_ADMIN_TIPO));

                    // Sincronizar cada admin
                    syncManager.sincronizarAdminEspecifico(usuario, senhaHash, tipo);
                    adminsSincronizados++;

                    // Pequena pausa para não sobrecarregar
                    Thread.sleep(500);
                }

                cursor.close();
                db.close();

                Log.d("DB_HELPER", "✅ " + adminsSincronizados + " admins sincronizados com Supabase");

            } catch (Exception e) {
                Log.e("DB_HELPER", "💥 Erro na sincronização manual: " + e.getMessage());
            }
        }).start();
    }

    // 🔹 MÉTODO PARA DEBUG DAS TABELAS
    public void debugTabelas() {
        Log.d("DB_HELPER", "📊 DEBUG DAS TABELAS:");

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String tableName = cursor.getString(0);
                    int count = contarRegistros(tableName);
                    Log.d("DB_HELPER", "   📋 " + tableName + " - Registros: " + count);
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            Log.e("DB_HELPER", "❌ Erro no debug: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
        }
    }

    // 🔹 MÉTODO PARA OBTER CREDENCIAIS DE TESTE
    public String[] getCredenciaisTeste() {
        return new String[]{
                "admin", // 🔥 AGORA É "admin"
                "master"  // 🔥 AGORA É "master"
        };
    }
}