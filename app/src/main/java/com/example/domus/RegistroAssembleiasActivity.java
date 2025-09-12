package com.example.domus;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RegistroAssembleiasActivity extends AppCompatActivity {

    private EditText editDataHora, editAssunto, editLocal, editDescricao;
    private Button buttonAnexarArquivos, buttonSalvar, buttonListaAssembleias;
    private RecyclerView recyclerAnexos;
    private LinearLayout layoutAnexosContainer;

    private List<Uri> anexos = new ArrayList<>();
    private RecyclerView.Adapter anexosAdapter;

    private AssembleiaDAO assembleiaDAO;
    private long editarId = -1; // ID da assembleia sendo editada (-1 = nova)
    private boolean modoVisualizacao = false; // flag modo visualização

    private final ActivityResultLauncher<String[]> selecionarArquivosLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(), uris -> {
                if (uris != null && !uris.isEmpty()) {
                    anexos.addAll(uris);
                    anexosAdapter.notifyDataSetChanged();
                } else {
                    Toast.makeText(this, "Nenhum arquivo selecionado.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_assembleias);

        editDataHora = findViewById(R.id.editDataHora);
        editAssunto = findViewById(R.id.editAssunto);
        editLocal = findViewById(R.id.editLocal);
        editDescricao = findViewById(R.id.editDescricao);
        buttonAnexarArquivos = findViewById(R.id.buttonAnexarArquivos);
        buttonSalvar = findViewById(R.id.buttonSalvar);
        buttonListaAssembleias = findViewById(R.id.buttonListaAssembleias);
        recyclerAnexos = findViewById(R.id.recyclerAnexos);
        layoutAnexosContainer = findViewById(R.id.layoutAnexosContainer);

        assembleiaDAO = new AssembleiaDAO(this);

        anexosAdapter = new AnexosAdapter(anexos);
        recyclerAnexos.setLayoutManager(new LinearLayoutManager(this));
        recyclerAnexos.setAdapter(anexosAdapter);

        editDataHora.setOnClickListener(v -> mostrarDateTimePicker());

        buttonAnexarArquivos.setOnClickListener(v -> {
            if (!modoVisualizacao) { // só permite anexar se não for visualização
                String[] tipos = {
                        "application/pdf",
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        "application/vnd.ms-excel",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                };
                selecionarArquivosLauncher.launch(tipos);
            }
        });

        buttonSalvar.setOnClickListener(v -> {
            if (!modoVisualizacao) salvarAssembleia();
        });

        buttonListaAssembleias.setOnClickListener(v -> {
            Intent intent = new Intent(RegistroAssembleiasActivity.this, ListaAssembleiasActivity.class);
            startActivity(intent);
        });

        // Verifica se veio assembleia para edição/visualização
        if (getIntent().hasExtra("assembleiaJSON")) {
            try {
                JSONObject assembleia = new JSONObject(getIntent().getStringExtra("assembleiaJSON"));
                editarId = assembleia.optLong("id", -1);
                modoVisualizacao = getIntent().getBooleanExtra("modoVisualizacao", false);
                preencherCampos(assembleia);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Se for modo visualização, desabilita edição
        if (modoVisualizacao) {
            editDataHora.setEnabled(false);
            editAssunto.setEnabled(false);
            editLocal.setEnabled(false);
            editDescricao.setEnabled(false);
            buttonSalvar.setEnabled(false);
            buttonAnexarArquivos.setEnabled(false);
        }
    }

    private void salvarAssembleia() {
        String dataHora = editDataHora.getText().toString().trim();
        String assunto = editAssunto.getText().toString().trim();
        String local = editLocal.getText().toString().trim();
        String descricao = editDescricao.getText().toString().trim();

        if (dataHora.isEmpty() || assunto.isEmpty() || local.isEmpty() || descricao.isEmpty()) {
            Toast.makeText(this, "Preencha data/hora, assunto, local e descrição.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject assembleia = new JSONObject();
            assembleia.put("datahora", dataHora);
            assembleia.put("assunto", assunto);
            assembleia.put("local", local);
            assembleia.put("descricao", descricao);

            JSONArray jsonAnexos = new JSONArray();
            for (Uri uri : anexos) {
                File arquivo = BackupUtil.copiarUriParaArquivo(this, uri);
                if (arquivo != null) jsonAnexos.put(Uri.fromFile(arquivo).toString());
            }
            assembleia.put("anexos", jsonAnexos.toString());

            boolean sucesso;
            if (editarId != -1) {
                sucesso = assembleiaDAO.atualizarAssembleia(editarId, assembleia);
            } else {
                long id = assembleiaDAO.inserirAssembleia(assembleia);
                sucesso = id != -1;
            }

            if (sucesso) {
                Toast.makeText(this, "Assembleia salva com sucesso!", Toast.LENGTH_SHORT).show();
                limparCampos();
                editarId = -1;
            } else {
                Toast.makeText(this, "Erro ao salvar assembleia.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao salvar assembleia.", Toast.LENGTH_SHORT).show();
        }
    }

    private void limparCampos() {
        editDataHora.setText("");
        editAssunto.setText("");
        editLocal.setText("");
        editDescricao.setText("");
        anexos.clear();
        anexosAdapter.notifyDataSetChanged();
    }

    private void mostrarDateTimePicker() {
        if (modoVisualizacao) return; // não mostra picker se for visualização

        final Calendar calendar = Calendar.getInstance();

        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                String dataHoraFormatada = DateFormat.format("dd/MM/yyyy HH:mm", calendar).toString();
                editDataHora.setText(dataHoraFormatada);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void preencherCampos(JSONObject assembleia) {
        try {
            editDataHora.setText(assembleia.optString("datahora", ""));
            editAssunto.setText(assembleia.optString("assunto", ""));
            editLocal.setText(assembleia.optString("local", ""));
            editDescricao.setText(assembleia.optString("descricao", ""));

            anexos.clear();
            JSONArray jsonAnexos = new JSONArray(assembleia.optString("anexos", "[]"));
            for (int i = 0; i < jsonAnexos.length(); i++) {
                anexos.add(Uri.parse(jsonAnexos.getString(i)));
            }
            anexosAdapter.notifyDataSetChanged();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
