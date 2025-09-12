package com.example.domus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class DashBoardActivity extends AppCompatActivity {

    private Button btnCadastro, btnLista, btnBackup, btnRegistroOcorrencias,
            btnFuncionarios, btnManutencao, btnAssembleias, btnDespesas,
            btnAdministradores, btnListaAssembleias, btnListaDespesas, btnAvisos, btnListaAvisos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        // Inicializa todos os botões
        btnCadastro = findViewById(R.id.buttonCadastroMoradores);
        btnLista = findViewById(R.id.buttonListaMoradores);
        btnBackup = findViewById(R.id.buttonBackup);
        btnRegistroOcorrencias = findViewById(R.id.buttonRegistroOcorrencias);
        btnFuncionarios = findViewById(R.id.buttonFuncionarios);
        btnManutencao = findViewById(R.id.buttonManutencao);
        btnAssembleias = findViewById(R.id.buttonAssembleias);
        btnDespesas = findViewById(R.id.buttonDespesas);
        btnAdministradores = findViewById(R.id.buttonAdministradores);
        btnListaAssembleias = findViewById(R.id.buttonListaAssembleias);
        btnListaDespesas = findViewById(R.id.buttonListaDespesas);
        btnAvisos = findViewById(R.id.buttonAvisos);

        // Botão para Lista de Avisos
        btnListaAvisos = findViewById(R.id.buttonListaAvisos);

        // Verifica o tipo de usuário vindo do Login
        String tipoUsuario = getIntent().getStringExtra("tipo_usuario");

        // Verificação segura para evitar null pointer
        if (tipoUsuario != null && "morador".equalsIgnoreCase(tipoUsuario)) {
            // Oculta todos os botões de administração para moradores
            btnCadastro.setVisibility(View.GONE);
            btnLista.setVisibility(View.GONE);
            btnBackup.setVisibility(View.GONE);
            btnRegistroOcorrencias.setVisibility(View.GONE);
            btnFuncionarios.setVisibility(View.GONE);
            btnManutencao.setVisibility(View.GONE);
            btnAdministradores.setVisibility(View.GONE);
            btnAssembleias.setVisibility(View.GONE); // Oculta REGISTRO de assembleias
            btnDespesas.setVisibility(View.GONE);    // Oculta REGISTRO de despesas
            btnAvisos.setVisibility(View.GONE);      // Oculta CADASTRO de avisos

            // MOSTRA apenas as LISTAS para moradores
            btnListaAssembleias.setVisibility(View.VISIBLE);
            btnListaDespesas.setVisibility(View.VISIBLE);
            btnListaAvisos.setVisibility(View.VISIBLE); // Mostra LISTA de avisos para moradores

            // Ajusta a posição dos botões visíveis para melhor layout
            ajustarLayoutParaMorador();
        } else {
            // Para administradores, mostra todos os botões
            btnCadastro.setVisibility(View.VISIBLE);
            btnLista.setVisibility(View.VISIBLE);
            btnBackup.setVisibility(View.VISIBLE);
            btnRegistroOcorrencias.setVisibility(View.VISIBLE);
            btnFuncionarios.setVisibility(View.VISIBLE);
            btnManutencao.setVisibility(View.VISIBLE);
            btnAdministradores.setVisibility(View.VISIBLE);
            btnAssembleias.setVisibility(View.VISIBLE);
            btnDespesas.setVisibility(View.VISIBLE);
            btnAvisos.setVisibility(View.VISIBLE);
            btnListaAssembleias.setVisibility(View.VISIBLE);
            btnListaDespesas.setVisibility(View.VISIBLE);
            btnListaAvisos.setVisibility(View.VISIBLE);
        }

        // Configuração dos cliques
        configurarCliques();
    }

    private void ajustarLayoutParaMorador() {
        // Ajusta as constraints para centralizar os botões visíveis (apenas listas)
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams paramsListaAssembleias =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) btnListaAssembleias.getLayoutParams();
        paramsListaAssembleias.topToTop = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
        paramsListaAssembleias.topMargin = 150; // Ajustado para dar espaço para mais botões
        btnListaAssembleias.setLayoutParams(paramsListaAssembleias);

        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams paramsListaDespesas =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) btnListaDespesas.getLayoutParams();
        paramsListaDespesas.topToBottom = btnListaAssembleias.getId();
        paramsListaDespesas.topMargin = 32;
        btnListaDespesas.setLayoutParams(paramsListaDespesas);

        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams paramsListaAvisos =
                (androidx.constraintlayout.widget.ConstraintLayout.LayoutParams) btnListaAvisos.getLayoutParams();
        paramsListaAvisos.topToBottom = btnListaDespesas.getId();
        paramsListaAvisos.topMargin = 32;
        btnListaAvisos.setLayoutParams(paramsListaAvisos);
    }

    private void configurarCliques() {
        btnCadastro.setOnClickListener(v -> startActivity(
                new Intent(DashBoardActivity.this, CadastroMoradorActivity.class)));

        btnLista.setOnClickListener(v -> startActivity(
                new Intent(DashBoardActivity.this, ListaMoradoresActivity.class)));

        btnBackup.setOnClickListener(v -> startActivity(
                new Intent(DashBoardActivity.this, BackupActivity.class)));

        btnRegistroOcorrencias.setOnClickListener(v -> startActivity(
                new Intent(DashBoardActivity.this, RegistroOcorrenciasActivity.class)));

        btnFuncionarios.setOnClickListener(v -> startActivity(
                new Intent(DashBoardActivity.this, FuncionariosActivity.class)));

        btnManutencao.setOnClickListener(v -> startActivity(
                new Intent(DashBoardActivity.this, CadastroManutencaoActivity.class)));

        // Botão Registro de Assembleias - ABRE RegistroAssembleiasActivity (apenas admin)
        btnAssembleias.setOnClickListener(v -> startActivity(
                new Intent(DashBoardActivity.this, RegistroAssembleiasActivity.class)));

        // Botão Lista de Assembleias - ABRE ListaAssembleiasActivity (morador e admin)
        btnListaAssembleias.setOnClickListener(v -> {
            Intent intent = new Intent(DashBoardActivity.this, ListaAssembleiasActivity.class);
            String tipoUsuario = getIntent().getStringExtra("tipo_usuario");
            intent.putExtra("tipo_usuario", tipoUsuario); // Passa o tipo de usuário
            startActivity(intent);
        });

        // Botão Registro de Despesas - ABRE RegistroDespesasActivity (apenas admin)
        btnDespesas.setOnClickListener(v -> startActivity(
                new Intent(DashBoardActivity.this, RegistroDespesasActivity.class)));

        // Botão Lista de Despesas - ABRE ListaDespesasActivity (morador e admin)
        btnListaDespesas.setOnClickListener(v -> {
            Intent intent = new Intent(DashBoardActivity.this, ListaDespesasActivity.class);
            String tipoUsuario = getIntent().getStringExtra("tipo_usuario");
            intent.putExtra("tipo_usuario", tipoUsuario); // Passa o tipo de usuário
            startActivity(intent);
        });

        btnAdministradores.setOnClickListener(v -> startActivity(
                new Intent(DashBoardActivity.this, AdministradoresActivity.class)));

        btnAvisos.setOnClickListener(v -> startActivity(
                new Intent(DashBoardActivity.this, CadastroAvisosActivity.class)));

        // Botão Lista de Avisos - ABRE ListaAvisosActivity (morador e admin)
        btnListaAvisos.setOnClickListener(v -> {
            Intent intent = new Intent(DashBoardActivity.this, ListaAvisosActivity.class);
            String tipoUsuario = getIntent().getStringExtra("tipo_usuario");
            intent.putExtra("tipo_usuario", tipoUsuario); // Passa o tipo de usuário
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Garante que o layout seja atualizado ao retornar para esta activity
        String tipoUsuario = getIntent().getStringExtra("tipo_usuario");
        if (tipoUsuario != null && "morador".equalsIgnoreCase(tipoUsuario)) {
            ajustarLayoutParaMorador();
        }
    }
}