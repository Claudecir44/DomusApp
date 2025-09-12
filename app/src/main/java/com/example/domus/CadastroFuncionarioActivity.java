package com.example.domus;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CadastroFuncionarioActivity extends AppCompatActivity {

    private EditText etNome, etRua, etNumero, etBairro, etCep, etCidade, etEstado, etPais, etTelefone, etEmail,
            etRG, etCPF, etCargaHoraria, etTurno, etHoraEntrada, etHoraSaida;
    private Button btnSalvar;
    private ImageView imageFuncionario;
    private Uri imagemSelecionadaUri;
    private int indexEditando = -1;

    private final ActivityResultLauncher<String> selecionarImagemLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    getContentResolver().takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    imagemSelecionadaUri = uri;
                    imageFuncionario.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_funcionario);

        // Campos
        etNome = findViewById(R.id.editNomeFuncionario);
        etRua = findViewById(R.id.editRuaFuncionario);
        etNumero = findViewById(R.id.editNumeroFuncionario);
        etBairro = findViewById(R.id.editBairroFuncionario);
        etCep = findViewById(R.id.editCepFuncionario);
        etCidade = findViewById(R.id.editCidadeFuncionario);
        etEstado = findViewById(R.id.editEstadoFuncionario);
        etPais = findViewById(R.id.editPaisFuncionario);
        etTelefone = findViewById(R.id.editTelefoneFuncionario);
        etEmail = findViewById(R.id.editEmailFuncionario);

        etRG = findViewById(R.id.editRGFuncionario);
        etCPF = findViewById(R.id.editCPFFuncionario);
        etCargaHoraria = findViewById(R.id.editCargaHoraria);
        etTurno = findViewById(R.id.editTurno);
        etHoraEntrada = findViewById(R.id.editHoraEntrada);
        etHoraSaida = findViewById(R.id.editHoraSaida);

        btnSalvar = findViewById(R.id.buttonSalvarFuncionario);
        imageFuncionario = findViewById(R.id.imageFuncionario);

        // Selecionar imagem
        imageFuncionario.setOnClickListener(v -> selecionarImagemLauncher.launch("image/*"));

        indexEditando = getIntent().getIntExtra("index", -1);
        if (indexEditando >= 0) {
            carregarFuncionario(indexEditando);
        }

        btnSalvar.setOnClickListener(v -> salvarFuncionario());
    }

    private void carregarFuncionario(int index) {
        SharedPreferences prefs = getSharedPreferences("funcionarios", MODE_PRIVATE);
        String json = prefs.getString("lista", "[]");
        try {
            JSONArray array = new JSONArray(json);
            JSONObject f = array.getJSONObject(index);

            etNome.setText(f.getString("nome"));
            etRua.setText(f.optString("rua", ""));
            etNumero.setText(f.optString("numero", ""));
            etBairro.setText(f.optString("bairro", ""));
            etCep.setText(f.optString("cep", ""));
            etCidade.setText(f.optString("cidade", ""));
            etEstado.setText(f.optString("estado", ""));
            etPais.setText(f.optString("pais", ""));
            etTelefone.setText(f.optString("telefone", ""));
            etEmail.setText(f.optString("email", ""));

            etRG.setText(f.getString("rg"));
            etCPF.setText(f.getString("cpf"));
            etCargaHoraria.setText(f.getString("cargaHoraria"));
            etTurno.setText(f.getString("turno"));
            etHoraEntrada.setText(f.getString("horaEntrada"));
            etHoraSaida.setText(f.getString("horaSaida"));

            String uriStr = f.optString("imagemUri", "");
            if (!uriStr.isEmpty()) {
                imagemSelecionadaUri = Uri.parse(uriStr);
                imageFuncionario.setImageURI(imagemSelecionadaUri);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void salvarFuncionario() {
        String nome = etNome.getText().toString().trim();

        if (nome.isEmpty() || imagemSelecionadaUri == null) {
            Toast.makeText(this, "Preencha o nome e selecione uma imagem!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Outros campos são opcionais
        String rua = etRua.getText().toString().trim();
        String numero = etNumero.getText().toString().trim();
        String bairro = etBairro.getText().toString().trim();
        String cep = etCep.getText().toString().trim();
        String cidade = etCidade.getText().toString().trim();
        String estado = etEstado.getText().toString().trim();
        String pais = etPais.getText().toString().trim();
        String telefone = etTelefone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String rg = etRG.getText().toString().trim();
        String cpf = etCPF.getText().toString().trim();
        String cargaHoraria = etCargaHoraria.getText().toString().trim();
        String turno = etTurno.getText().toString().trim();
        String horaEntrada = etHoraEntrada.getText().toString().trim();
        String horaSaida = etHoraSaida.getText().toString().trim();

        SharedPreferences prefs = getSharedPreferences("funcionarios", MODE_PRIVATE);
        String json = prefs.getString("lista", "[]");

        try {
            JSONArray array = new JSONArray(json);
            JSONObject f = new JSONObject();

            int id = indexEditando >= 0 ? getIdExistente(indexEditando) : gerarNovoId();
            f.put("id", id);
            f.put("nome", nome);
            f.put("rua", rua);
            f.put("numero", numero);
            f.put("bairro", bairro);
            f.put("cep", cep);
            f.put("cidade", cidade);
            f.put("estado", estado);
            f.put("pais", pais);
            f.put("telefone", telefone);
            f.put("email", email);
            f.put("rg", rg);
            f.put("cpf", cpf);
            f.put("cargaHoraria", cargaHoraria);
            f.put("turno", turno);
            f.put("horaEntrada", horaEntrada);
            f.put("horaSaida", horaSaida);
            f.put("imagemUri", imagemSelecionadaUri.toString());

            if (indexEditando >= 0) {
                array.put(indexEditando, f);
            } else {
                array.put(f);
            }

            prefs.edit().putString("lista", array.toString()).apply();
            Toast.makeText(this, "Funcionário salvo!", Toast.LENGTH_SHORT).show();
            finish();

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private int gerarNovoId() {
        SharedPreferences prefs = getSharedPreferences("funcionarios", MODE_PRIVATE);
        String json = prefs.getString("lista", "[]");
        int maxId = 0;
        try {
            JSONArray array = new JSONArray(json);
            for (int i = 0; i < array.length(); i++) {
                JSONObject f = array.getJSONObject(i);
                int fid = f.getInt("id");
                if (fid > maxId) maxId = fid;
            }
        } catch (Exception e) { e.printStackTrace(); }
        return maxId + 1;
    }

    private int getIdExistente(int index) {
        SharedPreferences prefs = getSharedPreferences("funcionarios", MODE_PRIVATE);
        String json = prefs.getString("lista", "[]");
        try {
            JSONArray array = new JSONArray(json);
            JSONObject f = array.getJSONObject(index);
            return f.getInt("id");
        } catch (Exception e) { e.printStackTrace(); }
        return -1;
    }
}
