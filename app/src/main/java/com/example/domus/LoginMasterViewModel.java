package com.example.domus.presentation.loginmaster;

import android.app.Application;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.domus.domain.repository.MasterAdminRepository;
import com.example.domus.domain.usercase.GetAdminStatsUseCase;
import com.example.domus.domain.usercase.SetupMasterAdminUseCase;
import com.example.domus.domain.usercase.ValidateMasterLoginUseCase;
import com.example.domus.data.repository.MasterAdminRepositoryImpl;

public class LoginMasterViewModel extends AndroidViewModel {

    private final SetupMasterAdminUseCase setupMasterAdminUseCase;
    private final ValidateMasterLoginUseCase validateMasterLoginUseCase;
    private final GetAdminStatsUseCase getAdminStatsUseCase;

    private final MutableLiveData<LoginMasterUiState> uiState = new MutableLiveData<>();
    private final MutableLiveData<Boolean> navigateToLoginAdmin = new MutableLiveData<>();

    public LoginMasterViewModel(Application application) {
        super(application);

        MasterAdminRepository repository = new MasterAdminRepositoryImpl(application);
        this.setupMasterAdminUseCase = new SetupMasterAdminUseCase(repository);
        this.validateMasterLoginUseCase = new ValidateMasterLoginUseCase(repository);
        this.getAdminStatsUseCase = new GetAdminStatsUseCase(repository);

        // Estado inicial
        uiState.setValue(LoginMasterUiState.idle());

        // Configura admin master automaticamente
        setupMasterAdmin();
    }

    public LiveData<LoginMasterUiState> getUiState() {
        return uiState;
    }

    public LiveData<Boolean> getNavigateToLoginAdmin() {
        return navigateToLoginAdmin;
    }

    private void setupMasterAdmin() {
        // Executa em background
        new Thread(() -> {
            setupMasterAdminUseCase.execute();

            // Carrega estatísticas após setup
            loadStats();
        }).start();
    }

    private void loadStats() {
        new Thread(() -> {
            GetAdminStatsUseCase.Stats stats = getAdminStatsUseCase.execute();

            // Atualiza UI na thread principal
            new android.os.Handler(getApplication().getMainLooper()).post(() -> {
                LoginMasterUiState.AdminStats uiStats = new LoginMasterUiState.AdminStats(
                        stats.getTotalAdmins(),
                        stats.hasMasterAdmin()
                );
                uiState.setValue(LoginMasterUiState.withStats(uiStats));
            });
        }).start();
    }

    public void onLoginClicked(String usuario, String senha) {
        uiState.setValue(LoginMasterUiState.loading());

        // Executa em background
        new Thread(() -> {
            ValidateMasterLoginUseCase.Result result = validateMasterLoginUseCase.execute(usuario, senha);

            // Atualiza UI na thread principal
            new android.os.Handler(getApplication().getMainLooper()).post(() -> {
                if (result.isSuccess()) {
                    uiState.setValue(com.example.domus.presentation.loginmaster.LoginMasterUiState.success(result.getMessage(), result.getAdmin()));
                    navigateToLoginAdmin.setValue(true);
                } else {
                    uiState.setValue(com.example.domus.presentation.loginmaster.LoginMasterUiState.error(result.getMessage()));
                }
            });
        }).start();
    }

    public void onMessageShown() {
        LoginMasterUiState current = uiState.getValue();
        if (current != null && (current.getSuccessMessage() != null || current.getErrorMessage() != null)) {
            uiState.setValue(LoginMasterUiState.idle());
        }
    }

    public void refreshStats() {
        loadStats();
    }
}