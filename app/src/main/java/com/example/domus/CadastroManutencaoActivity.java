package com.example.domus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CadastroManutencaoActivity extends AppCompatActivity {

    private EditText etTipo, etDataHora, etLocal, etServico, etResponsavel, etValor, etNotas;
    private Button btnAnexar, btnSalvar, btnLista;
    private Uri documentoSelecionado;

    private int indexEditando = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_manutencao);

        // Campos
        etTipo = findViewById(R.id.editTipoManutencao);
        etDataHora = findViewById(R.id.editDataHora);
        etLocal = findViewById(R.id.editLocal);
        etServico = findViewById(R.id.editServico);
        etResponsavel = findViewById(R.id.editResponsavel);
        etValor = findViewById(R.id.editValor);
        etNotas = findViewById(R.id.editNotas);

        // Botões
        btnAnexar = findViewById(R.id.buttonAnexar);
        btnSalvar = findViewById(R.id.buttonSalvarManutencao);
        btnLista = findViewById(R.id.buttonListaManutencao);

        // Verifica se é edição
        indexEditando = getIntent().getIntExtra("index", -1);
        if (indexEditando >= 0) {
            carregarManutencao(indexEditando);
        }

        // Anexar arquivo
        ActivityResultLauncher<String[]> arquivoLauncher = registerForActivityResult(
                new ActivityResultContracts.OpenDocument(),
                uri -> {
                    if (uri != null) {
                        documentoSelecionado = uri;
                        Toast.makeText(this, "Arquivo selecionado", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        btnAnexar.setOnClickListener(v -> arquivoLauncher.launch(
                new String[]{"application/pdf", "application/msword", "application/vnd.ms-excel"}
        ));

        // Salvar manutenção
        btnSalvar.setOnClickListener(v -> salvarManutencao());

        // Abrir lista de manutenções
        btnLista.setOnClickListener(v -> startActivity(
                new Intent(CadastroManutencaoActivity.this, ListaManutencaoActivity.class)
        ));
    }

    private void carregarManutencao(int index) {
        try {
            JSONArray array = new JSONArray(getSharedPreferences("manutencoes", MODE_PRIVATE)
                    .getString("lista", "[]"));
            JSONObject m = array.getJSONObject(index);

            etTipo.setText(m.optString("tipo", ""));
            etDataHora.setText(m.optString("dataHora", ""));
            etLocal.setText(m.optString("local", ""));
            etServico.setText(m.optString("servico", ""));
            etResponsavel.setText(m.optString("responsavel", ""));
            etValor.setText(m.optString("valor", ""));
            etNotas.setText(m.optString("notas", ""));
            if (m.has("documento"))
                documentoSelecionado = Uri.parse(m.optString("documento", ""));

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void salvarManutencao() {
        try {
            String tipo = etTipo.getText().toString().trim();
            String dataHora = etDataHora.getText().toString().trim();
            String local = etLocal.getText().toString().trim();
            String servico = etServico.getText().toString().trim();
            String responsavel = etResponsavel.getText().toString().trim();
            String valor = etValor.getText().toString().trim();
            String notas = etNotas.getText().toString().trim();

            if (tipo.isEmpty() || dataHora.isEmpty() || servico.isEmpty()) {
                Toast.makeText(this, "Preencha os campos obrigatórios!", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONArray array = new JSONArray(getSharedPreferences("manutencoes", MODE_PRIVATE)
                    .getString("lista", "[]"));

            JSONObject m = new JSONObject();
            m.put("tipo", tipo);
            m.put("dataHora", dataHora);
            m.put("local", local);
            m.put("servico", servico);
            m.put("responsavel", responsavel);
            m.put("valor", valor);
            m.put("notas", notas);
            if (documentoSelecionado != null)
                m.put("documento", documentoSelecionado.toString());

            if (indexEditando >= 0) {
                array.put(indexEditando, m);
            } else {
                array.put(m);
            }

            getSharedPreferences("manutencoes", MODE_PRIVATE)
                    .edit().putString("lista", array.toString()).apply();

            Toast.makeText(this, "Manutenção salva!", Toast.LENGTH_SHORT).show();

            if (indexEditando < 0) {
                // Limpa campos após cadastro
                etTipo.setText("");
                etDataHora.setText("");
                etLocal.setText("");
                etServico.setText("");
                etResponsavel.setText("");
                etValor.setText("");
                etNotas.setText("");
                documentoSelecionado = null;
            } else {
                finish(); // fecha se for edição
            }

        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao salvar manutenção", Toast.LENGTH_SHORT).show();
        }
    }
}
