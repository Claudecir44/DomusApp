package com.example.domus.presentation.main;

public class MainUiState {
    private final boolean isLoading;
    private final boolean showButtons;
    private final boolean showProgressBar;
    private final String errorMessage;
    private final boolean hasError;

    private MainUiState(Builder builder) {
        this.isLoading = builder.isLoading;
        this.showButtons = builder.showButtons;
        this.showProgressBar = builder.showProgressBar;
        this.errorMessage = builder.errorMessage;
        this.hasError = builder.hasError;
    }

    // Estado inicial (loading)
    public static MainUiState loading() {
        return new Builder()
                .setIsLoading(true)
                .setShowProgressBar(true)
                .setShowButtons(false)
                .build();
    }

    // Estado pronto (sem erro)
    public static MainUiState ready() {
        return new Builder()
                .setIsLoading(false)
                .setShowProgressBar(false)
                .setShowButtons(true)
                .build();
    }

    // Estado com erro
    public static MainUiState error(String message) {
        return new Builder()
                .setIsLoading(false)
                .setShowProgressBar(false)
                .setShowButtons(true) // Mostra botões mesmo com erro? Decisão de negócio
                .setHasError(true)
                .setErrorMessage(message)
                .build();
    }

    // Getters
    public boolean isLoading() { return isLoading; }
    public boolean isShowButtons() { return showButtons; }
    public boolean isShowProgressBar() { return showProgressBar; }
    public String getErrorMessage() { return errorMessage; }
    public boolean hasError() { return hasError; }

    // Builder pattern
    public static class Builder {
        private boolean isLoading = false;
        private boolean showButtons = false;
        private boolean showProgressBar = false;
        private String errorMessage = "";
        private boolean hasError = false;

        public Builder setIsLoading(boolean isLoading) {
            this.isLoading = isLoading;
            return this;
        }

        public Builder setShowButtons(boolean showButtons) {
            this.showButtons = showButtons;
            return this;
        }

        public Builder setShowProgressBar(boolean showProgressBar) {
            this.showProgressBar = showProgressBar;
            return this;
        }

        public Builder setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
            return this;
        }

        public Builder setHasError(boolean hasError) {
            this.hasError = hasError;
            return this;
        }

        public MainUiState build() {
            return new MainUiState(this);
        }
    }
}