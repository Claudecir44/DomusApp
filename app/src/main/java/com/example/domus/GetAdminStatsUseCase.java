package com.example.domus.domain.usercase;

import com.example.domus.domain.repository.MasterAdminRepository;

public class GetAdminStatsUseCase {
    private final MasterAdminRepository repository;

    public GetAdminStatsUseCase(MasterAdminRepository repository) {
        this.repository = repository;
    }

    public Stats execute() {
        int count = repository.getAdminCount();
        boolean hasMaster = repository.existsMasterAdmin();
        return new Stats(count, hasMaster);
    }

    public static class Stats {
        private final int totalAdmins;
        private final boolean hasMasterAdmin;

        public Stats(int totalAdmins, boolean hasMasterAdmin) {
            this.totalAdmins = totalAdmins;
            this.hasMasterAdmin = hasMasterAdmin;
        }

        public int getTotalAdmins() { return totalAdmins; }
        public boolean hasMasterAdmin() { return hasMasterAdmin; }
    }
}