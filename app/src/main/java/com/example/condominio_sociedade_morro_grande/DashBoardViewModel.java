package com.cjstudio.condominio_sociedade_morro_grande.presentation.dashboard;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cjstudio.condominio_sociedade_morro_grande.domain.model.ButtonVisibility;

public class DashBoardViewModel extends AndroidViewModel {

    private final MutableLiveData<DashBoardUiState> uiState = new MutableLiveData<>();
    private final MutableLiveData<NavigationEvent> navigationEvent = new MutableLiveData<>();

    private String currentTipoUsuario;
    private String currentMoradorNome;

    public DashBoardViewModel(Application application) {
        super(application);
        uiState.setValue(DashBoardUiState.loading());
    }

    public LiveData<DashBoardUiState> getUiState() {
        return uiState;
    }

    public LiveData<NavigationEvent> getNavigationEvent() {
        return navigationEvent;
    }

    public void loadUserPermissions(String tipoUsuario, String moradorNome) {
        this.currentTipoUsuario = tipoUsuario;
        this.currentMoradorNome = moradorNome;

        ButtonVisibility visibility = getButtonVisibility();

        uiState.setValue(com.cjstudio.condominio_sociedade_morro_grande.presentation.dashboard.DashBoardUiState.ready(visibility, tipoUsuario, moradorNome));
    }

    private ButtonVisibility getButtonVisibility() {
        ButtonVisibility visibility = new ButtonVisibility();

        boolean isAdmin = "admin".equals(currentTipoUsuario);
        boolean isMorador = "morador".equals(currentTipoUsuario);

        // ========== BOTÕES EXCLUSIVOS DE ADMIN ==========
        visibility.setVisible("cadastro", isAdmin);
        visibility.setVisible("lista", isAdmin);
        visibility.setVisible("backup", isAdmin);
        visibility.setVisible("funcionarios", isAdmin);
        visibility.setVisible("manutencao", isAdmin);
        visibility.setVisible("assembleias", isAdmin);
        visibility.setVisible("despesas", isAdmin);
        visibility.setVisible("administradores", isAdmin);
        visibility.setVisible("avisos", isAdmin);

        // 🔥 CORREÇÃO: Registro de Ocorrências – APENAS ADMIN
        visibility.setVisible("ocorrencias", isAdmin);

        // ========== BOTÕES PARA MORADOR (e também admin) ==========
        visibility.setVisible("listaAssembleias", isAdmin || isMorador);
        visibility.setVisible("listaDespesas", isAdmin || isMorador);
        visibility.setVisible("listaAvisos", isAdmin || isMorador);

        return visibility;
    }

    public void onButtonClicked(String buttonKey) {
        NavigationTarget target = getNavigationTarget(buttonKey);
        if (target != null) {
            navigationEvent.setValue(new NavigationEvent(target, currentTipoUsuario));
        }
    }

    private NavigationTarget getNavigationTarget(String buttonKey) {
        switch (buttonKey) {
            case "cadastro": return NavigationTarget.CADASTRO_MORADOR;
            case "lista": return NavigationTarget.LISTA_MORADORES;
            case "backup": return NavigationTarget.BACKUP;
            case "ocorrencias": return NavigationTarget.REGISTRO_OCORRENCIAS;
            case "funcionarios": return NavigationTarget.FUNCIONARIOS;
            case "manutencao": return NavigationTarget.MANUTENCAO;
            case "assembleias": return NavigationTarget.REGISTRO_ASSEMBLEIAS;
            case "despesas": return NavigationTarget.REGISTRO_DESPESAS;
            case "administradores": return NavigationTarget.ADMINISTRADORES;
            case "avisos": return NavigationTarget.CADASTRO_AVISOS;
            case "listaAssembleias": return NavigationTarget.LISTA_ASSEMBLEIAS;
            case "listaDespesas": return NavigationTarget.LISTA_DESPESAS;
            case "listaAvisos": return NavigationTarget.LISTA_AVISOS;
            default: return null;
        }
    }

    public enum NavigationTarget {
        CADASTRO_MORADOR, LISTA_MORADORES, BACKUP, REGISTRO_OCORRENCIAS,
        FUNCIONARIOS, MANUTENCAO, REGISTRO_ASSEMBLEIAS, REGISTRO_DESPESAS,
        ADMINISTRADORES, CADASTRO_AVISOS, LISTA_ASSEMBLEIAS, LISTA_DESPESAS, LISTA_AVISOS
    }

    public static class NavigationEvent {
        private final NavigationTarget target;
        private final String tipoUsuario;

        public NavigationEvent(NavigationTarget target, String tipoUsuario) {
            this.target = target;
            this.tipoUsuario = tipoUsuario;
        }

        public NavigationTarget getTarget() { return target; }
        public String getTipoUsuario() { return tipoUsuario; }
    }
}