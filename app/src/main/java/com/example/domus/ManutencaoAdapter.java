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
        StringBuilder dados = new StringBuilder();
        dados.append("Tipo: ").append(m.getTipo()).append("\n")
                .append("Data/Hora: ").append(m.getDataHora()).append("\n")
                .append("Local: ").append(m.getLocal()).append("\n")
                .append("Serviço: ").append(m.getServico()).append("\n")
                .append("Responsável: ").append(m.getResponsavel()).append("\n")
                .append("Valor: ").append(m.getValor()).append("\n");

        if(m.getAnexos() != null && !m.getAnexos().isEmpty()){
            dados.append("Anexos disponíveis (clique para abrir)");
        }

        holder.tvDados.setText(dados.toString());

        holder.btnEditar.setOnClickListener(v -> listener.onEditar(position));
        holder.btnExcluir.setOnClickListener(v -> listener.onExcluir(position));

        holder.tvDados.setOnClickListener(v -> {
            if(m.getAnexos() != null && !m.getAnexos().isEmpty()){
                for(String path : m.getAnexos()){
                    try{
                        Uri uri = Uri.parse(path);
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndType(uri, "*/*");
                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        context.startActivity(intent);
                    } catch(Exception e){
                        e.printStackTrace();
                        Toast.makeText(context, "Não foi possível abrir o anexo.", Toast.LENGTH_SHORT).show();
                    }
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
