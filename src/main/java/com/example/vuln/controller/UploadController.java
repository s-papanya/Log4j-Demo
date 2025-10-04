package com.example.vuln.controller;

import com.example.vuln.service.ZipImageService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;

@RestController
@RequestMapping("/products/{productId}/images")
public class UploadController {

    private final ZipImageService service;

    public UploadController(ZipImageService service) {
        this.service = service;
    }

    @PostMapping(value = "/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadZip(@PathVariable Long productId,
                                       @RequestPart("file") MultipartFile file) {
        try {
            List<String> saved = service.handleZipUpload(productId, file);
            return ResponseEntity.ok(Map.of("savedFiles", saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body(Map.of("error", e.getMessage()));
        }
    }
}
