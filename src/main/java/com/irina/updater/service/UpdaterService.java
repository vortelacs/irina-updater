package com.irina.updater.service;

import com.irina.updater.model.dto.UpdateRequestDTO;
import com.irina.updater.model.dto.FileInfo;
import com.irina.updater.model.dto.ProductRequestDTO;
import com.irina.updater.repository.VersionFileRepository;
import com.irina.updater.util.ManifestGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

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

    private final VersionFileRepository versionFileRepository;
    private final ZipperService zipperService;
    @Value("${irinabot.updater.location}/cache")
    private String zipFolder;
    @Value("${irinabot.updater.location}/temp")
    private String tempPath;

    @Autowired
    UpdaterService(VersionFileRepository versionFileRepository, ZipperService zipperService) {
        this.versionFileRepository = versionFileRepository;
        this.zipperService = zipperService;
    }

    private boolean checkZipFile(String zipName) {
        Path path = Paths.get(zipName);
        return Files.exists(path);
    }

    public String getUpdateZipFile(UpdateRequestDTO versionInfo) throws IOException {
        Map<String, String> changedFiles;
        String zipName = String.format("%s-%s-%s-%s.zip", versionInfo.getUserVersion(), versionInfo.getLatestVersion(), versionInfo.getChannel(), versionInfo.getProduct());
        if (!checkZipFile(zipName)) {
            changedFiles = getUpdates(versionInfo);
            ManifestGenerator.generateUpdateManifest(tempPath + File.separator +"_manifest.json" , changedFiles);
            zipperService.generateProductUpdateZip(changedFiles, zipFolder, zipName);
        }

        return zipFolder + "/" + zipName;
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

    public String getProductZip(ProductRequestDTO productRequest) {

        productRequest.getFiles().forEach((path, productData) -> {
            String version = versionFileRepository.findProductLatestVersion(productData.getChannel(), productData.getProduct());

            List<FileInfo> productFileList  = versionFileRepository.getFileInfoList(Long.parseLong(version), productData.getChannel(), productData.getProduct());

            zipperService.addProductToZip(productRequest.getName() + ".zip", path, productFileList);
        });


        return tempPath + File.separator + productRequest.getName() + ".zip";
    }


}
