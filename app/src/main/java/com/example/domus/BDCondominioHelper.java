package com.example.domus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class BDCondominioHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "bdcondominio.db";
    private static final int DATABASE_VERSION = 14;

    // Tabelas
    public static final String TABELA_MORADORES = "moradores";
    public static final String TABELA_OCORRENCIAS = "ocorrencias";
    public static final String TABELA_FUNCIONARIOS = "funcionarios";
    public static final String TABELA_MANUTENCOES = "manutencoes";
    public static final String TABELA_ASSEMBLEIAS = "assembleias";
    public static final String TABELA_DESPESAS = "despesas";
    public static final String TABELA_USUARIOS_ADMIN = "usuarios_admin";
    public static final String TABELA_AVISOS = "avisos";

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
    public static final String COL_MANU_DATAHORA = "dataHora";
    public static final String COL_MANU_LOCAL = "local";
    public static final String COL_MANU_SERVICO = "servico";
    public static final String COL_MANU_RESPONSAVEL = "responsavel";
    public static final String COL_MANU_VALOR = "valor";
    public static final String COL_MANU_NOTAS = "notas";
    public static final String COL_MANU_DOCUMENTO = "documento";

    // Colunas Assembleias
    public static final String COL_ASS_ID = "id";
    public static final String COL_ASS_DATAHORA = "dataHora";
    public static final String COL_ASS_LOCAL = "local";
    public static final String COL_ASS_ASSUNTO = "assunto";
    public static final String COL_ASS_DESCRICAO = "descricao";
    public static final String COL_ASS_DOCUMENTO = "documento";

    // Colunas Despesas
    public static final String COL_DESP_ID = "id";
    public static final String COL_DESP_DATAHORA = "dataHora";
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

    public BDCondominioHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        criarTodasTabelas(db);
        criarAdminMaster(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop de todas as tabelas para garantir instalação limpa
        db.execSQL("DROP TABLE IF EXISTS " + TABELA_MORADORES);
        db.execSQL("DROP TABLE IF EXISTS " + TABELA_OCORRENCIAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABELA_FUNCIONARIOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABELA_MANUTENCOES);
        db.execSQL("DROP TABLE IF EXISTS " + TABELA_ASSEMBLEIAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABELA_DESPESAS);
        db.execSQL("DROP TABLE IF EXISTS " + TABELA_AVISOS);
        db.execSQL("DROP TABLE IF EXISTS " + TABELA_USUARIOS_ADMIN);

        // Recriar todas as tabelas vazias
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    private void criarTodasTabelas(SQLiteDatabase db) {
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

        db.execSQL("CREATE TABLE " + TABELA_OCORRENCIAS + " (" +
                COL_OCOR_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_OCOR_TIPO + " TEXT, " +
                COL_OCOR_ENVOLVIDOS + " TEXT, " +
                COL_OCOR_DESCRICAO + " TEXT NOT NULL, " +
                COL_OCOR_DATAHORA + " TEXT NOT NULL, " +
                COL_OCOR_ANEXOS + " TEXT);");

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

        db.execSQL("CREATE TABLE " + TABELA_MANUTENCOES + " (" +
                COL_MANU_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_MANU_TIPO + " TEXT NOT NULL, " +
                COL_MANU_DATAHORA + " TEXT NOT NULL, " +
                COL_MANU_LOCAL + " TEXT, " +
                COL_MANU_SERVICO + " TEXT, " +
                COL_MANU_RESPONSAVEL + " TEXT, " +
                COL_MANU_VALOR + " TEXT, " +
                COL_MANU_NOTAS + " TEXT, " +
                COL_MANU_DOCUMENTO + " TEXT);");

        db.execSQL("CREATE TABLE " + TABELA_ASSEMBLEIAS + " (" +
                COL_ASS_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ASS_DATAHORA + " TEXT NOT NULL, " +
                COL_ASS_LOCAL + " TEXT, " +
                COL_ASS_ASSUNTO + " TEXT NOT NULL, " +
                COL_ASS_DESCRICAO + " TEXT, " +
                COL_ASS_DOCUMENTO + " TEXT);");

        db.execSQL("CREATE TABLE " + TABELA_DESPESAS + " (" +
                COL_DESP_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_DESP_DATAHORA + " TEXT NOT NULL, " +
                COL_DESP_NOME + " TEXT NOT NULL, " +
                COL_DESP_DESCRICAO + " TEXT, " +
                COL_DESP_VALOR + " REAL, " +
                COL_DESP_ANEXOS + " TEXT);");

        db.execSQL("CREATE TABLE " + TABELA_AVISOS + " (" +
                COL_AVISO_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_AVISO_DATAHORA + " TEXT NOT NULL, " +
                COL_AVISO_ASSUNTO + " TEXT NOT NULL, " +
                COL_AVISO_DESCRICAO + " TEXT, " +
                COL_AVISO_ANEXOS + " TEXT, " +
                COL_AVISO_CRIADO_EM + " DATETIME DEFAULT CURRENT_TIMESTAMP, " +
                COL_AVISO_ATUALIZADO_EM + " DATETIME DEFAULT CURRENT_TIMESTAMP);");

        db.execSQL("CREATE TABLE " + TABELA_USUARIOS_ADMIN + " (" +
                COL_ADMIN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ADMIN_USUARIO + " TEXT UNIQUE NOT NULL, " +
                COL_ADMIN_SENHA_HASH + " TEXT NOT NULL, " +
                COL_ADMIN_TIPO + " TEXT NOT NULL, " +
                COL_ADMIN_DATA + " TEXT);");
    }

    private void criarAdminMaster(SQLiteDatabase db) {
        ContentValues cv = new ContentValues();
        cv.put(COL_ADMIN_USUARIO, "admin");
        cv.put(COL_ADMIN_SENHA_HASH, gerarHash("master"));
        cv.put(COL_ADMIN_TIPO, "MASTER");
        cv.put(COL_ADMIN_DATA, String.valueOf(System.currentTimeMillis()));
        db.insert(TABELA_USUARIOS_ADMIN, null, cv);
    }

    public boolean existeAdmin() {
        SQLiteDatabase db = this.getReadableDatabase();
        int count = 0;
        Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABELA_USUARIOS_ADMIN, null);
        if (c != null) {
            if (c.moveToFirst()) count = c.getInt(0);
            c.close();
        }
        return count > 0;
    }

    // Método de hash SHA-256 padronizado
    public static String gerarHash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}