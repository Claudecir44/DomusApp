package com.cjstudio.condominio_sociedade_morro_grande;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.cjstudio.condominio_sociedade_morro_grande.R;

public class FuncionariosActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_funcionarios);

        Button btnCadastroFuncionario = findViewById(R.id.buttonCadastroFuncionario);
        Button btnListaFuncionarios = findViewById(R.id.buttonListaFuncionarios);

        btnCadastroFuncionario.setOnClickListener(v -> {
            Intent intent = new Intent(FuncionariosActivity.this, CadastroFuncionarioActivity.class);
            startActivity(intent);
        });

        btnListaFuncionarios.setOnClickListener(v -> {
            Intent intent = new Intent(FuncionariosActivity.this, ListaFuncionariosActivity.class);
            startActivity(intent);
        });
    }
}