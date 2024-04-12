package com.irina.updater.controller;

import com.irina.updater.model.dto.UpdateRequestDTO;
import com.irina.updater.model.dto.ProductRequestDTO;
import com.irina.updater.service.UpdaterService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@RestController
@RequestMapping(value = "/v1/updates")
public class UpdaterController {

    private final UpdaterService updaterService;

    UpdaterController(UpdaterService updaterService){
        this.updaterService = updaterService;
    }

    @RequestMapping(value = "/archive", produces = "application/zip")
    public ResponseEntity<?> getUpdate(@RequestParam String userVersion, @RequestParam String channel, @RequestParam String product) throws IOException {

        UpdateRequestDTO updateRequest = new UpdateRequestDTO(userVersion, channel, product);
        updateRequest.setLatestVersion(updaterService.getLatestVersion(updateRequest.getChannel(), updateRequest.getProduct()));

        if (updateRequest.getLatestVersion() == null || updateRequest.getLatestVersion().isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Product with the given name and channel doesn't exist");
        }

        if (updateRequest.getLatestVersion().equals(updateRequest.getUserVersion())) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No updates available for this product");
        }

        ByteArrayOutputStream updateZipFile = updaterService.getUpdateZipFile(updateRequest);
        return createResponse(updateZipFile);
    }

    @RequestMapping(value = "/product")
    public ResponseEntity<byte[]> getProduct(@RequestBody ProductRequestDTO product) throws IOException {
        ByteArrayOutputStream productZip = updaterService.getProductZip(product);
        return createResponse(productZip);
    }

    private ResponseEntity<byte[]> createResponse(ByteArrayOutputStream resource){
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
        headers.setContentDispositionFormData("attachment", "product.zip");

        byte[] byteArray = resource.toByteArray();
        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(byteArray.length)
                .body(byteArray);
    }
}