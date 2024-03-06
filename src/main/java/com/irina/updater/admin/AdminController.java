package com.irina.updater.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping(value = "/admin")
public class AdminController {

    private final AdminService adminService;
    private final UpdateLoaderService updateLoaderService;

    @Autowired
    AdminController(AdminService adminService, UpdateLoaderService updateLoaderService){
        this.adminService = adminService;
        this.updateLoaderService = updateLoaderService;
    }

    @DeleteMapping("/delete-cache")
    public void deleteCache() throws IOException {
        adminService.deleteZipFiles();
    }

    @PostMapping("/upload-updates")
    public void uploadNewUpdate(@RequestBody MultipartFile zip) throws IOException {
        updateLoaderService.deployUpdate(zip);
    }

    @GetMapping("/check-updates")
    public void checkLocalUpdates() {
    }

}
