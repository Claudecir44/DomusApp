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

public class ListaFuncionariosActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private FuncionarioAdapter adapter;
    private FuncionarioDAO dao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lista_funcionarios);

        recyclerView = findViewById(R.id.recyclerFuncionarios);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        dao = new FuncionarioDAO(this);
        List<Funcionario> lista = dao.listarTodos();

        adapter = new FuncionarioAdapter(lista, new FuncionarioAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Funcionario funcionario) {
                Toast.makeText(ListaFuncionariosActivity.this,
                        "Funcionário selecionado: " + funcionario.getNome(),
                        Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onEditar(Funcionario funcionario, int position) {
                Intent intent = new Intent(ListaFuncionariosActivity.this, CadastroFuncionarioActivity.class);
                intent.putExtra("index", position);
                startActivity(intent);
            }

            @Override
            public void onExcluir(Funcionario funcionario, int position) {
                // Confirmação de exclusão
                new AlertDialog.Builder(ListaFuncionariosActivity.this)
                        .setTitle("Confirmação")
                        .setMessage("Deseja realmente excluir o funcionário: " + funcionario.getNome() + "?")
                        .setPositiveButton("Sim", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dao.excluir(funcionario.getId());
                                adapter.removerItem(position);
                                Toast.makeText(ListaFuncionariosActivity.this,
                                        "Funcionário excluído: " + funcionario.getNome(),
                                        Toast.LENGTH_SHORT).show();
                            }
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
        List<Funcionario> lista = dao.listarTodos();
        adapter = new FuncionarioAdapter(lista, adapter.getListener());
        recyclerView.setAdapter(adapter);
    }
}
