package com.example.domus;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.util.List;

public class AvisoAdapter extends RecyclerView.Adapter<AvisoAdapter.AvisoViewHolder> {

    private Context context;
    private List<JSONObject> listaAvisos;
    private AvisoDAO avisoDAO;

    public AvisoAdapter(Context context, List<JSONObject> listaAvisos, AvisoDAO dao) {
        this.context = context;
        this.listaAvisos = listaAvisos;
        this.avisoDAO = dao;
    }

    @NonNull
    @Override
    public AvisoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_aviso, parent, false);
        return new AvisoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AvisoViewHolder holder, int position) {
        JSONObject aviso = listaAvisos.get(position);

        // Definir textos
        holder.tvDataHora.setText("Data/Hora: " + aviso.optString("datahora"));
        holder.tvAssunto.setText("Assunto: " + aviso.optString("assunto"));
        holder.tvDescricao.setText("Descrição: " + aviso.optString("descricao"));

        // Limpar anexos anteriores
        holder.layoutAnexos.removeAllViews();

        // Processar anexos
        try {
            JSONArray anexos = new JSONArray(aviso.optString("anexos", "[]"));
            if (anexos.length() == 0) {
                TextView tvVazio = new TextView(context);
                tvVazio.setText("Sem anexos");
                tvVazio.setPadding(8, 8, 8, 8);
                holder.layoutAnexos.addView(tvVazio);
            } else {
                for (int i = 0; i < anexos.length(); i++) {
                    String path = anexos.getString(i);
                    File file = new File(path);

                    ImageView img = new ImageView(context);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(150, 150);
                    params.setMargins(8, 8, 8, 8);
                    img.setLayoutParams(params);

                    if (file.getName().endsWith(".jpg") ||
                            file.getName().endsWith(".png") ||
                            file.getName().endsWith(".jpeg")) {
                        img.setImageURI(Uri.fromFile(file));
                    } else {
                        img.setImageResource(android.R.drawable.ic_menu_report_image);
                    }

                    final String arquivoPath = path;
                    img.setOnClickListener(v -> abrirArquivo(arquivoPath));
                    holder.layoutAnexos.addView(img);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Configurar botões
        holder.btnEditar.setOnClickListener(v -> editarAviso(aviso));
        holder.btnExcluir.setOnClickListener(v -> excluirAviso(aviso, position));
    }

    private void abrirArquivo(String path) {
        try {
            Uri uri = Uri.fromFile(new File(path));
            Intent intent = new Intent(Intent.ACTION_VIEW);

            if (path.endsWith(".pdf")) {
                intent.setDataAndType(uri, "application/pdf");
            } else if (path.endsWith(".doc") || path.endsWith(".docx")) {
                intent.setDataAndType(uri, "application/msword");
            } else if (path.endsWith(".xls") || path.endsWith(".xlsx")) {
                intent.setDataAndType(uri, "application/vnd.ms-excel");
            } else {
                intent.setData(uri);
            }

            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(context, "Não foi possível abrir o arquivo", Toast.LENGTH_SHORT).show();
        }
    }

    private void editarAviso(JSONObject aviso) {
        Intent intent = new Intent(context, CadastroAvisosActivity.class);
        intent.putExtra("avisoJSON", aviso.toString());
        context.startActivity(intent);
    }

    private void excluirAviso(JSONObject aviso, int position) {
        long id = aviso.optLong("id");
        if (avisoDAO.excluirAviso(id)) {
            listaAvisos.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, listaAvisos.size());
            Toast.makeText(context, "Aviso excluído", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Erro ao excluir aviso", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public int getItemCount() {
        return listaAvisos.size();
    }

    public static class AvisoViewHolder extends RecyclerView.ViewHolder {
        TextView tvDataHora, tvAssunto, tvDescricao;
        LinearLayout layoutAnexos;
        Button btnEditar, btnExcluir;

        public AvisoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDataHora = itemView.findViewById(R.id.tvDataHora);
            tvAssunto = itemView.findViewById(R.id.tvAssunto);
            tvDescricao = itemView.findViewById(R.id.tvDescricao);
            layoutAnexos = itemView.findViewById(R.id.layoutAnexos);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnExcluir = itemView.findViewById(R.id.btnExcluir);
        }
    }
}