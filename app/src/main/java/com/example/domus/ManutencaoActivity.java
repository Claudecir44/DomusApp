package com.example.domus;

import android.content.Intent;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ManutencaoActivity extends AppCompatActivity {

    private EditText etTipo, etDataHora, etLocal, etServico, etResponsavel, etValor;
    private RecyclerView recyclerViewAnexos;
    private Button btnSalvar, btnAnexarNotas, btnListaManutencao;

    private ManutencaoDAO manutencaoDAO;
    private List<Uri> anexosCaminhos = new ArrayList<>();
    private AnexosAdapter anexosAdapter;

    private long editarId = -1;

    private final ActivityResultLauncher<String[]> selecionarArquivosLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(), uris -> {
                if (uris != null && !uris.isEmpty()) {
                    for (Uri uri : uris) {
                        // Concede permissão persistente
                        getContentResolver().takePersistableUriPermission(
                                uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION
                        );
                    }
                    anexosCaminhos.addAll(uris);
                    anexosAdapter.notifyDataSetChanged();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manutencao);

        // Inicialização de campos
        etTipo = findViewById(R.id.etTipo);
        etDataHora = findViewById(R.id.etDataHora);
        etLocal = findViewById(R.id.etLocal);
        etServico = findViewById(R.id.etServico);
        etResponsavel = findViewById(R.id.etResponsavel);
        etValor = findViewById(R.id.etValor);

        recyclerViewAnexos = findViewById(R.id.recyclerAnexos);
        recyclerViewAnexos.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        anexosAdapter = new AnexosAdapter(anexosCaminhos);
        recyclerViewAnexos.setAdapter(anexosAdapter);

        btnSalvar = findViewById(R.id.btnSalvarManutencao);
        btnAnexarNotas = findViewById(R.id.btnAnexarManutencao);
        btnListaManutencao = findViewById(R.id.btnListaManutencao);

        manutencaoDAO = new ManutencaoDAO(this);

        // DateTimePicker para etDataHora
        etDataHora.setOnClickListener(v -> mostrarDateTimePicker());

        // Verificar se é edição
        editarId = getIntent().getLongExtra("editarId", -1);
        if (editarId > 0) {
            carregarManutencao(editarId);
        }

        btnAnexarNotas.setOnClickListener(v -> {
            String[] tipos = {
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            };
            selecionarArquivosLauncher.launch(tipos);
        });

        btnSalvar.setOnClickListener(v -> salvarManutencao());

        btnListaManutencao.setOnClickListener(v -> {
            Intent intent = new Intent(ManutencaoActivity.this, ListaManutencaoActivity.class);
            startActivity(intent);
        });
    }

    private void mostrarDateTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                String dataHoraFormatada = DateFormat.format("dd/MM/yyyy HH:mm", calendar).toString();
                etDataHora.setText(dataHoraFormatada);
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void carregarManutencao(long id) {
        Manutencao m = manutencaoDAO.getManutencaoById(id);
        if (m != null) {
            etTipo.setText(m.getTipo());
            etDataHora.setText(m.getDataHora());
            etLocal.setText(m.getLocal());
            etServico.setText(m.getServico());
            etResponsavel.setText(m.getResponsavel());
            etValor.setText(m.getValor());

            anexosCaminhos.clear();
            if (m.getAnexos() != null) {
                for (String path : m.getAnexos()) {
                    anexosCaminhos.add(Uri.parse(path));
                }
            }
            anexosAdapter.notifyDataSetChanged();
        }
    }

    private void salvarManutencao() {
        String tipo = etTipo.getText().toString().trim();
        String dataHora = etDataHora.getText().toString().trim();
        String local = etLocal.getText().toString().trim();
        String servico = etServico.getText().toString().trim();
        String responsavel = etResponsavel.getText().toString().trim();
        String valor = etValor.getText().toString().trim();

        if (tipo.isEmpty() || dataHora.isEmpty()) {
            Toast.makeText(this, "Preencha os campos Tipo e Data/Hora.", Toast.LENGTH_SHORT).show();
            return;
        }

        Manutencao manutencao = new Manutencao();
        manutencao.setTipo(tipo);
        manutencao.setDataHora(dataHora);
        manutencao.setLocal(local);
        manutencao.setServico(servico);
        manutencao.setResponsavel(responsavel);
        manutencao.setValor(valor);

        List<String> paths = new ArrayList<>();
        for (Uri uri : anexosCaminhos) {
            paths.add(uri.toString());
        }
        manutencao.setAnexos(paths);

        boolean sucesso;
        if (editarId > 0) {
            manutencao.setId(editarId);
            sucesso = manutencaoDAO.atualizarManutencao(manutencao) > 0;
        } else {
            long id = manutencaoDAO.inserirManutencao(manutencao);
            sucesso = id != -1;
        }

        if (sucesso) {
            Toast.makeText(this, "Manutenção salva com sucesso!", Toast.LENGTH_SHORT).show();
            limparCampos();
        } else {
            Toast.makeText(this, "Erro ao salvar manutenção.", Toast.LENGTH_SHORT).show();
        }
    }

    private void limparCampos() {
        etTipo.setText("");
        etDataHora.setText("");
        etLocal.setText("");
        etServico.setText("");
        etResponsavel.setText("");
        etValor.setText("");

        anexosCaminhos.clear();
        anexosAdapter.notifyDataSetChanged();

        editarId = -1; // reseta modo de edição
    }
}
