package com.example.domus;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class FuncionarioAdapter extends RecyclerView.Adapter<FuncionarioAdapter.FuncionarioViewHolder> {

    private List<Funcionario> listaFuncionarios;
    private OnItemClickListener listener;

    // Interface para ações de clique
    public interface OnItemClickListener {
        void onEditar(Funcionario funcionario, int position);
        void onExcluir(Funcionario funcionario, int position);
        void onItemClick(Funcionario funcionario);
    }

    // Construtor
    public FuncionarioAdapter(List<Funcionario> lista, OnItemClickListener listener){
        this.listaFuncionarios = lista;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FuncionarioViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_funcionario, parent, false);
        return new FuncionarioViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FuncionarioViewHolder holder, int position) {
        Funcionario f = listaFuncionarios.get(position);

        // Monta o texto completo com todos os campos preenchidos
        StringBuilder dados = new StringBuilder();
        if (f.getNome() != null && !f.getNome().isEmpty()) dados.append("Nome: ").append(f.getNome()).append("\n");
        if (f.getCpf() != null && !f.getCpf().isEmpty()) dados.append("CPF: ").append(f.getCpf()).append("\n");
        if (f.getRg() != null && !f.getRg().isEmpty()) dados.append("RG: ").append(f.getRg()).append("\n");
        if (f.getRua() != null && !f.getRua().isEmpty()) dados.append("Endereço: ").append(f.getRua());
        if (f.getNumero() != null && !f.getNumero().isEmpty()) dados.append(", ").append(f.getNumero());
        if (f.getBairro() != null && !f.getBairro().isEmpty()) dados.append(", ").append(f.getBairro());
        if (f.getCidade() != null && !f.getCidade().isEmpty()) dados.append(", ").append(f.getCidade());
        if (f.getEstado() != null && !f.getEstado().isEmpty()) dados.append(", ").append(f.getEstado());
        if (f.getPais() != null && !f.getPais().isEmpty()) dados.append(", ").append(f.getPais()).append("\n");
        if (f.getCep() != null && !f.getCep().isEmpty()) dados.append("CEP: ").append(f.getCep()).append("\n");
        if (f.getTelefone() != null && !f.getTelefone().isEmpty()) dados.append("Telefone: ").append(f.getTelefone()).append("\n");
        if (f.getEmail() != null && !f.getEmail().isEmpty()) dados.append("Email: ").append(f.getEmail()).append("\n");
        if (f.getCargaHoraria() != null && !f.getCargaHoraria().isEmpty()) dados.append("Carga Horária: ").append(f.getCargaHoraria()).append("\n");
        if (f.getTurno() != null && !f.getTurno().isEmpty()) dados.append("Turno: ").append(f.getTurno()).append("\n");
        if (f.getHoraEntrada() != null && !f.getHoraEntrada().isEmpty()) dados.append("Entrada: ").append(f.getHoraEntrada()).append("\n");
        if (f.getHoraSaida() != null && !f.getHoraSaida().isEmpty()) dados.append("Saída: ").append(f.getHoraSaida());

        holder.tvDados.setText(dados.toString());

        // Exibe a imagem, ou ícone padrão
        Uri img = f.getImagemUri();
        if (img != null) holder.ivFoto.setImageURI(img);
        else holder.ivFoto.setImageResource(android.R.drawable.ic_menu_camera);

        // Clique no item
        holder.itemView.setOnClickListener(v -> {
            if(listener != null) listener.onItemClick(f);
        });

        // Clique no botão Editar
        holder.btnEditar.setOnClickListener(v -> {
            if(listener != null) listener.onEditar(f, position);
        });

        // Clique no botão Excluir (somente chama listener, confirmação fica na activity)
        holder.btnExcluir.setOnClickListener(v -> {
            if(listener != null) listener.onExcluir(f, position);
        });
    }

    @Override
    public int getItemCount() {
        return listaFuncionarios.size();
    }

    // Remove item da lista (chamar apenas se confirmado na activity)
    public void removerItem(int position){
        listaFuncionarios.remove(position);
        notifyItemRemoved(position);
    }

    public OnItemClickListener getListener() {
        return listener;
    }

    static class FuncionarioViewHolder extends RecyclerView.ViewHolder{
        ImageView ivFoto;
        TextView tvDados;
        Button btnEditar, btnExcluir;

        public FuncionarioViewHolder(@NonNull View itemView){
            super(itemView);
            ivFoto = itemView.findViewById(R.id.ivFuncionarioFoto);
            tvDados = itemView.findViewById(R.id.tvFuncionarioDados);
            btnEditar = itemView.findViewById(R.id.btnEditarFuncionario);
            btnExcluir = itemView.findViewById(R.id.btnExcluirFuncionario);
        }
    }
}
