package com.example.domus.presentation.loginmorador;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.domus.MoradorDAO;
import org.json.JSONObject;

public class LoginMoradorViewModel extends AndroidViewModel {

    private final MoradorDAO moradorDAO;
    private final MutableLiveData<com.example.domus.presentation.loginmorador.LoginMoradorUiState> uiState = new MutableLiveData<>();
    private final MutableLiveData<LoginSuccessData> navigationToDashboard = new MutableLiveData<>();

    public LoginMoradorViewModel(Application application) {
        super(application);
        this.moradorDAO = new MoradorDAO(application);
        uiState.setValue(com.example.domus.presentation.loginmorador.LoginMoradorUiState.idle());
    }

    public LiveData<com.example.domus.presentation.loginmorador.LoginMoradorUiState> getUiState() {
        return uiState;
    }

    public LiveData<LoginSuccessData> getNavigationToDashboard() {
        return navigationToDashboard;
    }

    public void onLoginClicked(String usuario, String senha) {
        if (usuario == null || usuario.isEmpty() || senha == null || senha.isEmpty()) {
            uiState.setValue(com.example.domus.presentation.loginmorador.LoginMoradorUiState.error("Preencha os campos usuário e senha!"));
            return;
        }

        uiState.setValue(com.example.domus.presentation.loginmorador.LoginMoradorUiState.loading());

        // Executa em background
        new Thread(() -> {
            try {
                JSONObject morador = moradorDAO.validarLoginMorador(usuario, senha);

                // Atualiza UI na thread principal
                new android.os.Handler(getApplication().getMainLooper()).post(() -> {
                    if (morador != null) {
                        uiState.setValue(com.example.domus.presentation.loginmorador.LoginMoradorUiState.success(morador));
                        navigationToDashboard.setValue(new LoginSuccessData(morador));
                    } else {
                        uiState.setValue(com.example.domus.presentation.loginmorador.LoginMoradorUiState.error("Usuário ou senha incorretos!"));
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                new android.os.Handler(getApplication().getMainLooper()).post(() -> {
                    uiState.setValue(com.example.domus.presentation.loginmorador.LoginMoradorUiState.error("Erro ao fazer login. Tente novamente."));
                });
            }
        }).start();
    }

    public void onMessageShown() {
        com.example.domus.presentation.loginmorador.LoginMoradorUiState current = uiState.getValue();
        if (current != null && current.getErrorMessage() != null) {
            uiState.setValue(com.example.domus.presentation.loginmorador.LoginMoradorUiState.idle());
        }
    }

    public void clearFields() {
        uiState.setValue(com.example.domus.presentation.loginmorador.LoginMoradorUiState.idle());
    }

    // Classe de dados para navegação
    public static class LoginSuccessData {
        private final JSONObject moradorData;

        public LoginSuccessData(JSONObject moradorData) {
            this.moradorData = moradorData;
        }

        public JSONObject getMoradorData() { return moradorData; }
        public String getMoradorNome() { return moradorData.optString("nome"); }
        public String getMoradorCod() { return moradorData.optString("cod"); }
    }
}