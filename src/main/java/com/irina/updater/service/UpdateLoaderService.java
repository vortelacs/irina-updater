package com.irina.updater.service;

import com.irina.updater.model.FileIndex;
import com.irina.updater.model.VersionFile;
import com.irina.updater.model.dto.ProductInfoDTO;
import com.irina.updater.repository.FileIndexRepository;
import com.irina.updater.repository.VersionFileRepository;
import com.irina.updater.util.FileChecksumManager;
import com.irina.updater.util.FileManager;
import com.irina.updater.util.VersionParser;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;


@Service
public class UpdateLoaderService {

    private final static Logger log = LoggerFactory.getLogger(UpdateLoaderService.class);
    private final ZipperService zipperService;
    private final VersionFileRepository versionFileRepository;
    private final FileIndexRepository fileIndexRepository;
    @Value("${irinabot.updater.location}/product")
    private String productPath;
    @Value("${irinabot.updater.location}/temp")
    private String tempFolderPath;

    @Autowired
    UpdateLoaderService(VersionFileRepository versionFileRepository, ZipperService zipperService, FileIndexRepository fileIndexRepository) {
        this.versionFileRepository = versionFileRepository;
        this.zipperService = zipperService;
        this.fileIndexRepository = fileIndexRepository;
    }

    /**
     * Deploy an update for multiple products
     *
     * @param updateZip receives a zip with files to be updated
     * @param channel receive the channel of the product
     */
    public void deployUpdate(MultipartFile updateZip, String channel) throws IOException {
        String updateZipPath = FileManager.saveReceivedFile(tempFolderPath, updateZip);
        if (!updateZipPath.isEmpty()) {
            File updateFolder = zipperService.unzipUpdate(updateZipPath);

            ArrayList<Map<VersionFile, FileSystemResource>> fileResourceMapList = new ArrayList<>();
            Map<String, ProductInfoDTO> productPathMap = FileManager.getProductList(updateFolder, channel);

            productPathMap.forEach((path, productInfo) -> {
                if (isVersionNew(productInfo))
                    fileResourceMapList.add(FileManager.processProductFolder(new File(path), productInfo));
                else {
                    log.info("Skipping " + productInfo.getProduct() + " update. Version too low - " + productInfo.getVersion());
                }
            });

            processFileResourceMapList(fileResourceMapList);

            new File(updateZipPath).delete();
            FileUtils.cleanDirectory(updateFolder);
        } else {
            log.warn("The update zip was empty so no update will be deployed");
        }

    }

    /**
     * Deploy an update for a single product
     *
     * @param updateZip receives a zip with files to be updated
     * @param product   name of the product
     * @param version   version of the new update
     * @param channel receive the channel of the product
     * @param ignoredPaths a list of ignored paths
     */
    public void deployUpdate(MultipartFile updateZip, String product, String version, String channel, List<String> ignoredPaths) throws IOException {
        ProductInfoDTO productInfo = new ProductInfoDTO(product, channel, VersionParser.parseNumbers(version), ignoredPaths);
        if (!isVersionNew(productInfo)) {
            log.info("Skipped " + productInfo.getProduct() + " product due to the version lower than the last one - " + productInfo.getVersion());
            return;
        }
        String tempUpdateFolder = FileManager.saveReceivedFile(tempFolderPath, updateZip);
        if (!tempUpdateFolder.isEmpty()) {
            File updateFolder = zipperService.unzipUpdate(tempUpdateFolder);
            if (isVersionNew(productInfo)) {
                Map<VersionFile, FileSystemResource> fileResourceMapList = FileManager.processProductFolder(updateFolder, productInfo);
                processFileMap(fileResourceMapList);
            }

            new File(tempUpdateFolder).delete();
            FileUtils.cleanDirectory(updateFolder);
            updateFolder.delete();
        } else {
            log.warn("The update zip was empty so no update will be deployed");
        }
    }


    private void processFileResourceMapList(ArrayList<Map<VersionFile, FileSystemResource>> fileResourceMapList) throws IOException {
        for (Map<VersionFile, FileSystemResource> fileMap : fileResourceMapList) {
            processFileMap(fileMap);
        }
    }

    private void processFileMap(Map<VersionFile, FileSystemResource> fileMap) throws IOException {
        for (Map.Entry<VersionFile, FileSystemResource> entry : fileMap.entrySet()) {
            VersionFile versionFile = entry.getKey();
            FileSystemResource fileResource = entry.getValue();
            processVersionFile(versionFile, fileResource);
        }
    }

    private void processVersionFile(VersionFile versionFile, FileSystemResource fileResource) throws IOException {
        Integer fileId = versionFileRepository.getFileIdByFilePath(versionFile.getFilePath());
        if (fileId != null) {
            versionFile.setFileIndex(fileIndexRepository.getReferenceById(fileId));
        } else {
            FileIndex fileIndex = createAndSaveFileIndex(fileResource.getPath());
            versionFile.setFileIndex(fileIndex);
            FileManager.copyResourceFile(fileResource.getPath(), productPath, FileChecksumManager.byteArrayToHexString(fileIndex.getFileHash()));
        }
        versionFileRepository.save(versionFile);
    }

    private FileIndex createAndSaveFileIndex(String filePath) {
        byte[] checksum = Objects.requireNonNull(FileChecksumManager.calculateChecksum(filePath));
        return fileIndexRepository.save(new FileIndex(checksum));
    }

    private boolean isVersionNew(ProductInfoDTO productInfo) {

        String latestVersion = versionFileRepository.findProductLatestVersion(productInfo.getChannel(), productInfo.getProduct());
        if (latestVersion != null && !latestVersion.isEmpty())
            return Long.parseLong(latestVersion) < productInfo.getVersion();

        return true;
    }
}