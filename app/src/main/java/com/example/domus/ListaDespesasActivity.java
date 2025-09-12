package com.example.domus;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ListaDespesasActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private DespesaAdapter adapter;
    private DespesaDAO despesaDAO;
    private List<Despesa> listaDespesas;
    private String tipoUsuario; // Variável para controlar o tipo de usuário

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_despesas);

        // Obter o tipo de usuário
        tipoUsuario = getIntent().getStringExtra("tipo_usuario");
        if (tipoUsuario == null) {
            tipoUsuario = "admin"; // Padrão para admin se não especificado
        }

        // Corrigido: ID do RecyclerView conforme XML
        recyclerView = findViewById(R.id.recyclerViewDespesas);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        despesaDAO = new DespesaDAO(this);

        carregarLista();
    }

    private void carregarLista() {
        listaDespesas = despesaDAO.listarTodos();

        adapter = new DespesaAdapter(this, listaDespesas, new DespesaAdapter.OnDespesaListener() {
            @Override
            public void onEditar(Despesa despesa, int position) {
                // Verificar se é morador antes de permitir edição
                if ("morador".equalsIgnoreCase(tipoUsuario)) {
                    Toast.makeText(ListaDespesasActivity.this, "Acesso negado. Apenas administradores podem editar despesas.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Ao editar, abre o RegistroDespesasActivity com os dados preenchidos
                Intent intent = new Intent(ListaDespesasActivity.this, RegistroDespesasActivity.class);
                intent.putExtra("despesaIndex", position); // enviando o índice para edição
                startActivity(intent);
            }

            @Override
            public void onExcluir(Despesa despesa, int position) {
                // Verificar se é morador antes de permitir exclusão
                if ("morador".equalsIgnoreCase(tipoUsuario)) {
                    Toast.makeText(ListaDespesasActivity.this, "Acesso negado. Apenas administradores podem excluir despesas.", Toast.LENGTH_SHORT).show();
                    return;
                }

                new AlertDialog.Builder(ListaDespesasActivity.this)
                        .setTitle("Confirmação")
                        .setMessage("Deseja realmente excluir esta despesa?")
                        .setPositiveButton("Sim", (dialog, which) -> {
                            despesaDAO.excluir(position); // exclui pelo índice
                            Toast.makeText(ListaDespesasActivity.this, "Despesa excluída!", Toast.LENGTH_SHORT).show();
                            carregarLista(); // Recarrega a lista
                        })
                        .setNegativeButton("Não", null)
                        .show();
            }
        }, tipoUsuario); // Passa o tipo de usuário para o adapter

        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarLista(); // Recarrega sempre que voltar para a lista
    }
}