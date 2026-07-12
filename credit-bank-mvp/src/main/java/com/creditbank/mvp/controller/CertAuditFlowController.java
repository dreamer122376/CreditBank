package com.creditbank.mvp.controller;

import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.entity.CertAuditFlow;
import com.creditbank.mvp.service.CertAuditFlowService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cert-audit-flow")
public class CertAuditFlowController {

    private final CertAuditFlowService certAuditFlowService;

    public CertAuditFlowController(CertAuditFlowService certAuditFlowService) {
        this.certAuditFlowService = certAuditFlowService;
    }

    /** 查询某认证标准的完整审批链 */
    @GetMapping("/list/{certStandardId}")
    public Result<List<CertAuditFlow>> list(@PathVariable Long certStandardId) {
        return Result.ok(certAuditFlowService.listByStandard(certStandardId));
    }

    /** 保存（整体替换）某标准的审批流程 */
    @PostMapping("/save/{certStandardId}")
    public Result<List<CertAuditFlow>> save(@PathVariable Long certStandardId,
                                             @RequestBody List<CertAuditFlow> nodes) {
        return Result.ok(certAuditFlowService.saveFlow(certStandardId, nodes));
    }

    /** 删除某标准的整个审批流程 */
    @DeleteMapping("/{certStandardId}")
    public Result<Void> delete(@PathVariable Long certStandardId) {
        certAuditFlowService.deleteFlow(certStandardId);
        return Result.ok(null);
    }
}
