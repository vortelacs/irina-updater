package com.irina.updater.controller;

import com.irina.updater.model.dto.UpdateRequestDTO;
import com.irina.updater.model.dto.ProductRequestDTO;
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

import java.io.IOException;

@RestController
@RequestMapping(value = "/v1/updates")
public class UpdaterController {

    private final UpdaterService updaterService;

    UpdaterController(UpdaterService updaterService){
        this.updaterService = updaterService;
    }

    @RequestMapping(value = "/archive", produces="application/zip")
    public ResponseEntity<?> getUpdate(@RequestParam String userVersion,@RequestParam String channel,@RequestParam String product) throws IOException {

        UpdateRequestDTO updateRequest = new UpdateRequestDTO(userVersion, channel, product);
        updateRequest.setLatestVersion(updaterService.getLatestVersion(updateRequest.getChannel(), updateRequest.getProduct()));

        if(updateRequest.getLatestVersion() == null || updateRequest.getLatestVersion().isEmpty())
        {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Product with the given name and channel doesn't exist");
        }

        if(updateRequest.getLatestVersion().equals(updateRequest.getUserVersion())){
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No updates available for this product");
        }

            return createResponse(updaterService.getUpdateZipFile(updateRequest));
    }


    @RequestMapping(value = "/product")
    public ResponseEntity<FileSystemResource> getProduct(@RequestBody ProductRequestDTO product) throws IOException{

        return createResponse(updaterService.getProductZip(product));
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