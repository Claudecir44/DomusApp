package com.example.domus.presentation.loginadmin;

public class LoginAdminUiState {
    private final boolean isLoading;
    private final boolean showCadastroForm;
    private final String successMessage;
    private final String errorMessage;

    private LoginAdminUiState(Builder builder) {
        this.isLoading = builder.isLoading;
        this.showCadastroForm = builder.showCadastroForm;
        this.successMessage = builder.successMessage;
        this.errorMessage = builder.errorMessage;
    }

    public static LoginAdminUiState idle() {
        return new Builder().setIsLoading(false).build();
    }

    public static LoginAdminUiState loading() {
        return new Builder().setIsLoading(true).build();
    }

    public static LoginAdminUiState success(String message) {
        return new Builder()
                .setIsLoading(false)
                .setSuccessMessage(message)
                .build();
    }

    public static LoginAdminUiState error(String message) {
        return new Builder()
                .setIsLoading(false)
                .setErrorMessage(message)
                .build();
    }

    public static LoginAdminUiState withCadastroForm(boolean show) {
        return new Builder()
                .setIsLoading(false)
                .setShowCadastroForm(show)
                .build();
    }

    // Getters
    public boolean isLoading() { return isLoading; }
    public boolean isShowCadastroForm() { return showCadastroForm; }
    public String getSuccessMessage() { return successMessage; }
    public String getErrorMessage() { return errorMessage; }

    public static class Builder {
        private boolean isLoading = false;
        private boolean showCadastroForm = true;
        private String successMessage;
        private String errorMessage;

        public Builder setIsLoading(boolean isLoading) {
            this.isLoading = isLoading;
            return this;
        }

        public Builder setShowCadastroForm(boolean showCadastroForm) {
            this.showCadastroForm = showCadastroForm;
            return this;
        }

        public Builder setSuccessMessage(String successMessage) {
            this.successMessage = successMessage;
            return this;
        }

        public Builder setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public LoginAdminUiState build() {
            return new LoginAdminUiState(this);
        }
    }
}