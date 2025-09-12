package com.example.domus;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class FuncionarioDAO {

    private final SharedPreferences prefs;
    private static final String PREFS_NAME = "funcionarios";
    private static final String KEY_LISTA = "lista";

    public FuncionarioDAO(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public List<Funcionario> listarTodos() {
        List<Funcionario> lista = new ArrayList<>();
        String json = prefs.getString(KEY_LISTA, "[]");
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject f = array.getJSONObject(i);
                Funcionario func = new Funcionario();
                func.setId(f.getInt("id"));
                func.setNome(f.getString("nome"));
                func.setRua(f.optString("rua",""));
                func.setNumero(f.optString("numero",""));
                func.setBairro(f.optString("bairro",""));
                func.setCep(f.optString("cep",""));
                func.setCidade(f.optString("cidade",""));
                func.setEstado(f.optString("estado",""));
                func.setPais(f.optString("pais",""));
                func.setTelefone(f.optString("telefone",""));
                func.setEmail(f.optString("email",""));
                func.setRg(f.optString("rg",""));
                func.setCpf(f.optString("cpf",""));
                func.setCargaHoraria(f.optString("cargaHoraria",""));
                func.setTurno(f.optString("turno",""));
                func.setHoraEntrada(f.optString("horaEntrada",""));
                func.setHoraSaida(f.optString("horaSaida",""));
                String uriStr = f.optString("imagemUri","");
                if(!uriStr.isEmpty()) func.setImagemUri(Uri.parse(uriStr));
                lista.add(func);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return lista;
    }

    public void salvar(Funcionario funcionario, int indexEditando) {
        String json = prefs.getString(KEY_LISTA, "[]");
        try {
            JSONArray array = new JSONArray(json);
            JSONObject f = new JSONObject();
            f.put("id", funcionario.getId());
            f.put("nome", funcionario.getNome());
            f.put("rua", funcionario.getRua());
            f.put("numero", funcionario.getNumero());
            f.put("bairro", funcionario.getBairro());
            f.put("cep", funcionario.getCep());
            f.put("cidade", funcionario.getCidade());
            f.put("estado", funcionario.getEstado());
            f.put("pais", funcionario.getPais());
            f.put("telefone", funcionario.getTelefone());
            f.put("email", funcionario.getEmail());
            f.put("rg", funcionario.getRg());
            f.put("cpf", funcionario.getCpf());
            f.put("cargaHoraria", funcionario.getCargaHoraria());
            f.put("turno", funcionario.getTurno());
            f.put("horaEntrada", funcionario.getHoraEntrada());
            f.put("horaSaida", funcionario.getHoraSaida());
            f.put("imagemUri", funcionario.getImagemUri()!=null? funcionario.getImagemUri().toString():"");

            if(indexEditando>=0 && indexEditando<array.length()) {
                array.put(indexEditando,f);
            } else {
                array.put(f);
            }

            prefs.edit().putString(KEY_LISTA,array.toString()).apply();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void excluir(int id) {
        String json = prefs.getString(KEY_LISTA, "[]");
        try {
            JSONArray array = new JSONArray(json);
            JSONArray novoArray = new JSONArray();
            for(int i=0;i<array.length();i++){
                JSONObject f = array.getJSONObject(i);
                if(f.getInt("id")!=id) novoArray.put(f);
            }
            prefs.edit().putString(KEY_LISTA,novoArray.toString()).apply();
        } catch (JSONException e) { e.printStackTrace(); }
    }

    public int gerarNovoId() {
        List<Funcionario> lista = listarTodos();
        int max=0;
        for(Funcionario f : lista){
            if(f.getId()>max) max=f.getId();
        }
        return max+1;
    }
}
