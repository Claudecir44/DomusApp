package com.cjstudio.condominio_sociedade_morro_grande.data;

import android.content.Context;

import com.cjstudio.condominio_sociedade_morro_grande.domain.model.Despesa;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.File;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class DespesaDAO {

    private final Context context;
    private final String FILE_NAME = "despesas.json";
    private List<Despesa> listaDespesas;

    public DespesaDAO(Context context) {
        this.context = context;
        carregarDespesas();
    }

    public void inserir(Despesa despesa) {
        listaDespesas.add(despesa);
        salvarNoArquivo();
    }

    public void atualizar(int index, Despesa despesa) {
        if (index >= 0 && index < listaDespesas.size()) {
            listaDespesas.set(index, despesa);
            salvarNoArquivo();
        }
    }

    public void excluir(int index) {
        if (index >= 0 && index < listaDespesas.size()) {
            listaDespesas.remove(index);
            salvarNoArquivo();
        }
    }

    public List<Despesa> listarTodos() {
        return new ArrayList<>(listaDespesas);
    }

    public Despesa buscarPorIndex(int index) {
        if (index >= 0 && index < listaDespesas.size()) {
            return listaDespesas.get(index);
        }
        return null;
    }

    private void salvarNoArquivo() {
        try {
            File file = new File(context.getFilesDir(), FILE_NAME);
            try (FileWriter writer = new FileWriter(file, false)) {
                new Gson().toJson(listaDespesas, writer);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void carregarDespesas() {
        try {
            File file = new File(context.getFilesDir(), FILE_NAME);
            if (file.exists()) {
                String json = new String(Files.readAllBytes(file.toPath()));
                Type type = new TypeToken<List<Despesa>>() {}.getType();
                listaDespesas = new Gson().fromJson(json, type);
            } else {
                listaDespesas = new ArrayList<>();
            }
        } catch (Exception e) {
            listaDespesas = new ArrayList<>();
            e.printStackTrace();
        }
    }
}