package com.creditbank.mvp.controller;

import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.entity.CertStandard;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.service.CertStandardService;
import com.creditbank.mvp.service.UserService;
import com.creditbank.mvp.util.CurrentUserUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cert-standard")
public class CertStandardController {

    private final CertStandardService certStandardService;
    private final UserService userService;

    public CertStandardController(CertStandardService certStandardService, UserService userService) {
        this.certStandardService = certStandardService;
        this.userService = userService;
    }

    @GetMapping("/list")
    public Result<List<CertStandard>> list() {
        Long userId = CurrentUserUtil.getCurrentUserId();
        if (userId != null) {
            SysUser user = userService.getUser(userId);
            if ("org_admin".equals(user.getRole()) && user.getOrgId() != null) {
                return Result.ok(certStandardService.listByOrg(user.getOrgId()));
            }
        }
        return Result.ok(certStandardService.list());
    }

    @GetMapping("/{id}")
    public Result<CertStandard> getById(@PathVariable Long id) {
        return Result.ok(certStandardService.getById(id));
    }

    @PostMapping("/create")
    public Result<CertStandard> create(@RequestBody CertStandard standard) {
        Long userId = CurrentUserUtil.getCurrentUserId();
        if (userId != null) {
            SysUser user = userService.getUser(userId);
            if ("org_admin".equals(user.getRole())) {
                standard.setOrgId(user.getOrgId());
            }
        }
        return Result.ok(certStandardService.create(standard));
    }

    @PostMapping("/update")
    public Result<CertStandard> update(@RequestBody CertStandard standard) {
        Long userId = CurrentUserUtil.getCurrentUserId();
        if (userId != null) {
            SysUser user = userService.getUser(userId);
            if ("org_admin".equals(user.getRole())) {
                CertStandard exist = certStandardService.getById(standard.getId());
                if (exist == null || exist.getOrgId() == null || !exist.getOrgId().equals(user.getOrgId())) {
                    throw new com.creditbank.mvp.common.BizException("无权限编辑该认证标准");
                }
                standard.setOrgId(user.getOrgId());
            }
        }
        return Result.ok(certStandardService.update(standard));
    }

    @PostMapping("/{id}/toggle")
    public Result<CertStandard> toggle(@PathVariable Long id) {
        Long userId = CurrentUserUtil.getCurrentUserId();
        if (userId != null) {
            SysUser user = userService.getUser(userId);
            if ("org_admin".equals(user.getRole())) {
                CertStandard exist = certStandardService.getById(id);
                if (exist == null || exist.getOrgId() == null || !exist.getOrgId().equals(user.getOrgId())) {
                    throw new com.creditbank.mvp.common.BizException("无权限操作该认证标准");
                }
            }
        }
        return Result.ok(certStandardService.toggle(id));
    }
}
