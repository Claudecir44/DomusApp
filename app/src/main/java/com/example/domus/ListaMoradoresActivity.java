package com.example.domus;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.List;

public class ListaMoradoresActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private MoradorAdapter adapter;
    private MoradorDAO moradorDAO;
    private List<JSONObject> moradores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_moradores);

        recyclerView = findViewById(R.id.recyclerMoradores);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        moradorDAO = new MoradorDAO(this);

        carregarLista();
    }

    private void carregarLista() {
        moradores = moradorDAO.listarMoradoresJSON();

        if (adapter == null) {
            adapter = new MoradorAdapter(this, moradores, new MoradorAdapter.OnItemClickListener() {
                @Override
                public void onEditar(JSONObject morador, int position) {
                    Intent intent = new Intent(ListaMoradoresActivity.this, CadastroMoradorActivity.class);
                    intent.putExtra("editarCod", morador.optString("cod"));
                    startActivity(intent);
                }

                @Override
                public void onExcluir(JSONObject morador, int position) {
                    confirmarExclusao(morador, position);
                }
            });
            recyclerView.setAdapter(adapter);
        } else {
            adapter.atualizarListaCompleta(moradores);
        }
    }

    private void confirmarExclusao(JSONObject morador, int position) {
        new AlertDialog.Builder(ListaMoradoresActivity.this)
                .setTitle("Excluir Morador")
                .setMessage("Deseja excluir " + morador.optString("nome") + "?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    String codigo = morador.optString("cod");
                    boolean sucesso = moradorDAO.excluirMoradorPorCodigo(codigo);

                    if (sucesso) {
                        adapter.removerItemPorCodigo(codigo);
                        Toast.makeText(ListaMoradoresActivity.this, "Morador excluído", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(ListaMoradoresActivity.this, "Erro ao excluir morador", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Não", null)
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Forçar atualização da lista quando a activity for retomada
        carregarLista();
    }
}