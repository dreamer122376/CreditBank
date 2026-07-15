package com.creditbank.mvp.controller;

import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.entity.CertAuditFlow;
import com.creditbank.mvp.entity.CertStandard;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.service.CertAuditFlowService;
import com.creditbank.mvp.service.CertStandardService;
import com.creditbank.mvp.service.UserService;
import com.creditbank.mvp.util.CurrentUserUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cert-audit-flow")
public class CertAuditFlowController {

    private final CertAuditFlowService certAuditFlowService;
    private final CertStandardService certStandardService;
    private final UserService userService;

    public CertAuditFlowController(CertAuditFlowService certAuditFlowService,
                                   CertStandardService certStandardService,
                                   UserService userService) {
        this.certAuditFlowService = certAuditFlowService;
        this.certStandardService = certStandardService;
        this.userService = userService;
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
        checkOrgAdminPermission(certStandardId);
        return Result.ok(certAuditFlowService.saveFlow(certStandardId, nodes));
    }

    /** 删除某标准的整个审批流程 */
    @DeleteMapping("/{certStandardId}")
    public Result<Void> delete(@PathVariable Long certStandardId) {
        checkOrgAdminPermission(certStandardId);
        certAuditFlowService.deleteFlow(certStandardId);
        return Result.ok(null);
    }

    private void checkOrgAdminPermission(Long certStandardId) {
        Long userId = CurrentUserUtil.getCurrentUserId();
        if (userId == null) {
            return;
        }
        SysUser user = userService.getUser(userId);
        if ("org_admin".equals(user.getRole())) {
            CertStandard standard = certStandardService.getById(certStandardId);
            if (standard == null || standard.getOrgId() == null || !standard.getOrgId().equals(user.getOrgId())) {
                throw new com.creditbank.mvp.common.BizException("无权限操作该认证标准的审批流程");
            }
        }
    }
}
