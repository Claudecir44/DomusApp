package com.example.domus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ListaAssembleiasActivity extends AppCompatActivity implements
        AssembleiaAdapter.OnVisualizarClickListener,
        AssembleiaAdapter.OnExcluirClickListener,
        AssembleiaAdapter.OnEditarClickListener {

    private RecyclerView recyclerView;
    private AssembleiaAdapter adapter;
    private AssembleiaDAO assembleiaDAO;
    private String tipoUsuario; // Variável para controlar o tipo de usuário

    // Views do painel de visualização
    private View panelVisualizacao;
    private TextView tvDataHora, tvAssunto, tvLocal, tvDescricao;
    private RecyclerView recyclerAnexos;
    private Button btnFecharVisualizacao, btnEditarVisualizacao, btnExcluirVisualizacao;
    private AnexosAdapter anexosAdapter;
    private List<Uri> listaAnexos = new ArrayList<>();

    private JSONObject assembleiaAtual; // guarda a assembleia selecionada

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_assembleias);

        // Obter o tipo de usuário
        tipoUsuario = getIntent().getStringExtra("tipo_usuario");
        if (tipoUsuario == null) {
            tipoUsuario = "admin"; // Padrão para admin se não especificado
        }

        recyclerView = findViewById(R.id.recyclerAssembleias);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Inicializar views do painel de visualização
        panelVisualizacao = findViewById(R.id.panelVisualizacao);
        tvDataHora = findViewById(R.id.tvDataHoraVisualizacao);
        tvAssunto = findViewById(R.id.tvAssuntoVisualizacao);
        tvLocal = findViewById(R.id.tvLocalVisualizacao);
        tvDescricao = findViewById(R.id.tvDescricaoVisualizacao);
        recyclerAnexos = findViewById(R.id.recyclerAnexos);
        btnFecharVisualizacao = findViewById(R.id.btnFecharVisualizacao);
        btnEditarVisualizacao = findViewById(R.id.btnEditarVisualizacao);
        btnExcluirVisualizacao = findViewById(R.id.btnExcluirVisualizacao);

        // Ocultar botões de editar e excluir se for morador
        if ("morador".equalsIgnoreCase(tipoUsuario)) {
            btnEditarVisualizacao.setVisibility(View.GONE);
            btnExcluirVisualizacao.setVisibility(View.GONE);
        }

        recyclerAnexos.setLayoutManager(new LinearLayoutManager(this));
        anexosAdapter = new AnexosAdapter(listaAnexos);
        recyclerAnexos.setAdapter(anexosAdapter);

        assembleiaDAO = new AssembleiaDAO(this);

        carregarAssembleias();

        // Fechar painel de visualização
        btnFecharVisualizacao.setOnClickListener(v -> {
            panelVisualizacao.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        });

        // Botão editar
        btnEditarVisualizacao.setOnClickListener(v -> {
            if (assembleiaAtual != null) {
                try {
                    long id = assembleiaAtual.getLong("id");
                    Intent intent = new Intent(this, RegistroAssembleiasActivity.class);
                    intent.putExtra("assembleiaId", id);
                    startActivity(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(this, "Erro ao abrir assembleia para edição", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Botão excluir
        btnExcluirVisualizacao.setOnClickListener(v -> {
            if (assembleiaAtual != null) {
                excluirAssembleia(assembleiaAtual);
            }
        });
    }

    private void carregarAssembleias() {
        List<JSONObject> lista = assembleiaDAO.listarAssembleias();
        // Passar o tipoUsuario para o adapter
        adapter = new AssembleiaAdapter(this, lista, this, this, this, tipoUsuario);
        recyclerView.setAdapter(adapter);
    }

    @Override
    public void onVisualizar(JSONObject assembleia) {
        mostrarDetalhesAssembleia(assembleia);
    }

    @Override
    public void onExcluir(JSONObject assembleia) {
        excluirAssembleia(assembleia);
    }

    @Override
    public void onEditar(JSONObject assembleia) {
        try {
            long id = assembleia.getLong("id");
            Intent intent = new Intent(this, RegistroAssembleiasActivity.class);
            intent.putExtra("assembleiaId", id);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao abrir assembleia para edição", Toast.LENGTH_SHORT).show();
        }
    }

    private void mostrarDetalhesAssembleia(JSONObject assembleia) {
        try {
            assembleiaAtual = assembleia;

            tvDataHora.setText(assembleia.optString("datahora", "Data/Hora não informada"));
            tvAssunto.setText(assembleia.optString("assunto", "Assunto não informado"));
            tvLocal.setText(assembleia.optString("local", "Local não informado"));
            tvDescricao.setText(assembleia.optString("descricao", "Descrição não informada"));

            listaAnexos.clear();
            if (assembleia.has("anexos")) {
                JSONArray jsonAnexos = assembleia.getJSONArray("anexos");
                for (int i = 0; i < jsonAnexos.length(); i++) {
                    JSONObject anexo = jsonAnexos.getJSONObject(i);
                    if (anexo.has("caminho")) {
                        listaAnexos.add(Uri.parse(anexo.getString("caminho")));
                    }
                }
            }
            anexosAdapter.notifyDataSetChanged();

            panelVisualizacao.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao carregar dados da assembleia", Toast.LENGTH_SHORT).show();
        }
    }

    private void excluirAssembleia(JSONObject assembleia) {
        try {
            long id = assembleia.getLong("id");

            new AlertDialog.Builder(this)
                    .setTitle("Confirmar exclusão")
                    .setMessage("Deseja realmente excluir esta assembleia?")
                    .setPositiveButton("Sim", (dialog, which) -> {
                        boolean sucesso = assembleiaDAO.excluirAssembleia(id);
                        if (sucesso) {
                            Toast.makeText(this, "Assembleia excluída", Toast.LENGTH_SHORT).show();
                            carregarAssembleias();
                            panelVisualizacao.setVisibility(View.GONE);
                            recyclerView.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(this, "Erro ao excluir assembleia", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Não", null)
                    .show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao excluir assembleia", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onBackPressed() {
        if (panelVisualizacao.getVisibility() == View.VISIBLE) {
            panelVisualizacao.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
        } else {
            super.onBackPressed();
        }
    }
}