package com.creditbank.mvp.controller;

import com.creditbank.mvp.common.Result;
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
}
