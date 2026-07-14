package com.creditbank.mvp.controller;

import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.dto.OrganizationAuditResult;
import com.creditbank.mvp.entity.Organization;
import com.creditbank.mvp.service.OrganizationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/organization")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    @GetMapping("/list")
    public Result<List<Organization>> list() {
        return Result.ok(organizationService.list());
    }

    @PostMapping("/create")
    public Result<Organization> create(@RequestBody Organization org) {
        return Result.ok(organizationService.create(org));
    }

    @PostMapping("/update")
    public Result<Organization> update(@RequestBody Organization org) {
        return Result.ok(organizationService.update(org));
    }

    @PostMapping("/{id}/status")
    public Result<OrganizationAuditResult> changeStatus(@PathVariable Long id, @RequestBody StatusRequest req) {
        return Result.ok(organizationService.changeStatus(id, req.getStatus()));
    }

    @PostMapping("/{id}/reject")
    public Result<Void> reject(@PathVariable Long id, @RequestBody RejectRequest req) {
        organizationService.reject(id, req.getReason());
        return Result.ok(null);
    }

    public static class StatusRequest {
        private Integer status;

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }

    public static class RejectRequest {
        private String reason;

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }
}
