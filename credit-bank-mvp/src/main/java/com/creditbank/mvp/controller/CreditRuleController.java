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
        List<CreditRule> rules;
        if (enabled != null && enabled) {
            rules = creditRuleService.listByEnabled(true);
        } else {
            rules = creditRuleService.list();
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
        checkAdmin();
        return Result.ok(creditRuleService.update(rule));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        checkAdmin();
        creditRuleService.delete(id);
        return Result.ok();
    }

    @PostMapping("/{id}/toggle")
    public Result<CreditRule> toggle(@PathVariable Long id, @RequestBody ToggleRequest request) {
        checkAdmin();
        return Result.ok(creditRuleService.toggleEnabled(id, request.getIsEnabled()));
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

    public static class ToggleRequest {
        private Integer isEnabled;

        public Integer getIsEnabled() {
            return isEnabled;
        }

        public void setIsEnabled(Integer isEnabled) {
            this.isEnabled = isEnabled;
        }
    }
}