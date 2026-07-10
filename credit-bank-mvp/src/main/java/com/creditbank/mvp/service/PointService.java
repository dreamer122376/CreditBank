package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.Account;
import com.creditbank.mvp.entity.PointRule;
import com.creditbank.mvp.entity.PointTransaction;
import com.creditbank.mvp.mapper.AccountMapper;
import com.creditbank.mvp.mapper.PointRuleMapper;
import com.creditbank.mvp.mapper.PointTransactionMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 积分业务的核心。整个系统最重要的方法就是下面的 earn()。
 * （真实项目里通常再拆成接口 + 实现类，本骨架为了精简合成一个类。）
 */
@Service
public class PointService {

    private final AccountMapper accountMapper;
    private final PointRuleMapper pointRuleMapper;
    private final PointTransactionMapper pointTransactionMapper;

    // 构造器注入：清晰、便于测试，优于字段注入
    public PointService(AccountMapper accountMapper,
                        PointRuleMapper pointRuleMapper,
                        PointTransactionMapper pointTransactionMapper) {
        this.accountMapper = accountMapper;
        this.pointRuleMapper = pointRuleMapper;
        this.pointTransactionMapper = pointTransactionMapper;
    }

    /**
     * 加分：完成一件事 -> 按规则算分 -> 原子地更新余额 + 写流水。
     *
     * @Transactional：更新余额和插入流水必须"要么都成功、要么都回滚"，
     *   否则会出现"扣了余额却没流水"或反之的账实不符。这是账本的生命线。
     */
    @Transactional(rollbackFor = Exception.class)
    public Account earn(Long accountId, String ruleCode) {
        // 1. 校验账户
        Account account = accountMapper.selectById(accountId);
        if (account == null) {
            throw new BizException("账户不存在：" + accountId);
        }

        // 2. 查规则（数据驱动：分值来自规则表，不是写死在代码里）
        PointRule rule = pointRuleMapper.selectOne(
                new LambdaQueryWrapper<PointRule>()
                        .eq(PointRule::getRuleCode, ruleCode)
                        .eq(PointRule::getEnabled, 1));
        if (rule == null) {
            throw new BizException("积分规则不存在或已停用：" + ruleCode);
        }

        // 3. 计算新余额并更新账户（乐观锁：@Version 会自动带上 version 条件）
        BigDecimal newBalance = account.getBalance().add(rule.getPoints());
        account.setBalance(newBalance);
        int rows = accountMapper.updateById(account);
        if (rows == 0) {
            // 更新影响行数为 0，说明 version 已被别的请求改过 -> 并发冲突
            throw new BizException("账户被并发修改，请重试");
        }

        // 4. 写一条不可修改的流水
        PointTransaction txn = new PointTransaction();
        txn.setAccountId(accountId);
        txn.setRuleCode(ruleCode);
        txn.setChangeAmount(rule.getPoints());
        txn.setBalanceAfter(newBalance);
        txn.setRemark(rule.getRuleName());
        pointTransactionMapper.insert(txn);

        // 返回最新账户信息
        return accountMapper.selectById(accountId);
    }

    public Account getAccount(Long id) {
        Account account = accountMapper.selectById(id);
        if (account == null) {
            throw new BizException("账户不存在：" + id);
        }
        return account;
    }

    public List<Account> listAccounts() {
        return accountMapper.selectList(null);
    }

    public List<PointTransaction> listTransactions(Long accountId) {
        return pointTransactionMapper.selectList(
                new LambdaQueryWrapper<PointTransaction>()
                        .eq(PointTransaction::getAccountId, accountId)
                        .orderByDesc(PointTransaction::getId));
    }

    public List<PointRule> listRules() {
        return pointRuleMapper.selectList(
                new LambdaQueryWrapper<PointRule>().eq(PointRule::getEnabled, 1));
    }
}
