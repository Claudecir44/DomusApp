package com.example.domus;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

public class AssembleiaAdapter extends RecyclerView.Adapter<AssembleiaAdapter.AssembleiaViewHolder> {

    private final List<JSONObject> lista;
    private final OnVisualizarClickListener visualizarListener;
    private final OnExcluirClickListener excluirListener;
    private final OnEditarClickListener editarListener;
    private final Context context;
    private final String tipoUsuario; // Novo campo para controlar o tipo de usuário

    public interface OnVisualizarClickListener {
        void onVisualizar(JSONObject assembleia);
    }

    public interface OnExcluirClickListener {
        void onExcluir(JSONObject assembleia);
    }

    public interface OnEditarClickListener {
        void onEditar(JSONObject assembleia);
    }

    // Construtor atualizado para receber tipoUsuario
    public AssembleiaAdapter(Context context, List<JSONObject> lista,
                             OnVisualizarClickListener visualizarListener,
                             OnExcluirClickListener excluirListener,
                             OnEditarClickListener editarListener,
                             String tipoUsuario) {
        this.context = context;
        this.lista = lista;
        this.visualizarListener = visualizarListener;
        this.excluirListener = excluirListener;
        this.editarListener = editarListener;
        this.tipoUsuario = tipoUsuario;
    }

    @NonNull
    @Override
    public AssembleiaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_assembleia, parent, false);
        return new AssembleiaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AssembleiaViewHolder holder, int position) {
        JSONObject assembleia = lista.get(position);
        holder.bind(assembleia);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    class AssembleiaViewHolder extends RecyclerView.ViewHolder {

        TextView tvDataHora, tvAssunto, tvLocal, tvDescricao, btnMostrarAnexos;
        LinearLayout layoutExpandido, layoutAnexos;
        ImageButton btnExcluir, btnEditar;

        public AssembleiaViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDataHora = itemView.findViewById(R.id.tvDataHora);
            tvAssunto = itemView.findViewById(R.id.tvAssunto);
            tvLocal = itemView.findViewById(R.id.tvLocal);
            tvDescricao = itemView.findViewById(R.id.tvDescricao);
            btnMostrarAnexos = itemView.findViewById(R.id.btnMostrarAnexos);
            layoutExpandido = itemView.findViewById(R.id.layoutExpandido);
            layoutAnexos = itemView.findViewById(R.id.layoutAnexos);
            btnExcluir = itemView.findViewById(R.id.btnExcluir);
            btnEditar = itemView.findViewById(R.id.btnEditar);
        }

        void bind(JSONObject assembleia) {
            tvDataHora.setText(assembleia.optString("datahora", ""));
            tvAssunto.setText(assembleia.optString("assunto", ""));
            tvLocal.setText("Local: " + assembleia.optString("local", ""));
            tvDescricao.setText("Descrição: " + assembleia.optString("descricao", ""));

            // Controlar visibilidade dos botões baseado no tipo de usuário
            if ("morador".equalsIgnoreCase(tipoUsuario)) {
                btnExcluir.setVisibility(View.GONE);
                btnEditar.setVisibility(View.GONE);
            } else {
                btnExcluir.setVisibility(View.VISIBLE);
                btnEditar.setVisibility(View.VISIBLE);
            }

            layoutExpandido.setVisibility(View.GONE);
            layoutAnexos.setVisibility(View.GONE);
            btnMostrarAnexos.setVisibility(View.GONE);

            // Ao clicar no assunto, mostrar/ocultar detalhes
            tvAssunto.setOnClickListener(v -> {
                if (layoutExpandido.getVisibility() == View.GONE)
                    layoutExpandido.setVisibility(View.VISIBLE);
                else layoutExpandido.setVisibility(View.GONE);
            });

            // Clique em excluir
            btnExcluir.setOnClickListener(v -> {
                if (excluirListener != null) {
                    excluirListener.onExcluir(assembleia);
                }
            });

            // Clique em editar
            btnEditar.setOnClickListener(v -> {
                if (editarListener != null) {
                    editarListener.onEditar(assembleia);
                }
            });

            // Mostrar anexos
            try {
                JSONArray anexos = new JSONArray(assembleia.optString("anexos", "[]"));
                if (anexos.length() > 0) {
                    btnMostrarAnexos.setVisibility(View.VISIBLE);
                    btnMostrarAnexos.setOnClickListener(v -> {
                        layoutAnexos.removeAllViews();
                        layoutAnexos.setVisibility(View.VISIBLE);
                        for (int i = 0; i < anexos.length(); i++) {
                            try {
                                String caminho = anexos.getString(i);
                                TextView tvAnexo = new TextView(context);
                                tvAnexo.setText(new File(Uri.parse(caminho).getPath()).getName());
                                tvAnexo.setTextColor(0xFF1976D2);
                                tvAnexo.setPadding(8, 8, 8, 8);
                                tvAnexo.setOnClickListener(view -> abrirArquivo(caminho));
                                layoutAnexos.addView(tvAnexo);
                            } catch (Exception ex) {
                                ex.printStackTrace();
                            }
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void abrirArquivo(String caminhoArquivo) {
            try {
                Uri uri = Uri.parse(caminhoArquivo);
                File file = new File(uri.getPath());

                if (file.exists()) {
                    Uri fileUri = FileProvider.getUriForFile(context,
                            context.getPackageName() + ".provider",
                            file);

                    String tipo = "*/*";
                    if (file.getName().endsWith(".pdf")) tipo = "application/pdf";
                    else if (file.getName().endsWith(".doc") || file.getName().endsWith(".docx")) tipo = "application/msword";
                    else if (file.getName().endsWith(".xls") || file.getName().endsWith(".xlsx")) tipo = "application/vnd.ms-excel";
                    else if (file.getName().endsWith(".jpg") || file.getName().endsWith(".jpeg")) tipo = "image/jpeg";
                    else if (file.getName().endsWith(".png")) tipo = "image/png";

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(fileUri, tipo);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    // Verificar se há app disponível para abrir o arquivo
                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "Nenhum aplicativo encontrado para abrir este arquivo", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, "Arquivo não encontrado", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(context, "Erro ao abrir arquivo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }
        }
    }
}