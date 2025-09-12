package com.example.domus;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class ListaAvisosActivity extends AppCompatActivity {

    private LinearLayout layoutAvisos;
    private AvisoDAO avisoDAO;
    private String tipoUsuario; // Variável para controlar o tipo de usuário

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obter o tipo de usuário
        tipoUsuario = getIntent().getStringExtra("tipo_usuario");
        if (tipoUsuario == null) {
            tipoUsuario = "admin"; // Padrão para admin se não especificado
        }

        // Criar layout principal
        LinearLayout mainLayout = new LinearLayout(this);
        mainLayout.setOrientation(LinearLayout.VERTICAL);
        mainLayout.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));

        // Adicionar título "Avisos" no topo
        TextView tvTitulo = new TextView(this);
        tvTitulo.setText("Avisos");
        tvTitulo.setTextSize(22);
        tvTitulo.setTextColor(getResources().getColor(android.R.color.black));
        tvTitulo.setTypeface(null, android.graphics.Typeface.BOLD);
        tvTitulo.setGravity(Gravity.CENTER);
        tvTitulo.setPadding(0, 16, 0, 16);

        LinearLayout.LayoutParams tituloParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        tvTitulo.setLayoutParams(tituloParams);

        mainLayout.addView(tvTitulo);

        // Criar ScrollView para a lista de avisos
        ScrollView scrollView = new ScrollView(this);
        scrollView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
        ));

        layoutAvisos = new LinearLayout(this);
        layoutAvisos.setOrientation(LinearLayout.VERTICAL);
        layoutAvisos.setPadding(16, 0, 16, 16);

        scrollView.addView(layoutAvisos);
        mainLayout.addView(scrollView);

        setContentView(mainLayout);

        avisoDAO = new AvisoDAO(this);
        exibirAvisos();
    }

    private void exibirAvisos() {
        List<JSONObject> avisos = avisoDAO.listarAvisos();
        layoutAvisos.removeAllViews();

        // Mensagem se não houver avisos
        if (avisos.isEmpty()) {
            TextView tvVazio = new TextView(this);
            tvVazio.setText("Nenhum aviso cadastrado");
            tvVazio.setTextSize(16);
            tvVazio.setGravity(Gravity.CENTER);
            tvVazio.setPadding(0, 32, 0, 32);
            layoutAvisos.addView(tvVazio);
            return;
        }

        for (int i = 0; i < avisos.size(); i++) {
            JSONObject aviso = avisos.get(i);
            final int position = i;
            final long avisoId = aviso.optLong("id");

            // Card para cada aviso
            LinearLayout cardAviso = new LinearLayout(this);
            cardAviso.setOrientation(LinearLayout.VERTICAL);
            cardAviso.setPadding(16, 16, 16, 16);
            cardAviso.setBackgroundResource(R.drawable.card_background);

            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            cardParams.setMargins(0, 0, 0, 16);
            cardAviso.setLayoutParams(cardParams);

            // Data/Hora
            TextView tvData = new TextView(this);
            tvData.setText("Data/Hora: " + aviso.optString("datahora"));
            tvData.setTextSize(14);
            tvData.setTextColor(getResources().getColor(android.R.color.darker_gray));
            cardAviso.addView(tvData);

            // Linha em branco
            View espaco1 = new View(this);
            espaco1.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    10
            ));
            cardAviso.addView(espaco1);

            // Assunto
            TextView tvAssunto = new TextView(this);
            tvAssunto.setText("Assunto:\n " + aviso.optString("assunto"));
            tvAssunto.setTextSize(18);
            tvAssunto.setTextColor(getResources().getColor(android.R.color.black));
            tvAssunto.setPadding(0, 0, 0, 0);
            cardAviso.addView(tvAssunto);

            // Linha em branco
            View espaco2 = new View(this);
            espaco2.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    10
            ));
            cardAviso.addView(espaco2);

            // Descrição
            TextView tvDescricao = new TextView(this);
            tvDescricao.setText("Descrição:\n " + aviso.optString("descricao"));
            tvDescricao.setTextSize(16);
            tvDescricao.setTextColor(getResources().getColor(android.R.color.black));
            tvDescricao.setPadding(0, 0, 0, 16);
            cardAviso.addView(tvDescricao);

            // Linha em branco
            View espaco3 = new View(this);
            espaco3.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    10
            ));
            cardAviso.addView(espaco3);

            // Verificar se há anexos
            try {
                JSONArray jsonAnexos = new JSONArray(aviso.optString("anexos", "[]"));
                if (jsonAnexos.length() > 0) {
                    // Subtítulo "Documentos em Anexo:"
                    TextView tvSubtituloAnexos = new TextView(this);
                    tvSubtituloAnexos.setText("Documentos em Anexo:");
                    tvSubtituloAnexos.setTextSize(16);
                    tvSubtituloAnexos.setTextColor(getResources().getColor(android.R.color.black));
                    tvSubtituloAnexos.setTypeface(null, android.graphics.Typeface.BOLD);
                    tvSubtituloAnexos.setPadding(0, 0, 0, 8);
                    cardAviso.addView(tvSubtituloAnexos);

                    // Anexos
                    LinearLayout layoutAnexos = new LinearLayout(this);
                    layoutAnexos.setOrientation(LinearLayout.HORIZONTAL);
                    layoutAnexos.setPadding(0, 8, 0, 16);

                    for (int j = 0; j < jsonAnexos.length(); j++) {
                        String path = jsonAnexos.optString(j);
                        File file = new File(path);

                        ImageView img = new ImageView(this);
                        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(120, 120);
                        params.setMargins(0, 0, 16, 0);
                        img.setLayoutParams(params);
                        img.setPadding(8, 8, 8, 8);
                        img.setBackgroundResource(R.drawable.image_border);

                        if (file.exists()) {
                            if (file.getName().toLowerCase().endsWith(".jpg") ||
                                    file.getName().toLowerCase().endsWith(".png") ||
                                    file.getName().toLowerCase().endsWith(".jpeg")) {

                                // Carregar imagem
                                img.setImageURI(Uri.fromFile(file));
                                img.setScaleType(ImageView.ScaleType.CENTER_CROP);

                                // Clique para ampliar imagem
                                final String imagePath = path;
                                img.setOnClickListener(v -> {
                                    Intent intent = new Intent(ListaAvisosActivity.this, ImageViewerActivity.class);
                                    intent.putExtra("imagePath", imagePath);
                                    startActivity(intent);
                                });

                            } else if (file.getName().toLowerCase().endsWith(".pdf") ||
                                    file.getName().toLowerCase().endsWith(".doc") ||
                                    file.getName().toLowerCase().endsWith(".docx") ||
                                    file.getName().toLowerCase().endsWith(".xls") ||
                                    file.getName().toLowerCase().endsWith(".xlsx")) {

                                // Ícone para documentos
                                img.setImageResource(R.drawable.ic_document);
                                img.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

                                // Clique para abrir documento
                                final String docPath = path;
                                img.setOnClickListener(v -> abrirArquivoComAppExterno(docPath));

                            } else {
                                // Ícone genérico para outros arquivos
                                img.setImageResource(android.R.drawable.ic_menu_report_image);
                                img.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

                                // Clique para tentar abrir o arquivo
                                final String filePath = path;
                                img.setOnClickListener(v -> abrirArquivoComAppExterno(filePath));
                            }
                        } else {
                            // Arquivo não encontrado
                            img.setImageResource(android.R.drawable.ic_delete);
                            img.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                        }

                        layoutAnexos.addView(img);
                    }
                    cardAviso.addView(layoutAnexos);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Linha separadora
            View separador = new View(this);
            separador.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
            ));
            separador.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));
            separador.setPadding(0, 8, 0, 8);
            cardAviso.addView(separador);

            // Container para os botões Editar e Excluir - SOMENTE PARA ADMIN
            if (!"morador".equalsIgnoreCase(tipoUsuario)) {
                LinearLayout layoutBotoes = new LinearLayout(this);
                layoutBotoes.setOrientation(LinearLayout.HORIZONTAL);
                layoutBotoes.setLayoutParams(new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                ));
                layoutBotoes.setGravity(Gravity.END);

                // Botão Editar
                Button btnEditar = new Button(this);
                LinearLayout.LayoutParams paramsEditar = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                paramsEditar.setMargins(0, 0, 16, 0);
                btnEditar.setLayoutParams(paramsEditar);
                btnEditar.setText("Editar");
                btnEditar.setBackgroundColor(getResources().getColor(R.color.teal_700));
                btnEditar.setTextColor(getResources().getColor(android.R.color.white));
                btnEditar.setAllCaps(false);
                btnEditar.setPadding(32, 8, 32, 8);

                btnEditar.setOnClickListener(v -> editarAviso(aviso));

                // Botão Excluir
                Button btnExcluir = new Button(this);
                LinearLayout.LayoutParams paramsExcluir = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        LinearLayout.LayoutParams.WRAP_CONTENT
                );
                btnExcluir.setLayoutParams(paramsExcluir);
                btnExcluir.setText("Excluir");
                btnExcluir.setBackgroundColor(getResources().getColor(android.R.color.holo_red_dark));
                btnExcluir.setTextColor(getResources().getColor(android.R.color.white));
                btnExcluir.setAllCaps(false);
                btnExcluir.setPadding(32, 8, 32, 8);

                btnExcluir.setOnClickListener(v -> mostrarConfirmacaoExclusao(avisoId, position, aviso));

                // Adicionar botões ao layout
                layoutBotoes.addView(btnEditar);
                layoutBotoes.addView(btnExcluir);
                cardAviso.addView(layoutBotoes);
            }

            layoutAvisos.addView(cardAviso);
        }
    }

    private void mostrarConfirmacaoExclusao(long avisoId, int position, JSONObject aviso) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Confirmar Exclusão");
        builder.setMessage("Tem certeza que deseja excluir este aviso?");

        builder.setPositiveButton("Sim", (dialog, which) -> {
            excluirAviso(avisoId, position, aviso);
        });

        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            dialog.dismiss();
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void editarAviso(JSONObject aviso) {
        try {
            Intent intent = new Intent(this, CadastroAvisosActivity.class);
            intent.putExtra("avisoJSON", aviso.toString());
            intent.putExtra("modoEdicao", true);
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao editar aviso", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void excluirAviso(long avisoId, int position, JSONObject aviso) {
        try {
            // Primeiro, excluir os arquivos físicos dos anexos
            excluirArquivosAnexos(aviso);

            // Depois, excluir o aviso do banco de dados
            if (avisoDAO.excluirAviso(avisoId)) {
                Toast.makeText(this, "Aviso excluído com sucesso", Toast.LENGTH_SHORT).show();
                // Recarregar a lista
                exibirAvisos();
            } else {
                Toast.makeText(this, "Erro ao excluir aviso", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, "Erro ao excluir aviso", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void excluirArquivosAnexos(JSONObject aviso) {
        try {
            JSONArray jsonAnexos = new JSONArray(aviso.optString("anexos", "[]"));
            for (int i = 0; i < jsonAnexos.length(); i++) {
                String path = jsonAnexos.getString(i);
                File arquivo = new File(path);
                if (arquivo.exists()) {
                    arquivo.delete();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void abrirArquivoComAppExterno(String filePath) {
        try {
            File file = new File(filePath);
            if (file.exists()) {
                // Gerar URI usando FileProvider
                Uri uri = FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".provider",
                        file
                );

                // Determinar o tipo MIME
                String mimeType = obterTipoMime(file.getName());

                // Criar intent para abrir o arquivo
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(uri, mimeType);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                // Verificar se há app disponível
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                } else {
                    // Tentar com tipo MIME genérico
                    Intent intentGenerico = new Intent(Intent.ACTION_VIEW);
                    intentGenerico.setDataAndType(uri, "*/*");
                    intentGenerico.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    if (intentGenerico.resolveActivity(getPackageManager()) != null) {
                        startActivity(intentGenerico);
                    } else {
                        Toast.makeText(this, "Nenhum app encontrado para abrir este tipo de arquivo", Toast.LENGTH_SHORT).show();
                    }
                }
            } else {
                Toast.makeText(this, "Arquivo não encontrado: " + filePath, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao abrir arquivo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String obterTipoMime(String fileName) {
        String nomeArquivo = fileName.toLowerCase();

        if (nomeArquivo.endsWith(".pdf")) return "application/pdf";
        if (nomeArquivo.endsWith(".doc")) return "application/msword";
        if (nomeArquivo.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (nomeArquivo.endsWith(".xls")) return "application/vnd.ms-excel";
        if (nomeArquivo.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (nomeArquivo.endsWith(".jpg") || nomeArquivo.endsWith(".jpeg")) return "image/jpeg";
        if (nomeArquivo.endsWith(".png")) return "image/png";
        if (nomeArquivo.endsWith(".txt")) return "text/plain";
        if (nomeArquivo.endsWith(".zip")) return "application/zip";
        if (nomeArquivo.endsWith(".rar")) return "application/x-rar-compressed";

        return "*/*"; // Tipo genérico
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recarregar avisos quando retornar à tela
        exibirAvisos();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (avisoDAO != null) avisoDAO.fechar();
    }
}