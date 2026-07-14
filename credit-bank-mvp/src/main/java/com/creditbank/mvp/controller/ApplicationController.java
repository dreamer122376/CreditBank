package com.creditbank.mvp.controller;

import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.dto.ApplicationDetailDTO;
import com.creditbank.mvp.entity.Application;
import com.creditbank.mvp.entity.ApplicationAuditLog;
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
        return Result.ok(applicationService.audit(id, req.getRole(), req.getUserId(), req.isApprove(), req.getReason()));
    }

    @PostMapping("/{id}/resubmit")
    public Result<Application> resubmit(@PathVariable Long id, @RequestBody ResubmitRequest req) {
        return Result.ok(applicationService.resubmit(id, req.getUserId(), req.getFormData()));
    }

    @GetMapping("/{id}/audit-logs")
    public Result<List<ApplicationAuditLog>> getAuditLogs(@PathVariable Long id) {
        return Result.ok(applicationService.getAuditLogs(id));
    }

    public static class AuditRequest {
        private String role;
        private Long userId;
        private boolean approve;
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

    public static class ResubmitRequest {
        private Long userId;
        private String formData;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getFormData() { return formData; }
        public void setFormData(String formData) { this.formData = formData; }
    }

    /** 公开接口：查询机构入驻申请状态 */
    @GetMapping("/org-register-status")
    public Result<java.util.Map<String, Object>> queryOrgRegisterStatus(
            @RequestParam String orgName,
            @RequestParam String applicantName) {
        return Result.ok(applicationService.queryOrgRegisterStatus(orgName, applicantName));
    }
}
