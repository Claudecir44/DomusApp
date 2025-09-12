package com.example.domus;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ListaManutencaoActivity extends AppCompatActivity {

    private RecyclerView recyclerViewManutencao;
    private ManutencaoDAO manutencaoDAO;
    private List<Manutencao> listaManutencoes;
    private ManutencaoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_manutencao);

        recyclerViewManutencao = findViewById(R.id.recyclerViewManutencao);
        recyclerViewManutencao.setLayoutManager(new LinearLayoutManager(this));

        manutencaoDAO = new ManutencaoDAO(this);
        carregarLista();
    }

    private void carregarLista() {
        listaManutencoes = manutencaoDAO.getTodasManutencoes();

        if (listaManutencoes.isEmpty()) {
            Toast.makeText(this, "Nenhuma manutenção cadastrada", Toast.LENGTH_SHORT).show();
        }

        adapter = new ManutencaoAdapter(this, listaManutencoes, new ManutencaoAdapter.OnItemClickListener() {
            @Override
            public void onEditar(int position) {
                Manutencao m = listaManutencoes.get(position);
                Intent intent = new Intent(ListaManutencaoActivity.this, CadastroManutencaoActivity.class);
                intent.putExtra("editarId", m.getId());
                startActivity(intent);
            }

            @Override
            public void onExcluir(int position) {
                confirmarExclusao(listaManutencoes.get(position));
            }
        });
        recyclerViewManutencao.setAdapter(adapter);
    }

    private void confirmarExclusao(final Manutencao manutencao) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Excluir Manutenção");
        builder.setMessage("Deseja realmente excluir esta manutenção?");
        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int linhas = manutencaoDAO.excluirManutencao(manutencao.getId());
                if (linhas > 0) {
                    Toast.makeText(ListaManutencaoActivity.this, "Manutenção excluída!", Toast.LENGTH_SHORT).show();
                    carregarLista();
                } else {
                    Toast.makeText(ListaManutencaoActivity.this, "Erro ao excluir manutenção!", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("Não", null);
        builder.show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarLista(); // Recarrega a lista quando a activity retornar ao foco
    }
}