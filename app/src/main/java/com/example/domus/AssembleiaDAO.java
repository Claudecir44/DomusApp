package com.example.domus;

import android.content.Context;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

public class AssembleiaDAO {

    private static final String FILE_NAME = "assembleias.json";
    private Context context;

    public AssembleiaDAO(Context context) {
        this.context = context;
    }

    /** Inserir nova assembleia */
    public long inserirAssembleia(JSONObject assembleia) {
        try {
            JSONArray jsonArray = lerArquivoJson();
            long novoId = gerarNovoId(jsonArray);
            assembleia.put("id", novoId);

            // Garante que documento e assunto existam
            if (!assembleia.has("documento")) {
                assembleia.put("documento", new JSONArray().toString());
            }
            if (!assembleia.has("assunto")) {
                assembleia.put("assunto", "");
            }

            jsonArray.put(assembleia);
            salvarArquivoJson(jsonArray);
            return novoId;
        } catch (Exception e) {
            e.printStackTrace();
            return -1;
        }
    }

    /** Atualizar assembleia existente */
    public boolean atualizarAssembleia(long id, JSONObject assembleiaAtualizada) {
        try {
            JSONArray jsonArray = lerArquivoJson();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                if (item.getLong("id") == id) {
                    assembleiaAtualizada.put("id", id);

                    // Garante documento e assunto
                    if (!assembleiaAtualizada.has("documento")) {
                        assembleiaAtualizada.put("documento", new JSONArray().toString());
                    }
                    if (!assembleiaAtualizada.has("assunto")) {
                        assembleiaAtualizada.put("assunto", "");
                    }

                    jsonArray.put(i, assembleiaAtualizada);
                    salvarArquivoJson(jsonArray);
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Excluir assembleia */
    public boolean excluirAssembleia(long id) {
        try {
            JSONArray jsonArray = lerArquivoJson();
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject item = jsonArray.getJSONObject(i);
                if (item.getLong("id") == id) {
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

    /** Listar todas as assembleias */
    public List<JSONObject> listarAssembleias() {
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

    /** Lê o arquivo JSON local, retorna JSONArray vazio se não existir */
    private JSONArray lerArquivoJson() {
        try {
            File file = new File(context.getFilesDir(), FILE_NAME);
            if (!file.exists()) {
                return new JSONArray();
            }
            FileInputStream fis = new FileInputStream(file);
            byte[] dados = new byte[(int) file.length()];
            fis.read(dados);
            fis.close();
            String conteudo = new String(dados);
            return new JSONArray(conteudo);
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONArray();
        }
    }

    /** Salva o JSONArray no arquivo local */
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

    /** Gera novo ID sequencial */
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

    /** Converte lista de URIs para JSONArray String */
    public static String anexosParaJson(List<Uri> anexos) {
        JSONArray jsonArray = new JSONArray();
        for (Uri uri : anexos) {
            jsonArray.put(uri.toString());
        }
        return jsonArray.toString();
    }

    /** Converte JSONArray String para lista de URIs */
    public static List<Uri> jsonParaAnexos(String json) {
        List<Uri> lista = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                lista.add(Uri.parse(jsonArray.getString(i)));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lista;
    }
}
