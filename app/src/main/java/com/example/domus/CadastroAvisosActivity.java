package com.example.domus;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CadastroAvisosActivity extends AppCompatActivity {

    private EditText editDataHora, editAssunto, editDescricao;
    private Button buttonSelecionarArquivos, buttonSalvar, buttonVoltarLista;
    private LinearLayout layoutAnexos, layoutAnexosExistentes;
    private TextView tvAnexosExistentes;

    private List<String> anexos = new ArrayList<>();
    private List<String> anexosExistentes = new ArrayList<>();
    private List<String> anexosRemovidos = new ArrayList<>();
    private AvisoDAO avisoDAO;
    private long editarId = -1;

    private final ActivityResultLauncher<String[]> selecionarArquivosLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenMultipleDocuments(), uris -> {
                if (uris != null && !uris.isEmpty()) {
                    if (anexos.size() + anexosExistentes.size() + uris.size() > 5) {
                        Toast.makeText(this, "Máximo de 5 anexos.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    for (Uri uri : uris) {
                        File arquivo = BackupUtil.copiarUriParaArquivo(this, uri);
                        if (arquivo != null) anexos.add(arquivo.getAbsolutePath());
                        else Toast.makeText(this, "Falha ao copiar arquivo.", Toast.LENGTH_SHORT).show();
                    }
                    atualizarAnexos();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_avisos);

        // Inicializar componentes
        editDataHora = findViewById(R.id.editDataHora);
        editAssunto = findViewById(R.id.editAssunto);
        editDescricao = findViewById(R.id.editDescricao);
        buttonSelecionarArquivos = findViewById(R.id.buttonSelecionarArquivos);
        buttonSalvar = findViewById(R.id.buttonSalvar);
        buttonVoltarLista = findViewById(R.id.buttonVoltarLista);
        layoutAnexos = findViewById(R.id.layoutAnexos);
        layoutAnexosExistentes = findViewById(R.id.layoutAnexosExistentes);
        tvAnexosExistentes = findViewById(R.id.tvAnexosExistentes);

        avisoDAO = new AvisoDAO(this);

        editDataHora.setOnClickListener(v -> mostrarDateTimePicker());

        buttonSelecionarArquivos.setOnClickListener(v -> {
            String[] tipos = {
                    "image/*",
                    "application/pdf",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            };
            selecionarArquivosLauncher.launch(tipos);
        });

        buttonSalvar.setOnClickListener(v -> salvarAviso());

        buttonVoltarLista.setOnClickListener(v -> {
            startActivity(new Intent(this, ListaAvisosActivity.class));
            finish();
        });

        // Edição
        if (getIntent().hasExtra("avisoJSON")) {
            try {
                JSONObject aviso = new JSONObject(getIntent().getStringExtra("avisoJSON"));
                editarId = aviso.optLong("id", -1);
                preencherCampos(aviso);
                buttonVoltarLista.setVisibility(Button.GONE);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void salvarAviso() {
        String dataHora = editDataHora.getText().toString().trim();
        String assunto = editAssunto.getText().toString().trim();
        String descricao = editDescricao.getText().toString().trim();

        if (dataHora.isEmpty() || assunto.isEmpty() || descricao.isEmpty()) {
            Toast.makeText(this, "Preencha data/hora, assunto e descrição.", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject aviso = new JSONObject();
            aviso.put("datahora", dataHora);
            aviso.put("assunto", assunto);
            aviso.put("descricao", descricao);

            // Combinar anexos existentes (que não foram removidos) com novos anexos
            JSONArray jsonAnexos = new JSONArray();

            // Adicionar anexos existentes que não foram removidos
            for (String path : anexosExistentes) {
                if (!anexosRemovidos.contains(path)) {
                    jsonAnexos.put(path);
                }
            }

            // Adicionar novos anexos
            for (String path : anexos) {
                jsonAnexos.put(path);
            }

            aviso.put("anexos", jsonAnexos.toString());

            boolean sucesso;
            if (editarId != -1) {
                sucesso = avisoDAO.atualizarAviso(editarId, aviso);
                if (sucesso) {
                    Toast.makeText(this, "Aviso atualizado!", Toast.LENGTH_SHORT).show();
                    // Remover fisicamente os arquivos que foram excluídos
                    removerArquivosExcluidos();
                }
            } else {
                long id = avisoDAO.inserirAviso(aviso);
                sucesso = id != -1;
                if (sucesso) Toast.makeText(this, "Aviso salvo!", Toast.LENGTH_SHORT).show();
            }

            if (sucesso) {
                // APENAS LIMPAR OS CAMPOS E PERMANECER NA MESMA PÁGINA
                limparCampos();

                // Se for uma edição, voltar para a lista de avisos
                if (editarId != -1) {
                    startActivity(new Intent(this, ListaAvisosActivity.class));
                    finish();
                }
            } else {
                Toast.makeText(this, "Erro ao salvar aviso.", Toast.LENGTH_SHORT).show();
            }

        } catch (Exception e) {
            Log.e("AVISO_ERROR", "Erro: " + e.getMessage());
            e.printStackTrace();
            Toast.makeText(this, "Erro ao salvar aviso.", Toast.LENGTH_SHORT).show();
        }
    }

    private void removerArquivosExcluidos() {
        for (String path : anexosRemovidos) {
            File arquivo = new File(path);
            if (arquivo.exists()) {
                arquivo.delete();
            }
        }
        anexosRemovidos.clear();
    }

    private void limparCampos() {
        editDataHora.setText("");
        editAssunto.setText("");
        editDescricao.setText("");
        anexos.clear();
        anexosExistentes.clear();
        anexosRemovidos.clear();
        atualizarAnexos();
        atualizarAnexosExistentes();
        editarId = -1;

        // Restaurar visibilidade do botão Voltar se estava oculto
        buttonVoltarLista.setVisibility(Button.VISIBLE);
    }

    private void atualizarAnexos() {
        layoutAnexos.removeAllViews();
        for (String path : anexos) {
            adicionarAnexoUI(path, false);
        }
    }

    private void atualizarAnexosExistentes() {
        layoutAnexosExistentes.removeAllViews();
        for (String path : anexosExistentes) {
            if (!anexosRemovidos.contains(path)) {
                adicionarAnexoUI(path, true);
            }
        }

        // Mostrar ou ocultar a seção de anexos existentes
        if (anexosExistentes.isEmpty() || anexosExistentes.size() == anexosRemovidos.size()) {
            tvAnexosExistentes.setVisibility(View.GONE);
            layoutAnexosExistentes.setVisibility(View.GONE);
        } else {
            tvAnexosExistentes.setVisibility(View.VISIBLE);
            layoutAnexosExistentes.setVisibility(View.VISIBLE);
        }
    }

    private void adicionarAnexoUI(String path, boolean isExistente) {
        File file = new File(path);

        LinearLayout layoutAnexo = new LinearLayout(this);
        layoutAnexo.setOrientation(LinearLayout.HORIZONTAL);
        layoutAnexo.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        layoutAnexo.setPadding(0, 8, 0, 8);

        // ImageView para mostrar o arquivo
        ImageView img = new ImageView(this);
        LinearLayout.LayoutParams paramsImagem = new LinearLayout.LayoutParams(100, 100);
        img.setLayoutParams(paramsImagem);
        img.setPadding(8, 8, 8, 8);

        if (file.getName().toLowerCase().endsWith(".jpg") ||
                file.getName().toLowerCase().endsWith(".png") ||
                file.getName().toLowerCase().endsWith(".jpeg")) {
            img.setImageURI(Uri.fromFile(file));
        } else {
            img.setImageResource(android.R.drawable.ic_menu_report_image);
        }
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);

        // TextView com nome do arquivo
        TextView tvNomeArquivo = new TextView(this);
        LinearLayout.LayoutParams paramsTexto = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT, 1
        );
        tvNomeArquivo.setLayoutParams(paramsTexto);
        tvNomeArquivo.setText(file.getName());
        tvNomeArquivo.setPadding(16, 0, 0, 0);
        tvNomeArquivo.setGravity(Gravity.CENTER_VERTICAL);

        // Botão para excluir o arquivo
        Button btnExcluir = new Button(this);
        LinearLayout.LayoutParams paramsBtn = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        btnExcluir.setLayoutParams(paramsBtn);
        btnExcluir.setText("Excluir");
        btnExcluir.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
        btnExcluir.setTextColor(getResources().getColor(android.R.color.white));
        btnExcluir.setAllCaps(false);
        btnExcluir.setPadding(16, 8, 16, 8);

        final String caminhoFinal = path;
        btnExcluir.setOnClickListener(v -> {
            if (isExistente) {
                // Para anexos existentes, marca para remoção
                anexosRemovidos.add(caminhoFinal);
                atualizarAnexosExistentes();
            } else {
                // Para novos anexos, remove diretamente
                anexos.remove(caminhoFinal);
                File arquivoParaExcluir = new File(caminhoFinal);
                if (arquivoParaExcluir.exists()) {
                    arquivoParaExcluir.delete();
                }
                atualizarAnexos();
            }
            Toast.makeText(this, "Arquivo removido", Toast.LENGTH_SHORT).show();
        });

        layoutAnexo.addView(img);
        layoutAnexo.addView(tvNomeArquivo);
        layoutAnexo.addView(btnExcluir);

        if (isExistente) {
            layoutAnexosExistentes.addView(layoutAnexo);
        } else {
            layoutAnexos.addView(layoutAnexo);
        }
    }

    private void mostrarDateTimePicker() {
        final Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            calendar.set(year, month, dayOfMonth);
            new TimePickerDialog(this, (view1, hourOfDay, minute) -> {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                editDataHora.setText(DateFormat.format("dd/MM/yyyy HH:mm", calendar));
            }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), true).show();
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void preencherCampos(JSONObject aviso) {
        try {
            editDataHora.setText(aviso.optString("datahora", ""));
            editAssunto.setText(aviso.optString("assunto", ""));
            editDescricao.setText(aviso.optString("descricao", ""));

            anexos.clear();
            anexosExistentes.clear();
            anexosRemovidos.clear();

            JSONArray jsonAnexos = new JSONArray(aviso.optString("anexos", "[]"));
            for (int i = 0; i < jsonAnexos.length(); i++) {
                String path = jsonAnexos.getString(i);
                anexosExistentes.add(path);
            }

            // Mostrar seção de anexos existentes
            atualizarAnexosExistentes();
            atualizarAnexos();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (avisoDAO != null) avisoDAO.fechar();
    }
}