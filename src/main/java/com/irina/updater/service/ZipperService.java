package com.irina.updater.service;

import com.irina.updater.model.dto.FileInfo;
import com.irina.updater.util.FileChecksumManager;
import com.irina.updater.util.Zipper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ZipperService {


    private final static Logger log = LoggerFactory.getLogger(ZipperService.class);
    public static final String UPDATE_FOLDER_NAME = "update_folder";
    public static final String MANIFEST_FILE_NAME = "_manifest.json";
    public static final String FILE_DELETION_MARK = "null";

    @Value("${irinabot.updater.location}/product")
    private String updaterFilesPath;

    @Value("${irinabot.updater.location}/temp")
    private String tempPath;

    public ByteArrayOutputStream generateProductUpdateZip(Map<String, String> changedFiles, String zipPath, String zipName) {

        String manifestFilePath = tempPath + File.separator + MANIFEST_FILE_NAME;

        ByteArrayOutputStream zipByteArray = new ByteArrayOutputStream();

        Zipper.appendFileToZipInMemory(zipByteArray, manifestFilePath);

        for (Map.Entry<String, String> fileP : changedFiles.entrySet()) {
            if (!fileP.getValue().equals(FILE_DELETION_MARK))
                Zipper.appendFileToZipInMemory(zipByteArray, updaterFilesPath + File.separator + fileP.getValue());
        }

        return zipByteArray;
    }


    public File unzipUpdate(String updateFolderPath) throws IOException {
        File updateFolderFile = new File(updateFolderPath);
        File destinationFile = new File(updateFolderFile.getParent() + File.separator + UPDATE_FOLDER_NAME);
        Zipper.unzip(updateFolderFile, destinationFile);
        return destinationFile;
    }

    public void addProductToZip(ByteArrayOutputStream productZipByteArray, String productPath, List<FileInfo> productFileList) {
        for (FileInfo file : productFileList) {
            String filePath = updaterFilesPath + File.separator + FileChecksumManager.byteArrayToHexString(file.getCheckSum());
            String entryName = productPath + File.separator + file.getFilePath();
            Zipper.appendFileToZipInMemory(productZipByteArray, filePath, entryName);

        }
    }
}