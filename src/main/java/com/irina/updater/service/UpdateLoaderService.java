package com.irina.updater.service;

import com.irina.updater.exception.InvalidVersionException;
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
import java.util.Map;
import java.util.Objects;

import static com.irina.updater.util.FileManager.getUpdateVersions;

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
     * Deploy an update for a single product
     *
     * @param updateZip receives a zip with files to be updated
     */
    public void deployUpdate(MultipartFile updateZip) throws IOException {
        String tempUpdateFolder = FileManager.saveReceivedFile(tempFolderPath, updateZip);
        if (!tempUpdateFolder.isEmpty()) {
            File updateFolder = zipperService.unzipUpdate(tempUpdateFolder);

            getUpdateVersions(updateFolder).forEach(this::validateVersion);

            ArrayList<Map<VersionFile, FileSystemResource>> fileResourceMapList = FileManager.processUpdateFolder(updateFolder);
            processFileResourceMapList(fileResourceMapList);

            new File(tempUpdateFolder).delete();
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
     */
    public void deployUpdate(MultipartFile updateZip, String product, String version, String channel) throws IOException {
        String tempUpdateFolder = FileManager.saveReceivedFile(tempFolderPath, updateZip);
        ProductInfoDTO productInfo = new ProductInfoDTO(product, channel, VersionParser.parseNumbers(version));
        if (!tempUpdateFolder.isEmpty()) {
            File updateFolder = zipperService.unzipUpdate(tempUpdateFolder);
            validateVersion(productInfo);
            Map<VersionFile, FileSystemResource> fileResourceMapList = FileManager.processProductFolder(updateFolder, productInfo);
            processFileMap(fileResourceMapList);
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

    private void validateVersion(ProductInfoDTO productInfo) {
        try {
            String latestVersion = versionFileRepository.findProductLatestVersion(productInfo.getChannel(), productInfo.getProduct());
            if (latestVersion != null && !latestVersion.isEmpty())
                if (Long.parseLong(latestVersion) >= productInfo.getVersion())
                    throw new InvalidVersionException("Version of the " + productInfo.getProduct() + " product is equal or lower to the existent one");
        } catch (InvalidVersionException e) {
            throw new RuntimeException(e);
        }
    }


}
