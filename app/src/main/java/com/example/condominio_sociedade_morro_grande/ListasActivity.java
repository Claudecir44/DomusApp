package com.cjstudio.condominio_sociedade_morro_grande;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class ListasActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listas);

        Button btnListaMoradores = findViewById(R.id.btnListaMoradores);
        Button btnListaAssembleias = findViewById(R.id.btnListaAssembleias);
        Button btnListaDespesas = findViewById(R.id.btnListaDespesas);
        Button btnListaAvisos = findViewById(R.id.btnListaAvisos);

        btnListaMoradores.setOnClickListener(v -> startActivity(new Intent(this, com.cjstudio.condominio_sociedade_morro_grande.ListaMoradoresActivity.class)));
        btnListaAssembleias.setOnClickListener(v -> startActivity(new Intent(this, com.cjstudio.condominio_sociedade_morro_grande.ListaAssembleiasActivity.class)));
        btnListaDespesas.setOnClickListener(v -> startActivity(new Intent(this, com.cjstudio.condominio_sociedade_morro_grande.ListaDespesasActivity.class)));
        btnListaAvisos.setOnClickListener(v -> startActivity(new Intent(this, com.cjstudio.condominio_sociedade_morro_grande.ListaAvisosActivity.class)));
    }
}