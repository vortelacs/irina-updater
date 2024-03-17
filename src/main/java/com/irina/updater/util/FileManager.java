package com.irina.updater.util;

import com.irina.updater.model.VersionFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FileManager {

    private final static Logger log = LoggerFactory.getLogger(FileManager.class);

    public static boolean doesFileExistInFolder(String folderPath, String fileName) {
        File folder = new File(folderPath);

        if (!folder.exists() || !folder.isDirectory()) {
            System.out.println("Invalid folder path.");
            return false;
        }

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isFile() && file.getName().equals(fileName)) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void copyResourceFile(String sourceFile, String destinationFolder, String destinationFileName) throws IOException {
        Path sourcePath = Paths.get(sourceFile);
        Path destinationPath = Paths.get(destinationFolder, destinationFileName);
        Files.copy(sourcePath, destinationPath);
    }

    /**
     * Method for saving locally the update zip
     * Returns the path of the saved file or empty string if for some reason it couldn't be saved
     */
    public static String saveReceivedFile(String saveDestination, MultipartFile file) {
        if (file.isEmpty()) {
            log.warn("The update zip was empty");
            return "";
        }

        try {
            File folder = new File(saveDestination);
            if (!folder.exists()) folder.mkdirs();

            String fileName = file.getOriginalFilename();
            String filePath = saveDestination + File.separator + fileName;
            File dest = new File(filePath);
            file.transferTo(dest);
            return filePath;

        } catch (
                IOException e) {
            return "";
        }
    }


    public static ArrayList<Map<VersionFile, FileSystemResource>> processUpdateFolder(File folder) throws IOException {
        log.info("Processing " + folder.getPath() + " update folder");
        ArrayList<Map<VersionFile, FileSystemResource>> update = new ArrayList<>();
        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (!doesFileExistInFolder(file.getPath(), "_productMap.json")) {
                        update.addAll(processUpdateFolder(file));
                    } else {
                        String product = JsonUtility.getValueFromJsonByKey(new File(file.getPath() + File.separator + "_productMap.json"), "product");
                        String version = JsonUtility.getValueFromJsonByKey(new File(file.getPath() + File.separator + "_productMap.json"), "version");
                        String channel = JsonUtility.getValueFromJsonByKey(new File(file.getPath() + File.separator + "_productMap.json"), "channel");
                        log.info("Found a \"_productMap.json\" file in \"" + file.getPath() + "\" folder with product = " + product + " and version = " + version);
                        assert product != null;
                        assert version != null;
                        update.add(processProductFolder(file, file, product, Long.parseLong(version), channel));
                        update.addAll(processUpdateFolder(file));
                    }
                }
            }

        }
        return update;
    }

    public static Map<VersionFile, FileSystemResource> processProductFolder(File productFolder, String product, Long version, String channel){
        log.info("Processing " + productFolder.getPath() + " update folder");
        return processProductFolder(productFolder, productFolder, product, version, channel);
    }

    private static Map<VersionFile, FileSystemResource> processProductFolder(File productFolder, File indexFolder, String product, Long version, String channel) {
        log.trace("Processing \"" + indexFolder.getPath() + "\" folder for product: " + product);
        File[] files = indexFolder.listFiles();

        Map<VersionFile, FileSystemResource> filesMap = new HashMap<>();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    if (!doesFileExistInFolder(file.getPath(), "_productMap.json")) {
                        filesMap.putAll(processProductFolder(productFolder, file, product, version, channel));
                    }
                } else if (file.isFile()) {
                    if (file.getName().matches("_productMap.json") || file.getName().matches("^.*\\.zip$")) {
                        log.info("Skipping " + file.getName() + " file in \"" + file.getPath() + "\" folder");
                        continue;
                    }
                    String relativePath = productFolder.toURI().relativize(file.toURI()).getPath();
                    filesMap.put(new VersionFile(relativePath, product, channel, version), new FileSystemResource(file));
                }
            }
        } else {
            System.out.println("Folder is empty.");
        }
        return filesMap;
    }
}