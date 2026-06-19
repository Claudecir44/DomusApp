package com.cjstudio.condominio_sociedade_morro_grande.data;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class OcorrenciaDAO {

    private static final String FILE_NAME = "ocorrencias.json";
    private final Context context;

    public OcorrenciaDAO(Context context) {
        this.context = context;
    }

    public long inserirOcorrencia(JSONObject ocorrencia) {
        try {
            JSONArray jsonArray = lerArquivoJson();
            long novoId = gerarNovoId(jsonArray);
            ocorrencia.put("id", novoId);
            jsonArray.put(ocorrencia);
            salvarArquivoJson(jsonArray);
            return novoId;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    public boolean atualizarOcorrencia(long id, JSONObject ocorrenciaAtualizada) {
        try {
            JSONArray jsonArray = lerArquivoJson();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                if (item.optLong("id") == id) {
                    ocorrenciaAtualizada.put("id", id);
                    jsonArray.put(i, ocorrenciaAtualizada);
                    salvarArquivoJson(jsonArray);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean excluirOcorrencia(long id) {
        try {
            JSONArray jsonArray = lerArquivoJson();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                if (item.optLong("id") == id) {
                    jsonArray.remove(i);
                    salvarArquivoJson(jsonArray);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public List<JSONObject> listarOcorrencias() {
        List<JSONObject> lista = new ArrayList<>();
        try {
            JSONArray jsonArray = lerArquivoJson();
            for (int i = 0; i < jsonArray.length(); i++) {
                lista.add(jsonArray.getJSONObject(i));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }

    private JSONArray lerArquivoJson() {
        try {
            File file = new File(context.getFilesDir(), FILE_NAME);
            if (!file.exists()) return new JSONArray();
            FileInputStream fis = new FileInputStream(file);
            byte[] dados = new byte[(int) file.length()];
            fis.read(dados);
            fis.close();
            return new JSONArray(new String(dados));
        } catch (Exception e) {
            return new JSONArray();
        }
    }

    private void salvarArquivoJson(JSONArray jsonArray) {
        try {
            File file = new File(context.getFilesDir(), FILE_NAME);
            FileOutputStream fos = new FileOutputStream(file);
            fos.write(jsonArray.toString().getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private long gerarNovoId(JSONArray jsonArray) {
        long maxId = 0;
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.optJSONObject(i);
            if (obj != null) {
                long id = obj.optLong("id", 0);
                if (id > maxId) maxId = id;
            }
        }
        return maxId + 1;
    }
}