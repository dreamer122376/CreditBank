package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.CreditRule;
import com.creditbank.mvp.mapper.CreditRuleMapper;
import com.creditbank.mvp.mapper.ProjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class CreditRuleService {

    public static final int STATUS_DISABLED = 0;
    public static final int STATUS_ENABLED = 1;

    private final CreditRuleMapper creditRuleMapper;
    private final ProjectMapper projectMapper;

    public CreditRuleService(CreditRuleMapper creditRuleMapper, ProjectMapper projectMapper) {
        this.creditRuleMapper = creditRuleMapper;
        this.projectMapper = projectMapper;
    }

    public List<CreditRule> list() {
        return creditRuleMapper.selectList(
                new LambdaQueryWrapper<CreditRule>().orderByDesc(CreditRule::getId));
    }

    public List<CreditRule> listByEnabled(boolean enabled) {
        return creditRuleMapper.selectList(
                new LambdaQueryWrapper<CreditRule>()
                        .eq(CreditRule::getIsEnabled, enabled ? STATUS_ENABLED : STATUS_DISABLED)
                        .orderByDesc(CreditRule::getId));
    }

    public CreditRule getById(Long id) {
        CreditRule rule = creditRuleMapper.selectById(id);
        if (rule == null) {
            throw new BizException("积分规则不存在：" + id);
        }
        return rule;
    }

    public List<CreditRule> listByProjectId(Long projectId) {
        return creditRuleMapper.selectList(
                new LambdaQueryWrapper<CreditRule>()
                        .eq(CreditRule::getProjectId, projectId)
                        .orderByDesc(CreditRule::getId));
    }

    public List<CreditRule> listByOrgId(Long orgId) {
        return creditRuleMapper.selectList(
                new LambdaQueryWrapper<CreditRule>()
                        .and(wrapper -> wrapper
                                .isNull(CreditRule::getProjectId)
                                .or()
                                .inSql(CreditRule::getProjectId, "SELECT id FROM project WHERE org_id = " + orgId))
                        .orderByDesc(CreditRule::getId));
    }

    @Transactional(rollbackFor = Exception.class)
    public CreditRule create(CreditRule rule) {
        if (rule.getEventCode() == null || rule.getEventCode().trim().isEmpty()) {
            throw new BizException("事件编码不能为空");
        }
        if (rule.getEventName() == null || rule.getEventName().trim().isEmpty()) {
            throw new BizException("事件名称不能为空");
        }
        if (rule.getCreditValue() == null || rule.getCreditValue() <= 0) {
            throw new BizException("积分值必须大于0");
        }
        if (rule.getProjectId() != null) {
            if (projectMapper.selectById(rule.getProjectId()) == null) {
                throw new BizException("项目不存在：" + rule.getProjectId());
            }
        }
        if (rule.getStartTime() != null && rule.getEndTime() != null) {
            if (rule.getStartTime().isAfter(rule.getEndTime())) {
                throw new BizException("开始时间不能晚于结束时间");
            }
        }
        rule.setId(null);
        rule.setCreatedAt(LocalDateTime.now());
        creditRuleMapper.insert(rule);
        return creditRuleMapper.selectById(rule.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public CreditRule update(CreditRule rule) {
        CreditRule exist = getById(rule.getId());
        if (rule.getCreditValue() != null && rule.getCreditValue() <= 0) {
            throw new BizException("积分值必须大于0");
        }
        if (rule.getProjectId() != null) {
            if (projectMapper.selectById(rule.getProjectId()) == null) {
                throw new BizException("项目不存在：" + rule.getProjectId());
            }
        }
        if (rule.getStartTime() != null && rule.getEndTime() != null) {
            if (rule.getStartTime().isAfter(rule.getEndTime())) {
                throw new BizException("开始时间不能晚于结束时间");
            }
        }
        rule.setCreatedAt(exist.getCreatedAt());
        rule.setUpdatedAt(LocalDateTime.now());
        creditRuleMapper.updateById(rule);
        return creditRuleMapper.selectById(rule.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        getById(id);
        creditRuleMapper.deleteById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public CreditRule toggleEnabled(Long id, Integer isEnabled) {
        CreditRule exist = getById(id);
        if (isEnabled == null || (isEnabled != STATUS_ENABLED && isEnabled != STATUS_DISABLED)) {
            throw new BizException("非法的状态值：" + isEnabled);
        }
        exist.setIsEnabled(isEnabled);
        exist.setUpdatedAt(LocalDateTime.now());
        creditRuleMapper.updateById(exist);
        return exist;
    }
}