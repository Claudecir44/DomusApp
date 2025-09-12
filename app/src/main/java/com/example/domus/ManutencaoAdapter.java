package com.example.domus;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ManutencaoAdapter extends RecyclerView.Adapter<ManutencaoAdapter.ManutencaoViewHolder> {

    public interface OnItemClickListener {
        void onEditar(int position);
        void onExcluir(int position);
    }

    private Context context;
    private List<Manutencao> lista;
    private OnItemClickListener listener;

    public ManutencaoAdapter(Context context, List<Manutencao> lista, OnItemClickListener listener){
        this.context = context;
        this.lista = lista;
        this.listener = listener;
    }

    @Override
    public ManutencaoViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View view = LayoutInflater.from(context).inflate(R.layout.item_manutencao, parent, false);
        return new ManutencaoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ManutencaoViewHolder holder, int position){
        Manutencao m = lista.get(position);

        // Construir string de dados formatada
        StringBuilder dados = new StringBuilder();
        dados.append("Tipo: ").append(m.getTipo() != null ? m.getTipo() : "N/A").append("\n")
                .append("Data/Hora: ").append(m.getDataHora() != null ? m.getDataHora() : "N/A").append("\n")
                .append("Local: ").append(m.getLocal() != null ? m.getLocal() : "N/A").append("\n")
                .append("Serviço: ").append(m.getServico() != null ? m.getServico() : "N/A").append("\n")
                .append("Responsável: ").append(m.getResponsavel() != null ? m.getResponsavel() : "N/A").append("\n")
                .append("Valor: ").append(m.getValor() != null ? m.getValor() : "N/A").append("\n");

        // Verificar se há anexos
        boolean temAnexos = m.getAnexos() != null && !m.getAnexos().isEmpty();
        if(temAnexos){
            dados.append("Anexos disponíveis (clique para abrir)");
        }

        holder.tvDados.setText(dados.toString());

        // Configurar botões de editar e excluir
        holder.btnEditar.setOnClickListener(v -> {
            if (listener != null) {
                listener.onEditar(position);
            }
        });

        holder.btnExcluir.setOnClickListener(v -> {
            if (listener != null) {
                listener.onExcluir(position);
            }
        });

        // Configurar clique nos dados para abrir anexos
        holder.tvDados.setOnClickListener(v -> {
            if(temAnexos){
                // Abrir apenas o primeiro anexo (ou você pode modificar para abrir todos)
                String primeiroAnexo = m.getAnexos().get(0);
                try{
                    Uri uri = Uri.parse(primeiroAnexo);
                    Intent intent = new Intent(Intent.ACTION_VIEW);

                    // Determinar o tipo MIME do arquivo
                    String tipoMime = obterTipoMime(primeiroAnexo);

                    intent.setDataAndType(uri, tipoMime);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

                    // Verificar se há app disponível para abrir o arquivo
                    if (intent.resolveActivity(context.getPackageManager()) != null) {
                        context.startActivity(intent);
                    } else {
                        Toast.makeText(context, "Nenhum aplicativo encontrado para abrir este arquivo", Toast.LENGTH_SHORT).show();
                    }
                } catch(Exception e){
                    e.printStackTrace();
                    Toast.makeText(context, "Não foi possível abrir o anexo.", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Nenhum anexo disponível.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount(){
        return lista != null ? lista.size() : 0;
    }

    // Método para obter o tipo MIME baseado na extensão do arquivo
    private String obterTipoMime(String caminhoArquivo) {
        if (caminhoArquivo == null) return "*/*";

        String caminho = caminhoArquivo.toLowerCase();
        if (caminho.endsWith(".pdf")) return "application/pdf";
        if (caminho.endsWith(".doc")) return "application/msword";
        if (caminho.endsWith(".docx")) return "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
        if (caminho.endsWith(".xls")) return "application/vnd.ms-excel";
        if (caminho.endsWith(".xlsx")) return "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        if (caminho.endsWith(".jpg") || caminho.endsWith(".jpeg")) return "image/jpeg";
        if (caminho.endsWith(".png")) return "image/png";
        if (caminho.endsWith(".txt")) return "text/plain";

        return "*/*";
    }

    static class ManutencaoViewHolder extends RecyclerView.ViewHolder {
        TextView tvDados;
        Button btnEditar, btnExcluir;

        public ManutencaoViewHolder(View itemView){
            super(itemView);
            tvDados = itemView.findViewById(R.id.tvManutencaoDados);
            btnEditar = itemView.findViewById(R.id.btnEditarManutencao);
            btnExcluir = itemView.findViewById(R.id.btnExcluirManutencao);
        }
    }
}