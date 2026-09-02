package com.example.controller;

import com.example.common.Result;
import com.example.common.enums.RoleEnum;
import com.example.common.security.RequireRoles;
import com.example.service.FileStorageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;

/**
 * Image upload and download endpoints.
 */
@RestController
@RequestMapping("/files")
public class FileController {

    private final FileStorageService fileStorageService;

    @Value("${server.port:9090}")
    private String port;

    @Value("${ip:localhost}")
    private String ip;

    public FileController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/upload")
    public Result upload(@RequestParam("file") MultipartFile file) {
        String storedName = fileStorageService.storeImage(file);
        String url = "http://" + ip + ":" + port + "/files/" + storedName;
        return Result.success(url);
    }

    @GetMapping("/{storedName}")
    public ResponseEntity<Resource> download(@PathVariable String storedName) {
        Resource resource = fileStorageService.load(storedName);
        return ResponseEntity.ok()
                .contentType(contentType(storedName))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.inline()
                                .filename(storedName, StandardCharsets.UTF_8)
                                .build().toString())
                .body(resource);
    }

    @DeleteMapping("/{storedName}")
    @RequireRoles(RoleEnum.ADMIN)
    public Result delFile(@PathVariable String storedName) {
        fileStorageService.delete(storedName);
        return Result.success();
    }

    private MediaType contentType(String storedName) {
        String lowerName = storedName.toLowerCase();
        if (lowerName.endsWith(".png")) {
            return MediaType.IMAGE_PNG;
        }
        if (lowerName.endsWith(".gif")) {
            return MediaType.IMAGE_GIF;
        }
        return MediaType.IMAGE_JPEG;
    }
}
