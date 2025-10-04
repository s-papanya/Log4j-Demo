package com.example.vuln.service;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ZipImageService {

    private final Path productsRoot;
    private final int thumbMaxW, thumbMaxH, smallThumbMaxW, smallThumbMaxH;

    private static final Set<String> ALLOWED_EXT =
            new HashSet<>(Arrays.asList("jpg","jpeg","png","gif","webp","bmp"));

    public ZipImageService(
            @Value("${app.storage.products-dir}") String productsDir,
            @Value("${app.thumb.max-width}") int thumbMaxW,
            @Value("${app.thumb.max-height}") int thumbMaxH,
            @Value("${app.thumb.small.max-width}") int smallThumbMaxW,
            @Value("${app.thumb.small.max-height}") int smallThumbMaxH
    ) throws IOException {
        this.productsRoot = Paths.get(productsDir);
        Files.createDirectories(productsRoot);
        this.thumbMaxW = thumbMaxW;
        this.thumbMaxH = thumbMaxH;
        this.smallThumbMaxW = smallThumbMaxW;
        this.smallThumbMaxH = smallThumbMaxH;
    }

    public List<String> handleZipUpload(Long productId, MultipartFile zipFile) throws IOException {
        List<String> savedFiles = new ArrayList<>();

        String filename = Optional.ofNullable(zipFile.getOriginalFilename()).orElse("").toLowerCase();
        if (!filename.endsWith(".zip")) {
            throw new IllegalArgumentException("ไฟล์ต้องเป็น .zip เท่านั้น");
        }

        Path productDir = productsRoot.resolve(String.valueOf(productId));
        Path thumbsDir = productDir.resolve("thumbs");
        Path smallThumbsDir = productDir.resolve("thumbs_small");
        Files.createDirectories(productDir);
        Files.createDirectories(thumbsDir);
        Files.createDirectories(smallThumbsDir);

        try (ZipInputStream zin = new ZipInputStream(new BufferedInputStream(zipFile.getInputStream()))) {
            ZipEntry entry;
            while ((entry = zin.getNextEntry()) != null) {
                if (entry.isDirectory()) { zin.closeEntry(); continue; }

                String entryName = Paths.get(entry.getName()).getFileName().toString();
                String ext = getExt(entryName).toLowerCase();
                if (!ALLOWED_EXT.contains(ext)) { zin.closeEntry(); continue; }

                // เขียนไฟล์จาก ZIP → temp file (stream)
                String tempName = UUID.randomUUID() + ".part";
                Path tempPath = productDir.resolve(tempName);
                Files.copy(zin, tempPath, StandardCopyOption.REPLACE_EXISTING);
                zin.closeEntry();

                // ตรวจ header คร่าว ๆ ให้มั่นใจว่าเป็นภาพจริง
                if (!looksLikeImage(tempPath, ext)) {
                    safeDelete(tempPath);
                    continue;
                }

                // ย้ายเป็นชื่อไฟล์จริง
                String storedName = UUID.randomUUID() + "." + (ext.equals("jpg") ? "jpeg" : ext);
                Path finalPath = productDir.resolve(storedName);
                Files.move(tempPath, finalPath, StandardCopyOption.ATOMIC_MOVE);

                // ทำ thumbnail (กลาง + เล็ก)
                String thumbName = UUID.randomUUID() + "-md." + (ext.equals("jpg") ? "jpeg" : ext);
                String smallThumbName = UUID.randomUUID() + "-sm." + (ext.equals("jpg") ? "jpeg" : ext);

                Thumbnails.of(finalPath.toFile()).size(thumbMaxW, thumbMaxH)
                          .toFile(thumbsDir.resolve(thumbName).toFile());

                Thumbnails.of(finalPath.toFile()).size(smallThumbMaxW, smallThumbMaxH)
                          .toFile(smallThumbsDir.resolve(smallThumbName).toFile());

                savedFiles.add(storedName);
            }
        }
        return savedFiles;
    }

    private static String getExt(String name) {
        int i = name.lastIndexOf('.');
        return (i >= 0) ? name.substring(i + 1) : "";
    }

    private static boolean looksLikeImage(Path file, String ext) {
        try (InputStream in = Files.newInputStream(file)) {
            byte[] h = new byte[12];
            int r = in.read(h);
            if (r < 4) return false;

            if ((ext.equals("jpg") || ext.equals("jpeg")) && h[0]==(byte)0xFF && h[1]==(byte)0xD8) return true;     // JPEG
            if (ext.equals("png")  && h[0]==(byte)0x89 && h[1]=='P' && h[2]=='N' && h[3]=='G') return true;        // PNG
            if (ext.equals("gif")  && h[0]=='G' && h[1]=='I' && h[2]=='F') return true;                             // GIF
            if (ext.equals("webp") && h[0]=='R' && h[1]=='I' && h[2]=='F' && h[3]=='F') return true;                // WEBP (RIFF)
            if (ext.equals("bmp")  && h[0]=='B' && h[1]=='M') return true;                                          // BMP
            return false;
        } catch (IOException e) {
            return false;
        }
    }

    private static void safeDelete(Path p) {
        try { Files.deleteIfExists(p); } catch (IOException ignored) {}
    }
}
