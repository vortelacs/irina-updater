package com.irina.updater.util;

import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class Zipper {

    public static void appendFileToZipInMemory(ByteArrayOutputStream zipOutputStream, String filePath) {
        appendFileToZipInMemory(zipOutputStream, filePath, Paths.get(filePath).getFileName().toString());
    }

    public static void appendFileToZipInMemory(ByteArrayOutputStream zipOutputStream, String filePath, String entryName) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            throw new IllegalArgumentException("Invalid file path: " + filePath);
        }

        try (ZipOutputStream zipStream = new ZipOutputStream(zipOutputStream)) {
            ZipEntry entry = new ZipEntry(entryName);
            zipStream.putNextEntry(entry);
            Files.copy(file.toPath(), zipStream);
            zipStream.closeEntry();
        } catch (IOException e) {
            throw new RuntimeException("Error appending file to ZIP: " + e.getMessage(), e);
        }
    }


    public static void unzip(File fileZip, File destDir) throws IOException {
        byte[] buffer = new byte[4096];
        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(fileZip))) {
            ZipEntry zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                File newFile = newFile(destDir, zipEntry);
                if (zipEntry.isDirectory()) {
                    if (!newFile.isDirectory() && !newFile.mkdirs()) {
                        throw new IOException("Failed to create directory " + newFile);
                    }
                } else {
                    File parent = newFile.getParentFile();
                    if (!parent.isDirectory() && !parent.mkdirs()) {
                        throw new IOException("Failed to create directory " + parent);
                    }
                    try (FileOutputStream fos = new FileOutputStream(newFile)) {
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            fos.write(buffer, 0, len);
                        }
                    }
                }
                zipEntry = zis.getNextEntry();
            }
        }
    }
    private static File newFile(File destinationDir, ZipEntry zipEntry) throws IOException {
        File destFile = new File(destinationDir, zipEntry.getName());
        String destDirPath = destinationDir.getCanonicalPath();
        String destFilePath = destFile.getCanonicalPath();
        if (!destFilePath.startsWith(destDirPath + File.separator)) {
            throw new IOException("Entry is outside of the target dir: " + zipEntry.getName());
        }
        return destFile;
    }
}
