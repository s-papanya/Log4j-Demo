// src/main/java/com/example/vuln/controller/PageController.java
package com.example.vuln.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    // เรียกหน้าอัปโหลดผ่านเทมเพลต
    @GetMapping("/upload")
    public String uploadPage() {
        return "upload"; // ชื่อไฟล์ใน templates = upload.html
    }
}
