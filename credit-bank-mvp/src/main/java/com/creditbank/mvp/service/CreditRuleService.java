package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.CreditRule;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.entity.TransactionLog;
import com.creditbank.mvp.mapper.CreditRuleMapper;
import com.creditbank.mvp.mapper.ProjectMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.mapper.TransactionLogMapper;
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
    private final TransactionLogMapper transactionLogMapper;
    private final SysUserMapper sysUserMapper;

    public CreditRuleService(CreditRuleMapper creditRuleMapper, ProjectMapper projectMapper,
                             TransactionLogMapper transactionLogMapper, SysUserMapper sysUserMapper) {
        this.creditRuleMapper = creditRuleMapper;
        this.projectMapper = projectMapper;
        this.transactionLogMapper = transactionLogMapper;
        this.sysUserMapper = sysUserMapper;
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
        Integer oldCreditValue = exist.getCreditValue();
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
        creditRuleMapper.updateById(rule);
        CreditRule updated = creditRuleMapper.selectById(rule.getId());

        // 若积分值发生变更，自动触发已存在流水的补差操作
        if (oldCreditValue != null && rule.getCreditValue() != null
                && !oldCreditValue.equals(rule.getCreditValue())) {
            adjustForRule(updated, oldCreditValue);
        }

        return updated;
    }

    @Transactional(rollbackFor = Exception.class)
    public int adjustForRule(CreditRule rule, Integer oldCreditValue) {
        int diff = rule.getCreditValue() - oldCreditValue;
        if (diff == 0) return 0;

        int adjustedCount = 0;

        // 查找该规则下所有 REWARD 类型的流水（在规则活动日期内）
        LambdaQueryWrapper<TransactionLog> wrapper = new LambdaQueryWrapper<TransactionLog>()
                .eq(TransactionLog::getBizType, "REWARD")
                .eq(TransactionLog::getRelatedRuleId, rule.getId());
        if (rule.getStartTime() != null) {
            wrapper.ge(TransactionLog::getCreatedAt, rule.getStartTime());
        }
        if (rule.getEndTime() != null) {
            wrapper.le(TransactionLog::getCreatedAt, rule.getEndTime());
        }

        List<TransactionLog> logs = transactionLogMapper.selectList(wrapper);

        for (TransactionLog log : logs) {
            // 跳过已被补差过的原流水
            long exists = transactionLogMapper.selectCount(
                    new LambdaQueryWrapper<TransactionLog>()
                            .eq(TransactionLog::getBizType, "ADMIN")
                            .eq(TransactionLog::getRelatedRuleId, log.getId())
                            .like(TransactionLog::getDescription, "规则自动补差")
            );
            if (exists > 0) continue;

            SysUser user = sysUserMapper.selectById(log.getUserId());
            if (user == null) continue;

            int newBalance = user.getBalance() + diff;
            // 余额不足则跳过
            if (newBalance < 0) continue;

            user.setBalance(newBalance);
            sysUserMapper.updateById(user);

            TransactionLog adjustLog = new TransactionLog();
            adjustLog.setUserId(log.getUserId());
            adjustLog.setAmount(diff);
            adjustLog.setBalanceAfter(newBalance);
            adjustLog.setBizType("ADMIN");
            adjustLog.setRelatedRuleId(log.getId());
            adjustLog.setDescription("规则自动补差：[" + rule.getEventName() + "]积分值由"
                    + oldCreditValue + "调整为" + rule.getCreditValue()
                    + "，补差" + (diff > 0 ? "+" : "") + diff);
            adjustLog.setCreatedAt(LocalDateTime.now());
            transactionLogMapper.insert(adjustLog);
            adjustedCount++;
        }

        return adjustedCount;
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
        creditRuleMapper.updateById(exist);
        return exist;
    }
}