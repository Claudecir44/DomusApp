package com.example.domus;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ManutencaoAdapter extends RecyclerView.Adapter<ManutencaoAdapter.ManutencaoViewHolder> {

    public interface OnItemClickListener {
        void onEditar(int position);
        void onExcluir(int position);
    }

    private final Context context;
    private final List<Manutencao> lista;
    private final OnItemClickListener listener;

    public ManutencaoAdapter(Context context, List<Manutencao> lista, OnItemClickListener listener) {
        this.context = context;
        this.lista = lista;
        this.listener = listener;
    }

    @Override
    public ManutencaoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_manutencao, parent, false);
        return new ManutencaoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ManutencaoViewHolder holder, int position) {
        Manutencao m = lista.get(position);

        // Construir string de dados
        StringBuilder dados = new StringBuilder();
        dados.append("Tipo: ").append(m.getTipo() != null ? m.getTipo() : "N/A").append("\n")
                .append("Data/Hora: ").append(m.getDataHora() != null ? m.getDataHora() : "N/A").append("\n")
                .append("Local: ").append(m.getLocal() != null ? m.getLocal() : "N/A").append("\n")
                .append("Serviço: ").append(m.getServico() != null ? m.getServico() : "N/A").append("\n")
                .append("Responsável: ").append(m.getResponsavel() != null ? m.getResponsavel() : "N/A").append("\n")
                .append("Valor: ").append(m.getValor() != null ? m.getValor() : "N/A");

        holder.tvDados.setText(dados.toString());

        // Limpar layout de anexos
        holder.layoutAnexos.removeAllViews();

        // Mostrar anexos
        if (m.getAnexos() != null && !m.getAnexos().isEmpty()) {
            for (String anexoUri : m.getAnexos()) {
                TextView tvAnexo = new TextView(context);

                // Obter nome do arquivo da URI
                String fileName = getFileNameFromUri(Uri.parse(anexoUri));
                tvAnexo.setText("📎 " + fileName);
                tvAnexo.setTextColor(0xFF1976D2);
                tvAnexo.setPadding(8, 4, 8, 4);

                tvAnexo.setOnClickListener(v -> abrirAnexo(Uri.parse(anexoUri)));

                holder.layoutAnexos.addView(tvAnexo);
            }
        }

        // Botões Editar e Excluir
        holder.btnEditar.setOnClickListener(v -> {
            if (listener != null) listener.onEditar(position);
        });

        holder.btnExcluir.setOnClickListener(v -> {
            if (listener != null) listener.onExcluir(position);
        });
    }

    @Override
    public int getItemCount() {
        return lista != null ? lista.size() : 0;
    }

    // Método para obter o nome do arquivo a partir de uma URI
    private String getFileNameFromUri(Uri uri) {
        String result = null;
        if (uri.getScheme().equals("content")) {
            try (Cursor cursor = context.getContentResolver().query(uri, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    if (index != -1) {
                        result = cursor.getString(index);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        if (result == null) {
            result = uri.getLastPathSegment();
        }
        return result;
    }

    // Método para abrir anexos - CORRIGIDO para URIs de conteúdo
    private void abrirAnexo(Uri uri) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);

            // Determinar o tipo MIME baseado na extensão do arquivo
            String mimeType = getMimeTypeFromUri(uri);

            intent.setDataAndType(uri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            // Verificar se há app para abrir o arquivo
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                // Tentar com tipo genérico se não encontrar app específico
                Intent genericIntent = new Intent(Intent.ACTION_VIEW);
                genericIntent.setDataAndType(uri, "*/*");
                genericIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                genericIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                if (genericIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(genericIntent);
                } else {
                    Toast.makeText(context, "Nenhum aplicativo encontrado para abrir este arquivo", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(context, "Erro ao abrir arquivo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    // Método para obter o tipo MIME baseado na URI
    private String getMimeTypeFromUri(Uri uri) {
        String fileName = getFileNameFromUri(uri);
        return getMimeType(fileName);
    }

    // Método auxiliar para determinar o tipo MIME do arquivo
    private String getMimeType(String fileName) {
        if (fileName == null) return "*/*";

        String extension = "";
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot != -1 && lastDot < fileName.length() - 1) {
            extension = fileName.substring(lastDot + 1).toLowerCase();
        }

        switch (extension) {
            case "pdf": return "application/pdf";
            case "doc": return "application/msword";
            case "docx": return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
            case "xls": return "application/vnd.ms-excel";
            case "xlsx": return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
            case "jpg": case "jpeg": return "image/jpeg";
            case "png": return "image/png";
            case "gif": return "image/gif";
            case "txt": return "text/plain";
            case "zip": return "application/zip";
            case "rar": return "application/x-rar-compressed";
            default: return "*/*";
        }
    }

    static class ManutencaoViewHolder extends RecyclerView.ViewHolder {
        TextView tvDados;
        LinearLayout layoutAnexos;
        Button btnEditar, btnExcluir;

        public ManutencaoViewHolder(View itemView) {
            super(itemView);
            tvDados = itemView.findViewById(R.id.tvManutencaoDados);
            layoutAnexos = itemView.findViewById(R.id.layoutAnexosManutencao);
            btnEditar = itemView.findViewById(R.id.btnEditarManutencao);
            btnExcluir = itemView.findViewById(R.id.btnExcluirManutencao);
        }
    }
}