package com.example.domus.presentation.main;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.domus.domain.usercase.CheckAdminExistsUseCase;
import com.example.domus.domain.usercase.GetNetworkStatusUseCase;

public class MainViewModel extends AndroidViewModel {

    private final CheckAdminExistsUseCase checkAdminExistsUseCase;
    private final GetNetworkStatusUseCase getNetworkStatusUseCase;

    private final MutableLiveData<MainUiState> uiState = new MutableLiveData<>();
    private final MutableLiveData<NavigationEvent> navigationEvent = new MutableLiveData<>();

    public MainViewModel(Application application) {
        super(application);
        // Inicializa os Use Cases
        this.checkAdminExistsUseCase = new CheckAdminExistsUseCase(application);
        this.getNetworkStatusUseCase = new GetNetworkStatusUseCase(application);

        // Estado inicial
        uiState.setValue(MainUiState.loading());
    }

    public LiveData<MainUiState> getUiState() {
        return uiState;
    }

    public LiveData<NavigationEvent> getNavigationEvent() {
        return navigationEvent;
    }

    public void initialize() {
        // Simula um delay de carregamento (1 segundo)
        new android.os.Handler().postDelayed(() -> {
            // Verifica rede (apenas para log/informação)
            boolean hasNetwork = getNetworkStatusUseCase.execute();

            if (!hasNetwork) {
                // Apenas log, não impede o uso
                android.util.Log.w("MainViewModel", "Sem conexão de internet");
            }

            // Atualiza UI para estado pronto
            uiState.setValue(MainUiState.ready());
        }, 1000);
    }

    public void onUserTypeSelected(UserTypeSelection selection) {
        // Atualiza estado para loading enquanto processa
        uiState.setValue(MainUiState.loading());

        switch (selection) {
            case ADMIN:
                handleAdminSelection();
                break;
            case MORADOR:
                handleMoradorSelection();
                break;
        }
    }

    private void handleAdminSelection() {
        // Executa em background (simplificado - idealmente com coroutines/thread)
        new Thread(() -> {
            try {
                boolean adminExists = checkAdminExistsUseCase.execute();

                // Posta resultado na thread principal
                new android.os.Handler(getApplication().getMainLooper()).post(() -> {
                    if (!adminExists) {
                        navigationEvent.setValue(NavigationEvent.toLoginMaster());
                    } else {
                        navigationEvent.setValue(NavigationEvent.toLoginAdmin());
                    }
                });
            } catch (Exception e) {
                new android.os.Handler(getApplication().getMainLooper()).post(() -> {
                    uiState.setValue(MainUiState.error("Erro ao verificar administrador: " + e.getMessage()));
                });
            }
        }).start();
    }

    private void handleMoradorSelection() {
        navigationEvent.setValue(NavigationEvent.toLoginMorador());
    }

    public void onErrorDismissed() {
        // Limpa erro e volta ao estado ready
        uiState.setValue(MainUiState.ready());
    }

    // Enum para tipos de seleção
    public enum UserTypeSelection {
        ADMIN, MORADOR
    }

    // Eventos de navegação (selados)
    public static class NavigationEvent {
        private final Type type;

        private NavigationEvent(Type type) {
            this.type = type;
        }

        public Type getType() { return type; }

        public static NavigationEvent toLoginAdmin() {
            return new NavigationEvent(Type.LOGIN_ADMIN);
        }

        public static NavigationEvent toLoginMaster() {
            return new NavigationEvent(Type.LOGIN_MASTER);
        }

        public static NavigationEvent toLoginMorador() {
            return new NavigationEvent(Type.LOGIN_MORADOR);
        }

        public enum Type {
            LOGIN_ADMIN, LOGIN_MASTER, LOGIN_MORADOR
        }
    }
}