package com.example.domus;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.ArrayList;
import java.util.List;

public class ManutencaoDAO {

    private SQLiteDatabase db;
    private BDCondominioHelper dbHelper;

    public ManutencaoDAO(Context context) {
        dbHelper = new BDCondominioHelper(context);
    }

    public void abrir() {
        db = dbHelper.getWritableDatabase();
    }

    public void fechar() {
        dbHelper.close();
    }

    // Método para obter manutenção por ID (usando long)
    public Manutencao getManutencaoById(long id) {
        return getManutencaoPorId((int) id);
    }

    // Método para salvar manutenção
    public long salvarManutencao(Manutencao manutencao) {
        abrir();

        ContentValues values = new ContentValues();
        values.put(BDCondominioHelper.COL_MANU_TIPO, manutencao.getTipo());
        values.put(BDCondominioHelper.COL_MANU_DATAHORA, manutencao.getDataHora());
        values.put(BDCondominioHelper.COL_MANU_LOCAL, manutencao.getLocal());
        values.put(BDCondominioHelper.COL_MANU_SERVICO, manutencao.getServico());
        values.put(BDCondominioHelper.COL_MANU_RESPONSAVEL, manutencao.getResponsavel());
        values.put(BDCondominioHelper.COL_MANU_VALOR, manutencao.getValor());
        values.put(BDCondominioHelper.COL_MANU_NOTAS, manutencao.getNotas());

        // Tratar anexos (convertendo lista para string)
        if (manutencao.getAnexos() != null && !manutencao.getAnexos().isEmpty()) {
            values.put(BDCondominioHelper.COL_MANU_ANEXOS,
                    android.text.TextUtils.join(",", manutencao.getAnexos()));
        } else {
            values.putNull(BDCondominioHelper.COL_MANU_ANEXOS);
        }

        long id = db.insert(BDCondominioHelper.TABELA_MANUTENCOES, null, values);
        fechar();
        return id;
    }

