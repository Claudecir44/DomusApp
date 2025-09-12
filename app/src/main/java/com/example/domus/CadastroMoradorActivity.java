package com.example.domus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

public class CadastroMoradorActivity extends AppCompatActivity {

    private TextView textCodigo;
    private ImageView imageMorador;
    private EditText editNome, editCPF, editEmail, editRua, editNumero, editTelefone, editQuadra, editLote;
    private Button btnSalvar;

    private MoradorDAO moradorDAO;
    private String editarCod = null;
    private Uri fotoUri = null;
    private String savedImagePath = null;

    private final ActivityResultLauncher<String> selectImageLauncher =
            registerForActivityResult(new ActivityResultContracts.GetContent(), uri -> {
                if (uri != null) {
                    fotoUri = uri;
                    imageMorador.setImageURI(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_morador);

        textCodigo = findViewById(R.id.textCodigo);
        imageMorador = findViewById(R.id.imageMorador);
        editNome = findViewById(R.id.editNome);
        editCPF = findViewById(R.id.editCPF);
        editEmail = findViewById(R.id.editEmail);
        editRua = findViewById(R.id.editRua);
        editNumero = findViewById(R.id.editNumero);
        editTelefone = findViewById(R.id.editTelefone);
        editQuadra = findViewById(R.id.editQuadra);
        editLote = findViewById(R.id.editLote);
        btnSalvar = findViewById(R.id.btnSalvar);

        moradorDAO = new MoradorDAO(this);

        // Clique para selecionar foto
        imageMorador.setOnClickListener(v -> selectImageLauncher.launch("image/*"));

        // Verifica se é edição
        if (getIntent().hasExtra("editarCod")) {
            editarCod = getIntent().getStringExtra("editarCod");
            preencherCampos(editarCod);
        } else {
            // Novo cadastro: gerar código inicial
            int novoCod = gerarNovoCodigo();
            textCodigo.setText("Cod: " + String.format("%02d", novoCod));
        }

        btnSalvar.setOnClickListener(v -> salvarMorador());
    }

    private void preencherCampos(String cod) {
        JSONArray lista = moradorDAO.getListaMoradores();
        try {
            for (int i = 0; i < lista.length(); i++) {
                JSONObject morador = lista.getJSONObject(i);
                if (morador.optString("cod").equals(cod)) {
                    textCodigo.setText("Cod: " + morador.optString("cod"));
                    editNome.setText(morador.optString("nome"));
                    editCPF.setText(morador.optString("cpf"));
                    editEmail.setText(morador.optString("email", ""));
                    editRua.setText(morador.optString("rua", ""));
                    editNumero.setText(morador.optString("numero", ""));
                    editTelefone.setText(morador.optString("telefone", ""));
                    editQuadra.setText(morador.optString("quadra", ""));
                    editLote.setText(morador.optString("lote", ""));

                    // Preenche a foto - agora usando o caminho salvo
                    String imagePath = morador.optString("foto", "");
                    if (!imagePath.isEmpty()) {
                        try {
                            // Carregar a imagem do caminho salvo
                            Uri loadedUri = BackupUtil.loadImageFromInternalStorage(this, imagePath);
                            if (loadedUri != null) {
                                fotoUri = loadedUri;
                                imageMorador.setImageURI(loadedUri);
                                savedImagePath = imagePath; // Manter referência ao caminho
                            } else {
                                imageMorador.setImageResource(android.R.drawable.ic_menu_camera);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            imageMorador.setImageResource(android.R.drawable.ic_menu_camera);
                        }
                    }
                    break;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int gerarNovoCodigo() {
        JSONArray lista = moradorDAO.getListaMoradores();
        int maxCod = 0;
        try {
            for (int i = 0; i < lista.length(); i++) {
                JSONObject m = lista.getJSONObject(i);
                String codStr = m.optString("cod", "0");
                try {
                    int c = Integer.parseInt(codStr);
                    if (c > maxCod) maxCod = c;
                } catch (NumberFormatException e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return maxCod + 1;
    }

    private void salvarMorador() {
        String nome = editNome.getText().toString().trim();
        String cpf = editCPF.getText().toString().trim();
        String email = editEmail.getText().toString().trim();
        String rua = editRua.getText().toString().trim();
        String numero = editNumero.getText().toString().trim();
        String telefone = editTelefone.getText().toString().trim();
        String quadra = editQuadra.getText().toString().trim();
        String lote = editLote.getText().toString().trim();

        // Validação apenas para foto, nome e CPF (campos obrigatórios)
        if (fotoUri == null) {
            Toast.makeText(this, "Selecione uma foto!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (nome.isEmpty()) {
            Toast.makeText(this, "Preencha o nome!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (cpf.isEmpty()) {
            Toast.makeText(this, "Preencha o CPF!", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Salvar a imagem permanentemente
            String imagePath = BackupUtil.saveImageToInternalStorage(this, fotoUri);
            if (imagePath == null) {
                Toast.makeText(this, "Erro ao salvar imagem!", Toast.LENGTH_SHORT).show();
                return;
            }

            JSONObject morador = new JSONObject();
            morador.put("nome", nome);
            morador.put("cpf", cpf);

            // Campos opcionais - só adiciona se não estiverem vazios
            if (!email.isEmpty()) morador.put("email", email);
            if (!rua.isEmpty()) morador.put("rua", rua);
            if (!numero.isEmpty()) morador.put("numero", numero);
            if (!telefone.isEmpty()) morador.put("telefone", telefone);
            if (!quadra.isEmpty()) morador.put("quadra", quadra);
            if (!lote.isEmpty()) morador.put("lote", lote);

            // Salvar o caminho da imagem em vez da URI temporária
            morador.put("foto", imagePath);

            if (editarCod != null) {
                // Antes de atualizar, excluir a imagem antiga se existir
                JSONObject moradorAntigo = moradorDAO.buscarPorCodigo(editarCod);
                if (moradorAntigo != null) {
                    String oldImagePath = moradorAntigo.optString("foto", "");
                    if (!oldImagePath.isEmpty() && !oldImagePath.equals(imagePath)) {
                        BackupUtil.deleteImageFile(oldImagePath);
                    }
                }

                morador.put("cod", editarCod);
                boolean sucesso = moradorDAO.atualizarMorador(editarCod, morador);
                if (sucesso) {
                    Toast.makeText(this, "Morador atualizado!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Erro ao atualizar morador!", Toast.LENGTH_SHORT).show();
                }
            } else {
                // Novo morador
                int novoCod = gerarNovoCodigo();
                String codStr = String.format("%02d", novoCod);
                morador.put("cod", codStr);
                boolean sucesso = moradorDAO.inserirMorador(morador);
                if (sucesso) {
                    Toast.makeText(this, "Morador cadastrado!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "Erro ao cadastrar morador!", Toast.LENGTH_SHORT).show();
                }
            }

            finish();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao salvar: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Se não salvou e há uma imagem temporária, limpar
        if (savedImagePath == null && fotoUri != null) {
            try {
                String uriString = fotoUri.toString();
                if (uriString.contains("cache") || uriString.contains("temp")) {
                    // É um arquivo temporário, podemos tentar excluir
                    // (implementação depende da sua estrutura)
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}