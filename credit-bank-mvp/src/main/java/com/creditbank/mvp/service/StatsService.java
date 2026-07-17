package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.dto.DashboardVO;
import com.creditbank.mvp.dto.PointOverviewDTO;
import com.creditbank.mvp.dto.StatsSummaryDTO;
import com.creditbank.mvp.dto.TodoItemDTO;
import com.creditbank.mvp.entity.Application;
import com.creditbank.mvp.entity.ApplicationAuditLog;
import com.creditbank.mvp.entity.CertAuditFlow;
import com.creditbank.mvp.entity.CertStandard;
import com.creditbank.mvp.entity.ConversionApplication;
import com.creditbank.mvp.entity.Organization;
import com.creditbank.mvp.entity.Project;
import com.creditbank.mvp.entity.StudentProject;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.entity.TransactionLog;
import com.creditbank.mvp.mapper.ApplicationAuditLogMapper;
import com.creditbank.mvp.mapper.ApplicationMapper;
import com.creditbank.mvp.mapper.CertAuditFlowMapper;
import com.creditbank.mvp.mapper.CertStandardMapper;
import com.creditbank.mvp.mapper.ConversionApplicationMapper;
import com.creditbank.mvp.mapper.OrganizationMapper;
import com.creditbank.mvp.mapper.ProjectMapper;
import com.creditbank.mvp.mapper.StudentProjectMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.mapper.TransactionLogMapper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
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
    private final CertStandardMapper certStandardMapper;
    private final CertAuditFlowMapper certAuditFlowMapper;
    private final ConversionApplicationMapper conversionApplicationMapper;
    private final ProjectMapper projectMapper;
    private final StudentProjectMapper studentProjectMapper;
    private final ApplicationAuditLogMapper applicationAuditLogMapper;

    public StatsService(SysUserMapper sysUserMapper,
                        OrganizationMapper organizationMapper,
                        ApplicationMapper applicationMapper,
                        TransactionLogMapper transactionLogMapper,
                        CertStandardMapper certStandardMapper,
                        CertAuditFlowMapper certAuditFlowMapper,
                        ConversionApplicationMapper conversionApplicationMapper,
                        ProjectMapper projectMapper,
                        StudentProjectMapper studentProjectMapper,
                        ApplicationAuditLogMapper applicationAuditLogMapper) {
        this.sysUserMapper = sysUserMapper;
        this.organizationMapper = organizationMapper;
        this.applicationMapper = applicationMapper;
        this.transactionLogMapper = transactionLogMapper;
        this.certStandardMapper = certStandardMapper;
        this.certAuditFlowMapper = certAuditFlowMapper;
        this.conversionApplicationMapper = conversionApplicationMapper;
        this.projectMapper = projectMapper;
        this.studentProjectMapper = studentProjectMapper;
        this.applicationAuditLogMapper = applicationAuditLogMapper;
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
                            .eq(Application::getCurrentStatus, 1));
            // 加上转换申请中待审核(status=0)的数量
            pendingCount += conversionApplicationMapper.selectCount(
                    new LambdaQueryWrapper<ConversionApplication>()
                            .eq(ConversionApplication::getStatus, 0));
        } else if ("org_admin".equals(role)) {
            SysUser user = sysUserMapper.selectById(userId);
            if (user == null || user.getOrgId() == null) {
                pendingCount = 0L;
            } else {
                pendingCount = applicationMapper.selectCount(
                        new LambdaQueryWrapper<Application>()
                                .eq(Application::getOrgId, user.getOrgId())
                                .eq(Application::getCurrentStatus, 1));
                // 机构管理员待审核：本机构申请(convertedOrgId=orgId) + 通用申请(convertedOrgId=null)
                pendingCount += conversionApplicationMapper.selectCount(
                        new LambdaQueryWrapper<ConversionApplication>()
                                .eq(ConversionApplication::getStatus, 0)
                                .and(w -> w.eq(ConversionApplication::getConvertedOrgId, user.getOrgId())
                                        .or().isNull(ConversionApplication::getConvertedOrgId)));
            }
        } else if ("expert".equals(role)) {
            List<Long> myNodeIds = certAuditFlowMapper.selectList(
                            new LambdaQueryWrapper<CertAuditFlow>().eq(CertAuditFlow::getAuditorId, userId))
                    .stream().map(CertAuditFlow::getId).collect(Collectors.toList());
            if (myNodeIds.isEmpty()) {
                pendingCount = 0L;
            } else {
                pendingCount = applicationMapper.selectCount(
                        new LambdaQueryWrapper<Application>()
                                .in(Application::getCurrentNodeId, myNodeIds)
                                .eq(Application::getCurrentStatus, 1));
            }
            // 专家不参与转换申请审核，不额外累加
        } else {
            pendingCount = applicationMapper.selectCount(
                    new LambdaQueryWrapper<Application>()
                            .eq(Application::getApplicantId, userId)
                            .eq(Application::getCurrentStatus, 1));
            // 学生自己的转换申请待审核也计入 pending
            pendingCount += conversionApplicationMapper.selectCount(
                    new LambdaQueryWrapper<ConversionApplication>()
                            .eq(ConversionApplication::getStudentId, userId)
                            .eq(ConversionApplication::getStatus, 0));
        }
        dto.setPendingCount(pendingCount);

        List<SysUser> users = sysUserMapper.selectList(null);
        long totalCredit = users.stream()
                .mapToLong(u -> u.getBalance() != null ? u.getBalance() : 0)
                .sum();
        dto.setTotalCredit(totalCredit);

        // 扩展统计字段
        dto.setRewardCount(transactionLogMapper.selectCount(
                new LambdaQueryWrapper<TransactionLog>().eq(TransactionLog::getBizType, "REWARD")));

        SysUser currentUser = sysUserMapper.selectById(userId);
        Long orgId = currentUser != null ? currentUser.getOrgId() : null;

        if ("org_admin".equals(role) && orgId != null) {
            dto.setOrgStudentCount(sysUserMapper.selectCount(
                    new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getOrgId, orgId)
                            .eq(SysUser::getRole, "student")));
            dto.setOrgProjectCount(projectMapper.selectCount(
                    new LambdaQueryWrapper<Project>().eq(Project::getOrgId, orgId)));
        }

        if ("student".equals(role) && userId != null) {
            dto.setJoinedProjectCount(studentProjectMapper.selectCount(
                    new LambdaQueryWrapper<StudentProject>()
                            .eq(StudentProject::getStudentId, userId)
                            .ne(StudentProject::getStatus, StudentProject.STATUS_CANCELLED)));
        }

        if ("expert".equals(role) && userId != null) {
            List<Long> myNodeIds = certAuditFlowMapper.selectList(
                            new LambdaQueryWrapper<CertAuditFlow>().eq(CertAuditFlow::getAuditorId, userId))
                    .stream().map(CertAuditFlow::getId).collect(Collectors.toList());
            if (!myNodeIds.isEmpty()) {
                dto.setReviewedCount(applicationAuditLogMapper.selectCount(
                        new LambdaQueryWrapper<ApplicationAuditLog>()
                                .in(ApplicationAuditLog::getNodeId, myNodeIds)));
            } else {
                dto.setReviewedCount(0L);
            }
        }

        // 本月积分变动
        LocalDate monthStart = LocalDate.now().withDayOfMonth(1);
        LambdaQueryWrapper<TransactionLog> monthQuery = new LambdaQueryWrapper<TransactionLog>()
                .ge(TransactionLog::getCreatedAt, monthStart.atStartOfDay());
        if ("student".equals(role) && userId != null) {
            monthQuery.eq(TransactionLog::getUserId, userId);
        } else if ("org_admin".equals(role) && orgId != null) {
            List<Long> orgUserIds = sysUserMapper.selectList(
                            new LambdaQueryWrapper<SysUser>().eq(SysUser::getOrgId, orgId))
                    .stream().map(SysUser::getId).collect(Collectors.toList());
            if (!orgUserIds.isEmpty()) {
                monthQuery.in(TransactionLog::getUserId, orgUserIds);
            } else {
                monthQuery.eq(TransactionLog::getUserId, -1L);
            }
        }
        List<TransactionLog> monthLogs = transactionLogMapper.selectList(monthQuery);
        int monthPointChange = monthLogs.stream()
                .mapToInt(log -> log.getAmount() != null ? log.getAmount() : 0)
                .sum();
        dto.setMonthPointChange(monthPointChange);

        return dto;
    }

    /**
     * 获取近 N 天积分概览（按日统计收入/支出/兑换/活动/净值）
     */
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
                            .eq(Application::getCurrentStatus, 1)
                            .orderByDesc(Application::getAppliedAt)
                            .last("LIMIT " + limit));
        } else if ("org_admin".equals(role)) {
            SysUser user = sysUserMapper.selectById(userId);
            if (user == null || user.getOrgId() == null) {
                apps = new ArrayList<>();
            } else {
                apps = applicationMapper.selectList(
                        new LambdaQueryWrapper<Application>()
                                .eq(Application::getOrgId, user.getOrgId())
                                .eq(Application::getCurrentStatus, 1)
                                .orderByDesc(Application::getAppliedAt)
                                .last("LIMIT " + limit));
            }
        } else if ("expert".equals(role)) {
            List<Long> myNodeIds = certAuditFlowMapper.selectList(
                            new LambdaQueryWrapper<CertAuditFlow>().eq(CertAuditFlow::getAuditorId, userId))
                    .stream().map(CertAuditFlow::getId).collect(Collectors.toList());
            if (myNodeIds.isEmpty()) {
                apps = new ArrayList<>();
            } else {
                apps = applicationMapper.selectList(
                        new LambdaQueryWrapper<Application>()
                                .in(Application::getCurrentNodeId, myNodeIds)
                                .eq(Application::getCurrentStatus, 1)
                                .orderByDesc(Application::getAppliedAt)
                                .last("LIMIT " + limit));
            }
        } else {
            apps = applicationMapper.selectList(
                    new LambdaQueryWrapper<Application>()
                            .eq(Application::getApplicantId, userId)
                            .eq(Application::getCurrentStatus, 1)
                            .orderByDesc(Application::getAppliedAt)
                            .last("LIMIT " + limit));
        }

        Map<String, String> bizTypeName = new HashMap<>();
        bizTypeName.put("PROJECT_UP", "项目上架审核");
        bizTypeName.put("CERT_APPLY", "证书认证申请");
        bizTypeName.put("EXPERT_CERT", "专家认证申请");
        bizTypeName.put("UNFREEZE_APPEAL", "解冻申诉");
        bizTypeName.put("ORG_REGISTER", "机构入驻申请");

        Map<Integer, String[]> statusMap = new HashMap<>();
        statusMap.put(1, new String[]{"审核中", "warning"});
        statusMap.put(2, new String[]{"已通过", "success"});
        statusMap.put(3, new String[]{"已驳回", "danger"});

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

        // 合并转换申请 status=0(PENDING) 的待办，保持工作台与审核管理数据一致
        List<ConversionApplication> convApps = new ArrayList<>();
        if ("admin".equals(role)) {
            convApps = conversionApplicationMapper.selectList(
                    new LambdaQueryWrapper<ConversionApplication>()
                            .eq(ConversionApplication::getStatus, 0)
                            .orderByDesc(ConversionApplication::getCreatedAt)
                            .last("LIMIT " + limit));
        } else if ("org_admin".equals(role)) {
            SysUser user = sysUserMapper.selectById(userId);
            if (user != null && user.getOrgId() != null) {
                convApps = conversionApplicationMapper.selectList(
                        new LambdaQueryWrapper<ConversionApplication>()
                                .eq(ConversionApplication::getStatus, 0)
                                .and(w -> w.eq(ConversionApplication::getConvertedOrgId, user.getOrgId())
                                        .or().isNull(ConversionApplication::getConvertedOrgId))
                                .orderByDesc(ConversionApplication::getCreatedAt)
                                .last("LIMIT " + limit));
            }
        } else if ("expert".equals(role)) {
            // 专家不参与转换申请审核
        } else if (userId != null) {
            convApps = conversionApplicationMapper.selectList(
                    new LambdaQueryWrapper<ConversionApplication>()
                            .eq(ConversionApplication::getStudentId, userId)
                            .eq(ConversionApplication::getStatus, 0)
                            .orderByDesc(ConversionApplication::getCreatedAt)
                            .last("LIMIT " + limit));
        }

        if (!convApps.isEmpty()) {
            // 收集转换申请相关学生姓名，展示发起人（管理员端显示"发起人=学生姓名；学生端显示"发起人=自己"保持一致）
            List<Long> convStudentIds = convApps.stream()
                    .map(ConversionApplication::getStudentId)
                    .distinct()
                    .collect(Collectors.toList());
            if (!convStudentIds.isEmpty() && !"student".equals(role)) {
                List<SysUser> convApplicants = sysUserMapper.selectBatchIds(convStudentIds);
                for (SysUser u : convApplicants) {
                    userNameMap.put(u.getId(), u.getRealName());
                }
            }
            for (ConversionApplication ca : convApps) {
                TodoItemDTO dto = new TodoItemDTO();
                dto.setId(ca.getId());
                dto.setType("CONVERSION_APPLICATION");
                dto.setTypeName("成果转换申请");
                dto.setInitiator(userNameMap.getOrDefault(ca.getStudentId(), "未知"));
                dto.setTime(ca.getCreatedAt());
                dto.setStatus("待审核");
                dto.setStatusType("warning");
                result.add(dto);
            }
        }

        // 合并后按时间倒序，截取 limit 条返回
        result.sort(Comparator.comparing(TodoItemDTO::getTime, Comparator.nullsLast(Comparator.reverseOrder())));
        if (result.size() > limit) {
            result = result.subList(0, limit);
        }

        return result;
    }

    public List<TransactionLog> getRecentTransactions(String role, Long userId, int limit) {
        LambdaQueryWrapper<TransactionLog> query = new LambdaQueryWrapper<TransactionLog>()
                .ne(TransactionLog::getBizType, "DAILY")
                .orderByDesc(TransactionLog::getCreatedAt);

        if ("admin".equals(role)) {
            // 管理员查看所有交易
        } else if ("org_admin".equals(role)) {
            // 机构管理员查看机构内所有用户的交易
            SysUser user = sysUserMapper.selectById(userId);
            if (user != null && user.getOrgId() != null) {
                List<Long> orgUserIds = sysUserMapper.selectList(
                        new LambdaQueryWrapper<SysUser>()
                                .eq(SysUser::getOrgId, user.getOrgId()))
                        .stream().map(SysUser::getId).collect(Collectors.toList());
                if (!orgUserIds.isEmpty()) {
                    query.in(TransactionLog::getUserId, orgUserIds);
                } else {
                    query.eq(TransactionLog::getUserId, -1L);
                }
            } else {
                query.eq(TransactionLog::getUserId, userId);
            }
        } else {
            query.eq(TransactionLog::getUserId, userId);
        }

        List<TransactionLog> logs = transactionLogMapper.selectList(query.last("LIMIT " + limit));

        if (("admin".equals(role) || "org_admin".equals(role)) && !logs.isEmpty()) {
            List<Long> userIds = logs.stream()
                    .map(TransactionLog::getUserId)
                    .distinct()
                    .collect(Collectors.toList());
            Map<Long, String> userNameMap = new HashMap<>();
            if (!userIds.isEmpty()) {
                List<SysUser> users = sysUserMapper.selectBatchIds(userIds);
                for (SysUser u : users) {
                    userNameMap.put(u.getId(), u.getRealName());
                }
            }
            for (TransactionLog log : logs) {
                log.setUserName(userNameMap.getOrDefault(log.getUserId(), "未知"));
            }
        }

        return logs;
    }

    public List<Map<String, Object>> getPointTrend(Long userId, int days) {
        List<Map<String, Object>> result = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        LocalDate startDate = LocalDate.now().minusDays(days - 1);
        LocalDate endDate = LocalDate.now();

        // 锚点：今天的余额就是用户当前余额，这是最可靠的数据
        SysUser user = sysUserMapper.selectById(userId);
        int currentBalance = user != null && user.getBalance() != null ? user.getBalance() : 0;

        // 查询范围内的所有交易（正序也可以，这里只需要聚合每天的净变化量）
        List<TransactionLog> allLogs = transactionLogMapper.selectList(
                new LambdaQueryWrapper<TransactionLog>()
                        .eq(TransactionLog::getUserId, userId)
                        .ge(TransactionLog::getCreatedAt, startDate.atStartOfDay())
                        .le(TransactionLog::getCreatedAt, endDate.atTime(23, 59, 59)));

        // 第一步：按日期聚合计算每一天的积分净变化量（amount 总和）
        Map<String, Integer> dailyNetChange = new HashMap<>();
        for (TransactionLog log : allLogs) {
            String logDate = log.getCreatedAt().toLocalDate().format(formatter);
            int amount = log.getAmount() != null ? log.getAmount() : 0;
            dailyNetChange.merge(logDate, amount, Integer::sum);
        }

        // 第二步：从今天倒推到 startDate，计算每一天的日终余额
        // 原理：今天余额 - 今天净变化 = 昨天余额；昨天余额 - 昨天净变化 = 前天余额，以此类推
        Map<String, Integer> dailyEndBalance = new HashMap<>();
        int workingBalance = currentBalance;
        LocalDate cursor = endDate;
        for (int i = 0; i < days; i++) {
            String dateStr = cursor.format(formatter);
            dailyEndBalance.put(dateStr, workingBalance);
            // 下一个（更靠前的）日期的余额 = 当前余额 - 当前日期的净变化量
            int netChange = dailyNetChange.getOrDefault(dateStr, 0);
            workingBalance = workingBalance - netChange;
            cursor = cursor.minusDays(1);
        }

        // 第三步：按 startDate → endDate 顺序组装结果
        cursor = startDate;
        for (int i = 0; i < days; i++) {
            String dateStr = cursor.format(formatter);
            Map<String, Object> dayData = new HashMap<>();
            dayData.put("date", dateStr);
            dayData.put("balance", dailyEndBalance.get(dateStr));
            result.add(dayData);
            cursor = cursor.plusDays(1);
        }

        return result;
    }

    // ==================== 数据大屏 ====================

    /**
     * 获取数据大屏数据：KPI 指标、角色分类、同比对比、省份分布、月度趋势
     */
    public DashboardVO getDashboardData() {
        DashboardVO vo = new DashboardVO();

        // KPI
        DashboardVO.KpiDTO kpi = new DashboardVO.KpiDTO();
        kpi.setTotalArchives(sysUserMapper.selectCount(null));
        kpi.setTotalCertifications(transactionLogMapper.selectCount(
                new LambdaQueryWrapper<TransactionLog>().eq(TransactionLog::getBizType, "REWARD")));
        kpi.setTotalRules(certStandardMapper.selectCount(
                new LambdaQueryWrapper<CertStandard>().eq(CertStandard::getIsEnabled, 1)));
        kpi.setTotalCenters(organizationMapper.selectCount(null));
        vo.setKpi(kpi);

        // 分类统计（用角色模拟）
        List<Map<String, Object>> categories = new ArrayList<>();
        addCategory(categories, "学生档案", sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, "student")));
        addCategory(categories, "机构管理员", sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, "org_admin")));
        addCategory(categories, "专家", sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, "expert")));
        addCategory(categories, "系统管理员", sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getRole, "admin")));
        vo.setCategories(categories);

        // 同比对比（去年 vs 今年，用 created_at 年份过滤）
        List<DashboardVO.YoYDTO> yoy = new ArrayList<>();
        long thisYearUsers = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().ge(SysUser::getCreatedAt, LocalDate.now().withDayOfYear(1).atStartOfDay()));
        long lastYearUsers = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>()
                        .ge(SysUser::getCreatedAt, LocalDate.now().minusYears(1).withDayOfYear(1).atStartOfDay())
                        .lt(SysUser::getCreatedAt, LocalDate.now().withDayOfYear(1).atStartOfDay()));
        addYoY(yoy, "新增档案", lastYearUsers, thisYearUsers);

        long thisYearTxn = transactionLogMapper.selectCount(
                new LambdaQueryWrapper<TransactionLog>().ge(TransactionLog::getCreatedAt, LocalDate.now().withDayOfYear(1).atStartOfDay()));
        long lastYearTxn = transactionLogMapper.selectCount(
                new LambdaQueryWrapper<TransactionLog>()
                        .ge(TransactionLog::getCreatedAt, LocalDate.now().minusYears(1).withDayOfYear(1).atStartOfDay())
                        .lt(TransactionLog::getCreatedAt, LocalDate.now().withDayOfYear(1).atStartOfDay()));
        addYoY(yoy, "积分流水", lastYearTxn, thisYearTxn);
        vo.setYoy(yoy);

        // 省份分布（从机构表 province 聚合计数）
        List<Map<String, Object>> provinces = new ArrayList<>();
        List<Organization> allOrgs = organizationMapper.selectList(
                new LambdaQueryWrapper<Organization>().isNotNull(Organization::getProvince));
        Map<String, Long> provinceCount = new HashMap<>();
        for (Organization org : allOrgs) {
            if (org.getProvince() != null && !org.getProvince().isEmpty()) {
                provinceCount.merge(org.getProvince(), 1L, Long::sum);
            }
        }
        for (Map.Entry<String, Long> entry : provinceCount.entrySet()) {
            Map<String, Object> p = new HashMap<>();
            p.put("name", entry.getKey());
            p.put("value", entry.getValue());
            provinces.add(p);
        }
        vo.setProvinces(provinces);

        // 月度趋势（近12个月）
        List<Map<String, Object>> monthly = new ArrayList<>();
        for (int i = 11; i >= 0; i--) {
            LocalDate month = LocalDate.now().minusMonths(i).withDayOfMonth(1);
            LocalDate nextMonth = month.plusMonths(1);
            Map<String, Object> m = new HashMap<>();
            m.put("month", month.format(DateTimeFormatter.ofPattern("yyyy-MM")));
            m.put("earn", transactionLogMapper.selectCount(
                    new LambdaQueryWrapper<TransactionLog>()
                            .eq(TransactionLog::getBizType, "REWARD")
                            .ge(TransactionLog::getCreatedAt, month.atStartOfDay())
                            .lt(TransactionLog::getCreatedAt, nextMonth.atStartOfDay())));
            m.put("exchange", transactionLogMapper.selectCount(
                    new LambdaQueryWrapper<TransactionLog>()
                            .eq(TransactionLog::getBizType, "EXCHANGE")
                            .ge(TransactionLog::getCreatedAt, month.atStartOfDay())
                            .lt(TransactionLog::getCreatedAt, nextMonth.atStartOfDay())));
            monthly.add(m);
        }
        vo.setMonthlyTrend(monthly);

        return vo;
    }

    private void addCategory(List<Map<String, Object>> list, String name, long value) {
        Map<String, Object> m = new HashMap<>();
        m.put("name", name);
        m.put("value", value);
        list.add(m);
    }

    private void addYoY(List<DashboardVO.YoYDTO> list, String name, long last, long thisYear) {
        DashboardVO.YoYDTO dto = new DashboardVO.YoYDTO();
        dto.setName(name);
        dto.setLastYear(last);
        dto.setThisYear(thisYear);
        dto.setGrowth(thisYear - last);
        dto.setGrowthRate(last > 0 ? String.format("%.1f%%", (thisYear - last) * 100.0 / last) : "—");
        list.add(dto);
    }
}
