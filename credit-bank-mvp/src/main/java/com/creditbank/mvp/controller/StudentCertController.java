package com.creditbank.mvp.controller;

import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.dto.StudentCertVerifyDTO;
import com.creditbank.mvp.entity.StudentCert;
import com.creditbank.mvp.service.StudentCertService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student-cert")
public class StudentCertController {

    private final StudentCertService studentCertService;

    public StudentCertController(StudentCertService studentCertService) {
        this.studentCertService = studentCertService;
    }

    @GetMapping("/student/{studentId}")
    public Result<List<StudentCert>> byStudent(@PathVariable Long studentId,
                                               @RequestParam(required = false) String role,
                                               @RequestParam(required = false) Long userId) {
        return Result.ok(studentCertService.listByStudent(studentId, role, userId));
    }

    @GetMapping("/{id}")
    public Result<StudentCert> get(@PathVariable Long id,
                                   @RequestParam(required = false) String role,
                                   @RequestParam(required = false) Long userId) {
        return Result.ok(studentCertService.getById(id, role, userId));
    }

    @GetMapping("/application/{applicationId}")
    public Result<StudentCert> byApplication(@PathVariable Long applicationId,
                                             @RequestParam(required = false) String role,
                                             @RequestParam(required = false) Long userId) {
        return Result.ok(studentCertService.getByApplication(applicationId, role, userId));
    }

    @PostMapping("/{id}/revoke")
    public Result<StudentCert> revoke(@PathVariable Long id, @RequestBody RevokeRequest request) {
        return Result.ok(studentCertService.revoke(id, request.getRole(), request.getUserId(), request.getReason()));
    }

    @GetMapping("/verify")
    public Result<StudentCertVerifyDTO> verify(@RequestParam String certNo,
                                               @RequestParam String verifyCode) {
        return Result.ok(studentCertService.verify(certNo, verifyCode));
    }

    public static class RevokeRequest {
        private String role;
        private Long userId;
        private String reason;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
