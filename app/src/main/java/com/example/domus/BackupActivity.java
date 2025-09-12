package com.example.domus;

import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class BackupActivity extends AppCompatActivity {

    private Button buttonExportar, buttonImportar;

    private final ActivityResultLauncher<String> exportarLauncher =
            registerForActivityResult(new ActivityResultContracts.CreateDocument("application/zip"), uri -> {
                if (uri != null) {
                    exportarBackupCompleto(uri);
                } else {
                    Toast.makeText(this, "Exportação cancelada.", Toast.LENGTH_SHORT).show();
                }
            });

    private final ActivityResultLauncher<String[]> importarLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
                if (uri != null) {
                    importarBackupCompleto(uri);
                } else {
                    Toast.makeText(this, "Importação cancelada.", Toast.LENGTH_SHORT).show();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_backup);

        buttonExportar = findViewById(R.id.buttonExportar);
        buttonImportar = findViewById(R.id.buttonImportar);

        buttonExportar.setOnClickListener(v -> exportarLauncher.launch("backup_condominio.zip"));
        buttonImportar.setOnClickListener(v -> importarLauncher.launch(new String[]{"application/zip"}));
    }

    // --- EXPORTAÇÃO ---
    private void exportarBackupCompleto(Uri uriDestino) {
        File dbFile = getDatabasePath("bdcondominio.db");
        File pastaFiles = getFilesDir(); // inclui fotos salvas em /files/fotos/

        try (OutputStream os = getContentResolver().openOutputStream(uriDestino);
             ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(os))) {

            // Adiciona o banco
            zipFile(dbFile, "bdcondominio.db", zos);
            // Adiciona todos os arquivos do diretório interno (incluindo fotos)
            zipDirectory(pastaFiles, "files", zos);

            zos.flush();
            Toast.makeText(this, "Backup exportado com sucesso!", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao exportar backup.", Toast.LENGTH_SHORT).show();
        }
    }

    private void zipFile(File arquivo, String nomeZipEntry, ZipOutputStream zos) throws Exception {
        try (FileInputStream fis = new FileInputStream(arquivo);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            ZipEntry zipEntry = new ZipEntry(nomeZipEntry);
            zos.putNextEntry(zipEntry);

            byte[] buffer = new byte[4096];
            int read;
            while ((read = bis.read(buffer)) != -1) {
                zos.write(buffer, 0, read);
            }
            zos.closeEntry();
        }
    }

    private void zipDirectory(File pasta, String caminhoZip, ZipOutputStream zos) throws Exception {
        File[] files = pasta.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                zipDirectory(file, caminhoZip + "/" + file.getName(), zos);
            } else {
                zipFile(file, caminhoZip + "/" + file.getName(), zos);
            }
        }
    }

    // --- IMPORTAÇÃO ---
    private void importarBackupCompleto(Uri uriOrigem) {
        File dbFile = getDatabasePath("bdcondominio.db");
        File pastaFiles = getFilesDir();

        try (InputStream is = getContentResolver().openInputStream(uriOrigem);
             ZipInputStream zis = new ZipInputStream(new BufferedInputStream(is))) {

            // Deleta banco e arquivos antigos
            if (dbFile.exists()) dbFile.delete();
            deleteDirectoryContents(pastaFiles);

            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                File arquivoDestino;

                if (entry.getName().equals("bdcondominio.db")) {
                    arquivoDestino = dbFile;
                } else if (entry.getName().startsWith("files/")) {
                    String relativePath = entry.getName().substring("files/".length());
                    arquivoDestino = new File(pastaFiles, relativePath);
                } else {
                    zis.closeEntry();
                    continue;
                }

                if (entry.isDirectory()) {
                    arquivoDestino.mkdirs();
                } else {
                    File parent = arquivoDestino.getParentFile();
                    if (parent != null && !parent.exists()) parent.mkdirs();

                    try (FileOutputStream fos = new FileOutputStream(arquivoDestino)) {
                        byte[] buffer = new byte[4096];
                        int count;
                        while ((count = zis.read(buffer)) != -1) {
                            fos.write(buffer, 0, count);
                        }
                        fos.flush();
                    }
                }
                zis.closeEntry();
            }

            Toast.makeText(this, "Backup importado com sucesso. Reinicie o app para ver as fotos.", Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao importar backup.", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteDirectoryContents(File dir) {
        if (dir == null || !dir.exists() || !dir.isDirectory()) return;

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File f : files) {
            if (f.isDirectory()) {
                deleteDirectoryContents(f);
                f.delete();
            } else {
                f.delete();
            }
        }
    }
}
