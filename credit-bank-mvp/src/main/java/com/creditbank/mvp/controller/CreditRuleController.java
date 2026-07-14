package com.creditbank.mvp.controller;

import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.entity.CreditRule;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.service.CreditRuleService;
import com.creditbank.mvp.util.CurrentUserUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/credit-rule")
public class CreditRuleController {

    private final CreditRuleService creditRuleService;
    private final SysUserMapper sysUserMapper;

    public CreditRuleController(CreditRuleService creditRuleService, SysUserMapper sysUserMapper) {
        this.creditRuleService = creditRuleService;
        this.sysUserMapper = sysUserMapper;
    }

    @GetMapping("/list")
    public Result<List<CreditRule>> list(@RequestParam(required = false) Boolean enabled) {
        Long userId = CurrentUserUtil.getCurrentUserId();
        if (userId == null) {
            throw new BizException("未登录");
        }
        SysUser currentUser = sysUserMapper.selectById(userId);
        if (currentUser == null) {
            throw new BizException("用户不存在");
        }

        List<CreditRule> rules;
        if ("admin".equals(currentUser.getRole())) {
            if (enabled != null && enabled) {
                rules = creditRuleService.listByEnabled(true);
            } else {
                rules = creditRuleService.list();
            }
        } else {
            Long orgId = currentUser.getOrgId();
            if (orgId == null) {
                rules = creditRuleService.listByEnabled(true);
            } else {
                rules = creditRuleService.listByOrgId(orgId);
            }
        }
        return Result.ok(rules);
    }

    @GetMapping("/{id}")
    public Result<CreditRule> detail(@PathVariable Long id) {
        return Result.ok(creditRuleService.getById(id));
    }

    @PostMapping("/create")
    public Result<CreditRule> create(@RequestBody CreditRule rule) {
        checkAdmin();
        return Result.ok(creditRuleService.create(rule));
    }

    @PostMapping("/update")
    public Result<CreditRule> update(@RequestBody CreditRule rule) {
        checkRulePermission(rule.getId());
        return Result.ok(creditRuleService.update(rule));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        checkRulePermission(id);
        creditRuleService.delete(id);
        return Result.ok();
    }

    @PostMapping("/{id}/toggle")
    public Result<CreditRule> toggle(@PathVariable Long id, @RequestBody ToggleRequest request) {
        checkRulePermission(id);
        return Result.ok(creditRuleService.toggleEnabled(id, request.getIsEnabled()));
    }

    @PostMapping("/{id}/adjust")
    public Result<AdjustResult> adjust(@PathVariable Long id) {
        checkAdmin();
        CreditRule rule = creditRuleService.getById(id);
        int count = creditRuleService.adjustForRule(rule);
        return Result.ok(new AdjustResult(count));
    }

    private void checkAdmin() {
        Long operatorId = CurrentUserUtil.getCurrentUserId();
        if (operatorId == null) {
            throw new BizException("未登录");
        }
        SysUser operator = sysUserMapper.selectById(operatorId);
        if (operator == null || !"admin".equals(operator.getRole())) {
            throw new BizException("无权限");
        }
    }

    private void checkRulePermission(Long ruleId) {
        Long operatorId = CurrentUserUtil.getCurrentUserId();
        if (operatorId == null) {
            throw new BizException("未登录");
        }
        SysUser operator = sysUserMapper.selectById(operatorId);
        if (operator == null) {
            throw new BizException("用户不存在");
        }
        if ("admin".equals(operator.getRole())) {
            return;
        }
        CreditRule rule = creditRuleService.getById(ruleId);
        if (rule.getOrgId() == null) {
            throw new BizException("无权限操作通用规则");
        }
        if (!rule.getOrgId().equals(operator.getOrgId())) {
            throw new BizException("无权限操作其他机构的规则");
        }
    }

    public static class ToggleRequest {
        private Integer isEnabled;

        public Integer getIsEnabled() {
            return isEnabled;
        }

        public void setIsEnabled(Integer isEnabled) {
            this.isEnabled = isEnabled;
        }
    }

    public static class AdjustResult {
        private int adjustedCount;

        public AdjustResult(int adjustedCount) {
            this.adjustedCount = adjustedCount;
        }

        public int getAdjustedCount() {
            return adjustedCount;
        }

        public void setAdjustedCount(int adjustedCount) {
            this.adjustedCount = adjustedCount;
        }
    }
}