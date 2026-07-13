package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.entity.TransactionLog;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.mapper.TransactionLogMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionLogService {

    private final TransactionLogMapper transactionLogMapper;
    private final SysUserMapper sysUserMapper;

    public TransactionLogService(TransactionLogMapper transactionLogMapper, SysUserMapper sysUserMapper) {
        this.transactionLogMapper = transactionLogMapper;
        this.sysUserMapper = sysUserMapper;
    }

    public Page<TransactionLog> page(Long operatorId, int pageNum, int pageSize, Long userId, String bizType,
                                      LocalDateTime startTime, LocalDateTime endTime) {
        SysUser operator = getOperator(operatorId);
        LambdaQueryWrapper<TransactionLog> wrapper = buildWrapper(operator, userId, bizType, startTime, endTime);
        wrapper.orderByDesc(TransactionLog::getCreatedAt);
        Page<TransactionLog> result = transactionLogMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        markReverted(result.getRecords());
        return result;
    }

    public List<TransactionLog> list(Long operatorId, Long userId, String bizType,
                                      LocalDateTime startTime, LocalDateTime endTime) {
        SysUser operator = getOperator(operatorId);
        LambdaQueryWrapper<TransactionLog> wrapper = buildWrapper(operator, userId, bizType, startTime, endTime);
        wrapper.orderByDesc(TransactionLog::getCreatedAt);
        List<TransactionLog> records = transactionLogMapper.selectList(wrapper);
        markReverted(records);
        return records;
    }

    private void markReverted(List<TransactionLog> records) {
        if (records == null || records.isEmpty()) {
            return;
        }
        List<Long> ids = records.stream().map(TransactionLog::getId).collect(Collectors.toList());
        List<Long> sourceIds = records.stream()
                .filter(r -> ("ATTACHMENT".equals(r.getBizType()) || "UPDATE_ADJUST".equals(r.getBizType())) && r.getRelatedRuleId() != null)
                .map(TransactionLog::getRelatedRuleId)
                .collect(Collectors.toList());
        ids.addAll(sourceIds);

        List<Long> revertedIds = transactionLogMapper.selectList(
                new LambdaQueryWrapper<TransactionLog>()
                        .eq(TransactionLog::getBizType, "REFUND")
                        .in(TransactionLog::getRelatedRuleId, ids)
                        .select(TransactionLog::getRelatedRuleId)
        ).stream().map(TransactionLog::getRelatedRuleId).distinct().collect(Collectors.toList());
        for (TransactionLog log : records) {
            boolean directly = revertedIds.contains(log.getId());
            boolean indirectly = ("ATTACHMENT".equals(log.getBizType()) || "UPDATE_ADJUST".equals(log.getBizType()))
                    && log.getRelatedRuleId() != null
                    && revertedIds.contains(log.getRelatedRuleId());
            log.setReverted(directly || indirectly);
        }
    }

    public TransactionLog getById(Long operatorId, Long id) {
        TransactionLog log = transactionLogMapper.selectById(id);
        if (log == null) {
            return null;
        }
        SysUser operator = getOperator(operatorId);
        if (!canAccess(operator, log.getUserId())) {
            throw new BizException("无权访问该记录");
        }
        return log;
    }

    @Transactional(rollbackFor = Exception.class)
    public TransactionLog revert(Long operatorId, Long transactionId) {
        SysUser operator = getOperator(operatorId);
        if (!"admin".equals(operator.getRole())) {
            throw new BizException("无权限");
        }
        
        TransactionLog original = transactionLogMapper.selectById(transactionId);
        if (original == null) {
            throw new BizException("流水不存在：" + transactionId);
        }
        if ("ATTACHMENT".equals(original.getBizType())) {
            throw new BizException("附加流水不能单独撤销，请撤销源流水");
        }
        
        long revertCount = transactionLogMapper.selectCount(
                new LambdaQueryWrapper<TransactionLog>()
                        .eq(TransactionLog::getRelatedRuleId, transactionId)
                        .eq(TransactionLog::getBizType, "REFUND"));
        if (revertCount > 0) {
            throw new BizException("该流水已被撤销");
        }
        
        SysUser user = sysUserMapper.selectById(original.getUserId());
        if (user == null) {
            throw new BizException("用户不存在：" + original.getUserId());
        }
        
        int reverseAmount = -original.getAmount();
        int newBalance = user.getBalance() + reverseAmount;
        if (newBalance < 0) {
            throw new BizException("用户积分不足，无法撤销此流水");
        }
        
        user.setBalance(newBalance);
        sysUserMapper.updateById(user);
        
        TransactionLog revertLog = new TransactionLog();
        revertLog.setUserId(original.getUserId());
        revertLog.setAmount(reverseAmount);
        revertLog.setBalanceAfter(newBalance);
        revertLog.setBizType("REFUND");
        revertLog.setRelatedRuleId(transactionId);
        revertLog.setDescription("管理员撤销流水 #" + transactionId + "：" + original.getDescription());
        revertLog.setCreatedAt(LocalDateTime.now());
        transactionLogMapper.insert(revertLog);
        
        List<TransactionLog> attachments = transactionLogMapper.selectList(
                new LambdaQueryWrapper<TransactionLog>()
                        .eq(TransactionLog::getRelatedRuleId, transactionId)
                        .eq(TransactionLog::getBizType, "ATTACHMENT"));
        for (TransactionLog attachment : attachments) {
            SysUser orgUser = sysUserMapper.selectById(attachment.getUserId());
            if (orgUser != null) {
                int orgReverseAmount = -attachment.getAmount();
                int orgNewBalance = orgUser.getBalance() + orgReverseAmount;
                orgUser.setBalance(orgNewBalance);
                sysUserMapper.updateById(orgUser);

                TransactionLog attachmentRevert = new TransactionLog();
                attachmentRevert.setUserId(orgUser.getId());
                attachmentRevert.setAmount(orgReverseAmount);
                attachmentRevert.setBalanceAfter(orgNewBalance);
                attachmentRevert.setBizType("REFUND");
                attachmentRevert.setRelatedRuleId(attachment.getId());
                attachmentRevert.setDescription("撤销附加流水 #" + attachment.getId() + "：" + attachment.getDescription());
                attachmentRevert.setCreatedAt(LocalDateTime.now());
                transactionLogMapper.insert(attachmentRevert);
            }
        }
        
        return revertLog;
    }

    public byte[] exportToCsv(Long operatorId, Long userId, String bizType,
                               LocalDateTime startTime, LocalDateTime endTime) {
        List<TransactionLog> logs = list(operatorId, userId, bizType, startTime, endTime);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (PrintWriter writer = new PrintWriter(new OutputStreamWriter(baos, StandardCharsets.UTF_8))) {
            writer.print("\uFEFF");
            writer.println("ID,用户ID,金额,变动后余额,业务类型,业务ID,描述,创建时间");
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (TransactionLog log : logs) {
                writer.printf("%d,%d,%d,%d,%s,%s,%s,%s%n",
                        log.getId(),
                        log.getUserId(),
                        log.getAmount(),
                        log.getBalanceAfter(),
                        escapeCsv(log.getBizType()),
                        log.getRelatedRuleId() != null ? String.valueOf(log.getRelatedRuleId()) : "",
                        escapeCsv(log.getDescription()),
                        log.getCreatedAt() != null ? log.getCreatedAt().format(formatter) : ""
                );
            }
        }
        return baos.toByteArray();
    }

    private SysUser getOperator(Long operatorId) {
        if (operatorId == null) {
            throw new BizException("未登录");
        }
        SysUser operator = sysUserMapper.selectById(operatorId);
        if (operator == null) {
            throw new BizException("用户不存在");
        }
        return operator;
    }

    private LambdaQueryWrapper<TransactionLog> buildWrapper(SysUser operator, Long userId, String bizType,
                                                             LocalDateTime startTime, LocalDateTime endTime) {
        LambdaQueryWrapper<TransactionLog> wrapper = new LambdaQueryWrapper<>();
        
        String role = operator.getRole();
        if ("student".equals(role)) {
            wrapper.eq(TransactionLog::getUserId, operator.getId());
        } else if ("org_admin".equals(role)) {
            List<Long> studentIds = sysUserMapper.selectList(
                    new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getOrgId, operator.getOrgId())
                            .eq(SysUser::getRole, "student")
            ).stream().map(SysUser::getId).collect(Collectors.toList());
            wrapper.in(TransactionLog::getUserId, studentIds);
        }
        
        if ("admin".equals(role) && userId != null) {
            wrapper.eq(TransactionLog::getUserId, userId);
        }
        
        if (bizType != null && !bizType.isEmpty()) {
            wrapper.eq(TransactionLog::getBizType, bizType);
        }
        if (startTime != null) {
            wrapper.ge(TransactionLog::getCreatedAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(TransactionLog::getCreatedAt, endTime);
        }
        
        return wrapper;
    }

    private boolean canAccess(SysUser operator, Long targetUserId) {
        String role = operator.getRole();
        if ("admin".equals(role)) {
            return true;
        }
        if ("student".equals(role)) {
            return operator.getId().equals(targetUserId);
        }
        if ("org_admin".equals(role)) {
            SysUser targetUser = sysUserMapper.selectById(targetUserId);
            return targetUser != null && operator.getOrgId().equals(targetUser.getOrgId());
        }
        return false;
    }

    private String escapeCsv(String value) {
        if (value == null) return "";
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}