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

            // ==== Java 8 way (no Map.of) ====
            Map<String, Object> body = new HashMap<String, Object>();
            body.put("savedFiles", saved);
            return ResponseEntity.ok(body);

        } catch (IllegalArgumentException e) {
            // Collections.singletonMap works fine on Java 8
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", e.getMessage()));
        } catch (IOException e) {
            return ResponseEntity.status(500)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}
