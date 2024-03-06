package com.irina.updater.controller;

import com.irina.updater.model.VersionInfo;
import com.irina.updater.model.dto.ProductRequest;
import com.irina.updater.service.UpdaterService;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping(value = "/v1/updates")
public class UpdaterController {

    private final UpdaterService updaterService;

    UpdaterController(UpdaterService updaterService){
        this.updaterService = updaterService;
    }

    @RequestMapping(value = "/archive", produces="application/zip")
    public ResponseEntity<FileSystemResource> getUpdate(@RequestParam String userVersion,@RequestParam String channel,@RequestParam String product) throws IOException {

        VersionInfo versionInfo = new VersionInfo(userVersion, channel, product);
        versionInfo.setLatestVersion(updaterService.getLatestVersion(versionInfo.getChannel(), versionInfo.getProduct()));

        if(versionInfo.getLatestVersion().equals(versionInfo.getUserVersion())){
            return new ResponseEntity<>(
                    new FileSystemResource("No updates"),
                    HttpStatus.NO_CONTENT);
        }

        return createResponse(new FileSystemResource(new File(updaterService.getUpdateZipFile(versionInfo))));
    }


    @RequestMapping(value = "/product")
    public ResponseEntity<FileSystemResource> getProduct(@RequestBody ProductRequest product) throws IOException{

        return createResponse(new FileSystemResource(new File(updaterService.getProductZip(product))));
    }

    private ResponseEntity<FileSystemResource> createResponse(FileSystemResource resource) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", resource.getFilename());

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(resource.contentLength())
                .body(resource);
    }

}