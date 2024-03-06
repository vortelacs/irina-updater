package com.irina.updater.admin;

import com.irina.updater.model.FileIndex;
import com.irina.updater.model.VersionFile;
import com.irina.updater.repository.FileIndexRepository;
import com.irina.updater.repository.VersionFileRepository;
import com.irina.updater.service.ZipperService;
import com.irina.updater.util.FileChecksumManager;
import com.irina.updater.util.FileManager;
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

@Service
public class UpdateLoaderService {

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

    public void deployUpdate(MultipartFile updateZip) throws IOException {
        String tempUpdateFolder = FileManager.saveReceivedFile(tempFolderPath, updateZip);
        if (!tempUpdateFolder.isEmpty()) {
            File updateFolder = zipperService.unzipUpdate(tempUpdateFolder);
            ArrayList<Map<VersionFile, FileSystemResource>> fileResourceMapList = FileManager.processUpdateFolder(updateFolder);
            processFileResourceMapList(fileResourceMapList);
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
            FileManager.copyResourceFile(fileResource.getPath(), productPath, new String(fileIndex.getFileHash()));
        }
        versionFileRepository.save(versionFile);
    }

    private FileIndex createAndSaveFileIndex(String filePath) {
        byte[] checksum = Objects.requireNonNull(FileChecksumManager.calculateChecksum(new File(filePath))).getBytes();
        return fileIndexRepository.save(new FileIndex(checksum));
    }


}
