package com.creditbank.mvp.controller;

import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.entity.ExpertCert;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.service.ExpertCertService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expert-cert")
public class ExpertCertController {

    private final ExpertCertService expertCertService;

    public ExpertCertController(ExpertCertService expertCertService) {
        this.expertCertService = expertCertService;
    }

    @GetMapping("/list")
    public Result<List<ExpertCert>> list() {
        return Result.ok(expertCertService.listValid());
    }

    @GetMapping("/expert/{expertId}")
    public Result<List<ExpertCert>> byExpert(@PathVariable Long expertId) {
        return Result.ok(expertCertService.listByExpert(expertId));
    }

    @GetMapping("/certified-experts")
    public Result<List<SysUser>> certifiedExperts(@RequestParam Long certStandardId) {
        return Result.ok(expertCertService.listCertifiedExperts(certStandardId));
    }
}
