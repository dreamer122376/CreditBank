package com.creditbank.mvp.controller;

import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.entity.ConversionApplication;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.service.ConversionApplicationService;
import com.creditbank.mvp.util.CurrentUserUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversion-application")
public class ConversionApplicationController {

    private final ConversionApplicationService applicationService;
    private final SysUserMapper sysUserMapper;

    public ConversionApplicationController(ConversionApplicationService applicationService, SysUserMapper sysUserMapper) {
        this.applicationService = applicationService;
        this.sysUserMapper = sysUserMapper;
    }

    @GetMapping("/list")
    public Result<List<ConversionApplication>> list(@RequestParam(required = false) Integer status) {
        Long userId = CurrentUserUtil.getCurrentUserId();
        if (userId == null) {
            throw new BizException("未登录");
        }
        SysUser currentUser = sysUserMapper.selectById(userId);
        if (currentUser == null) {
            throw new BizException("用户不存在");
        }

        List<ConversionApplication> applications;
        if ("student".equals(currentUser.getRole())) {
            applications = applicationService.listByStudentId(userId);
        } else if ("org_admin".equals(currentUser.getRole())) {
            // org_admin 仅能查看本机构或通用转换申请
            Long orgId = currentUser.getOrgId();
            if (orgId == null) {
                throw new BizException("当前机构管理员未绑定机构");
            }
            applications = applicationService.listByOrgId(orgId, status);
        } else {
            // admin 查看全平台数据
            if (status != null) {
                applications = applicationService.listByStatus(status);
            } else {
                applications = applicationService.list();
            }
        }
        return Result.ok(applications);
    }

    @GetMapping("/{id}")
    public Result<ConversionApplication> detail(@PathVariable Long id) {
        Long userId = CurrentUserUtil.getCurrentUserId();
        if (userId == null) {
            throw new BizException("未登录");
        }
        SysUser currentUser = sysUserMapper.selectById(userId);
        if (currentUser == null) {
            throw new BizException("用户不存在");
        }

        ConversionApplication application = applicationService.getById(id);
        if (!"admin".equals(currentUser.getRole()) && !"org_admin".equals(currentUser.getRole())) {
            if (!userId.equals(application.getStudentId())) {
                throw new BizException("无权限查看该申请");
            }
        }
        return Result.ok(application);
    }

    @PostMapping("/submit")
    public Result<ConversionApplication> submit(@RequestBody ConversionApplication application) {
        Long userId = CurrentUserUtil.getCurrentUserId();
        if (userId == null) {
            throw new BizException("未登录");
        }
        SysUser currentUser = sysUserMapper.selectById(userId);
        if (currentUser == null) {
            throw new BizException("用户不存在");
        }
        if (!"student".equals(currentUser.getRole())) {
            throw new BizException("只有学生可以提交转换申请");
        }
        application.setStudentId(userId);
        return Result.ok(applicationService.submit(application));
    }

    @PostMapping("/{id}/audit")
    public Result<ConversionApplication> audit(@PathVariable Long id, @RequestBody AuditRequest request) {
        Long userId = CurrentUserUtil.getCurrentUserId();
        if (userId == null) {
            throw new BizException("未登录");
        }
        SysUser operator = sysUserMapper.selectById(userId);
        if (operator == null) {
            throw new BizException("用户不存在");
        }
        if (!"admin".equals(operator.getRole()) && !"org_admin".equals(operator.getRole())) {
            throw new BizException("无权限审核");
        }
        return Result.ok(applicationService.audit(id, request.isApprove(), request.getReason(), operator));
    }

    public static class AuditRequest {
        private boolean approve;
        private String reason;

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