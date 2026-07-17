package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.CreditRule;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.entity.TransactionLog;
import com.creditbank.mvp.entity.UserOpLog;
import com.creditbank.mvp.mapper.CreditRuleMapper;
import com.creditbank.mvp.mapper.ProjectMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.mapper.TransactionLogMapper;
import com.creditbank.mvp.mapper.UserOpLogMapper;
import com.creditbank.mvp.util.CurrentUserUtil;
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
    private final UserOpLogMapper userOpLogMapper;

    public CreditRuleService(CreditRuleMapper creditRuleMapper, ProjectMapper projectMapper,
                             TransactionLogMapper transactionLogMapper, SysUserMapper sysUserMapper,
                             UserOpLogMapper userOpLogMapper) {
        this.creditRuleMapper = creditRuleMapper;
        this.projectMapper = projectMapper;
        this.transactionLogMapper = transactionLogMapper;
        this.sysUserMapper = sysUserMapper;
        this.userOpLogMapper = userOpLogMapper;
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
                                .isNull(CreditRule::getOrgId)
                                .or()
                                .eq(CreditRule::getOrgId, orgId))
                        .orderByDesc(CreditRule::getId));
    }

    /**
     * 创建积分规则，自动写入操作日志
     */
    @Transactional(rollbackFor = Exception.class)
    public CreditRule create(CreditRule rule) {
        if (rule.getEventCode() == null || rule.getEventCode().trim().isEmpty()) {
            rule.setEventCode("/");
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
        CreditRule saved = creditRuleMapper.selectById(rule.getId());
        SysUser operator = currentOperator();
        // 操作日志：创建积分规则
        if (operator != null) {
            userOpLogMapper.insert(UserOpLog.createLog(
                    operator.getId(), operator.getRealName(),
                    operator.getId(), operator.getRealName(),
                    UserOpLog.MODULE_CREDIT_RULE, UserOpLog.ACTION_CREATE,
                    "创建积分规则：" + buildRuleSummary(saved)));
        }
        return saved;
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
        creditRuleMapper.updateById(rule);
        CreditRule saved = creditRuleMapper.selectById(rule.getId());
        SysUser operator = currentOperator();
        // 操作日志：更新积分规则
        if (operator != null) {
            userOpLogMapper.insert(UserOpLog.createLog(
                    operator.getId(), operator.getRealName(),
                    operator.getId(), operator.getRealName(),
                    UserOpLog.MODULE_CREDIT_RULE, UserOpLog.ACTION_UPDATE,
                    "更新积分规则：" + buildRuleSummary(saved)));
        }
        return saved;
    }

    @Transactional(rollbackFor = Exception.class)
    public int adjustForRule(CreditRule rule) {
        int adjustedCount = 0;
        SysUser operator = currentOperator();
        Long startTime = System.currentTimeMillis();

        LambdaQueryWrapper<TransactionLog> rewardWrapper = new LambdaQueryWrapper<TransactionLog>()
                .eq(TransactionLog::getBizType, "REWARD")
                .eq(TransactionLog::getRelatedRuleId, rule.getId());
        if (rule.getStartTime() != null) {
            rewardWrapper.ge(TransactionLog::getCreatedAt, rule.getStartTime());
        }
        if (rule.getEndTime() != null) {
            rewardWrapper.le(TransactionLog::getCreatedAt, rule.getEndTime());
        }
        List<TransactionLog> rewardLogs = transactionLogMapper.selectList(rewardWrapper);

        List<TransactionLog> allLogs = new java.util.ArrayList<>(rewardLogs);

        if (!rewardLogs.isEmpty()) {
            List<Long> rewardIds = rewardLogs.stream().map(TransactionLog::getId).collect(java.util.stream.Collectors.toList());
            List<TransactionLog> attachmentLogs = transactionLogMapper.selectList(
                    new LambdaQueryWrapper<TransactionLog>()
                            .eq(TransactionLog::getBizType, "ATTACHMENT")
                            .in(TransactionLog::getRelatedRuleId, rewardIds)
            );
            allLogs.addAll(attachmentLogs);
        }

        for (TransactionLog log : allLogs) {
            List<TransactionLog> existingAdjusts = transactionLogMapper.selectList(
                    new LambdaQueryWrapper<TransactionLog>()
                            .eq(TransactionLog::getBizType, "UPDATE_ADJUST")
                            .eq(TransactionLog::getRelatedRuleId, log.getId())
            );

            int existingAdjustAmount = existingAdjusts.stream()
                    .mapToInt(TransactionLog::getAmount)
                    .sum();

            int currentTotal = log.getAmount() + existingAdjustAmount;

            int targetAmount = "REWARD".equals(log.getBizType())
                    ? rule.getCreditValue()
                    : -rule.getCreditValue();

            int adjustAmount = targetAmount - currentTotal;

            if (adjustAmount == 0) continue;

            SysUser user = sysUserMapper.selectById(log.getUserId());
            if (user == null) continue;

            int newBalance = user.getBalance() + adjustAmount;
            if (newBalance < 0) continue;

            user.setBalance(newBalance);
            sysUserMapper.updateById(user);

            TransactionLog adjustLog = new TransactionLog();
            adjustLog.setUserId(log.getUserId());
            adjustLog.setAmount(adjustAmount);
            adjustLog.setBalanceAfter(newBalance);
            adjustLog.setBizType("UPDATE_ADJUST");
            adjustLog.setRelatedRuleId(log.getId());
            adjustLog.setDescription("规则更新补差：[" + rule.getEventName() + "]，补差" + (adjustAmount > 0 ? "+" : "") + adjustAmount);
            adjustLog.setCreatedAt(java.time.LocalDateTime.now());
            transactionLogMapper.insert(adjustLog);
            adjustedCount++;
        }

        // 操作日志：批量调账（积分规则追溯补差）
        if (operator != null) {
            long duration = System.currentTimeMillis() - startTime;
            userOpLogMapper.insert(UserOpLog.createLog(
                    operator.getId(), operator.getRealName(),
                    operator.getId(), operator.getRealName(),
                    UserOpLog.MODULE_CREDIT_RULE, UserOpLog.ACTION_BATCH_ADJUST,
                    "批量调账（积分规则追溯补差）：" + buildRuleSummary(rule)
                            + " · 影响记录数：" + adjustedCount
                            + " · 耗时：" + duration + "ms"));
        }
        return adjustedCount;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id) {
        CreditRule rule = getById(id);
        creditRuleMapper.deleteById(id);
        SysUser operator = currentOperator();
        // 操作日志：删除积分规则
        if (operator != null) {
            userOpLogMapper.insert(UserOpLog.createLog(
                    operator.getId(), operator.getRealName(),
                    operator.getId(), operator.getRealName(),
                    UserOpLog.MODULE_CREDIT_RULE, UserOpLog.ACTION_DELETE,
                    "删除积分规则：" + buildRuleSummary(rule)));
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public CreditRule toggleEnabled(Long id, Integer isEnabled) {
        CreditRule exist = getById(id);
        if (isEnabled == null || (isEnabled != STATUS_ENABLED && isEnabled != STATUS_DISABLED)) {
            throw new BizException("非法的状态值：" + isEnabled);
        }
        boolean wasEnabled = exist.getIsEnabled() != null && exist.getIsEnabled() == STATUS_ENABLED;
        exist.setIsEnabled(isEnabled);
        creditRuleMapper.updateById(exist);
        SysUser operator = currentOperator();
        // 操作日志：启用/停用积分规则
        if (operator != null) {
            userOpLogMapper.insert(UserOpLog.createLog(
                    operator.getId(), operator.getRealName(),
                    operator.getId(), operator.getRealName(),
                    UserOpLog.MODULE_CREDIT_RULE, UserOpLog.ACTION_RULE_TOGGLE,
                    (wasEnabled ? "停用积分规则：" : "启用积分规则：") + buildRuleSummary(exist)));
        }
        return exist;
    }

    // --- 内部辅助方法 ---

    /** 从请求上下文中获取当前操作人 */
    private SysUser currentOperator() {
        Long userId = CurrentUserUtil.getCurrentUserId();
        return userId == null ? null : sysUserMapper.selectById(userId);
    }

    /** 构造积分规则的简要概述（用于操作日志的详情字段） */
    private String buildRuleSummary(CreditRule rule) {
        if (rule == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("#").append(rule.getId());
        if (rule.getEventName() != null) {
            sb.append(" · 事件：").append(rule.getEventName());
        }
        if (rule.getCreditValue() != null) {
            sb.append(" · 积分值：+").append(rule.getCreditValue());
        }
        if (rule.getEventCode() != null && !rule.getEventCode().trim().isEmpty() && !"/".equals(rule.getEventCode().trim())) {
            sb.append(" · 编码：").append(rule.getEventCode());
        }
        if (rule.getIsEnabled() != null) {
            sb.append(" · ").append(rule.getIsEnabled() == STATUS_ENABLED ? "启用中" : "已停用");
        }
        return sb.toString();
    }
}