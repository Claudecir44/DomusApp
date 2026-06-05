package com.example.domus.domain.usercase;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class GetNetworkStatusUseCase {
    private final Application application;

    public GetNetworkStatusUseCase(Application application) {
        this.application = application;
    }

    public boolean execute() {
        try {
            ConnectivityManager cm = (ConnectivityManager)
                    application.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = cm != null ? cm.getActiveNetworkInfo() : null;
            return activeNetwork != null && activeNetwork.isConnected();
        } catch (Exception e) {
            return false;
        }
    }
}