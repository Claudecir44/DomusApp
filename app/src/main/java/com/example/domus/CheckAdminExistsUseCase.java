package com.example.domus.domain.usercase;

import android.app.Application;
import com.example.domus.BDCondominioHelper;

public class CheckAdminExistsUseCase {
    private final Application application;

    public CheckAdminExistsUseCase(Application application) {
        this.application = application;
    }

    public boolean execute() {
        BDCondominioHelper dbHelper = null;
        try {
            dbHelper = new BDCondominioHelper(application);
            return dbHelper.existeAdmin();
        } catch (Exception e) {
            android.util.Log.e("CheckAdminExistsUseCase", "Erro ao verificar admin: " + e.getMessage());
            return false;
        } finally {
            if (dbHelper != null) {
                try {
                    dbHelper.close();
                } catch (Exception ignored) {}
            }
        }
    }
}