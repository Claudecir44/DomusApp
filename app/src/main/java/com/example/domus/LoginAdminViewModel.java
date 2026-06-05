package com.example.domus.presentation.loginadmin;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import com.example.domus.BDCondominioHelper;
import com.example.domus.utils.SecurityUtils;

public class LoginAdminViewModel extends AndroidViewModel {

    private final BDCondominioHelper dbHelper;
    private final MutableLiveData<com.example.domus.presentation.loginadmin.LoginAdminUiState> uiState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navigationToDashboard = new MutableLiveData<>();
    private final MutableLiveData<Boolean> adminsChanged = new MutableLiveData<>();

    public LoginAdminViewModel(Application application) {
        super(application);
        this.dbHelper = new BDCondominioHelper(application);

        boolean hasAdmin = dbHelper.existeAdmin();
        uiState.setValue(com.example.domus.presentation.loginadmin.LoginAdminUiState.withCadastroForm(!hasAdmin));
    }

    public LiveData<com.example.domus.presentation.loginadmin.LoginAdminUiState> getUiState() {
        return uiState;
    }

    public LiveData<Boolean> getNavigationToDashboard() {
        return navigationToDashboard;
    }

    public LiveData<Boolean> getAdminsChanged() {
        return adminsChanged;
    }

    public void onLoginClicked(String usuario, String senha) {
        if (usuario == null || usuario.isEmpty() || senha == null || senha.isEmpty()) {
            uiState.setValue(com.example.domus.presentation.loginadmin.LoginAdminUiState.error("Preencha todos os campos!"));
            return;
        }

        uiState.setValue(com.example.domus.presentation.loginadmin.LoginAdminUiState.loading());

        new Thread(() -> {
            boolean isValid = dbHelper.verificarCredenciaisAdmin(usuario, senha);

            new android.os.Handler(getApplication().getMainLooper()).post(() -> {
                if (isValid) {
                    saveLoggedAdmin(usuario);
                    uiState.setValue(com.example.domus.presentation.loginadmin.LoginAdminUiState.success("Login realizado com sucesso!"));
                    navigationToDashboard.setValue(true);
                } else {
                    uiState.setValue(com.example.domus.presentation.loginadmin.LoginAdminUiState.error("Usuário ou senha incorretos!"));
                }
            });
        }).start();
    }

    public void onRegisterClicked(String usuario, String senha, String masterSenha) {
        if (usuario == null || usuario.isEmpty()) {
            uiState.setValue(com.example.domus.presentation.loginadmin.LoginAdminUiState.error("Preencha o usuário!"));
            return;
        }

        if (senha == null || senha.isEmpty()) {
            uiState.setValue(com.example.domus.presentation.loginadmin.LoginAdminUiState.error("Preencha a senha!"));
            return;
        }

        if (!"master".equals(masterSenha)) {
            uiState.setValue(com.example.domus.presentation.loginadmin.LoginAdminUiState.error("Senha master incorreta!"));
            return;
        }

        if (!SecurityUtils.isValidSenhaAdmin(senha)) {
            uiState.setValue(com.example.domus.presentation.loginadmin.LoginAdminUiState.error("A senha deve ter exatamente 6 dígitos numéricos!"));
            return;
        }

        uiState.setValue(com.example.domus.presentation.loginadmin.LoginAdminUiState.loading());

        new Thread(() -> {
            String senhaHash = SecurityUtils.gerarHash(senha);

            if (senhaHash == null) {
                new android.os.Handler(getApplication().getMainLooper()).post(() -> {
                    uiState.setValue(com.example.domus.presentation.loginadmin.LoginAdminUiState.error("Erro ao processar senha!"));
                });
                return;
            }

            boolean success = dbHelper.cadastrarAdmin(usuario, senhaHash);

            new android.os.Handler(getApplication().getMainLooper()).post(() -> {
                if (success) {
                    uiState.setValue(com.example.domus.presentation.loginadmin.LoginAdminUiState.success("Admin cadastrado com sucesso!"));
                    adminsChanged.setValue(true); // Notifica que a lista mudou
                } else {
                    uiState.setValue(com.example.domus.presentation.loginadmin.LoginAdminUiState.error("Erro ao cadastrar. Usuário já existe?"));
                }
            });
        }).start();
    }

    /**
     * Remove um administrador
     * @param usuario Nome do usuário a ser removido
     */
    public void onRemoveAdminClicked(String usuario) {
        // Valida se não está tentando remover o admin master
        if ("admin".equalsIgnoreCase(usuario)) {
            uiState.setValue(com.example.domus.presentation.loginadmin.LoginAdminUiState.error("Não é possível remover o administrador master!"));
            return;
        }

        // Valida se não está tentando remover o próprio admin logado
        String loggedAdmin = getLoggedAdmin();
        if (usuario.equalsIgnoreCase(loggedAdmin)) {
            uiState.setValue(com.example.domus.presentation.loginadmin.LoginAdminUiState.error("Não é possível remover o administrador logado!"));
            return;
        }

        uiState.setValue(com.example.domus.presentation.loginadmin.LoginAdminUiState.loading());

        new Thread(() -> {
            boolean success = dbHelper.removerAdmin(usuario);

            new android.os.Handler(getApplication().getMainLooper()).post(() -> {
                if (success) {
                    uiState.setValue(com.example.domus.presentation.loginadmin.LoginAdminUiState.success("Administrador removido com sucesso!"));
                    adminsChanged.setValue(true); // Notifica que a lista mudou
                } else {
                    uiState.setValue(com.example.domus.presentation.loginadmin.LoginAdminUiState.error("Erro ao remover administrador!"));
                }
            });
        }).start();
    }

    private void saveLoggedAdmin(String usuario) {
        android.content.SharedPreferences prefs = getApplication().getSharedPreferences("admin_prefs", android.content.Context.MODE_PRIVATE);
        prefs.edit().putString("admin_logado", usuario).apply();
    }

    private String getLoggedAdmin() {
        android.content.SharedPreferences prefs = getApplication().getSharedPreferences("admin_prefs", android.content.Context.MODE_PRIVATE);
        return prefs.getString("admin_logado", null);
    }

    public void onMessageShown() {
        com.example.domus.presentation.loginadmin.LoginAdminUiState current = uiState.getValue();
        if (current != null && (current.getSuccessMessage() != null || current.getErrorMessage() != null)) {
            uiState.setValue(com.example.domus.presentation.loginadmin.LoginAdminUiState.idle());
        }
    }

    public void onAdminsChangedHandled() {
        adminsChanged.setValue(false);
    }
}