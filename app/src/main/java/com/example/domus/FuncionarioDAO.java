package com.example.domus.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.domus.BDCondominioHelper;
import com.example.domus.domain.model.Funcionario;

import java.util.ArrayList;
import java.util.List;

public class FuncionarioDAO {

    private static final String TAG = "FuncionarioDAO";
    private BDCondominioHelper dbHelper;

    public FuncionarioDAO(Context context) {
        this.dbHelper = new BDCondominioHelper(context);
    }

    public long inserir(Funcionario funcionario) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(BDCondominioHelper.COL_FUNC_NOME, funcionario.getNome());
        values.put(BDCondominioHelper.COL_FUNC_RUA, funcionario.getRua());
        values.put(BDCondominioHelper.COL_FUNC_NUMERO, funcionario.getNumero());
        values.put(BDCondominioHelper.COL_FUNC_BAIRRO, funcionario.getBairro());
        values.put(BDCondominioHelper.COL_FUNC_CEP, funcionario.getCep());
        values.put(BDCondominioHelper.COL_FUNC_CIDADE, funcionario.getCidade());
        values.put(BDCondominioHelper.COL_FUNC_ESTADO, funcionario.getEstado());
        values.put(BDCondominioHelper.COL_FUNC_PAIS, funcionario.getPais());
        values.put(BDCondominioHelper.COL_FUNC_TELEFONE, funcionario.getTelefone());
        values.put(BDCondominioHelper.COL_FUNC_EMAIL, funcionario.getEmail());
        values.put(BDCondominioHelper.COL_FUNC_RG, funcionario.getRg());
        values.put(BDCondominioHelper.COL_FUNC_CPF, funcionario.getCpf());
        values.put(BDCondominioHelper.COL_FUNC_CARGA_MENSAL, funcionario.getCargaHoraria());
        values.put(BDCondominioHelper.COL_FUNC_TURNO, funcionario.getTurno());
        values.put(BDCondominioHelper.COL_FUNC_HORA_ENTRADA, funcionario.getHoraEntrada());
        values.put(BDCondominioHelper.COL_FUNC_HORA_SAIDA, funcionario.getHoraSaida());
        values.put(BDCondominioHelper.COL_FUNC_IMAGEM_URI, funcionario.getImagemUri());
        values.put(BDCondominioHelper.COL_FUNC_CARGO, funcionario.getCargo());

        long id = db.insert(BDCondominioHelper.TABELA_FUNCIONARIOS, null, values);
        db.close();

