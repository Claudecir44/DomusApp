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

public class RegistroOcorrenciasActivity extends AppCompatActivity {

    private EditText editTipo, editDescricao, editDataHora;
    private Button buttonAnexarArquivos, buttonSalvar, buttonListaOcorrencias;
    private RecyclerView recyclerAnexos;
    private LinearLayout layoutEnvolvidos;

    private List<Uri> anexos = new ArrayList<>();
    private RecyclerView.Adapter anexosAdapter;

    private OcorrenciaDAO ocorrenciaDAO;
    private long editarId = -1; // ID da ocorrência sendo editada (-1 = nova)

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
        setContentView(R.layout.activity_registro_ocorrencias);

        editTipo = findViewById(R.id.editTipo);
        editDescricao = findViewById(R.id.editDescricao);
        editDataHora = findViewById(R.id.editDataHora);
        buttonAnexarArquivos = findViewById(R.id.buttonAnexarArquivos);
        buttonSalvar = findViewById(R.id.buttonSalvar);
        buttonListaOcorrencias = findViewById(R.id.buttonListaOcorrencias);
        recyclerAnexos = findViewById(R.id.recyclerAnexos);
        layoutEnvolvidos = findViewById(R.id.layoutEnvolvidos);

        ocorrenciaDAO = new OcorrenciaDAO(this);

        anexosAdapter = new AnexosAdapter(anexos); // Adapter apenas com lista de URIs
        recyclerAnexos.setLayoutManager(new LinearLayoutManager(this));
        recyclerAnexos.setAdapter(anexosAdapter);

        editDataHora.setOnClickListener(v -> mostrarDateTimePicker());

        buttonAnexarArquivos.setOnClickListener(v -> {
            String[] tipos = {
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            };
            selecionarArquivosLauncher.launch(tipos);
        });

        buttonSalvar.setOnClickListener(v -> salvarOcorrencia());

        // Corrige botão Lista de Ocorrências
        buttonListaOcorrencias.setOnClickListener(v -> {
            Intent intent = new Intent(RegistroOcorrenciasActivity.this, ListaOcorrenciasActivity.class);
            startActivity(intent);
        });

        // Se recebeu dados para edição via Intent
        if (getIntent().hasExtra("ocorrenciaJSON")) {
            try {
                JSONObject ocorrencia = new JSONObject(getIntent().getStringExtra("ocorrenciaJSON"));
                editarId = ocorrencia.optLong("id", -1);
                preencherCamposParaEdicao(ocorrencia);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void salvarOcorrencia() {
        String tipo = editTipo.getText().toString().trim();
        String descricao = editDescricao.getText().toString().trim();
        String dataHora = editDataHora.getText().toString().trim();

        if (tipo.isEmpty() || descricao.isEmpty() || dataHora.isEmpty()) {
            Toast.makeText(this, "Preencha tipo, descrição e data/hora.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject ocorrencia = new JSONObject();
            ocorrencia.put("tipo", tipo);
            ocorrencia.put("descricao", descricao);
            ocorrencia.put("datahora", dataHora);

            // Salva anexos fisicamente e adiciona os caminhos ao JSON
            JSONArray jsonAnexos = new JSONArray();
            for (Uri uri : anexos) {
                File arquivo = BackupUtil.copiarUriParaArquivo(this, uri);
                if (arquivo != null) {
                    jsonAnexos.put(Uri.fromFile(arquivo).toString());
                }
            }
            ocorrencia.put("anexos", jsonAnexos.toString());

            // Salva envolvidos
            JSONArray jsonEnvolvidos = new JSONArray();
            for (int i = 0; i < layoutEnvolvidos.getChildCount(); i++) {
                if (layoutEnvolvidos.getChildAt(i) instanceof EditText) {
                    String nome = ((EditText) layoutEnvolvidos.getChildAt(i)).getText().toString().trim();
                    if (!nome.isEmpty()) {
                        jsonEnvolvidos.put(nome);
                    }
                }
            }
            ocorrencia.put("envolvidos", jsonEnvolvidos.toString());

            boolean sucesso;
            if (editarId != -1) {
                sucesso = ocorrenciaDAO.atualizarOcorrencia(editarId, ocorrencia);
            } else {
                long id = ocorrenciaDAO.inserirOcorrencia(ocorrencia);
                sucesso = id != -1;
            }

            if (sucesso) {
                Toast.makeText(this, "Ocorrência salva com sucesso!", Toast.LENGTH_SHORT).show();
                limparCampos();
                editarId = -1;
            } else {
                Toast.makeText(this, "Erro ao salvar ocorrência.", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao salvar ocorrência.", Toast.LENGTH_SHORT).show();
        }
    }

    private void limparCampos() {
        editTipo.setText("");
        editDescricao.setText("");
        editDataHora.setText("");
        anexos.clear();
        anexosAdapter.notifyDataSetChanged();
        for (int i = 0; i < layoutEnvolvidos.getChildCount(); i++) {
            if (layoutEnvolvidos.getChildAt(i) instanceof EditText) {
                ((EditText) layoutEnvolvidos.getChildAt(i)).setText("");
            }
        }
    }

    private void mostrarDateTimePicker() {
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

    private void preencherCamposParaEdicao(JSONObject ocorrencia) {
        try {
            editTipo.setText(ocorrencia.optString("tipo", ""));
            editDescricao.setText(ocorrencia.optString("descricao", ""));
            editDataHora.setText(ocorrencia.optString("datahora", ""));

            // Carrega anexos
            anexos.clear();
            JSONArray jsonAnexos = new JSONArray(ocorrencia.optString("anexos", "[]"));
            for (int i = 0; i < jsonAnexos.length(); i++) {
                anexos.add(Uri.parse(jsonAnexos.getString(i)));
            }
            anexosAdapter.notifyDataSetChanged();

            // Carrega envolvidos
            JSONArray jsonEnvolvidos = new JSONArray(ocorrencia.optString("envolvidos", "[]"));
            for (int i = 0; i < layoutEnvolvidos.getChildCount() && i < jsonEnvolvidos.length(); i++) {
                if (layoutEnvolvidos.getChildAt(i) instanceof EditText) {
                    ((EditText) layoutEnvolvidos.getChildAt(i)).setText(jsonEnvolvidos.getString(i));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
