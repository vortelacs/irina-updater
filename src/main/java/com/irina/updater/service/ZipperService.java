package com.irina.updater.service;

import com.irina.updater.model.dto.FileInfo;
import com.irina.updater.util.FileChecksumManager;
import com.irina.updater.util.Zipper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

@Service
public class ZipperService {

    public static final String UPDATE_FOLDER_NAME = "update_folder";
    public static final String MANIFEST_FILE_NAME = "_manifest.json";
    public static final String FILE_DELETION_MARK = "null";

    @Value("${irinabot.updater.location}/product")
    private String updaterFilesPath;

    @Value("${irinabot.updater.location}/temp")
    private String tempPath;

    public void generateProductUpdateZip(Map<String, String> changedFiles, String zipPath, String zipName) {

        String manifestFilePath = tempPath + File.separator + MANIFEST_FILE_NAME;


        Zipper.appendFileToZip(zipPath + File.separator + zipName, manifestFilePath);

        for (Map.Entry<String, String> fileP : changedFiles.entrySet()) {
            if(!fileP.getValue().equals(FILE_DELETION_MARK))
                Zipper.appendFileToZip(zipPath + File.separator + zipName,   updaterFilesPath + File.separator + fileP.getValue());
        }
    }


    public File unzipUpdate(String updateFolderPath) throws IOException {
        File updateFolderFile = new File(updateFolderPath);
        File destinationFile = new File(updateFolderFile.getParent()  + File.separator + UPDATE_FOLDER_NAME);
        Zipper.unzip(updateFolderFile, destinationFile);
        return destinationFile;
    }


    public void addProductToZip(String zipName, String productPath, List<FileInfo> productFileList) {
        productFileList.forEach(file ->
                Zipper.appendFileToZip(tempPath  + File.separator + zipName, updaterFilesPath + File.separator + FileChecksumManager.byteArrayToHexString(file.getCheckSum()), productPath + File.separator + file.getFilePath())
                );
    }
}
