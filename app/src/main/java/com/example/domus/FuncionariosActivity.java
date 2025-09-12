package com.example.domus;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class FuncionariosActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_funcionarios);

        Button btnCadastroFuncionario = findViewById(R.id.buttonCadastroFuncionario);
        Button btnListaFuncionarios = findViewById(R.id.buttonListaFuncionarios);

        // Botão para abrir a tela de cadastro de funcionários
        btnCadastroFuncionario.setOnClickListener(v -> {
            Intent intent = new Intent(FuncionariosActivity.this, CadastroFuncionarioActivity.class);
            startActivity(intent);
        });

        // Botão para abrir a lista de funcionários
        btnListaFuncionarios.setOnClickListener(v -> {
            Intent intent = new Intent(FuncionariosActivity.this, ListaFuncionariosActivity.class);
            startActivity(intent);
        });
    }
}
