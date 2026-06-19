package com.cjstudio.condominio_sociedade_morro_grande.data.firestore;

import android.util.Log;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MoradorFirestoreRepository {

    private static final String TAG = "MoradorFirestore";
    private final FirebaseFirestore db;
    private final CollectionReference moradoresRef;

    public MoradorFirestoreRepository() {
        db = FirebaseFirestore.getInstance();
        moradoresRef = db.collection("moradores");
    }

    public void inserirMorador(JSONObject morador, OnCompleteListener<String> listener) {
        Map<String, Object> data = jsonToMap(morador);
        moradoresRef.add(data)
                .addOnSuccessListener(docRef -> {
                    String id = docRef.getId();
                    Log.d(TAG, "Morador inserido com ID: " + id);
                    moradoresRef.document(id).update("cod", id)
                            .addOnSuccessListener(aVoid -> listener.onComplete(id))
                            .addOnFailureListener(e -> listener.onError(e));
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao inserir: " + e.getMessage());
                    listener.onError(e);
                });
    }

    public void atualizarMorador(String cod, JSONObject morador, OnCompleteListener<Void> listener) {
        moradoresRef.whereEqualTo("cod", cod).get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        String docId = query.getDocuments().get(0).getId();
                        Map<String, Object> data = jsonToMap(morador);
                        moradoresRef.document(docId).set(data)
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Morador atualizado: " + cod);
                                    listener.onComplete(null);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Erro ao atualizar: " + e.getMessage());
                                    listener.onError(e);
                                });
                    } else {
                        listener.onError(new Exception("Morador não encontrado"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao buscar morador: " + e.getMessage());
                    listener.onError(e);
                });
    }

    public void excluirMorador(String cod, OnCompleteListener<Void> listener) {
        moradoresRef.whereEqualTo("cod", cod).get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        String docId = query.getDocuments().get(0).getId();
                        moradoresRef.document(docId).delete()
                                .addOnSuccessListener(aVoid -> {
                                    Log.d(TAG, "Morador excluído: " + cod);
                                    listener.onComplete(null);
                                })
                                .addOnFailureListener(e -> {
                                    Log.e(TAG, "Erro ao excluir: " + e.getMessage());
                                    listener.onError(e);
                                });
                    } else {
                        listener.onError(new Exception("Morador não encontrado"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao buscar morador: " + e.getMessage());
                    listener.onError(e);
                });
    }

    public void getListaMoradores(OnCompleteListener<List<JSONObject>> listener) {
        moradoresRef.orderBy("nome", Query.Direction.ASCENDING).get()
                .addOnSuccessListener(query -> {
                    List<JSONObject> lista = new ArrayList<>();
                    for (DocumentSnapshot doc : query.getDocuments()) {
                        Map<String, Object> data = doc.getData();
                        JSONObject obj = new JSONObject(data);
                        if (!obj.has("cod")) {
                            try {
                                obj.put("cod", doc.getId());
                            } catch (JSONException e) {
                                Log.e(TAG, "Erro ao adicionar cod: " + e.getMessage());
                            }
                        }
                        lista.add(obj);
                    }
                    listener.onComplete(lista);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro ao listar: " + e.getMessage());
                    listener.onError(e);
                });
    }

    public void validarLogin(String usuario, String senha, OnCompleteListener<JSONObject> listener) {
        moradoresRef.whereEqualTo("usuario", usuario)
                .whereEqualTo("senha", senha)
                .get()
                .addOnSuccessListener(query -> {
                    if (!query.isEmpty()) {
                        DocumentSnapshot doc = query.getDocuments().get(0);
                        JSONObject obj = new JSONObject(doc.getData());
                        if (!obj.has("cod")) {
                            try {
                                obj.put("cod", doc.getId());
                            } catch (JSONException e) {
                                Log.e(TAG, "Erro ao adicionar cod: " + e.getMessage());
                            }
                        }
                        listener.onComplete(obj);
                    } else {
                        listener.onComplete(null);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erro no login: " + e.getMessage());
                    listener.onError(e);
                });
    }

    // ==================== CONVERSÃO JSON -> MAP ====================
    private Map<String, Object> jsonToMap(JSONObject json) {
        Map<String, Object> map = new HashMap<>();
        Iterator<String> keys = json.keys();
        while (keys.hasNext()) {
            String key = keys.next();
            try {
                Object value = json.get(key);
                map.put(key, value);
            } catch (JSONException e) {
                Log.e(TAG, "Erro ao converter JSON para Map: " + e.getMessage());
            }
        }
        return map;
    }

    public interface OnCompleteListener<T> {
        void onComplete(T result);
        void onError(Exception e);
    }
}