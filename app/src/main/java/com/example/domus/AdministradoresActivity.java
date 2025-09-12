package com.example.domus;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class AdministradoresActivity extends AppCompatActivity {

    private LinearLayout listaAdmins;
    private String adminLogado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administradores);

        listaAdmins = findViewById(R.id.listaAdmins);

        // Recupera admin logado via SharedPreferences (ajuste conforme seu projeto)
        adminLogado = LoginAdminActivity.getAdminLogado(this);

        mostrarAdministradores();
    }

    private void mostrarAdministradores() {
        listaAdmins.removeAllViews();

        BDCondominioHelper dbHelper = new BDCondominioHelper(this);
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        Cursor cursor = db.query(BDCondominioHelper.TABELA_USUARIOS_ADMIN,
                new String[]{BDCondominioHelper.COL_ADMIN_USUARIO},
                null, null, null, null, BDCondominioHelper.COL_ADMIN_USUARIO + " ASC");

        List<String> admins = new ArrayList<>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                admins.add(cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_ADMIN_USUARIO)));
            }
            cursor.close();
        }

        // Mostra admin logado primeiro em verde
        if (adminLogado != null) {
            for (int i = 0; i < admins.size(); i++) {
                if (admins.get(i).equalsIgnoreCase(adminLogado)) {
                    String logado = admins.remove(i);
                    TextView tvLogado = criarTextViewAdmin(logado, true, false);
                    listaAdmins.addView(tvLogado);
                    break;
                }
            }
        }

        // Mostra os demais admins
        for (String admin : admins) {
            boolean principal = admin.equalsIgnoreCase("admin"); // admin principal
            TextView tv = criarTextViewAdmin(admin, false, principal);
            listaAdmins.addView(tv);
        }
    }

    /**
     * @param nome Nome do administrador
     * @param logado Se é o administrador logado
     * @param principal Se é o administrador principal (não pode ser removido)
     */
    private TextView criarTextViewAdmin(String nome, boolean logado, boolean principal) {
        TextView tv = new TextView(this);
        tv.setText(nome);
        tv.setTextSize(16);
        tv.setPadding(16, 16, 16, 16);

        if (logado) {
            tv.setTextColor(Color.GREEN);
        } else if (principal) {
            tv.setTextColor(Color.RED);
        } else {
            tv.setTextColor(Color.BLACK);
        }

        tv.setOnClickListener(v -> {
            if (logado) {
                Toast.makeText(this, "Administrador logado e não pode ser removido.", Toast.LENGTH_SHORT).show();
            } else if (principal) {
                Toast.makeText(this, "Administrador principal, não remover!", Toast.LENGTH_SHORT).show();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Remover Administrador")
                        .setMessage("Deseja realmente remover o administrador \"" + nome + "\"?")
                        .setPositiveButton("Sim", (dialog, which) -> removerAdministrador(nome))
                        .setNegativeButton("Não", null)
                        .show();
            }
        });

        return tv;
    }

    private void removerAdministrador(String nome) {
        BDCondominioHelper dbHelper = new BDCondominioHelper(this);
        SQLiteDatabase db = dbHelper.getWritableDatabase();

        int rows = db.delete(BDCondominioHelper.TABELA_USUARIOS_ADMIN,
                BDCondominioHelper.COL_ADMIN_USUARIO + "=?",
                new String[]{nome});

        if (rows > 0) {
            Toast.makeText(this, "Administrador removido com sucesso!", Toast.LENGTH_SHORT).show();
            mostrarAdministradores();
        } else {
            Toast.makeText(this, "Erro ao remover administrador.", Toast.LENGTH_SHORT).show();
        }
    }
}
