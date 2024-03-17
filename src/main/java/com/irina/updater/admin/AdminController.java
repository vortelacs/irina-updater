package com.irina.updater.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.springframework.web.bind.annotation.RequestMethod.POST;

@RestController
@RequestMapping("/v1/admin")
public class AdminController {

    private final AdminService adminService;
    private final UpdateLoaderService updateLoaderService;

    @Autowired
    AdminController(AdminService adminService, UpdateLoaderService updateLoaderService) {
        this.adminService = adminService;
        this.updateLoaderService = updateLoaderService;
    }

    @DeleteMapping("/cache")
    public void deleteCache() throws IOException {
        adminService.deleteCache();
    }

    @RequestMapping(path = "/update/products", method = POST, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public void uploadProducts(@RequestParam(required = false) MultipartFile zip) throws IOException {
        updateLoaderService.deployUpdate(zip);
    }

    @RequestMapping(path = "/update/product", method = POST, consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public void uploadProduct(@RequestParam(required = false) MultipartFile zip, String product, String version, String channel) throws IOException {
        updateLoaderService.deployUpdate(zip, product, version, channel);
    }

}
