package com.example.domus;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONObject;

import java.util.List;

public class MoradorAdapter extends RecyclerView.Adapter<MoradorAdapter.MoradorViewHolder> {

    public interface OnItemClickListener {
        void onEditar(JSONObject morador, int position);
        void onExcluir(JSONObject morador, int position);
    }

    private Context context;
    private List<JSONObject> moradores;
    private OnItemClickListener listener;

    public MoradorAdapter(Context context, List<JSONObject> moradores, OnItemClickListener listener) {
        this.context = context;
        this.moradores = moradores;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MoradorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_morador, parent, false);
        return new MoradorViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MoradorViewHolder holder, int position) {
        JSONObject morador = moradores.get(position);
        String codigo = morador.optString("cod");

        holder.tvDados.setText(
                "Cod: " + codigo + "\n" +
                        "Nome: " + morador.optString("nome") + "\n" +
                        "CPF: " + morador.optString("cpf") + "\n" +
                        "Email: " + morador.optString("email", "") + "\n" +
                        "Rua: " + morador.optString("rua", "") + "\n" +
                        "Número: " + morador.optString("numero", "") + "\n" +
                        "Telefone: " + morador.optString("telefone", "") + "\n" +
                        "Quadra: " + morador.optString("quadra", "") + "  Lote: " + morador.optString("lote", "")
        );

        // Carregar foto usando o caminho salvo permanentemente
        carregarFoto(holder.imgMorador, morador.optString("foto", ""));

        holder.btnEditar.setOnClickListener(v -> listener.onEditar(morador, position));
        holder.btnExcluir.setOnClickListener(v -> listener.onExcluir(morador, position));
    }

    // Método atualizado para carregar fotos do armazenamento permanente
    private void carregarFoto(ImageView imageView, String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                // Carregar a imagem do caminho salvo permanentemente
                Uri imageUri = BackupUtil.loadImageFromInternalStorage(context, imagePath);
                if (imageUri != null) {
                    imageView.setImageURI(imageUri);
                } else {
                    imageView.setImageResource(android.R.drawable.ic_menu_camera);
                }
            } catch (Exception e) {
                e.printStackTrace();
                imageView.setImageResource(android.R.drawable.ic_menu_camera);
            }
        } else {
            imageView.setImageResource(android.R.drawable.ic_menu_camera);
        }
    }

    @Override
    public int getItemCount() {
        return moradores.size();
    }

    // Método para atualizar a lista completa
    public void atualizarListaCompleta(List<JSONObject> novaLista) {
        moradores.clear();
        moradores.addAll(novaLista);
        notifyDataSetChanged();
    }

    // Método para remover um item da lista pelo código
    public void removerItemPorCodigo(String codigo) {
        for (int i = 0; i < moradores.size(); i++) {
            if (moradores.get(i).optString("cod").equals(codigo)) {
                // Antes de remover, excluir a imagem associada
                JSONObject morador = moradores.get(i);
                String imagePath = morador.optString("foto", "");
                if (!imagePath.isEmpty()) {
                    BackupUtil.deleteImageFile(imagePath);
                }

                moradores.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    // Método para atualizar um item específico
    public void atualizarItem(JSONObject moradorAtualizado, int position) {
        if (position >= 0 && position < moradores.size()) {
            moradores.set(position, moradorAtualizado);
            notifyItemChanged(position);
        }
    }

    // Método para encontrar a posição de um morador pelo código
    public int encontrarPosicaoPorCodigo(String codigo) {
        for (int i = 0; i < moradores.size(); i++) {
            if (moradores.get(i).optString("cod").equals(codigo)) {
                return i;
            }
        }
        return -1;
    }

    public static class MoradorViewHolder extends RecyclerView.ViewHolder {
        ImageView imgMorador;
        TextView tvDados;
        Button btnEditar, btnExcluir;

        public MoradorViewHolder(View itemView) {
            super(itemView);
            imgMorador = itemView.findViewById(R.id.imageMoradorItem);
            tvDados = itemView.findViewById(R.id.tvMoradorDados);
            btnEditar = itemView.findViewById(R.id.btnEditarMorador);
            btnExcluir = itemView.findViewById(R.id.btnExcluirMorador);
        }
    }
}