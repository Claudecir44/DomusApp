package com.example.domus.domain.usercase;

import com.example.domus.domain.repository.MasterAdminRepository;

public class SetupMasterAdminUseCase {
    private final MasterAdminRepository repository;

    public SetupMasterAdminUseCase(MasterAdminRepository repository) {
        this.repository = repository;
    }

    public void execute() {
        repository.setupMasterAdmin();
    }
}