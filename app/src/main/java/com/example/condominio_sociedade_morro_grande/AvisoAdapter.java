package com.cjstudio.condominio_sociedade_morro_grande;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;



import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AvisoAdapter extends RecyclerView.Adapter<AvisoAdapter.AvisoViewHolder> {

    private Context context;
    private List<JSONObject> listaAvisos;
    private com.cjstudio.condominio_sociedade_morro_grande.data.AvisoDAO avisoDAO;

    public AvisoAdapter(Context context, List<JSONObject> listaAvisos, com.cjstudio.condominio_sociedade_morro_grande.data.AvisoDAO dao) {
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

        String dataHora = aviso.optString("datahora", "");
        String dataFormatada = formatarDataHora(dataHora);

        holder.tvDataHora.setText("📅 " + dataFormatada);
        holder.tvAssunto.setText(aviso.optString("assunto", "Sem assunto"));
        holder.tvDescricao.setText("📝 " + aviso.optString("descricao", "Sem descrição"));

        holder.layoutAnexos.removeAllViews();

        try {
            String anexosStr = aviso.optString("anexos", "[]");
            JSONArray anexos = new JSONArray(anexosStr);

            if (anexos.length() > 0) {
                holder.tvAnexosLabel.setVisibility(View.VISIBLE);
                holder.scrollAnexos.setVisibility(View.VISIBLE);

                for (int i = 0; i < anexos.length(); i++) {
                    String path = anexos.getString(i);
                    File file = new File(path);

                    ImageView img = new ImageView(context);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(70, 70);
                    params.setMargins(8, 8, 8, 8);
                    img.setLayoutParams(params);
                    img.setScaleType(ImageView.ScaleType.CENTER_CROP);
                    img.setPadding(4, 4, 4, 4);
                    img.setBackgroundResource(android.R.drawable.editbox_background);

                    if (file.exists() && (file.getName().endsWith(".jpg") ||
                            file.getName().endsWith(".png") ||
                            file.getName().endsWith(".jpeg"))) {
                        img.setImageURI(Uri.fromFile(file));
                    } else if (file.exists()) {
                        if (path.endsWith(".pdf")) {
                            img.setImageResource(android.R.drawable.ic_menu_report_image);
                        } else if (path.endsWith(".doc") || path.endsWith(".docx")) {
                            img.setImageResource(android.R.drawable.ic_menu_edit);
                        } else {
                            img.setImageResource(android.R.drawable.ic_menu_save);
                        }
                    } else {
                        img.setImageResource(android.R.drawable.ic_menu_report_image);
                    }

                    final String arquivoPath = path;
                    img.setOnClickListener(v -> abrirArquivo(arquivoPath));
                    holder.layoutAnexos.addView(img);
                }
            } else {
                holder.tvAnexosLabel.setVisibility(View.GONE);
                holder.scrollAnexos.setVisibility(View.GONE);
            }
        } catch (Exception e) {
            e.printStackTrace();
            holder.tvAnexosLabel.setVisibility(View.GONE);
            holder.scrollAnexos.setVisibility(View.GONE);
        }

        holder.btnEditar.setOnClickListener(v -> editarAviso(aviso));
        holder.btnExcluir.setOnClickListener(v -> excluirAviso(aviso, position));
    }

    private String formatarDataHora(String dataHoraStr) {
        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(dataHoraStr);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dataHoraStr;
        }
    }

    private void abrirArquivo(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                Toast.makeText(context, "Arquivo não encontrado", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri uri = FileProvider.getUriForFile(context,
                    context.getPackageName() + ".provider",
                    file);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            String mimeType = getMimeType(path);
            intent.setDataAndType(uri, mimeType);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                Toast.makeText(context, "Nenhum aplicativo encontrado para abrir este arquivo", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(context, "Erro ao abrir arquivo: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String getMimeType(String path) {
        if (path.endsWith(".pdf")) return "application/pdf";
        if (path.endsWith(".doc") || path.endsWith(".docx")) return "application/msword";
        if (path.endsWith(".xls") || path.endsWith(".xlsx")) return "application/vnd.ms-excel";
        if (path.endsWith(".jpg") || path.endsWith(".jpeg")) return "image/jpeg";
        if (path.endsWith(".png")) return "image/png";
        if (path.endsWith(".mp4")) return "video/mp4";
        return "*/*";
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
        TextView tvDataHora;
        TextView tvAssunto;
        TextView tvDescricao;
        TextView tvAnexosLabel;
        HorizontalScrollView scrollAnexos;
        LinearLayout layoutAnexos;
        Button btnEditar, btnExcluir;

        public AvisoViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDataHora = itemView.findViewById(R.id.tvDataHora);
            tvAssunto = itemView.findViewById(R.id.tvAssunto);
            tvDescricao = itemView.findViewById(R.id.tvDescricao);
            tvAnexosLabel = itemView.findViewById(R.id.tvAnexosLabel);
            scrollAnexos = itemView.findViewById(R.id.scrollAnexos);
            layoutAnexos = itemView.findViewById(R.id.layoutAnexos);
            btnEditar = itemView.findViewById(R.id.btnEditar);
            btnExcluir = itemView.findViewById(R.id.btnExcluir);
        }
    }
}