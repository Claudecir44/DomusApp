package com.example.domus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.domus.utils.SecurityUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BDCondominioHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "bdcondominio.db";
    private static final int DATABASE_VERSION = 24; // Incrementado para versão com CARGO

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
    public static final String COL_USUARIO = "usuario";
    public static final String COL_SENHA = "senha";

    // Colunas Ocorrencias
    public static final String COL_OCOR_ID = "id";
    public static final String COL_OCOR_TIPO = "tipo";
    public static final String COL_OCOR_ENVOLVIDOS = "envolvidos";
    public static final String COL_OCOR_DESCRICAO = "descricao";
    public static final String COL_OCOR_DATAHORA = "datahora";
    public static final String COL_OCOR_ANEXOS = "anexos";
    public static final String COL_OCOR_STATUS = "status";

    // Colunas Funcionarios
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
    public static final String COL_FUNC_CARGO = "cargo"; // NOVA COLUNA

    // Colunas Manutencoes
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
    public static final String COL_AVISO_PRIORIDADE = "prioridade";

    private static final String TAG = "BDCondominioHelper";

    public BDCondominioHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "🔄 Criando todas as tabelas...");
        criarTodasTabelas(db);
        Log.d(TAG, "✅ Banco SQLite criado com sucesso");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "🔄 Atualizando banco da versão " + oldVersion + " para " + newVersion);
        recriarTodasTabelas(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    private void criarTodasTabelas(SQLiteDatabase db) {
        criarTabelaUsuariosAdmin(db);
        criarTabelaMoradores(db);
        criarTabelaOcorrencias(db);
        criarTabelaFuncionarios(db);
        criarTabelaManutencoes(db);
        criarTabelaAssembleias(db);
        criarTabelaDespesas(db);
        criarTabelaAvisos(db);
    }

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
    }

    private void criarTabelaUsuariosAdmin(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABELA_USUARIOS_ADMIN + " (" +
                COL_ADMIN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ADMIN_USUARIO + " TEXT UNIQUE NOT NULL, " +
                COL_ADMIN_SENHA_HASH + " TEXT NOT NULL, " +
                COL_ADMIN_TIPO + " TEXT NOT NULL, " +
                COL_ADMIN_DATA + " TEXT" +
                ");";
        db.execSQL(sql);
        Log.d(TAG, "✅ Tabela " + TABELA_USUARIOS_ADMIN + " criada");
    }

    private void criarTabelaMoradores(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABELA_MORADORES + " (" +
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
                COL_IMAGEM_URI + " TEXT, " +
                COL_USUARIO + " TEXT UNIQUE, " +
                COL_SENHA + " TEXT" +
                ");";
        db.execSQL(sql);
        Log.d(TAG, "✅ Tabela " + TABELA_MORADORES + " criada");
    }

    private void criarTabelaOcorrencias(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABELA_OCORRENCIAS + " (" +
                COL_OCOR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_OCOR_TIPO + " TEXT, " +
                COL_OCOR_ENVOLVIDOS + " TEXT, " +
                COL_OCOR_DESCRICAO + " TEXT NOT NULL, " +
                COL_OCOR_DATAHORA + " TEXT NOT NULL, " +
                COL_OCOR_STATUS + " TEXT DEFAULT 'pendente', " +
                COL_OCOR_ANEXOS + " TEXT);";
        db.execSQL(sql);
        Log.d(TAG, "✅ Tabela " + TABELA_OCORRENCIAS + " criada");
    }

    private void criarTabelaFuncionarios(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABELA_FUNCIONARIOS + " (" +
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
                COL_FUNC_IMAGEM_URI + " TEXT, " +
                COL_FUNC_CARGO + " TEXT" +
                ");";
        db.execSQL(sql);
        Log.d(TAG, "✅ Tabela " + TABELA_FUNCIONARIOS + " criada com coluna cargo");
    }

    private void criarTabelaManutencoes(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABELA_MANUTENCOES + " (" +
                COL_MANU_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_MANU_TIPO + " TEXT NOT NULL, " +
                COL_MANU_DATAHORA + " TEXT NOT NULL, " +
                COL_MANU_LOCAL + " TEXT, " +
                COL_MANU_SERVICO + " TEXT, " +
                COL_MANU_RESPONSAVEL + " TEXT, " +
                COL_MANU_VALOR + " TEXT, " +
                COL_MANU_NOTAS + " TEXT, " +
                COL_MANU_ANEXOS + " TEXT);";
        db.execSQL(sql);
        Log.d(TAG, "✅ Tabela " + TABELA_MANUTENCOES + " criada");
    }

    private void criarTabelaAssembleias(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABELA_ASSEMBLEIAS + " (" +
                COL_ASS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ASS_DATAHORA + " TEXT NOT NULL, " +
                COL_ASS_LOCAL + " TEXT, " +
                COL_ASS_ASSUNTO + " TEXT NOT NULL, " +
                COL_ASS_DESCRICAO + " TEXT, " +
                COL_ASS_ANEXOS + " TEXT);";
        db.execSQL(sql);
        Log.d(TAG, "✅ Tabela " + TABELA_ASSEMBLEIAS + " criada");
    }

    private void criarTabelaDespesas(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABELA_DESPESAS + " (" +
                COL_DESP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DESP_DATAHORA + " TEXT NOT NULL, " +
                COL_DESP_NOME + " TEXT NOT NULL, " +
                COL_DESP_DESCRICAO + " TEXT, " +
                COL_DESP_VALOR + " REAL, " +
                COL_DESP_ANEXOS + " TEXT);";
        db.execSQL(sql);
        Log.d(TAG, "✅ Tabela " + TABELA_DESPESAS + " criada");
    }

    private void criarTabelaAvisos(SQLiteDatabase db) {
        String sql = "CREATE TABLE " + TABELA_AVISOS + " (" +
                COL_AVISO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_AVISO_DATAHORA + " TEXT NOT NULL, " +
                COL_AVISO_ASSUNTO + " TEXT NOT NULL, " +
                COL_AVISO_DESCRICAO + " TEXT, " +
                COL_AVISO_PRIORIDADE + " TEXT DEFAULT 'normal', " +
                COL_AVISO_ANEXOS + " TEXT, " +
                COL_AVISO_CRIADO_EM + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                COL_AVISO_ATUALIZADO_EM + " DATETIME DEFAULT CURRENT_TIMESTAMP);";
        db.execSQL(sql);
        Log.d(TAG, "✅ Tabela " + TABELA_AVISOS + " criada");
    }

    // ============================================================
    // MÉTODOS PÚBLICOS
    // ============================================================

    public boolean cadastrarAdmin(String usuario, String senhaHash) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_ADMIN_USUARIO, usuario);
        values.put(COL_ADMIN_SENHA_HASH, senhaHash);
        values.put(COL_ADMIN_TIPO, "admin");
        values.put(COL_ADMIN_DATA, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));

        long resultado = db.insert(TABELA_USUARIOS_ADMIN, null, values);
        db.close();
        return resultado != -1;
    }

    public boolean removerAdmin(String usuario) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABELA_USUARIOS_ADMIN,
                COL_ADMIN_USUARIO + "=?",
                new String[]{usuario});
        db.close();
        Log.d(TAG, "🗑️ Admin removido: " + usuario + " - Linhas afetadas: " + rows);
        return rows > 0;
    }

    public int removerTodosAdmins() {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABELA_USUARIOS_ADMIN, null, null);
        db.close();
        Log.d(TAG, "🗑️ Todos os admins removidos. Total: " + rows);
        return rows;
    }

    public boolean existeAdmin() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = null;
        try {
            c = db.rawQuery("SELECT COUNT(*) FROM " + TABELA_USUARIOS_ADMIN, null);
            if (c != null && c.moveToFirst()) {
                return c.getInt(0) > 0;
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao verificar admin: " + e.getMessage());
        } finally {
            if (c != null) c.close();
            db.close();
        }
        return false;
    }

    public boolean verificarCredenciaisAdmin(String usuario, String senha) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            String hashSenha = SecurityUtils.gerarHash(senha);
            if (hashSenha == null) {
                Log.e(TAG, "❌ Erro ao gerar hash da senha");
                return false;
            }

            cursor = db.query(TABELA_USUARIOS_ADMIN,
                    new String[]{COL_ADMIN_ID},
                    COL_ADMIN_USUARIO + " = ? AND " + COL_ADMIN_SENHA_HASH + " = ?",
                    new String[]{usuario, hashSenha},
                    null, null, null);

            return cursor != null && cursor.getCount() > 0;
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao verificar credenciais: " + e.getMessage());
            return false;
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }

    public boolean tabelaExiste(String tabelaNome) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean existe = false;
        try {
            String query = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";
            cursor = db.rawQuery(query, new String[]{tabelaNome});
            existe = (cursor.getCount() > 0);
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao verificar tabela: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return existe;
    }

    public int contarRegistros(String tabelaNome) {
        int count = 0;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT COUNT(*) FROM " + tabelaNome, null);
            if (cursor != null && cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao contar registros da tabela " + tabelaNome + ": " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
        return count;
    }

    public void debugTabelas() {
        Log.d(TAG, "📊 DEBUG DAS TABELAS:");
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table'", null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    String tableName = cursor.getString(0);
                    int count = contarRegistros(tableName);
                    Log.d(TAG, "   📋 " + tableName + " - Registros: " + count);
                } while (cursor.moveToNext());
            } else {
                Log.d(TAG, "   ❌ Nenhuma tabela encontrada");
            }
        } catch (Exception e) {
            Log.e(TAG, "💥 Erro no debug: " + e.getMessage());
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }
    }
}