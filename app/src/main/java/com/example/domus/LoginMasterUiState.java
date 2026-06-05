package com.example.domus.presentation.loginmaster;

import com.example.domus.domain.model.MasterAdmin;

public class LoginMasterUiState {
    private final boolean isLoading;
    private final String successMessage;
    private final String errorMessage;
    private final MasterAdmin adminData;
    private final AdminStats stats;

    private LoginMasterUiState(Builder builder) {
        this.isLoading = builder.isLoading;
        this.successMessage = builder.successMessage;
        this.errorMessage = builder.errorMessage;
        this.adminData = builder.adminData;
        this.stats = builder.stats;
    }

    public static LoginMasterUiState idle() {
        return new Builder().setIsLoading(false).build();
    }

    public static LoginMasterUiState loading() {
        return new Builder().setIsLoading(true).build();
    }

    public static LoginMasterUiState success(String message, MasterAdmin admin) {
        return new Builder()
                .setIsLoading(false)
                .setSuccessMessage(message)
                .setAdminData(admin)
                .build();
    }

    public static LoginMasterUiState error(String message) {
        return new Builder()
                .setIsLoading(false)
                .setErrorMessage(message)
                .build();
    }

    public static LoginMasterUiState withStats(AdminStats stats) {
        return new Builder()
                .setIsLoading(false)
                .setStats(stats)
                .build();
    }

    // Getters
    public boolean isLoading() { return isLoading; }
    public String getSuccessMessage() { return successMessage; }
    public String getErrorMessage() { return errorMessage; }
    public MasterAdmin getAdminData() { return adminData; }
    public AdminStats getStats() { return stats; }

    public static class Builder {
        private boolean isLoading = false;
        private String successMessage;
        private String errorMessage;
        private MasterAdmin adminData;
        private AdminStats stats;

        public Builder setIsLoading(boolean isLoading) {
            this.isLoading = isLoading;
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

        public Builder setAdminData(MasterAdmin adminData) {
            this.adminData = adminData;
            return this;
        }

        public Builder setStats(AdminStats stats) {
            this.stats = stats;
            return this;
        }

        public LoginMasterUiState build() {
            return new LoginMasterUiState(this);
        }
    }

    public static class AdminStats {
        private final int totalAdmins;
        private final boolean hasMaster;

        public AdminStats(int totalAdmins, boolean hasMaster) {
            this.totalAdmins = totalAdmins;
            this.hasMaster = hasMaster;
        }

        public int getTotalAdmins() { return totalAdmins; }
        public boolean hasMaster() { return hasMaster; }
    }
}