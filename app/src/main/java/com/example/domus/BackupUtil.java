package com.example.domus;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class BackupUtil {

    private static final String BACKUP_JSON = "backup_moradores.json";
    private static final String FOTOS_DIR = "fotos_moradores";
    private static final String ANEXOS_DIR = "anexos";
    private static final String TAG = "BackupUtil";

    // Métodos para manipulação de imagens
    public static String saveImageToInternalStorage(Context context, Uri imageUri) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                Log.e(TAG, "InputStream é null para URI: " + imageUri);
                return null;
            }

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
            inputStream.close();

            if (bitmap == null) {
                Log.e(TAG, "Falha ao decodificar bitmap da URI: " + imageUri);
                return null;
            }

            // Criar nome único para o arquivo
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String imageFileName = "JPEG_" + timeStamp + ".jpg";

            // Criar diretório se não existir
            File storageDir = new File(context.getFilesDir(), FOTOS_DIR);
            if (!storageDir.exists()) {
                if (!storageDir.mkdirs()) {
                    Log.e(TAG, "Falha ao criar diretório: " + storageDir.getAbsolutePath());
                    return null;
                }
            }

            File imageFile = new File(storageDir, imageFileName);

            try (FileOutputStream out = new FileOutputStream(imageFile)) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
            }

            Log.d(TAG, "Imagem salva em: " + imageFile.getAbsolutePath());
            return imageFile.getAbsolutePath();

        } catch (IOException e) {
            Log.e(TAG, "Erro ao salvar imagem: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static Uri loadImageFromInternalStorage(Context context, String imagePath) {
        if (imagePath == null || imagePath.isEmpty()) {
            Log.e(TAG, "Caminho da imagem é nulo ou vazio");
            return null;
        }

        try {
            File imageFile = new File(imagePath);
            if (imageFile.exists()) {
                return Uri.fromFile(imageFile);
            } else {
                Log.e(TAG, "Arquivo de imagem não existe: " + imagePath);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao carregar imagem: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public static void deleteImageFile(String imagePath) {
        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    if (imageFile.delete()) {
                        Log.d(TAG, "Imagem deletada: " + imagePath);
                    } else {
                        Log.e(TAG, "Falha ao deletar imagem: " + imagePath);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Erro ao deletar imagem: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    // Método para limpar todas as fotos de moradores
    public static void limparTodasFotos(Context context) {
        try {
            File fotosDir = new File(context.getFilesDir(), FOTOS_DIR);
            if (fotosDir.exists() && fotosDir.isDirectory()) {
                File[] files = fotosDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        if (file.delete()) {
                            Log.d(TAG, "Arquivo deletado: " + file.getName());
                        } else {
                            Log.e(TAG, "Falha ao deletar arquivo: " + file.getName());
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao limpar fotos: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Converter Bitmap para Base64
    public static String bitmapToBase64(Bitmap bitmap) {
        if (bitmap == null) {
            Log.e(TAG, "Bitmap é null na conversão para Base64");
            return null;
        }

        try {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
            return Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao converter bitmap para Base64: " + e.getMessage());
            return null;
        }
    }

    // Converter Base64 para Bitmap
    public static Bitmap base64ToBitmap(String base64String) {
        if (base64String == null || base64String.isEmpty()) {
            Log.e(TAG, "String Base64 é nula ou vazia");
            return null;
        }

        try {
            byte[] decodedBytes = Base64.decode(base64String, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.length);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao converter Base64 para bitmap: " + e.getMessage());
            return null;
        }
    }

    // Métodos de backup
    public static boolean exportarMoradores(Context context, JSONArray moradoresArray) {
        try {
            if (moradoresArray == null) {
                Log.e(TAG, "JSONArray de moradores é null");
                return false;
            }

            // Salva JSON
            File backupJson = new File(context.getExternalFilesDir(null), BACKUP_JSON);
            try (FileWriter writer = new FileWriter(backupJson)) {
                writer.write(moradoresArray.toString(4));
            }

            // Salva fotos em backup_fotos dentro do externo
            File pastaFotosOrigem = new File(context.getFilesDir(), FOTOS_DIR);
            File pastaFotosBackup = new File(context.getExternalFilesDir(null), "backup_fotos");
            copyDirectory(pastaFotosOrigem, pastaFotosBackup);

            Log.d(TAG, "Backup exportado com sucesso");
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Erro ao exportar backup: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Importa JSON e fotos
    public static JSONArray importarMoradores(Context context) {
        try {
            // Lê JSON
            File backupJson = new File(context.getExternalFilesDir(null), BACKUP_JSON);
            if (!backupJson.exists()) {
                Log.e(TAG, "Arquivo de backup não existe: " + backupJson.getAbsolutePath());
                return null;
            }

            StringBuilder sb = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(backupJson))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
            }

            JSONArray moradoresArray = new JSONArray(sb.toString());

            // Restaura fotos
            File pastaFotosOrigem = new File(context.getExternalFilesDir(null), "backup_fotos");
            File pastaFotosDestino = new File(context.getFilesDir(), FOTOS_DIR);
            copyDirectory(pastaFotosOrigem, pastaFotosDestino);

            Log.d(TAG, "Backup importado com sucesso");
            return moradoresArray;
        } catch (Exception e) {
            Log.e(TAG, "Erro ao importar backup: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Copia todos os arquivos de uma pasta para outra
    private static void copyDirectory(File srcDir, File dstDir) throws Exception {
        if (srcDir == null || !srcDir.exists()) {
            Log.w(TAG, "Diretório origem não existe: " + (srcDir != null ? srcDir.getAbsolutePath() : "null"));
            return;
        }

        if (!dstDir.exists()) {
            if (!dstDir.mkdirs()) {
                throw new IOException("Falha ao criar diretório destino: " + dstDir.getAbsolutePath());
            }
        }

        File[] files = srcDir.listFiles();
        if (files == null) {
            Log.w(TAG, "Nenhum arquivo no diretório: " + srcDir.getAbsolutePath());
            return;
        }

        for (File file : files) {
            File dstFile = new File(dstDir, file.getName());
            if (file.isDirectory()) {
                copyDirectory(file, dstFile);
            } else {
                copyFile(file, dstFile);
            }
        }
    }

    private static void copyFile(File src, File dst) throws Exception {
        try (InputStream is = new FileInputStream(src);
             OutputStream os = new FileOutputStream(dst)) {
            byte[] buffer = new byte[4096];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            os.flush();
        }
        Log.d(TAG, "Arquivo copiado: " + src.getName() + " -> " + dst.getAbsolutePath());
    }

    // --- Copia URI para arquivo (MÉTODO CRÍTICO PARA AVISOS) ---
    public static File copiarUriParaArquivo(Context context, Uri srcUri) {
        try {
            if (srcUri == null) {
                Log.e(TAG, "URI de origem é null");
                return null;
            }

            String fileName = getFileName(context, srcUri);
            File pastaAnexos = new File(context.getFilesDir(), ANEXOS_DIR);
            if (!pastaAnexos.exists()) {
                if (!pastaAnexos.mkdirs()) {
                    Log.e(TAG, "Falha ao criar diretório de anexos");
                    return null;
                }
            }

            // Garantir nome único para evitar sobrescrita
            File dstFile = new File(pastaAnexos, fileName);
            int counter = 1;
            while (dstFile.exists()) {
                String newFileName = appendSuffixToFileName(fileName, "_" + counter);
                dstFile = new File(pastaAnexos, newFileName);
                counter++;
            }

            try (InputStream is = context.getContentResolver().openInputStream(srcUri);
                 OutputStream os = new FileOutputStream(dstFile)) {

                if (is == null) {
                    Log.e(TAG, "Não foi possível abrir InputStream da URI: " + srcUri);
                    return null;
                }

                byte[] buffer = new byte[8192]; // Buffer maior para melhor performance
                int bytesLidos;
                long totalBytes = 0;

                while ((bytesLidos = is.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesLidos);
                    totalBytes += bytesLidos;
                }
                os.flush();

                Log.d(TAG, "Arquivo copiado: " + totalBytes + " bytes para " + dstFile.getAbsolutePath());
            }

            return dstFile;

        } catch (Exception e) {
            Log.e(TAG, "Erro ao copiar URI para arquivo: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // Método auxiliar para adicionar sufixo ao nome do arquivo
    private static String appendSuffixToFileName(String fileName, String suffix) {
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1) {
            String name = fileName.substring(0, dotIndex);
            String extension = fileName.substring(dotIndex);
            return name + suffix + extension;
        }
        return fileName + suffix;
    }

    // Gera nome de arquivo a partir da URI (melhorado)
    private static String getFileName(Context context, Uri uri) {
        try {
            // Tenta obter o nome do arquivo da URI
            String displayName = null;

            try (android.database.Cursor cursor = context.getContentResolver().query(
                    uri,
                    new String[]{android.provider.OpenableColumns.DISPLAY_NAME},
                    null, null, null)) {

                if (cursor != null && cursor.moveToFirst()) {
                    int nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME);
                    if (nameIndex != -1) {
                        displayName = cursor.getString(nameIndex);
                    }
                }
            }

            if (displayName != null && !displayName.isEmpty()) {
                return displayName;
            }

            // Fallback: usa o último segmento do path
            String path = uri.getPath();
            if (path != null) {
                int lastSlash = path.lastIndexOf('/');
                if (lastSlash != -1 && lastSlash + 1 < path.length()) {
                    return path.substring(lastSlash + 1);
                }
                return path;
            }

        } catch (Exception e) {
            Log.e(TAG, "Erro ao obter nome do arquivo: " + e.getMessage());
        }

        // Último fallback: nome baseado no timestamp
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        return "anexo_" + timeStamp + ".dat";
    }

    // Método para limpar arquivos temporários antigos
    public static void limparArquivosTemporariosAntigos(Context context, long tempoMaximoMs) {
        try {
            File[] diretorios = {
                    new File(context.getFilesDir(), FOTOS_DIR),
                    new File(context.getFilesDir(), ANEXOS_DIR),
                    new File(context.getFilesDir(), "ocorrencias_anexos")
            };

            long tempoAtual = System.currentTimeMillis();

            for (File dir : diretorios) {
                if (dir.exists() && dir.isDirectory()) {
                    File[] files = dir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (tempoAtual - file.lastModified() > tempoMaximoMs) {
                                if (file.delete()) {
                                    Log.d(TAG, "Arquivo temporário antigo removido: " + file.getName());
                                } else {
                                    Log.e(TAG, "Falha ao remover arquivo temporário: " + file.getName());
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao limpar arquivos temporários: " + e.getMessage());
        }
    }

    // Método para listar arquivos em um diretório (útil para debug)
    public static void listarArquivosDiretorio(Context context, String directoryName) {
        try {
            File dir = new File(context.getFilesDir(), directoryName);
            if (dir.exists() && dir.isDirectory()) {
                File[] files = dir.listFiles();
                if (files != null) {
                    Log.d(TAG, "Arquivos em " + directoryName + ":");
                    for (File file : files) {
                        Log.d(TAG, " - " + file.getName() + " (" + file.length() + " bytes)");
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Erro ao listar arquivos: " + e.getMessage());
        }
    }

    // Método para verificar se um arquivo existe
    public static boolean arquivoExiste(Context context, String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return false;
        }
        File file = new File(filePath);
        return file.exists();
    }

    // Método para obter o tamanho de um arquivo
    public static long getTamanhoArquivo(Context context, String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return 0;
        }
        File file = new File(filePath);
        return file.exists() ? file.length() : 0;
    }
}