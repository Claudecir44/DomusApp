package com.example.domus;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class RegistroDespesasActivity extends AppCompatActivity {

    private EditText editDataHora, editNome, editDescricao, editValor;
    private Button btnSalvar, btnLista, btnAnexar;
    private List<String> anexos = new ArrayList<>();
    private int editIndex = -1; // Para saber se é edição
    private DespesaDAO despesaDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro_despesas);

        editDataHora = findViewById(R.id.editDataHoraDespesa);
        editNome = findViewById(R.id.editNomeDespesa);
        editDescricao = findViewById(R.id.editDescricaoDespesa);
        editValor = findViewById(R.id.editValorDespesa);

        btnSalvar = findViewById(R.id.btnSalvarDespesa);
        btnLista = findViewById(R.id.btnListaDespesas);
        btnAnexar = findViewById(R.id.btnAnexarNota);

        despesaDAO = new DespesaDAO(this);

        // Preenche data e hora atual
        String dataHoraAtual = new SimpleDateFormat("dd/MM/yyyy HH:mm").format(Calendar.getInstance().getTime());
        editDataHora.setText(dataHoraAtual);

        btnAnexar.setOnClickListener(v -> selecionarArquivo());

        btnSalvar.setOnClickListener(v -> salvarDespesa());

        btnLista.setOnClickListener(v -> {
            Intent intent = new Intent(this, ListaDespesasActivity.class);
            startActivity(intent);
        });

        // Edição de despesa
        if (getIntent().hasExtra("despesaIndex")) {
            editIndex = getIntent().getIntExtra("despesaIndex", -1);
            if (editIndex != -1) {
                Despesa d = despesaDAO.buscarPorIndex(editIndex); // método correto
                if (d != null) {
                    editDataHora.setText(d.getDataHora());
                    editNome.setText(d.getNome());
                    editDescricao.setText(d.getDescricao());
                    editValor.setText(String.valueOf(d.getValor()));
                    anexos = new ArrayList<>(d.getAnexos());
                }
            }
        }
    }

    private void selecionarArquivo() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        String[] mimeTypes = {
                "application/pdf",
                "application/msword",
                "application/vnd.ms-excel",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
        };
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        startActivityForResult(intent, 200);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            if (uri != null) {
                anexos.add(uri.toString());
                Toast.makeText(this, "Arquivo anexado", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void salvarDespesa() {
        String dataHora = editDataHora.getText().toString();
        String nome = editNome.getText().toString();
        String descricao = editDescricao.getText().toString();
        String valorStr = editValor.getText().toString();

        if (dataHora.isEmpty() || nome.isEmpty() || descricao.isEmpty() || valorStr.isEmpty()) {
            Toast.makeText(this, "Preencha todos os campos", Toast.LENGTH_SHORT).show();
            return;
        }

        double valor = Double.parseDouble(valorStr);
        Despesa despesa = new Despesa(dataHora, nome, descricao, valor, anexos);

        if (editIndex == -1) {
            despesaDAO.inserir(despesa); // novo cadastro
            Toast.makeText(this, "Despesa salva!", Toast.LENGTH_SHORT).show();
        } else {
            despesaDAO.atualizar(editIndex, despesa); // edição
            Toast.makeText(this, "Despesa atualizada!", Toast.LENGTH_SHORT).show();
        }

        // Limpa campos mantendo na mesma tela
        editDataHora.setText(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(Calendar.getInstance().getTime()));
        editNome.setText("");
        editDescricao.setText("");
        editValor.setText("");
        anexos.clear();
        editIndex = -1;
    }
}