    // Método para obter todas as manutenções
    public List<Manutencao> getTodasManutencoes() {
        abrir();
        List<Manutencao> manutencoes = new ArrayList<>();

        String[] colunas = {
                BDCondominioHelper.COL_MANU_ID,
                BDCondominioHelper.COL_MANU_TIPO,
                BDCondominioHelper.COL_MANU_DATAHORA,
                BDCondominioHelper.COL_MANU_LOCAL,
                BDCondominioHelper.COL_MANU_SERVICO,
                BDCondominioHelper.COL_MANU_RESPONSAVEL,
                BDCondominioHelper.COL_MANU_VALOR,
                BDCondominioHelper.COL_MANU_NOTAS,
                BDCondominioHelper.COL_MANU_ANEXOS
        };

        Cursor cursor = db.query(BDCondominioHelper.TABELA_MANUTENCOES,
                colunas, null, null, null, null,
                BDCondominioHelper.COL_MANU_DATAHORA + " DESC");

        if (cursor != null && cursor.moveToFirst()) {
            do {
                Manutencao manutencao = new Manutencao();
                manutencao.setId(cursor.getInt(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_ID)));
                manutencao.setTipo(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_TIPO)));
                manutencao.setDataHora(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_DATAHORA)));
                manutencao.setLocal(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_LOCAL)));
                manutencao.setServico(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_SERVICO)));
                manutencao.setResponsavel(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_RESPONSAVEL)));
                manutencao.setValor(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_VALOR)));
                manutencao.setNotas(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_NOTAS)));

                // Tratar anexos
                String anexosStr = cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_ANEXOS));
                if (anexosStr != null && !anexosStr.isEmpty()) {
                    List<String> anexos = new ArrayList<>();
                    String[] anexosArray = anexosStr.split(",");
                    for (String anexo : anexosArray) {
                        if (!anexo.trim().isEmpty()) {
                            anexos.add(anexo.trim());
                        }
                    }
                    manutencao.setAnexos(anexos);
                }

                manutencoes.add(manutencao);
            } while (cursor.moveToNext());

            cursor.close();
        }

        fechar();
        return manutencoes;
    }

    // Método para excluir manutenção
    public int excluirManutencao(int id) {
        abrir();
        int linhasAfetadas = db.delete(BDCondominioHelper.TABELA_MANUTENCOES,
                BDCondominioHelper.COL_MANU_ID + " = ?",
                new String[]{String.valueOf(id)});
        fechar();
        return linhasAfetadas;
    }

    // Método para obter manutenção por ID (para edição)
    public Manutencao getManutencaoPorId(int id) {
        abrir();
        Manutencao manutencao = null;

        String[] colunas = {
                BDCondominioHelper.COL_MANU_ID,
                BDCondominioHelper.COL_MANU_TIPO,
                BDCondominioHelper.COL_MANU_DATAHORA,
                BDCondominioHelper.COL_MANU_LOCAL,
                BDCondominioHelper.COL_MANU_SERVICO,
                BDCondominioHelper.COL_MANU_RESPONSAVEL,
                BDCondominioHelper.COL_MANU_VALOR,
                BDCondominioHelper.COL_MANU_NOTAS,
                BDCondominioHelper.COL_MANU_ANEXOS
        };

        Cursor cursor = db.query(BDCondominioHelper.TABELA_MANUTENCOES,
                colunas,
                BDCondominioHelper.COL_MANU_ID + " = ?",
                new String[]{String.valueOf(id)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            manutencao = new Manutencao();
            manutencao.setId(cursor.getInt(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_ID)));
            manutencao.setTipo(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_TIPO)));
            manutencao.setDataHora(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_DATAHORA)));
            manutencao.setLocal(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_LOCAL)));
            manutencao.setServico(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_SERVICO)));
            manutencao.setResponsavel(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_RESPONSAVEL)));
            manutencao.setValor(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_VALOR)));
            manutencao.setNotas(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_NOTAS)));

            // Tratar anexos
            String anexosStr = cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_MANU_ANEXOS));
            if (anexosStr != null && !anexosStr.isEmpty()) {
                List<String> anexos = new ArrayList<>();
                String[] anexosArray = anexosStr.split(",");
                for (String anexo : anexosArray) {
                    if (!anexo.trim().isEmpty()) {
                        anexos.add(anexo.trim());
                    }
                }
                manutencao.setAnexos(anexos);
            }

            cursor.close();
        }

        fechar();
        return manutencao;
    }

    // Método para atualizar manutenção
    public int atualizarManutencao(Manutencao manutencao) {
        abrir();

        ContentValues values = new ContentValues();
        values.put(BDCondominioHelper.COL_MANU_TIPO, manutencao.getTipo());
        values.put(BDCondominioHelper.COL_MANU_DATAHORA, manutencao.getDataHora());
        values.put(BDCondominioHelper.COL_MANU_LOCAL, manutencao.getLocal());
        values.put(BDCondominioHelper.COL_MANU_SERVICO, manutencao.getServico());
        values.put(BDCondominioHelper.COL_MANU_RESPONSAVEL, manutencao.getResponsavel());
        values.put(BDCondominioHelper.COL_MANU_VALOR, manutencao.getValor());
        values.put(BDCondominioHelper.COL_MANU_NOTAS, manutencao.getNotas());

        // Tratar anexos
        if (manutencao.getAnexos() != null && !manutencao.getAnexos().isEmpty()) {
            values.put(BDCondominioHelper.COL_MANU_ANEXOS,
                    android.text.TextUtils.join(",", manutencao.getAnexos()));
        } else {
            values.putNull(BDCondominioHelper.COL_MANU_ANEXOS);
        }

        int linhasAfetadas = db.update(BDCondominioHelper.TABELA_MANUTENCOES, values,
                BDCondominioHelper.COL_MANU_ID + " = ?",
                new String[]{String.valueOf(manutencao.getId())});
        fechar();
        return linhasAfetadas;
    }
}