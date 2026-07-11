package com.creditbank.mvp.service;

import com.creditbank.mvp.common.BizException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * 图片上传服务。
 * 文件保存到 {user.dir}/uploads/ 目录，
 * 通过 FileController.view() 对外提供访问。
 */
@Service
public class FileService {

    private static final List<String> ALLOWED_TYPES = Arrays.asList(
            "image/jpeg", "image/png", "image/gif", "image/webp");
    private static final long MAX_SIZE = 5 * 1024 * 1024;

    /** 证明材料：图片 + PDF + Word，供认证申请等场景上传 */
    private static final List<String> ATTACHMENT_EXTS = Arrays.asList(
            ".jpg", ".jpeg", ".png", ".gif", ".webp", ".pdf", ".doc", ".docx");
    private static final long ATTACHMENT_MAX_SIZE = 10 * 1024 * 1024;

    private final Path uploadDir;

    public FileService() {
        this.uploadDir = Paths.get(System.getProperty("user.dir"), "uploads").toAbsolutePath();
        try {
            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }
        } catch (IOException e) {
            throw new RuntimeException("无法创建上传目录：" + uploadDir, e);
        }
    }

    /** 获取上传目录 */
    public Path getUploadDir() {
        return uploadDir;
    }

    /**
     * 上传图片，返回访问路径 /api/files/view/{filename}
     */
    public String upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择要上传的文件");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new BizException("文件大小不能超过5MB");
        }
        String contentType = file.getContentType();
        if (!ALLOWED_TYPES.contains(contentType)) {
            throw new BizException("仅支持 JPG、PNG、GIF、WebP 格式");
        }

        try {
            String originalName = file.getOriginalFilename();
            String ext = "";
            if (originalName != null && originalName.contains(".")) {
                ext = originalName.substring(originalName.lastIndexOf("."));
            }
            String newFileName = UUID.randomUUID().toString().replace("-", "") + ext;

            Path targetPath = uploadDir.resolve(newFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

            // 返回 Controller 端点路径，而非静态资源路径
            return "/api/files/view/" + newFileName;
        } catch (IOException e) {
            throw new BizException("文件上传失败：" + e.getMessage());
        }
    }

    /**
     * 上传证明材料（图片/PDF/Word），按扩展名白名单校验，
     * 返回下载路径 /api/files/download/{filename}
     */
    public String uploadAttachment(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BizException("请选择要上传的文件");
        }
        if (file.getSize() > ATTACHMENT_MAX_SIZE) {
            throw new BizException("文件大小不能超过10MB");
        }
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        }
        if (!ATTACHMENT_EXTS.contains(ext)) {
            throw new BizException("仅支持 PDF、Word、JPG、PNG、GIF、WebP 格式");
        }

        try {
            String newFileName = UUID.randomUUID().toString().replace("-", "") + ext;
            Path targetPath = uploadDir.resolve(newFileName);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            return "/api/files/download/" + newFileName;
        } catch (IOException e) {
            throw new BizException("文件上传失败：" + e.getMessage());
        }
    }
}
