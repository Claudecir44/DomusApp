package com.example.domus;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class DespesaAdapter extends RecyclerView.Adapter<DespesaAdapter.DespesaViewHolder> {

    private final List<Despesa> lista;
    private final Context context;
    private final OnDespesaListener listener;
    private final String tipoUsuario; // Novo campo para controlar o tipo de usuário

    public interface OnDespesaListener {
        void onEditar(Despesa despesa, int position);
        void onExcluir(Despesa despesa, int position);
    }

    // Construtor atualizado para receber tipoUsuario
    public DespesaAdapter(Context context, List<Despesa> lista, OnDespesaListener listener, String tipoUsuario) {
        this.context = context;
        this.lista = lista;
        this.listener = listener;
        this.tipoUsuario = tipoUsuario;
    }

    @NonNull
    @Override
    public DespesaViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_despesa, parent, false);
        return new DespesaViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DespesaViewHolder holder, int position) {
        holder.bind(lista.get(position), position);
    }

    @Override
    public int getItemCount() {
        return lista.size();
    }

    class DespesaViewHolder extends RecyclerView.ViewHolder {

        TextView textDataHora, textNome, textDescricao, textValor;
        LinearLayout layoutAnexos;
        Button btnEditar, btnExcluir, btnMostrarAnexos;

        public DespesaViewHolder(@NonNull View itemView) {
            super(itemView);
            textDataHora = itemView.findViewById(R.id.textDataHora);
            textNome = itemView.findViewById(R.id.textNomeDespesa);
            textDescricao = itemView.findViewById(R.id.textDescricaoDespesa);
            textValor = itemView.findViewById(R.id.textValorDespesa);
            layoutAnexos = itemView.findViewById(R.id.layoutAnexos);
            btnEditar = itemView.findViewById(R.id.btnEditarDespesa);
            btnExcluir = itemView.findViewById(R.id.btnExcluirDespesa);
            btnMostrarAnexos = itemView.findViewById(R.id.btnMostrarAnexosDespesa);
        }

        void bind(Despesa despesa, int position) {
            textDataHora.setText(despesa.getDataHora());
            textNome.setText(despesa.getNome());
            textDescricao.setText(despesa.getDescricao());
            textValor.setText(String.format("R$ %.2f", despesa.getValor()));

            // Controlar visibilidade dos botões baseado no tipo de usuário
            if ("morador".equalsIgnoreCase(tipoUsuario)) {
                btnEditar.setVisibility(View.GONE);
                btnExcluir.setVisibility(View.GONE);
            } else {
                btnEditar.setVisibility(View.VISIBLE);
                btnExcluir.setVisibility(View.VISIBLE);
            }

            layoutAnexos.removeAllViews();
            btnMostrarAnexos.setVisibility(View.GONE);

            if (despesa.getAnexos() != null && !despesa.getAnexos().isEmpty()) {
                btnMostrarAnexos.setVisibility(View.VISIBLE);
                btnMostrarAnexos.setOnClickListener(v -> {
                    layoutAnexos.removeAllViews();
                    layoutAnexos.setVisibility(View.VISIBLE);

                    for (String uriString : despesa.getAnexos()) {
                        TextView tvAnexo = new TextView(context);
                        Uri uri = Uri.parse(uriString);
                        String nomeArquivo = uri.getLastPathSegment();
                        tvAnexo.setText(nomeArquivo != null ? nomeArquivo : "Arquivo");
                        tvAnexo.setTextColor(0xFF1976D2);
                        tvAnexo.setPadding(4, 4, 4, 4);
                        tvAnexo.setOnClickListener(view -> abrirArquivo(uri));
                        layoutAnexos.addView(tvAnexo);
                    }
                });
            }

            btnEditar.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditar(despesa, position);
                }
            });

            btnExcluir.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onExcluir(despesa, position);
                }
            });
        }

        private void abrirArquivo(Uri uri) {
            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                String caminho = uri.toString();

                // Define tipo do arquivo
                String tipo = "*/*";
                if (caminho.endsWith(".pdf")) tipo = "application/pdf";
                else if (caminho.endsWith(".doc") || caminho.endsWith(".docx")) tipo = "application/msword";
                else if (caminho.endsWith(".xls") || caminho.endsWith(".xlsx")) tipo = "application/vnd.ms-excel";
                else if (caminho.endsWith(".jpg") || caminho.endsWith(".jpeg")) tipo = "image/jpeg";
                else if (caminho.endsWith(".png")) tipo = "image/png";

                intent.setDataAndType(uri, tipo);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                // Verificar se há app disponível para abrir o arquivo
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(Intent.createChooser(intent, "Abrir arquivo com"));
                } else {
                    Toast.makeText(context, "Nenhum aplicativo encontrado para abrir este arquivo", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Toast.makeText(context, "Erro ao abrir arquivo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }
}