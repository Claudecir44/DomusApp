package com.example.domus;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

public class DashBoardActivity extends AppCompatActivity {

    private Button btnCadastro, btnLista, btnBackup, btnRegistroOcorrencias,
            btnFuncionarios, btnManutencao, btnAssembleias, btnDespesas,
            btnAdministradores, btnListaAssembleias, btnListaDespesas,
            btnAvisos, btnListaAvisos;

    private String tipoUsuario;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        // Inicializa todos os botões
        initializeButtons();

        // Recupera o tipo de usuário
        tipoUsuario = getIntent().getStringExtra("tipo_usuario");

        // Configura visibilidade baseada no tipo de usuário
        configurarVisibilidadeBotoes();

        // Configura os cliques
        configurarCliques();
    }

    private void initializeButtons() {
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
        btnListaAvisos = findViewById(R.id.buttonListaAvisos);
    }

    private void configurarVisibilidadeBotoes() {
        if (isMorador()) {
            // Oculta botões de administração para moradores
            ocultarBotoesAdministracao();
            // Mostra apenas botões de listagem
            mostrarBotoesListagem();
        } else {
            // Para administradores, mostra todos os botões
            mostrarTodosBotoes();
        }
    }

    private boolean isMorador() {
        return tipoUsuario != null && "morador".equalsIgnoreCase(tipoUsuario);
    }

    private void ocultarBotoesAdministracao() {
        btnCadastro.setVisibility(View.GONE);
        btnLista.setVisibility(View.GONE);
        btnBackup.setVisibility(View.GONE);
        btnRegistroOcorrencias.setVisibility(View.GONE);
        btnFuncionarios.setVisibility(View.GONE);
        btnManutencao.setVisibility(View.GONE);
        btnAdministradores.setVisibility(View.GONE);
        btnAssembleias.setVisibility(View.GONE);
        btnDespesas.setVisibility(View.GONE);
        btnAvisos.setVisibility(View.GONE);
    }

    private void mostrarBotoesListagem() {
        btnListaAssembleias.setVisibility(View.VISIBLE);
        btnListaDespesas.setVisibility(View.VISIBLE);
        btnListaAvisos.setVisibility(View.VISIBLE);
    }

    private void mostrarTodosBotoes() {
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

    private void configurarCliques() {
        // Cadastro de Moradores
        btnCadastro.setOnClickListener(v -> startActivity(
                new Intent(DashBoardActivity.this, CadastroMoradorActivity.class)));

        // Lista de Moradores
        btnLista.setOnClickListener(v -> startActivity(
                new Intent(DashBoardActivity.this, ListaMoradoresActivity.class)));

        // Backup
        btnBackup.setOnClickListener(v -> startActivity(
                new Intent(DashBoardActivity.this, BackupActivity.class)));

        // Registro de Ocorrências
        btnRegistroOcorrencias.setOnClickListener(v -> startActivity(
                new Intent(DashBoardActivity.this, RegistroOcorrenciasActivity.class)));

        // Cadastro de Funcionários
        btnFuncionarios.setOnClickListener(v -> startActivity(
                new Intent(DashBoardActivity.this, FuncionariosActivity.class)));

        // Cadastro de Manutenção
        btnManutencao.setOnClickListener(v -> startActivity(
                new Intent(DashBoardActivity.this, CadastroManutencaoActivity.class)));

        // Registro de Assembleias
        btnAssembleias.setOnClickListener(v -> startActivity(
                new Intent(DashBoardActivity.this, RegistroAssembleiasActivity.class)));

        // Lista de Assembleias
        btnListaAssembleias.setOnClickListener(v -> {
            Intent intent = new Intent(DashBoardActivity.this, ListaAssembleiasActivity.class);
            intent.putExtra("tipo_usuario", tipoUsuario);
            startActivity(intent);
        });

        // Registro de Despesas
        btnDespesas.setOnClickListener(v -> startActivity(
                new Intent(DashBoardActivity.this, RegistroDespesasActivity.class)));

        // Lista de Despesas (Prestação de Contas)
        btnListaDespesas.setOnClickListener(v -> {
            Intent intent = new Intent(DashBoardActivity.this, ListaDespesasActivity.class);
            intent.putExtra("tipo_usuario", tipoUsuario);
            startActivity(intent);
        });

        // Administradores
        btnAdministradores.setOnClickListener(v -> startActivity(
                new Intent(DashBoardActivity.this, AdministradoresActivity.class)));

        // Cadastro de Avisos
        btnAvisos.setOnClickListener(v -> startActivity(
                new Intent(DashBoardActivity.this, CadastroAvisosActivity.class)));

        // Lista de Avisos
        btnListaAvisos.setOnClickListener(v -> {
            Intent intent = new Intent(DashBoardActivity.this, ListaAvisosActivity.class);
            intent.putExtra("tipo_usuario", tipoUsuario);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Atualiza a visibilidade ao retornar para esta activity
        if (isMorador()) {
            ocultarBotoesAdministracao();
            mostrarBotoesListagem();
        }
    }
}