package com.example.domus.presentation.dashboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.example.domus.*;
import com.example.domus.R;
import com.example.domus.domain.model.ButtonVisibility;
import com.example.domus.presentation.dashboard.DashBoardViewModel.NavigationTarget;
import java.util.HashMap;
import java.util.Map;

public class DashBoardActivity extends AppCompatActivity {

    private DashBoardViewModel viewModel;
    private Map<String, Button> buttons = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);

        // Inicializa ViewModel
        viewModel = new ViewModelProvider(this).get(DashBoardViewModel.class);

        // Inicializa botões
        initializeButtons();

        // Observa estado da UI
        viewModel.getUiState().observe(this, this::updateUi);

        // Observa eventos de navegação
        viewModel.getNavigationEvent().observe(this, this::handleNavigation);

        // Carrega permissões do usuário
        String tipoUsuario = getIntent().getStringExtra("tipo_usuario");
        String moradorNome = getIntent().getStringExtra("morador_nome");
        viewModel.loadUserPermissions(tipoUsuario, moradorNome);
    }

    private void initializeButtons() {
        buttons.put("cadastro", findViewById(R.id.buttonCadastroMoradores));
        buttons.put("lista", findViewById(R.id.buttonListaMoradores));
        buttons.put("backup", findViewById(R.id.buttonBackup));
        buttons.put("ocorrencias", findViewById(R.id.buttonRegistroOcorrencias));
        buttons.put("funcionarios", findViewById(R.id.buttonFuncionarios));
        buttons.put("manutencao", findViewById(R.id.buttonManutencao));
        buttons.put("assembleias", findViewById(R.id.buttonAssembleias));
        buttons.put("despesas", findViewById(R.id.buttonDespesas));
        buttons.put("administradores", findViewById(R.id.buttonAdministradores));
        buttons.put("avisos", findViewById(R.id.buttonAvisos));
        buttons.put("listaAssembleias", findViewById(R.id.buttonListaAssembleias));
        buttons.put("listaDespesas", findViewById(R.id.buttonListaDespesas));
        buttons.put("listaAvisos", findViewById(R.id.buttonListaAvisos));

        // Configura cliques
        for (Map.Entry<String, Button> entry : buttons.entrySet()) {
            entry.getValue().setOnClickListener(v ->
                    viewModel.onButtonClicked(entry.getKey()));
        }
    }

    private void updateUi(DashBoardUiState uiState) {
        if (uiState.isLoading()) return;

        if (uiState.getErrorMessage() != null) {
            // Mostrar erro
            android.widget.Toast.makeText(this, uiState.getErrorMessage(),
                    android.widget.Toast.LENGTH_LONG).show();
            return;
        }

        ButtonVisibility visibility = uiState.getButtonVisibility();
        if (visibility != null) {
            for (Map.Entry<String, Button> entry : buttons.entrySet()) {
                entry.getValue().setVisibility(
                        visibility.isVisible(entry.getKey()) ? View.VISIBLE : View.GONE
                );
            }
        }
    }

    private void handleNavigation(DashBoardViewModel.NavigationEvent event) {
        Intent intent = null;

        switch (event.getTarget()) {
            case CADASTRO_MORADOR:
                intent = new Intent(this, CadastroMoradorActivity.class);
                break;
            case LISTA_MORADORES:
                intent = new Intent(this, ListaMoradoresActivity.class);
                break;
            case BACKUP:
                intent = new Intent(this, BackupActivity.class);
                break;
            case REGISTRO_OCORRENCIAS:
                intent = new Intent(this, RegistroOcorrenciasActivity.class);
                break;
            case FUNCIONARIOS:
                intent = new Intent(this, FuncionariosActivity.class);
                break;
            case MANUTENCAO:
                intent = new Intent(this, CadastroManutencaoActivity.class);
                break;
            case REGISTRO_ASSEMBLEIAS:
                intent = new Intent(this, RegistroAssembleiasActivity.class);
                break;
            case REGISTRO_DESPESAS:
                intent = new Intent(this, RegistroDespesasActivity.class);
                break;
            case ADMINISTRADORES:
                intent = new Intent(this, AdministradoresActivity.class);
                break;
            case CADASTRO_AVISOS:
                intent = new Intent(this, CadastroAvisosActivity.class);
                break;
            case LISTA_ASSEMBLEIAS:
                intent = new Intent(this, ListaAssembleiasActivity.class);
                intent.putExtra("tipo_usuario", event.getTipoUsuario());
                break;
            case LISTA_DESPESAS:
                intent = new Intent(this, ListaDespesasActivity.class);
                intent.putExtra("tipo_usuario", event.getTipoUsuario());
                break;
            case LISTA_AVISOS:
                intent = new Intent(this, ListaAvisosActivity.class);
                intent.putExtra("tipo_usuario", event.getTipoUsuario());
                break;
        }

        if (intent != null) {
            startActivity(intent);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarrega permissões ao retornar
        String tipoUsuario = getIntent().getStringExtra("tipo_usuario");
        String moradorNome = getIntent().getStringExtra("morador_nome");
        viewModel.loadUserPermissions(tipoUsuario, moradorNome);
    }
}