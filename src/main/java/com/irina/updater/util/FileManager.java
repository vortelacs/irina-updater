package com.irina.updater.util;

import com.irina.updater.model.VersionFile;
import com.irina.updater.model.dto.ProductInfoDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.FileSystemResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FileManager {

    private final static Logger log = LoggerFactory.getLogger(FileManager.class);
    public static final String MANIFEST_FILE = "_productMap.json";

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
        if (new File(destinationFolder + File.separator + destinationFileName).exists()) {
            log.trace("File " + destinationFileName + " exists already so it will not be copied");
        } else {
            Files.copy(sourcePath, destinationPath);
            log.trace("File " + destinationFileName + " was copied to the product folder");
        }
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

    public static Map<String, ProductInfoDTO> getProductList(File folder, String channel) throws IOException {
        log.trace("Searching in " + folder.getPath() + " update folder for " + MANIFEST_FILE);
        Map<String, ProductInfoDTO> productMap = new HashMap<>();
        if (doesFileExistInFolder(folder.getPath(), MANIFEST_FILE)) {
            ProductInfoDTO productInfo = getProductMapInfo(folder);
            productInfo.setChannel(channel);
            log.info("Found a \"" + MANIFEST_FILE + "\" file in \"" + folder.getPath() + "\" folder with product = " + productInfo.getProduct() + " and version = " + productInfo.getVersion());
            productMap.put(folder.getPath(), productInfo);
        }

        File[] files = folder.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    productMap.putAll(getProductList(file, channel));
                }
            }
        }
        return productMap;
    }



    public static Map<VersionFile, FileSystemResource> processProductFolder(File productFolder, ProductInfoDTO productInfo) {
        log.info("Processing " + productFolder.getPath() + " update folder");
        IgnoreFileParser.IgnorePaths ignorePaths = IgnoreFileParser.compile(productInfo.getIgnoredPaths());
        return processProductFolder(productFolder, productFolder, productInfo, ignorePaths);
    }

    public static Map<VersionFile, FileSystemResource> processProductFolder(File productFolder, File indexFolder, ProductInfoDTO productInfo, IgnoreFileParser.IgnorePaths ignorePaths) {
        log.trace("Processing \"" + indexFolder.getPath() + "\" folder for product: " + productInfo.getProduct());
        File[] files = indexFolder.listFiles();

        Map<VersionFile, FileSystemResource> filesMap = new HashMap<>();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory() && !doesFileExistInFolder(file.getPath(), MANIFEST_FILE)) {
                    filesMap.putAll(processProductFolder(productFolder, file, productInfo, ignorePaths));
                } else if (file.isFile()) {
                    if (ignorePaths.denies(file.getName())) {
                        log.info("Skipping " + file.getName() + " file in \"" + file.getPath() + "\" folder");
                        continue;
                    }
                    String relativePath = productFolder.toURI().relativize(file.toURI()).getPath();
                    filesMap.put(new VersionFile(relativePath, productInfo.getProduct(), productInfo.getChannel(), productInfo.getVersion()), new FileSystemResource(file));
                }
            }
        }
        return filesMap;
    }


    private static ProductInfoDTO getProductMapInfo(File productMapJson) throws IOException {
        String product = JsonUtility.getValueFromJsonByKey(new File(productMapJson.getPath() + File.separator + MANIFEST_FILE), "product");
        String version = JsonUtility.getValueFromJsonByKey(new File(productMapJson.getPath() + File.separator +MANIFEST_FILE), "version");
        List<String> ignoredPaths = JsonUtility.getArrayValueFromJsonByKey(new File(productMapJson.getPath() + File.separator + MANIFEST_FILE), "ignorePaths");
        assert version != null;

        ProductInfoDTO productInfoDTO = new ProductInfoDTO(product, VersionParser.parseNumbers(version));
        productInfoDTO.setIgnoredPaths(ignoredPaths);
        return productInfoDTO;
    }
}