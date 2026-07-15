package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.ExchangeRule;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.entity.TransactionLog;
import com.creditbank.mvp.entity.UserOpLog;
import com.creditbank.mvp.mapper.ExchangeRuleMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.mapper.TransactionLogMapper;
import com.creditbank.mvp.mapper.UserOpLogMapper;
import com.creditbank.mvp.util.CurrentUserUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ExchangeRuleService {

    private final ExchangeRuleMapper exchangeRuleMapper;
    private final SysUserMapper sysUserMapper;
    private final TransactionLogMapper transactionLogMapper;
    private final UserOpLogMapper userOpLogMapper;
    private final NotificationService notificationService;

    public ExchangeRuleService(ExchangeRuleMapper exchangeRuleMapper,
                               SysUserMapper sysUserMapper,
                               TransactionLogMapper transactionLogMapper,
                               UserOpLogMapper userOpLogMapper,
                               NotificationService notificationService) {
        this.exchangeRuleMapper = exchangeRuleMapper;
        this.sysUserMapper = sysUserMapper;
        this.transactionLogMapper = transactionLogMapper;
        this.userOpLogMapper = userOpLogMapper;
        this.notificationService = notificationService;
    }

    public List<ExchangeRule> list() {
        return exchangeRuleMapper.selectList(
                new LambdaQueryWrapper<ExchangeRule>().orderByDesc(ExchangeRule::getId));
    }

    @Transactional(rollbackFor = Exception.class)
    public ExchangeRule create(ExchangeRule rule) {
        if (rule.getItemName() == null || rule.getItemName().trim().isEmpty()) {
            throw new BizException("兑换品名称不能为空");
        }
        if (rule.getRequiredCredit() == null || rule.getRequiredCredit() <= 0) {
            throw new BizException("所需积分必须为正数");
        }
        if (rule.getStock() != null && rule.getStock() < 0) {
            throw new BizException("库存不能为负数");
        }
        SysUser operator = requireCurrentUser();
        if ("org_admin".equals(operator.getRole())) {
            // 机构管理员只能创建本机构的规则，归属以后端为准
            rule.setOrgId(operator.getOrgId());
        }
        rule.setId(null);
        if (rule.getIsEnabled() == null) {
            rule.setIsEnabled(1);
        }
        exchangeRuleMapper.insert(rule);
        if (rule.getIsEnabled() != null && rule.getIsEnabled() == 1) {
            notifyRulePublished(rule, operator, "CREATE");
        }
        return exchangeRuleMapper.selectById(rule.getId());
    }

    public ExchangeRule update(ExchangeRule rule) {
        ExchangeRule exist = exchangeRuleMapper.selectById(rule.getId());
        if (exist == null) {
            throw new BizException("兑换规则不存在：" + rule.getId());
        }
        SysUser operator = requireCurrentUser();
        checkRuleOwnership(exist, operator);
        if (rule.getRequiredCredit() != null && rule.getRequiredCredit() <= 0) {
            throw new BizException("所需积分必须为正数");
        }
        if (rule.getStock() != null && rule.getStock() < 0) {
            throw new BizException("库存不能为负数");
        }
        if ("org_admin".equals(operator.getRole())) {
            // 机构管理员不能把规则改挂到别的机构
            rule.setOrgId(exist.getOrgId());
        }
        exchangeRuleMapper.updateById(rule);
        return exchangeRuleMapper.selectById(rule.getId());
    }

    @Transactional(rollbackFor = Exception.class)
    public ExchangeRule toggle(Long id) {
        ExchangeRule exist = exchangeRuleMapper.selectById(id);
        if (exist == null) {
            throw new BizException("兑换规则不存在：" + id);
        }
        SysUser operator = requireCurrentUser();
        checkRuleOwnership(exist, operator);
        exist.setIsEnabled(exist.getIsEnabled() != null && exist.getIsEnabled() == 1 ? 0 : 1);
        exchangeRuleMapper.updateById(exist);
        if (exist.getIsEnabled() == 1) {
            notifyRulePublished(exist, operator, "ENABLE:" + System.currentTimeMillis());
        }
        return exist;
    }

    @Transactional(rollbackFor = Exception.class)
    public ExchangeRule exchange(Long userId, Long ruleId) {
        ExchangeRule rule = exchangeRuleMapper.selectById(ruleId);
        if (rule == null) {
            throw new BizException("兑换规则不存在：" + ruleId);
        }
        if (rule.getIsEnabled() == null || rule.getIsEnabled() != 1) {
            throw new BizException("该兑换品已停用");
        }

        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在：" + userId);
        }
        if (rule.getOrgId() != null && !rule.getOrgId().equals(user.getOrgId())) {
            throw new BizException("该兑换品仅限所属机构用户兑换");
        }

        if (rule.getPerUserLimit() != null && rule.getPerUserLimit() > 0) {
            Long exchangedCount = transactionLogMapper.selectCount(
                    new LambdaQueryWrapper<TransactionLog>()
                            .eq(TransactionLog::getUserId, userId)
                            .eq(TransactionLog::getBizType, "EXCHANGE")
                            .eq(TransactionLog::getRelatedRuleId, ruleId));
            if (exchangedCount >= rule.getPerUserLimit()) {
                throw new BizException("每人限兑换 " + rule.getPerUserLimit() + " 次");
            }
        }

        // 条件更新扣减，影响行数为 0 即余额/库存不足，避免并发超扣/超卖
        if (sysUserMapper.deductBalance(userId, rule.getRequiredCredit()) == 0) {
            throw new BizException("积分不足，当前余额：" + user.getBalance()
                    + "，需要：" + rule.getRequiredCredit());
        }
        if (rule.getStock() != null && exchangeRuleMapper.deductStock(ruleId) == 0) {
            throw new BizException("该兑换品库存不足");
        }

        Integer newBalance = sysUserMapper.selectById(userId).getBalance();

        TransactionLog txn = new TransactionLog();
        txn.setUserId(userId);
        txn.setAmount(-rule.getRequiredCredit());
        txn.setBalanceAfter(newBalance);
        txn.setBizType("EXCHANGE");
        txn.setRelatedRuleId(ruleId);
        txn.setDescription("兑换「" + rule.getItemName() + "」");
        transactionLogMapper.insert(txn);

        userOpLogMapper.insert(UserOpLog.createLog(
                userId, user.getRealName(), userId, user.getRealName(),
                UserOpLog.MODULE_POINT, "EXCHANGE",
                "用户「" + user.getRealName() + "」兑换「" + rule.getItemName() + "」，消耗 " + rule.getRequiredCredit() + " 积分，当前余额：" + newBalance));

        notificationService.sendToUser(
                "EXCHANGE_SUCCEEDED", NotificationService.CATEGORY_MALL, "SUCCESS",
                "商品兑换成功",
                "你已成功兑换“" + rule.getItemName() + "”，扣除 " + rule.getRequiredCredit()
                        + " 积分，当前余额 " + newBalance + "。",
                "/transactions", "TRANSACTION", txn.getId(),
                "EXCHANGE_SUCCEEDED:" + txn.getId(), userId, userId);

        return exchangeRuleMapper.selectById(ruleId);
    }

    // ---- 内部方法 ----

    private SysUser requireCurrentUser() {
        Long userId = CurrentUserUtil.getCurrentUserId();
        SysUser user = userId == null ? null : sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException("当前登录用户无效");
        }
        return user;
    }

    private void notifyRulePublished(ExchangeRule rule, SysUser operator, String version) {
        String content = "新商品“" + rule.getItemName() + "”已上架，兑换需要 "
                + rule.getRequiredCredit() + " 积分。";
        String dedupeKey = "EXCHANGE_RULE_PUBLISHED:" + rule.getId() + ":" + version;
        if (rule.getOrgId() == null) {
            notificationService.sendToRole(
                    "EXCHANGE_RULE_PUBLISHED", NotificationService.CATEGORY_MALL, "INFO",
                    "积分商城上新", content, "/point-mall", "EXCHANGE_RULE", rule.getId(),
                    dedupeKey, "student", operator.getId());
        } else {
            notificationService.sendToOrgStudents(
                    "EXCHANGE_RULE_PUBLISHED", NotificationService.CATEGORY_MALL, "INFO",
                    "本机构商品上新", content, "/point-mall", "EXCHANGE_RULE", rule.getId(),
                    dedupeKey, rule.getOrgId(), operator.getId());
        }
    }

    /** admin 可管理全部规则；org_admin 只能管理本机构规则，平台通用规则(orgId=NULL)不可动 */
    private void checkRuleOwnership(ExchangeRule rule, SysUser operator) {
        if ("admin".equals(operator.getRole())) {
            return;
        }
        if (rule.getOrgId() == null || !rule.getOrgId().equals(operator.getOrgId())) {
            throw new BizException("只能管理本机构的兑换规则");
        }
    }
}
