package com.creditbank.mvp.controller;

import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.entity.CertStandard;
import com.creditbank.mvp.service.CertStandardService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cert-standard")
public class CertStandardController {

    private final CertStandardService certStandardService;

    public CertStandardController(CertStandardService certStandardService) {
        this.certStandardService = certStandardService;
    }

    @GetMapping("/list")
    public Result<List<CertStandard>> list() {
        return Result.ok(certStandardService.list());
    }

    @GetMapping("/{id}")
    public Result<CertStandard> getById(@PathVariable Long id) {
        return Result.ok(certStandardService.getById(id));
    }

    @PostMapping("/create")
    public Result<CertStandard> create(@RequestBody CertStandard standard) {
        return Result.ok(certStandardService.create(standard));
    }

    @PostMapping("/update")
    public Result<CertStandard> update(@RequestBody CertStandard standard) {
        return Result.ok(certStandardService.update(standard));
    }

    @PostMapping("/{id}/toggle")
    public Result<CertStandard> toggle(@PathVariable Long id) {
        return Result.ok(certStandardService.toggle(id));
    }
}
