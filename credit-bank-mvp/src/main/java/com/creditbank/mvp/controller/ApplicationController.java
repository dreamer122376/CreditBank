package com.creditbank.mvp.controller;

import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.dto.ApplicationDetailDTO;
import com.creditbank.mvp.entity.Application;
import com.creditbank.mvp.service.ApplicationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/application")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @GetMapping("/list")
    public Result<List<ApplicationDetailDTO>> list(@RequestParam(required = false) String role,
                                                   @RequestParam(required = false) Long userId) {
        return Result.ok(applicationService.list(role, userId));
    }

    @PostMapping("/submit")
    public Result<Application> submit(@RequestBody Application app) {
        return Result.ok(applicationService.submit(app));
    }

    @PostMapping("/{id}/audit")
    public Result<Application> audit(@PathVariable Long id, @RequestBody AuditRequest req) {
        return Result.ok(applicationService.audit(id, req.getRole(), req.isApprove(), req.getReason()));
    }

    public static class AuditRequest {
        private String role;
        private boolean approve;
        private String reason;

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public boolean isApprove() {
            return approve;
        }

        public void setApprove(boolean approve) {
            this.approve = approve;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
