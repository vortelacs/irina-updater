package com.irina.updater.admin;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.io.File;
import java.io.IOException;

@Service
public class AdminService {

    private final static Logger log = LoggerFactory.getLogger(AdminService.class);

    @Value("${irinabot.updater.location}/cache")
    public String cacheFolder;

    @Value("${irinabot.updater.location}/temp")
    public String tempFolder;
    public void deleteCache() throws IOException {
        FileUtils.cleanDirectory(new File(cacheFolder));
        log.info("Deleted cache folder");
        FileUtils.cleanDirectory(new File(tempFolder));
        log.info("Deleted temp folder");
    }


}