        Log.d(TAG, id != -1 ? "✅ Funcionário inserido: " + funcionario.getNome() : "❌ Erro ao inserir funcionário");
        return id;
    }

    public int atualizar(Funcionario funcionario) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(BDCondominioHelper.COL_FUNC_NOME, funcionario.getNome());
        values.put(BDCondominioHelper.COL_FUNC_RUA, funcionario.getRua());
        values.put(BDCondominioHelper.COL_FUNC_NUMERO, funcionario.getNumero());
        values.put(BDCondominioHelper.COL_FUNC_BAIRRO, funcionario.getBairro());
        values.put(BDCondominioHelper.COL_FUNC_CEP, funcionario.getCep());
        values.put(BDCondominioHelper.COL_FUNC_CIDADE, funcionario.getCidade());
        values.put(BDCondominioHelper.COL_FUNC_ESTADO, funcionario.getEstado());
        values.put(BDCondominioHelper.COL_FUNC_PAIS, funcionario.getPais());
        values.put(BDCondominioHelper.COL_FUNC_TELEFONE, funcionario.getTelefone());
        values.put(BDCondominioHelper.COL_FUNC_EMAIL, funcionario.getEmail());
        values.put(BDCondominioHelper.COL_FUNC_RG, funcionario.getRg());
        values.put(BDCondominioHelper.COL_FUNC_CPF, funcionario.getCpf());
        values.put(BDCondominioHelper.COL_FUNC_CARGA_MENSAL, funcionario.getCargaHoraria());
        values.put(BDCondominioHelper.COL_FUNC_TURNO, funcionario.getTurno());
        values.put(BDCondominioHelper.COL_FUNC_HORA_ENTRADA, funcionario.getHoraEntrada());
        values.put(BDCondominioHelper.COL_FUNC_HORA_SAIDA, funcionario.getHoraSaida());
        values.put(BDCondominioHelper.COL_FUNC_IMAGEM_URI, funcionario.getImagemUri());
        values.put(BDCondominioHelper.COL_FUNC_CARGO, funcionario.getCargo());

        int rows = db.update(BDCondominioHelper.TABELA_FUNCIONARIOS, values,
                BDCondominioHelper.COL_FUNC_ID + " = ?",
                new String[]{String.valueOf(funcionario.getId())});
        db.close();

        Log.d(TAG, rows > 0 ? "✅ Funcionário atualizado: " + funcionario.getNome() : "❌ Erro ao atualizar funcionário");
        return rows;
    }

    public void excluir(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(BDCondominioHelper.TABELA_FUNCIONARIOS,
                BDCondominioHelper.COL_FUNC_ID + " = ?",
                new String[]{String.valueOf(id)});
        db.close();
        Log.d(TAG, "🗑️ Funcionário excluído - ID: " + id);
    }

    public List<Funcionario> listarTodos() {
        List<Funcionario> lista = new ArrayList<>();
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(BDCondominioHelper.TABELA_FUNCIONARIOS,
                null, null, null, null, null,
                BDCondominioHelper.COL_FUNC_NOME + " ASC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Funcionario f = new Funcionario();
                f.setId(cursor.getInt(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_ID)));
                f.setNome(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_NOME)));
                f.setRua(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_RUA)));
                f.setNumero(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_NUMERO)));
                f.setBairro(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_BAIRRO)));
                f.setCep(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_CEP)));
                f.setCidade(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_CIDADE)));
                f.setEstado(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_ESTADO)));
                f.setPais(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_PAIS)));
                f.setTelefone(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_TELEFONE)));
                f.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_EMAIL)));
                f.setRg(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_RG)));
                f.setCpf(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_CPF)));
                f.setCargaHoraria(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_CARGA_MENSAL)));
                f.setTurno(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_TURNO)));
                f.setHoraEntrada(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_HORA_ENTRADA)));
                f.setHoraSaida(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_HORA_SAIDA)));
                f.setImagemUri(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_IMAGEM_URI)));
                f.setCargo(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_CARGO)));
                lista.add(f);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();

        Log.d(TAG, "📋 Funcionários carregados: " + lista.size());
        return lista;
    }

    public Funcionario buscarPorId(int id) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = db.query(BDCondominioHelper.TABELA_FUNCIONARIOS,
                null,
                BDCondominioHelper.COL_FUNC_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null);

        Funcionario funcionario = null;
        if (cursor != null && cursor.moveToFirst()) {
            funcionario = new Funcionario();
            funcionario.setId(cursor.getInt(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_ID)));
            funcionario.setNome(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_NOME)));
            funcionario.setRua(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_RUA)));
            funcionario.setNumero(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_NUMERO)));
            funcionario.setBairro(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_BAIRRO)));
            funcionario.setCep(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_CEP)));
            funcionario.setCidade(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_CIDADE)));
            funcionario.setEstado(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_ESTADO)));
            funcionario.setPais(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_PAIS)));
            funcionario.setTelefone(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_TELEFONE)));
            funcionario.setEmail(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_EMAIL)));
            funcionario.setRg(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_RG)));
            funcionario.setCpf(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_CPF)));
            funcionario.setCargaHoraria(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_CARGA_MENSAL)));
            funcionario.setTurno(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_TURNO)));
            funcionario.setHoraEntrada(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_HORA_ENTRADA)));
            funcionario.setHoraSaida(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_HORA_SAIDA)));
            funcionario.setImagemUri(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_IMAGEM_URI)));
            funcionario.setCargo(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_FUNC_CARGO)));
            cursor.close();
        }
        db.close();

        return funcionario;
    }
}