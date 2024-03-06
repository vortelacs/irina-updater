package com.irina.updater.util;

import java.io.*;
import java.net.URI;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class Zipper {

    public static void appendFileToZip(String zipFilePath, String filePath) {
        appendFileToZip(zipFilePath, filePath, Paths.get(filePath).getFileName().toString());
    }

    public static void appendFileToZip(String zipFilePath, String filePath, String entryName) {
        Map<String, String> env = new HashMap<>();
        env.put("create", "true");

        Path fileToAppend = Paths.get(filePath);
        Path zipFile = Paths.get(zipFilePath);
        URI zipUri = URI.create("jar:" + zipFile.toUri());

        try {
            try (FileSystem zipFileSystem = FileSystems.newFileSystem(zipUri, env)) {
                String[] dirs = entryName.split("/");
                Path currentPath = zipFileSystem.getPath("");
                for (int i = 0; i < dirs.length - 1; i++) {
                    currentPath = currentPath.resolve(dirs[i]);
                    if (!Files.exists(currentPath)) {
                        Files.createDirectories(currentPath);
                    }
                }
                Path fileInZip = currentPath.resolve(dirs[dirs.length - 1]);
                Files.copy(fileToAppend, fileInZip, StandardCopyOption.REPLACE_EXISTING);
            }
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

    private static void zipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isHidden()) {
            return;
        }
        ZipFile(fileToZip, fileName, zipOut);
    }

    private static void ZipFile(File fileToZip, String fileName, ZipOutputStream zipOut) throws IOException {
        if (fileToZip.isDirectory()) {
            String[] children = fileToZip.list();
            assert children != null;
            for (String child : children) {
                zipFile(new File(fileToZip, child), fileName + File.separator + child, zipOut);
            }
            return;
        }
        try (FileInputStream fis = new FileInputStream(fileToZip)) {
            ZipEntry zipEntry = new ZipEntry(fileName);
            zipOut.putNextEntry(zipEntry);
            byte[] bytes = new byte[4096];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zipOut.write(bytes, 0, length);
            }
        }
    }


}
