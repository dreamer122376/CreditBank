package com.creditbank.mvp.controller;

import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.entity.ConversionRule;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.service.ConversionRuleService;
import com.creditbank.mvp.util.CurrentUserUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/conversion-rule")
public class ConversionRuleController {

    private final ConversionRuleService conversionRuleService;
    private final SysUserMapper sysUserMapper;

    public ConversionRuleController(ConversionRuleService conversionRuleService, SysUserMapper sysUserMapper) {
        this.conversionRuleService = conversionRuleService;
        this.sysUserMapper = sysUserMapper;
    }

    @GetMapping("/list")
    public Result<List<ConversionRule>> list(@RequestParam(required = false) Boolean enabled) {
        Long userId = CurrentUserUtil.getCurrentUserId();
        if (userId == null) {
            throw new BizException("未登录");
        }
        SysUser currentUser = sysUserMapper.selectById(userId);
        if (currentUser == null) {
            throw new BizException("用户不存在");
        }

        List<ConversionRule> rules;
        if ("admin".equals(currentUser.getRole())) {
            if (enabled != null && enabled) {
                rules = conversionRuleService.listByEnabled(true);
            } else {
                rules = conversionRuleService.list();
            }
        } else {
            Long orgId = currentUser.getOrgId();
            if (orgId == null) {
                rules = conversionRuleService.listByEnabled(true);
            } else {
                rules = conversionRuleService.listByOrgId(orgId);
            }
        }
        return Result.ok(rules);
    }

    @GetMapping("/{id}")
    public Result<ConversionRule> detail(@PathVariable Long id) {
        return Result.ok(conversionRuleService.getById(id));
    }

    @PostMapping("/create")
    public Result<ConversionRule> create(@RequestBody ConversionRule rule) {
        Long operatorId = CurrentUserUtil.getCurrentUserId();
        if (operatorId == null) {
            throw new BizException("未登录");
        }
        SysUser operator = sysUserMapper.selectById(operatorId);
        if (operator == null) {
            throw new BizException("用户不存在");
        }
        if (!"admin".equals(operator.getRole()) && !"org_admin".equals(operator.getRole())) {
            throw new BizException("无权限");
        }
        if ("org_admin".equals(operator.getRole())) {
            rule.setConvertedOrgId(operator.getOrgId());
        }
        rule.setCreatedBy(operatorId);
        return Result.ok(conversionRuleService.create(rule));
    }

    @PostMapping("/update")
    public Result<ConversionRule> update(@RequestBody ConversionRule rule) {
        checkRulePermission(rule.getId());
        return Result.ok(conversionRuleService.update(rule));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        checkRulePermission(id);
        conversionRuleService.delete(id);
        return Result.ok();
    }

    @PostMapping("/{id}/toggle")
    public Result<ConversionRule> toggle(@PathVariable Long id, @RequestBody ToggleRequest request) {
        checkRulePermission(id);
        return Result.ok(conversionRuleService.toggleEnabled(id, request.getIsEnabled()));
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
        ConversionRule rule = conversionRuleService.getById(ruleId);
        if (rule.getConvertedOrgId() == null) {
            throw new BizException("无权限操作通用规则");
        }
        if (!rule.getConvertedOrgId().equals(operator.getOrgId())) {
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
}