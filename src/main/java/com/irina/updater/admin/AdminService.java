package com.irina.updater.admin;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;

@Service
public class AdminService {

    @Value("${irinabot.updater.location}/cache")
    public String cacheFolder;

    @Value("${irinabot.updater.location}/temp")
    public String tempFolder;
    public void deleteZipFiles() throws IOException {
        FileUtils.cleanDirectory(new File(cacheFolder));
        FileUtils.cleanDirectory(new File(tempFolder));
    }


}
