package com.example.domus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.VideoView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class MainActivity extends AppCompatActivity {

    private VideoView videoSplash;
    private LinearLayout layoutBotoes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        videoSplash = findViewById(R.id.videoSplash);
        layoutBotoes = findViewById(R.id.layoutBotoes);

        // Carrega o vídeo da pasta raw
        Uri videoUri = Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.domus);
        videoSplash.setVideoURI(videoUri);

        videoSplash.setOnCompletionListener(mp -> {
            showUserChoiceDialog();
        });

        videoSplash.start();
    }

    private void showUserChoiceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Escolha o tipo de usuário")
                .setMessage("Você é Administrador ou Morador?")
                .setPositiveButton("Administrador", (dialog, which) -> {
                    if (!isAdminRegistered()) {
                        startActivity(new Intent(MainActivity.this, LoginMasterActivity.class));
                    } else {
                        startActivity(new Intent(MainActivity.this, LoginAdminActivity.class));
                    }
                    // NÃO chame finish() aqui - deixe a MainActivity na pilha
                })
                .setNegativeButton("Morador", (dialog, which) -> {
                    startActivity(new Intent(MainActivity.this, LoginMoradorActivity.class));
                    // NÃO chame finish() aqui - deixe a MainActivity na pilha
                })
                .setCancelable(false)
                .show();
    }

    private boolean isAdminRegistered() {
        BDCondominioHelper dbHelper = new BDCondominioHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(BDCondominioHelper.TABELA_USUARIOS_ADMIN,
                new String[]{BDCondominioHelper.COL_ADMIN_USUARIO},
                null, null, null, null, null);

        boolean existe = cursor != null && cursor.getCount() > 0;
        if (cursor != null) cursor.close();
        db.close();

        return existe;
    }
}