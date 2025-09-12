package com.example.domus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class ManutencaoActivity extends AppCompatActivity {

    private static final int PICK_IMAGES_REQUEST = 1;

    private EditText etDataHora, etLocal, etServico, etResponsavel, etValor, etNotas;
    private Spinner spinnerTipo;
    private Button btnAnexarManutencao, btnSalvarManutencao, btnListaManutencao;
    private List<Uri> anexosUris = new ArrayList<>();
    private ManutencaoDAO manutencaoDAO;
    private long editarId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manutencao);

        manutencaoDAO = new ManutencaoDAO(this);

        // Inicializar componentes com os IDs do XML
        spinnerTipo = findViewById(R.id.spinnerTipo);
        etDataHora = findViewById(R.id.etDataHora);
        etLocal = findViewById(R.id.etLocal);
        etServico = findViewById(R.id.etServico);
        etResponsavel = findViewById(R.id.etResponsavel);
        etValor = findViewById(R.id.etValor);
        etNotas = findViewById(R.id.etNotas);
        btnAnexarManutencao = findViewById(R.id.btnAnexarManutencao);
        btnSalvarManutencao = findViewById(R.id.btnSalvarManutencao);
        btnListaManutencao = findViewById(R.id.btnListaManutencao);

        // Configurar spinner com os tipos de manutenção
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.tipos_manutencao, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTipo.setAdapter(adapter);

        // Verificar se é edição
        Intent intent = getIntent();
        if (intent.hasExtra("MANUTENCAO_ID")) {
            editarId = intent.getLongExtra("MANUTENCAO_ID", -1);
            carregarManutencao(editarId);
        }

        btnAnexarManutencao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selecionarAnexos();
            }
        });

        btnSalvarManutencao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salvarManutencao();
            }
        });

        btnListaManutencao.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listarManutencoes();
            }
        });
    }

    private void carregarManutencao(long id) {
        Manutencao m = manutencaoDAO.getManutencaoById(id);
        if (m != null) {
            // Configurar spinner com o tipo correto
            ArrayAdapter<CharSequence> adapter = (ArrayAdapter<CharSequence>) spinnerTipo.getAdapter();
            int position = adapter.getPosition(m.getTipo());
            if (position >= 0) {
                spinnerTipo.setSelection(position);
            }

            etDataHora.setText(m.getDataHora());
            etLocal.setText(m.getLocal());
            etServico.setText(m.getServico());
            etResponsavel.setText(m.getResponsavel());
            etValor.setText(m.getValor());
            etNotas.setText(m.getNotas());

            // Carregar anexos (se houver)
            if (m.getAnexos() != null && !m.getAnexos().isEmpty()) {
                anexosUris.clear();
                for (String caminho : m.getAnexos()) {
                    anexosUris.add(Uri.parse(caminho));
                }
            }
        }
    }

    private void selecionarAnexos() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        startActivityForResult(intent, PICK_IMAGES_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK) {
            if (data != null) {
                if (data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for (int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        anexosUris.add(imageUri);
                    }
                } else if (data.getData() != null) {
                    Uri imageUri = data.getData();
                    anexosUris.add(imageUri);
                }
                Toast.makeText(this, "Anexos selecionados: " + anexosUris.size(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void salvarManutencao() {
        String tipo = spinnerTipo.getSelectedItem().toString();
        String dataHora = etDataHora.getText().toString();
        String local = etLocal.getText().toString();
        String servico = etServico.getText().toString();
        String responsavel = etResponsavel.getText().toString();
        String valor = etValor.getText().toString();
        String notas = etNotas.getText().toString();

        if (tipo.isEmpty() || dataHora.isEmpty() || local.isEmpty() || servico.isEmpty()) {
            Toast.makeText(this, "Preencha os campos obrigatórios", Toast.LENGTH_SHORT).show();
            return;
        }

        Manutencao manutencao = new Manutencao();
        manutencao.setTipo(tipo);
        manutencao.setDataHora(dataHora);
        manutencao.setLocal(local);
        manutencao.setServico(servico);
        manutencao.setResponsavel(responsavel);
        manutencao.setValor(valor);
        manutencao.setNotas(notas);

        // Converter URIs para strings
        List<String> caminhosAnexos = new ArrayList<>();
        for (Uri uri : anexosUris) {
            caminhosAnexos.add(uri.toString());
        }
        manutencao.setAnexos(caminhosAnexos);

        long resultado;
        if (editarId != -1) {
            manutencao.setId((int) editarId);
            resultado = manutencaoDAO.atualizarManutencao(manutencao);
            if (resultado > 0) {
                Toast.makeText(this, "Manutenção atualizada com sucesso!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Erro ao atualizar manutenção", Toast.LENGTH_SHORT).show();
            }
        } else {
            resultado = manutencaoDAO.salvarManutencao(manutencao);
            if (resultado != -1) {
                Toast.makeText(this, "Manutenção salva com sucesso!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Erro ao salvar manutenção", Toast.LENGTH_SHORT).show();
            }
        }

        if (resultado != -1) {
            finish();
        }
    }

    private void listarManutencoes() {
        Intent intent = new Intent(this, ListaManutencaoActivity.class);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (manutencaoDAO != null) {
            manutencaoDAO.fechar();
        }
    }
}