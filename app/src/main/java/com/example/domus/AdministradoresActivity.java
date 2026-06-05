package com.example.domus;

import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import com.example.domus.presentation.loginadmin.LoginAdminViewModel;
import java.util.ArrayList;
import java.util.List;

public class AdministradoresActivity extends AppCompatActivity {

    private static final String PREFS_NAME = "admin_prefs";
    private static final String KEY_ADMIN_LOGADO = "admin_logado";
    private static final String TAG = "AdministradoresActivity";

    private LinearLayout listaAdmins;
    private String adminLogado;
    private LoginAdminViewModel viewModel;
    private BDCondominioHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_administradores);

        listaAdmins = findViewById(R.id.listaAdmins);
        dbHelper = new BDCondominioHelper(this);

        // Inicializa ViewModel
        viewModel = new ViewModelProvider(this).get(LoginAdminViewModel.class);

        // Observa mudanças na lista de admins
        viewModel.getAdminsChanged().observe(this, changed -> {
            Log.d(TAG, "🔄 AdminsChanged observado: " + changed);
            if (changed != null && changed) {
                mostrarAdministradores();
                viewModel.onAdminsChangedHandled();
            }
        });

        // Observa mensagens de erro/sucesso
        viewModel.getUiState().observe(this, uiState -> {
            if (uiState.getErrorMessage() != null) {
                Log.d(TAG, "❌ Erro: " + uiState.getErrorMessage());
                Toast.makeText(this, uiState.getErrorMessage(), Toast.LENGTH_SHORT).show();
                viewModel.onMessageShown();
            }
            if (uiState.getSuccessMessage() != null) {
                Log.d(TAG, "✅ Sucesso: " + uiState.getSuccessMessage());
                Toast.makeText(this, uiState.getSuccessMessage(), Toast.LENGTH_SHORT).show();
                viewModel.onMessageShown();
            }
        });

        // Recupera admin logado
        adminLogado = getAdminLogado(this);
        Log.d(TAG, "👤 Admin logado: " + adminLogado);

        mostrarAdministradores();
    }

    private String getAdminLogado(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_ADMIN_LOGADO, null);
    }

    private void mostrarAdministradores() {
        Log.d(TAG, "📋 Carregando lista de administradores...");
        listaAdmins.removeAllViews();

        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        List<AdminInfo> admins = new ArrayList<>();

        try {
            cursor = db.query(BDCondominioHelper.TABELA_USUARIOS_ADMIN,
                    new String[]{
                            BDCondominioHelper.COL_ADMIN_USUARIO,
                            BDCondominioHelper.COL_ADMIN_TIPO
                    },
                    null, null, null, null,
                    BDCondominioHelper.COL_ADMIN_USUARIO + " ASC");

            if (cursor != null) {
                while (cursor.moveToNext()) {
                    String usuario = cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_ADMIN_USUARIO));
                    String tipo = cursor.getString(cursor.getColumnIndexOrThrow(BDCondominioHelper.COL_ADMIN_TIPO));
                    admins.add(new AdminInfo(usuario, tipo));
                    Log.d(TAG, "📌 Admin encontrado: " + usuario + " (tipo: " + tipo + ")");
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Erro ao carregar administradores: " + e.getMessage());
            Toast.makeText(this, "Erro ao carregar administradores", Toast.LENGTH_SHORT).show();
        } finally {
            if (cursor != null) cursor.close();
            db.close();
        }

        Log.d(TAG, "📊 Total de admins encontrados: " + admins.size());

        // Mostra admin logado primeiro
        AdminInfo loggedAdminInfo = null;
        for (int i = 0; i < admins.size(); i++) {
            if (admins.get(i).usuario.equalsIgnoreCase(adminLogado)) {
                loggedAdminInfo = admins.remove(i);
                break;
            }
        }

        if (loggedAdminInfo != null) {
            TextView tvLogado = criarTextViewAdmin(
                    loggedAdminInfo.usuario,
                    true,
                    "master".equals(loggedAdminInfo.tipo)
            );
            listaAdmins.addView(tvLogado);
        }

        // Mostra os demais admins
        for (AdminInfo admin : admins) {
            boolean isMaster = "master".equals(admin.tipo);
            TextView tv = criarTextViewAdmin(admin.usuario, false, isMaster);
            listaAdmins.addView(tv);
        }

        // Se não houver administradores
        if (admins.isEmpty() && loggedAdminInfo == null) {
            TextView tvEmpty = new TextView(this);
            tvEmpty.setText("Nenhum administrador cadastrado");
            tvEmpty.setTextSize(14);
            tvEmpty.setPadding(16, 16, 16, 16);
            tvEmpty.setTextColor(Color.GRAY);
            tvEmpty.setGravity(android.view.Gravity.CENTER);
            listaAdmins.addView(tvEmpty);
        }
    }

    private TextView criarTextViewAdmin(String nome, boolean logado, boolean isMaster) {
        TextView tv = new TextView(this);
        tv.setText(nome);
        tv.setTextSize(16);
        tv.setPadding(16, 16, 16, 16);

        if (logado) {
            tv.setTextColor(Color.parseColor("#2E7D32")); // Verde escuro
            tv.setBackgroundColor(Color.parseColor("#E8F5E9"));
            tv.append(" (logado)");
        } else if (isMaster) {
            tv.setTextColor(Color.RED);
            tv.setBackgroundColor(Color.parseColor("#FFEBEE"));
            tv.append(" (master)");
        } else {
            tv.setTextColor(Color.BLACK);
        }

        tv.setOnClickListener(v -> {
            if (logado) {
                Toast.makeText(this, "Você não pode remover seu próprio usuário!", Toast.LENGTH_SHORT).show();
            } else if (isMaster) {
                Toast.makeText(this, "Administrador master não pode ser removido!", Toast.LENGTH_SHORT).show();
            } else {
                new AlertDialog.Builder(this)
                        .setTitle("Remover Administrador")
                        .setMessage("Deseja realmente remover o administrador \"" + nome + "\"?")
                        .setPositiveButton("Sim", (dialog, which) -> {
                            Log.d(TAG, "🗑️ Removendo admin: " + nome);
                            // Remover diretamente usando o dbHelper
                            removerAdminDireto(nome);
                        })
                        .setNegativeButton("Não", null)
                        .show();
            }
        });

        return tv;
    }

    /**
     * Remove administrador diretamente
     */
    private void removerAdminDireto(String nome) {
        Log.d(TAG, "🔧 Executando remoção direta de: " + nome);

        boolean sucesso = dbHelper.removerAdmin(nome);

        if (sucesso) {
            Log.d(TAG, "✅ Admin removido com sucesso: " + nome);
            Toast.makeText(this, "Administrador removido com sucesso!", Toast.LENGTH_SHORT).show();
            // Recarrega a lista
            mostrarAdministradores();
        } else {
            Log.e(TAG, "❌ Falha ao remover admin: " + nome);
            Toast.makeText(this, "Erro ao remover administrador!", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "🔄 onResume - Recarregando lista");
        mostrarAdministradores();
    }

    // Classe auxiliar para armazenar informações do admin
    private static class AdminInfo {
        String usuario;
        String tipo;

        AdminInfo(String usuario, String tipo) {
            this.usuario = usuario;
            this.tipo = tipo;
        }
    }
}