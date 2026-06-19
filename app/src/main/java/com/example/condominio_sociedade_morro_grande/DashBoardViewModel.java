package com.cjstudio.condominio_sociedade_morro_grande.presentation.dashboard;

import android.app.Application;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.cjstudio.condominio_sociedade_morro_grande.domain.model.ButtonVisibility;

public class DashBoardViewModel extends AndroidViewModel {

    private final MutableLiveData<com.cjstudio.condominio_sociedade_morro_grande.presentation.dashboard.DashBoardUiState> uiState = new MutableLiveData<>();
    private final MutableLiveData<NavigationEvent> navigationEvent = new MutableLiveData<>();

    private String currentTipoUsuario;
    private String currentMoradorNome;

    public DashBoardViewModel(Application application) {
        super(application);
        uiState.setValue(com.cjstudio.condominio_sociedade_morro_grande.presentation.dashboard.DashBoardUiState.loading());
    }

    public LiveData<com.cjstudio.condominio_sociedade_morro_grande.presentation.dashboard.DashBoardUiState> getUiState() {
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

        // Botões de admin (visíveis apenas para admin)
        visibility.setVisible("administradores", isAdmin);
        visibility.setVisible("assembleias", isAdmin);
        visibility.setVisible("despesas", isAdmin);
        visibility.setVisible("avisos", isAdmin);
        visibility.setVisible("cadastro", isAdmin);
        visibility.setVisible("ocorrencias", isAdmin);
        visibility.setVisible("manutencao", isAdmin);
        visibility.setVisible("funcionarios", isAdmin);

        // Botão "Listas" – apenas admin
        visibility.setVisible("listas", isAdmin);

        // ========== BOTÕES PARA MORADOR ==========
        // 🔥 ADICIONADO: Lista de Assembleias para morador
        visibility.setVisible("listaAssembleias", isMorador);
        visibility.setVisible("listaDespesas", isMorador);
        visibility.setVisible("listaAvisos", isMorador);

        // (Opcional) Se quiser que o admin também veja esses botões diretamente, use isAdmin || isMorador
        // Mas como admin tem o botão "Listas", não é necessário.

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
            case "administradores": return NavigationTarget.ADMINISTRADORES;
            case "assembleias": return NavigationTarget.REGISTRO_ASSEMBLEIAS;
            case "despesas": return NavigationTarget.REGISTRO_DESPESAS;
            case "avisos": return NavigationTarget.CADASTRO_AVISOS;
            case "cadastro": return NavigationTarget.CADASTRO_MORADOR;
            case "ocorrencias": return NavigationTarget.REGISTRO_OCORRENCIAS;
            case "manutencao": return NavigationTarget.MANUTENCAO;
            case "funcionarios": return NavigationTarget.FUNCIONARIOS;
            case "listas": return NavigationTarget.LISTAS;
            // Botões para morador:
            case "listaAssembleias": return NavigationTarget.LISTA_ASSEMBLEIAS;
            case "listaDespesas": return NavigationTarget.LISTA_DESPESAS;
            case "listaAvisos": return NavigationTarget.LISTA_AVISOS;
            default: return null;
        }
    }

    public enum NavigationTarget {
        CADASTRO_MORADOR,
        REGISTRO_OCORRENCIAS,
        FUNCIONARIOS,
        MANUTENCAO,
        REGISTRO_ASSEMBLEIAS,
        REGISTRO_DESPESAS,
        ADMINISTRADORES,
        CADASTRO_AVISOS,
        LISTAS,
        LISTA_ASSEMBLEIAS,
        LISTA_DESPESAS,
        LISTA_AVISOS,
        // Mantidos para compatibilidade (não usados diretamente)
        LISTA_MORADORES
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