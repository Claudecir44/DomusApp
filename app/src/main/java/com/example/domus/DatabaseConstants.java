package com.example.domus.utils;

public class DatabaseConstants {
    // Admin table
    public static final String TABLE_ADMIN = "usuarios_admin";
    public static final String COL_ADMIN_USUARIO = "usuario";
    public static final String COL_ADMIN_SENHA_HASH = "senha_hash";
    public static final String COL_ADMIN_TIPO = "tipo";
    public static final String COL_ADMIN_DATA = "data_cadastro";

    // Admin types
    public static final String ADMIN_TYPE_MASTER = "master";
    public static final String ADMIN_TYPE_NORMAL = "admin";

    // Default master credentials
    public static final String MASTER_USERNAME = "admin";
    public static final String MASTER_PASSWORD = "master";

    private DatabaseConstants() {} // Prevent instantiation
}