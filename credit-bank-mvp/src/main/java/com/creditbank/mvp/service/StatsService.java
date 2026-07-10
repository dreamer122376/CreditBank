package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.dto.PointOverviewDTO;
import com.creditbank.mvp.dto.StatsSummaryDTO;
import com.creditbank.mvp.dto.TodoItemDTO;
import com.creditbank.mvp.entity.Application;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.entity.TransactionLog;
import com.creditbank.mvp.mapper.ApplicationMapper;
import com.creditbank.mvp.mapper.OrganizationMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.mapper.TransactionLogMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StatsService {

    private final SysUserMapper sysUserMapper;
    private final OrganizationMapper organizationMapper;
    private final ApplicationMapper applicationMapper;
    private final TransactionLogMapper transactionLogMapper;

    public StatsService(SysUserMapper sysUserMapper,
                        OrganizationMapper organizationMapper,
                        ApplicationMapper applicationMapper,
                        TransactionLogMapper transactionLogMapper) {
        this.sysUserMapper = sysUserMapper;
        this.organizationMapper = organizationMapper;
        this.applicationMapper = applicationMapper;
        this.transactionLogMapper = transactionLogMapper;
    }

    public StatsSummaryDTO getSummary(String role, Long userId) {
        StatsSummaryDTO dto = new StatsSummaryDTO();

        Long totalUsers = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getStatus, 1));
        dto.setTotalUsers(totalUsers);

        Long totalOrgs = organizationMapper.selectCount(
                new LambdaQueryWrapper<>());
        dto.setTotalOrgs(totalOrgs);

        Long pendingCount;
        if ("admin".equals(role)) {
            pendingCount = applicationMapper.selectCount(
                    new LambdaQueryWrapper<Application>()
                            .lt(Application::getCurrentStatus, 3));
        } else if ("org_admin".equals(role)) {
            SysUser user = sysUserMapper.selectById(userId);
            pendingCount = applicationMapper.selectCount(
                    new LambdaQueryWrapper<Application>()
                            .eq(Application::getOrgId, user.getOrgId())
                            .eq(Application::getCurrentStatus, 1));
        } else if ("expert".equals(role)) {
            pendingCount = applicationMapper.selectCount(
                    new LambdaQueryWrapper<Application>()
                            .eq(Application::getExpertId, userId)
                            .eq(Application::getCurrentStatus, 2));
        } else {
            pendingCount = applicationMapper.selectCount(
                    new LambdaQueryWrapper<Application>()
                            .eq(Application::getApplicantId, userId)
                            .lt(Application::getCurrentStatus, 3));
        }
        dto.setPendingCount(pendingCount);

        List<SysUser> users = sysUserMapper.selectList(null);
        long totalCredit = users.stream()
                .mapToLong(u -> u.getBalance() != null ? u.getBalance() : 0)
                .sum();
        dto.setTotalCredit(totalCredit);

        return dto;
    }

    public List<PointOverviewDTO> getPointOverview(int days) {
        List<PointOverviewDTO> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = days - 1; i >= 0; i--) {
            LocalDate date = LocalDate.now().minusDays(i);
            PointOverviewDTO dto = new PointOverviewDTO();
            dto.setDate(date.format(formatter));

            List<TransactionLog> dayLogs = transactionLogMapper.selectList(
                    new LambdaQueryWrapper<TransactionLog>()
                            .apply("DATE(created_at) = {0}", date.toString()));

            int earn = 0, spend = 0, convert = 0, activity = 0;
            for (TransactionLog log : dayLogs) {
                int amount = log.getAmount() != null ? log.getAmount() : 0;
                if (amount > 0) {
                    earn += amount;
                } else {
                    spend += Math.abs(amount);
                }
                if ("EXCHANGE".equals(log.getBizType())) {
                    convert += Math.abs(amount);
                }
                String desc = log.getDescription();
                if (desc != null && desc.contains("活动")) {
                    activity += Math.abs(amount);
                }
            }

            dto.setEarn(earn);
            dto.setSpend(spend);
            dto.setConvert(convert);
            dto.setActivity(activity);
            dto.setNet(earn - spend);

            result.add(dto);
        }

        return result;
    }

    public List<TodoItemDTO> getTodoList(String role, Long userId, int limit) {
        List<Application> apps;

        if ("admin".equals(role)) {
            apps = applicationMapper.selectList(
                    new LambdaQueryWrapper<Application>()
                            .lt(Application::getCurrentStatus, 3)
                            .orderByDesc(Application::getAppliedAt)
                            .last("LIMIT " + limit));
        } else if ("org_admin".equals(role)) {
            SysUser user = sysUserMapper.selectById(userId);
            apps = applicationMapper.selectList(
                    new LambdaQueryWrapper<Application>()
                            .eq(Application::getOrgId, user.getOrgId())
                            .eq(Application::getCurrentStatus, 1)
                            .orderByDesc(Application::getAppliedAt)
                            .last("LIMIT " + limit));
        } else if ("expert".equals(role)) {
            apps = applicationMapper.selectList(
                    new LambdaQueryWrapper<Application>()
                            .eq(Application::getExpertId, userId)
                            .eq(Application::getCurrentStatus, 2)
                            .orderByDesc(Application::getAppliedAt)
                            .last("LIMIT " + limit));
        } else {
            apps = applicationMapper.selectList(
                    new LambdaQueryWrapper<Application>()
                            .eq(Application::getApplicantId, userId)
                            .lt(Application::getCurrentStatus, 3)
                            .orderByDesc(Application::getAppliedAt)
                            .last("LIMIT " + limit));
        }

        Map<String, String> bizTypeName = new HashMap<>();
        bizTypeName.put("PROJECT_UP", "项目上架审核");
        bizTypeName.put("EXCHANGE", "学分转换申请");
        bizTypeName.put("CERT_APPLY", "证书认证申请");

        Map<Integer, String[]> statusMap = new HashMap<>();
        statusMap.put(0, new String[]{"草稿", "info"});
        statusMap.put(1, new String[]{"待机构审核", "warning"});
        statusMap.put(2, new String[]{"待专家评审", "warning"});
        statusMap.put(3, new String[]{"已通过", "success"});
        statusMap.put(4, new String[]{"已驳回", "danger"});

        List<Long> applicantIds = apps.stream()
                .map(Application::getApplicantId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> userNameMap = new HashMap<>();
        if (!applicantIds.isEmpty()) {
            List<SysUser> applicants = sysUserMapper.selectBatchIds(applicantIds);
            for (SysUser u : applicants) {
                userNameMap.put(u.getId(), u.getRealName());
            }
        }

        List<TodoItemDTO> result = new ArrayList<>();
        for (Application app : apps) {
            TodoItemDTO dto = new TodoItemDTO();
            dto.setId(app.getId());
            dto.setType(app.getBizType());
            dto.setTypeName(bizTypeName.getOrDefault(app.getBizType(), app.getBizType()));
            dto.setInitiator(userNameMap.getOrDefault(app.getApplicantId(), "未知"));
            dto.setTime(app.getAppliedAt());
            String[] statusInfo = statusMap.getOrDefault(app.getCurrentStatus(),
                    new String[]{"未知", "info"});
            dto.setStatus(statusInfo[0]);
            dto.setStatusType(statusInfo[1]);
            result.add(dto);
        }

        return result;
    }

    public List<TransactionLog> getRecentTransactions(Long userId, int limit) {
        return transactionLogMapper.selectList(
                new LambdaQueryWrapper<TransactionLog>()
                        .eq(TransactionLog::getUserId, userId)
                        .orderByDesc(TransactionLog::getCreatedAt)
                        .last("LIMIT " + limit));
    }
}
