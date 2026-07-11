package com.creditbank.mvp.controller;

import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.service.FileService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/files")
public class FileController {

    private final FileService fileService;

    public FileController(FileService fileService) {
        this.fileService = fileService;
    }

    /** 上传图片 */
    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) {
        return Result.ok(fileService.upload(file));
    }

    /** 上传证明材料（图片/PDF/Word） */
    @PostMapping("/upload-attachment")
    public Result<String> uploadAttachment(@RequestParam("file") MultipartFile file) {
        return Result.ok(fileService.uploadAttachment(file));
    }

    /** 浏览器可直接渲染的类型，预览时按真实类型内联展示 */
    private static final Map<String, MediaType> PREVIEW_TYPES = new HashMap<>();

    static {
        PREVIEW_TYPES.put("pdf", MediaType.APPLICATION_PDF);
        PREVIEW_TYPES.put("jpg", MediaType.IMAGE_JPEG);
        PREVIEW_TYPES.put("jpeg", MediaType.IMAGE_JPEG);
        PREVIEW_TYPES.put("png", MediaType.IMAGE_PNG);
        PREVIEW_TYPES.put("gif", MediaType.IMAGE_GIF);
        PREVIEW_TYPES.put("webp", MediaType.parseMediaType("image/webp"));
    }

    /** 在线预览：PDF/图片内联打开；Word 等浏览器不能渲染的类型自动转为下载 */
    @GetMapping("/preview/{filename}")
    public ResponseEntity<byte[]> preview(@PathVariable String filename,
                                          @RequestParam(required = false) String name) {
        Path filePath = fileService.getUploadDir().resolve(filename).normalize();
        if (!filePath.startsWith(fileService.getUploadDir()) || !Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }
        String ext = filename.contains(".")
                ? filename.substring(filename.lastIndexOf(".") + 1).toLowerCase() : "";
        MediaType type = PREVIEW_TYPES.get(ext);
        if (type == null) {
            return download(filename, name);
        }
        try {
            byte[] bytes = Files.readAllBytes(filePath);
            return ResponseEntity.ok()
                    .contentType(type)
                    .header("Content-Disposition", "inline; " + dispositionName(name, filename))
                    .contentLength(bytes.length)
                    .body(bytes);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /** 下载文件，?name= 传原始文件名则按原名保存 */
    @GetMapping("/download/{filename}")
    public ResponseEntity<byte[]> download(@PathVariable String filename,
                                           @RequestParam(required = false) String name) {
        Path filePath = fileService.getUploadDir().resolve(filename).normalize();
        if (!filePath.startsWith(fileService.getUploadDir()) || !Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }
        try {
            byte[] bytes = Files.readAllBytes(filePath);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .header("Content-Disposition", "attachment; " + dispositionName(name, filename))
                    .contentLength(bytes.length)
                    .body(bytes);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    /** 生成带原始文件名的 Content-Disposition 片段（RFC 5987，兼容中文名） */
    private String dispositionName(String name, String fallback) {
        String downloadName = (name != null && !name.trim().isEmpty()) ? name.trim() : fallback;
        downloadName = downloadName.replaceAll("[\\r\\n\"\\\\/]", "_");
        try {
            String encoded = URLEncoder.encode(downloadName, "UTF-8").replace("+", "%20");
            return "filename=\"" + encoded + "\"; filename*=UTF-8''" + encoded;
        } catch (IOException e) {
            return "filename=\"" + fallback + "\"";
        }
    }

    /** 查看图片 */
    @GetMapping("/view/{filename}")
    public ResponseEntity<byte[]> view(@PathVariable String filename) {
        Path filePath = fileService.getUploadDir().resolve(filename).normalize();
        if (!filePath.startsWith(fileService.getUploadDir()) || !Files.exists(filePath)) {
            return ResponseEntity.notFound().build();
        }
        try {
            byte[] bytes = Files.readAllBytes(filePath);
            String contentType = Files.probeContentType(filePath);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType != null ? contentType : "image/png"))
                    .contentLength(bytes.length)
                    .body(bytes);
        } catch (IOException e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
