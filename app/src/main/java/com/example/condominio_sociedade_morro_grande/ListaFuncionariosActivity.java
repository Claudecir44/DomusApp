package com.cjstudio.condominio_sociedade_morro_grande;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.cjstudio.condominio_sociedade_morro_grande.data.FuncionarioDAO;
import com.cjstudio.condominio_sociedade_morro_grande.domain.model.Funcionario;

import java.util.List;

public class ListaFuncionariosActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FuncionarioAdapter adapter;
    private FuncionarioDAO funcionarioDAO;
    private List<Funcionario> listaFuncionarios;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_funcionarios);

        recyclerView = findViewById(R.id.recyclerFuncionarios);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        funcionarioDAO = new FuncionarioDAO(this);

        carregarLista();
    }

    private void carregarLista() {
        listaFuncionarios = funcionarioDAO.listarTodos();

        adapter = new FuncionarioAdapter(listaFuncionarios, new FuncionarioAdapter.OnItemClickListener() {
            @Override
            public void onEditar(Funcionario funcionario, int position) {
                Intent intent = new Intent(ListaFuncionariosActivity.this, CadastroFuncionarioActivity.class);
                intent.putExtra("funcionario_id", funcionario.getId());
                startActivity(intent);
            }

            @Override
            public void onExcluir(Funcionario funcionario, int position) {
                new AlertDialog.Builder(ListaFuncionariosActivity.this)
                        .setTitle("Confirmação")
                        .setMessage("Deseja realmente excluir o funcionário " + funcionario.getNome() + "?")
                        .setPositiveButton("Sim", (dialog, which) -> {
                            funcionarioDAO.excluir(funcionario.getId());
                            listaFuncionarios.remove(position);
                            adapter.notifyItemRemoved(position);
                            Toast.makeText(ListaFuncionariosActivity.this,
                                    "Funcionário excluído!", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Não", null)
                        .show();
            }
        });

        recyclerView.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        carregarLista();
    }
}