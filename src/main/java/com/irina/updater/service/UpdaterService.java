package com.irina.updater.service;

import com.irina.updater.model.dto.UpdateRequestDTO;
import com.irina.updater.model.dto.FileInfo;
import com.irina.updater.model.dto.ProductRequestDTO;
import com.irina.updater.repository.VersionFileRepository;
import com.irina.updater.util.ManifestGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import static org.mariadb.jdbc.util.StringUtils.byteArrayToHexString;

@Service
public class UpdaterService {

    private final static Logger log = LoggerFactory.getLogger(UpdaterService.class);
    public static final String MANIFEST_FILE_NAME = "_manifest.json";
    public static final String UPDATE_NAME_FORMAT = "%s-%s-%s-%s.zip";
    private final VersionFileRepository versionFileRepository;
    private final ZipperService zipperService;
    @Value("${irinabot.updater.location}/cache")
    private String zipFolder;
    @Value("${irinabot.updater.location}/temp")
    private String tempFolder;

    @Autowired
    UpdaterService(VersionFileRepository versionFileRepository, ZipperService zipperService) {
        this.versionFileRepository = versionFileRepository;
        this.zipperService = zipperService;
    }

    private boolean checkZipFile(String zipName) {
        Path path = Paths.get(zipFolder + File.separator + zipName);
        return Files.exists(path);
    }

    public ByteArrayOutputStream getUpdateZipFile(UpdateRequestDTO versionInfo) throws IOException {
        Map<String, String> changedFiles;
        ByteArrayOutputStream updateFilesZip = new ByteArrayOutputStream();
        String zipName = String.format(UPDATE_NAME_FORMAT, versionInfo.getUserVersion(), versionInfo.getLatestVersion(), versionInfo.getChannel(), versionInfo.getProduct());
        if (!checkZipFile(zipName)) {
            changedFiles = getUpdates(versionInfo);
            ManifestGenerator.generateUpdateManifest(tempFolder + File.separator + MANIFEST_FILE_NAME, changedFiles);
            updateFilesZip = zipperService.generateProductUpdateZip(changedFiles, zipFolder, zipName);
        }

        return updateFilesZip;
    }

    public String getLatestVersion(String channel, String product) {
        return versionFileRepository.findProductLatestVersion(channel, product);
    }

    public Map<String, String> getUpdates(UpdateRequestDTO versionInfo) {
        Map<String, String> changedFiles = new HashMap<>();
        List<FileInfo> latestFiles = versionFileRepository.getFileInfoList(Long.parseLong(versionInfo.getLatestVersion()), versionInfo.getChannel(), versionInfo.getProduct());
        List<FileInfo> userFiles = versionFileRepository.getFileInfoList(Long.parseLong(versionInfo.getUserVersion()), versionInfo.getChannel(), versionInfo.getProduct());

        Map<String, byte[]> latestFilesMap = latestFiles.stream().collect(Collectors.toMap(FileInfo::getFilePath, FileInfo::getCheckSum));
        Map<String, byte[]> userFilesMap = userFiles.stream().collect(Collectors.toMap(FileInfo::getFilePath, FileInfo::getCheckSum));


        latestFilesMap
                .forEach((path, hash) -> {
                    if (!userFilesMap.containsKey(path) || !Arrays.equals(userFilesMap.get(path), hash))
                        changedFiles.put(path, byteArrayToHexString(hash));
                });

        userFilesMap
                .forEach((path, hash) -> {
                    if (!latestFilesMap.containsKey(path))
                        changedFiles.put(path, "null");
                });
        return changedFiles;
    }

    public ByteArrayOutputStream getProductZip(ProductRequestDTO productRequest) throws IOException {
        try (ByteArrayOutputStream productZipByteArray = new ByteArrayOutputStream()) {
            productRequest.getFiles().forEach((path, productData) -> {
                String version = versionFileRepository.findProductLatestVersion(productData.getChannel(), productData.getProduct());
                if (version == null || version.isEmpty()) {
                    log.warn("Product \"" + productData.getProduct() + "\" wasn't found - skipping...");
                } else {
                    List<FileInfo> productFileList = versionFileRepository.getFileInfoList(Long.parseLong(version), productData.getChannel(), productData.getProduct());
                    log.info("Adding product \"" + productData.getProduct() + "\" to the final zip");
                    zipperService.addProductToZip(productZipByteArray, path, productFileList);
                }
            });

            log.info("Final zip was generated");
            productZipByteArray.flush();
            return productZipByteArray;
        } catch (IOException e) {
            log.error("Error creating product zip");
            throw e;
        }
    }
}
