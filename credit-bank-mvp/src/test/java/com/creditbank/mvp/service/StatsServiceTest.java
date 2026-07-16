package com.creditbank.mvp.service;

import com.creditbank.mvp.dto.DashboardVO;
import com.creditbank.mvp.dto.PointOverviewDTO;
import com.creditbank.mvp.dto.StatsSummaryDTO;
import com.creditbank.mvp.dto.TodoItemDTO;
import com.creditbank.mvp.entity.Application;
import com.creditbank.mvp.entity.CertAuditFlow;
import com.creditbank.mvp.entity.Organization;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("StatsService 单元测试")
class StatsServiceTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private OrganizationMapper organizationMapper;

    @Mock
    private ApplicationMapper applicationMapper;

    @Mock
    private TransactionLogMapper transactionLogMapper;

    @Mock
    private CertStandardMapper certStandardMapper;

    @Mock
    private CertAuditFlowMapper certAuditFlowMapper;

    @Mock
    private ConversionApplicationMapper conversionApplicationMapper;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private StudentProjectMapper studentProjectMapper;

    @Mock
    private ApplicationAuditLogMapper applicationAuditLogMapper;

    private StatsService statsService;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        MockitoAnnotations.openMocks(this);
        statsService = new StatsService(sysUserMapper, organizationMapper, applicationMapper,
                transactionLogMapper, certStandardMapper, certAuditFlowMapper,
                conversionApplicationMapper, projectMapper, studentProjectMapper,
                applicationAuditLogMapper);
        System.out.println("========== 开始执行: " + testInfo.getDisplayName() + " ==========");
    }

    private SysUser mockUser(Long id, String role, Long orgId, Integer balance) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setRole(role);
        user.setOrgId(orgId);
        user.setBalance(balance);
        user.setRealName("User" + id);
        return user;
    }

    private TransactionLog mockTxn(Long userId, Integer amount, String bizType, String desc, LocalDateTime createdAt) {
        TransactionLog log = new TransactionLog();
        log.setUserId(userId);
        log.setAmount(amount);
        log.setBizType(bizType);
        log.setDescription(desc);
        log.setCreatedAt(createdAt);
        log.setBalanceAfter(balanceAfter(amount));
        return log;
    }

    private int balanceAfter(Integer amount) {
        return 100 + amount;
    }

    @Test
    @DisplayName("getSummary - 管理员查看全局汇总")
    void testGetSummaryAdmin() {
        when(sysUserMapper.selectCount(any())).thenReturn(10L);
        when(organizationMapper.selectCount(any())).thenReturn(5L);
        when(applicationMapper.selectCount(any())).thenReturn(3L);

        SysUser u1 = mockUser(1L, "admin", null, 100);
        SysUser u2 = mockUser(2L, "student", 1L, 200);
        when(sysUserMapper.selectList(isNull())).thenReturn(Arrays.asList(u1, u2));
        when(sysUserMapper.selectById(1L)).thenReturn(u1);
        when(transactionLogMapper.selectCount(any())).thenReturn(8L);

        StatsSummaryDTO dto = statsService.getSummary("admin", 1L);

        assertNotNull(dto);
        assertEquals(10L, dto.getTotalUsers());
        assertEquals(5L, dto.getTotalOrgs());
        assertEquals(3L, dto.getPendingCount());
        assertEquals(300L, dto.getTotalCredit());
        assertEquals(8L, dto.getRewardCount());
        System.out.println("✓ 测试通过: 管理员全局汇总 - 用户数=" + dto.getTotalUsers() + ", 机构数=" + dto.getTotalOrgs());
    }

    @Test
    @DisplayName("getSummary - 机构管理员查看机构汇总")
    void testGetSummaryOrgAdmin() {
        SysUser orgAdmin = mockUser(10L, "org_admin", 1L, 50);
        when(sysUserMapper.selectById(10L)).thenReturn(orgAdmin);
        when(sysUserMapper.selectCount(any())).thenReturn(20L, 4L);
        when(organizationMapper.selectCount(any())).thenReturn(6L);
        when(applicationMapper.selectCount(any())).thenReturn(2L);
        when(sysUserMapper.selectList(isNull())).thenReturn(Collections.singletonList(orgAdmin));
        when(transactionLogMapper.selectCount(any())).thenReturn(5L);
        when(projectMapper.selectCount(any())).thenReturn(3L);

        StatsSummaryDTO dto = statsService.getSummary("org_admin", 10L);

        assertNotNull(dto);
        assertEquals(20L, dto.getTotalUsers());
        assertEquals(2L, dto.getPendingCount());
        assertEquals(4L, dto.getOrgStudentCount());
        assertEquals(3L, dto.getOrgProjectCount());
        assertEquals(50L, dto.getTotalCredit());
        System.out.println("✓ 测试通过: 机构管理员汇总 - 待审=" + dto.getPendingCount() + ", 机构学生=" + dto.getOrgStudentCount());
    }

    @Test
    @DisplayName("getSummary - 学生查看个人汇总")
    void testGetSummaryStudent() {
        SysUser student = mockUser(100L, "student", 1L, 300);
        when(sysUserMapper.selectCount(any())).thenReturn(50L);
        when(organizationMapper.selectCount(any())).thenReturn(10L);
        when(applicationMapper.selectCount(any())).thenReturn(1L);
        when(sysUserMapper.selectList(isNull())).thenReturn(Collections.singletonList(student));
        when(sysUserMapper.selectById(100L)).thenReturn(student);
        when(transactionLogMapper.selectCount(any())).thenReturn(12L);
        when(studentProjectMapper.selectCount(any())).thenReturn(5L);
        when(transactionLogMapper.selectList(any())).thenReturn(Collections.emptyList());

        StatsSummaryDTO dto = statsService.getSummary("student", 100L);

        assertNotNull(dto);
        assertEquals(5L, dto.getJoinedProjectCount());
        assertEquals(300L, dto.getTotalCredit());
        System.out.println("✓ 测试通过: 学生个人汇总 - 参与项目=" + dto.getJoinedProjectCount());
    }

    @Test
    @DisplayName("getSummary - 专家查看审核统计")
    void testGetSummaryExpert() {
        SysUser expert = mockUser(50L, "expert", null, 80);
        CertAuditFlow node = new CertAuditFlow();
        node.setId(20L);
        node.setAuditorId(50L);

        when(sysUserMapper.selectCount(any())).thenReturn(30L);
        when(organizationMapper.selectCount(any())).thenReturn(4L);
        when(applicationMapper.selectCount(any())).thenReturn(6L);
        when(sysUserMapper.selectList(isNull())).thenReturn(Collections.singletonList(expert));
        when(sysUserMapper.selectById(50L)).thenReturn(expert);
        when(transactionLogMapper.selectCount(any())).thenReturn(7L);
        when(certAuditFlowMapper.selectList(any())).thenReturn(Collections.singletonList(node));
        when(applicationAuditLogMapper.selectCount(any())).thenReturn(15L);
        when(transactionLogMapper.selectList(any())).thenReturn(Collections.emptyList());

        StatsSummaryDTO dto = statsService.getSummary("expert", 50L);

        assertNotNull(dto);
        assertEquals(15L, dto.getReviewedCount());
        assertEquals(6L, dto.getPendingCount());
        System.out.println("✓ 测试通过: 专家审核统计 - 已审核=" + dto.getReviewedCount());
    }

    @Test
    @DisplayName("getPointOverview - 查询近7天积分概览")
    void testGetPointOverview() {
        LocalDate today = LocalDate.now();
        TransactionLog earnLog = mockTxn(1L, 100, "REWARD", "完成项目", today.atStartOfDay());
        TransactionLog spendLog = mockTxn(1L, -30, "EXCHANGE", "兑换商品", today.atStartOfDay());
        TransactionLog activityLog = mockTxn(1L, 50, "REWARD", "活动奖励", today.atStartOfDay());

        when(transactionLogMapper.selectList(any())).thenReturn(Arrays.asList(earnLog, spendLog, activityLog));

        List<PointOverviewDTO> result = statsService.getPointOverview(7);

        assertNotNull(result);
        assertEquals(7, result.size());
        PointOverviewDTO todayDto = result.get(6);
        assertEquals(150, todayDto.getEarn());
        assertEquals(30, todayDto.getSpend());
        assertEquals(30, todayDto.getConvert());
        assertEquals(50, todayDto.getActivity());
        assertEquals(120, todayDto.getNet());
        System.out.println("✓ 测试通过: 近7天积分概览 - 今日收入=" + todayDto.getEarn() + ", 支出=" + todayDto.getSpend());
    }

    @Test
    @DisplayName("getTodoList - 管理员查看待办")
    void testGetTodoListAdmin() {
        Application app = new Application();
        app.setId(1L);
        app.setBizType("PROJECT_UP");
        app.setApplicantId(100L);
        app.setCurrentStatus(1);
        app.setAppliedAt(LocalDateTime.now());

        SysUser applicant = mockUser(100L, "student", 1L, 0);
        applicant.setRealName("张三");

        when(applicationMapper.selectList(any())).thenReturn(Collections.singletonList(app));
        when(sysUserMapper.selectBatchIds(Collections.singletonList(100L))).thenReturn(Collections.singletonList(applicant));

        List<TodoItemDTO> result = statsService.getTodoList("admin", 1L, 5);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("项目上架审核", result.get(0).getTypeName());
        assertEquals("张三", result.get(0).getInitiator());
        assertEquals("审核中", result.get(0).getStatus());
        System.out.println("✓ 测试通过: 管理员待办列表 - 数量=" + result.size());
    }

    @Test
    @DisplayName("getTodoList - 机构管理员仅看本机构待办")
    void testGetTodoListOrgAdmin() {
        SysUser orgAdmin = mockUser(10L, "org_admin", 2L, 0);
        Application app = new Application();
        app.setId(2L);
        app.setBizType("CERT_APPLY");
        app.setApplicantId(200L);
        app.setOrgId(2L);
        app.setCurrentStatus(1);
        app.setAppliedAt(LocalDateTime.now());

        SysUser applicant = mockUser(200L, "student", 2L, 0);
        applicant.setRealName("李四");

        when(sysUserMapper.selectById(10L)).thenReturn(orgAdmin);
        when(applicationMapper.selectList(any())).thenReturn(Collections.singletonList(app));
        when(sysUserMapper.selectBatchIds(Collections.singletonList(200L))).thenReturn(Collections.singletonList(applicant));

        List<TodoItemDTO> result = statsService.getTodoList("org_admin", 10L, 5);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("证书认证申请", result.get(0).getTypeName());
        System.out.println("✓ 测试通过: 机构管理员待办隔离 - 数量=" + result.size());
    }

    @Test
    @DisplayName("getRecentTransactions - 管理员查看全部交易")
    void testGetRecentTransactionsAdmin() {
        TransactionLog log = mockTxn(1L, 100, "REWARD", "完成项目", LocalDateTime.now());
        SysUser user = mockUser(1L, "student", 1L, 0);
        user.setRealName("王五");

        when(transactionLogMapper.selectList(any())).thenReturn(Collections.singletonList(log));
        when(sysUserMapper.selectBatchIds(Collections.singletonList(1L))).thenReturn(Collections.singletonList(user));

        List<TransactionLog> result = statsService.getRecentTransactions("admin", 1L, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("王五", result.get(0).getUserName());
        System.out.println("✓ 测试通过: 管理员查看交易记录 - 数量=" + result.size());
    }

    @Test
    @DisplayName("getRecentTransactions - 机构管理员仅看本机构交易")
    void testGetRecentTransactionsOrgAdmin() {
        SysUser orgAdmin = mockUser(10L, "org_admin", 1L, 0);
        SysUser student = mockUser(20L, "student", 1L, 0);
        TransactionLog log = mockTxn(20L, 50, "REWARD", "完成项目", LocalDateTime.now());

        when(sysUserMapper.selectById(10L)).thenReturn(orgAdmin);
        when(sysUserMapper.selectList(any())).thenReturn(Collections.singletonList(student));
        when(transactionLogMapper.selectList(any())).thenReturn(Collections.singletonList(log));
        when(sysUserMapper.selectBatchIds(Collections.singletonList(20L))).thenReturn(Collections.singletonList(student));

        List<TransactionLog> result = statsService.getRecentTransactions("org_admin", 10L, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(20L, result.get(0).getUserId());
        System.out.println("✓ 测试通过: 机构管理员交易隔离 - 数量=" + result.size());
    }

    @Test
    @DisplayName("getPointTrend - 查询个人积分趋势")
    void testGetPointTrend() {
        SysUser student = mockUser(100L, "student", 1L, 500);
        LocalDate today = LocalDate.now();
        TransactionLog log = mockTxn(100L, 100, "REWARD", "完成项目", today.atStartOfDay());
        log.setBalanceAfter(500);

        when(sysUserMapper.selectById(100L)).thenReturn(student);
        when(transactionLogMapper.selectList(any())).thenReturn(Collections.singletonList(log));

        List<Map<String, Object>> result = statsService.getPointTrend(100L, 7);

        assertNotNull(result);
        assertEquals(7, result.size());
        assertEquals(500, result.get(6).get("balance"));
        System.out.println("✓ 测试通过: 个人积分趋势 - 今日余额=" + result.get(6).get("balance"));
    }

    @Test
    @DisplayName("getDashboardData - 数据大屏聚合数据")
    void testGetDashboardData() {
        Organization org1 = new Organization();
        org1.setId(1L);
        org1.setName("北京机构");
        org1.setProvince("北京市");

        Organization org2 = new Organization();
        org2.setId(2L);
        org2.setName("上海机构");
        org2.setProvince("上海市");

        when(sysUserMapper.selectCount(any())).thenReturn(4L, 1L, 1L, 1L, 1L);
        when(transactionLogMapper.selectCount(any())).thenReturn(10L);
        when(certStandardMapper.selectCount(any())).thenReturn(3L);
        when(organizationMapper.selectCount(any())).thenReturn(2L);
        when(organizationMapper.selectList(any())).thenReturn(Arrays.asList(org1, org2));

        DashboardVO vo = statsService.getDashboardData();

        assertNotNull(vo);
        assertNotNull(vo.getKpi());
        assertEquals(4L, vo.getKpi().getTotalArchives());
        assertEquals(4, vo.getCategories().size());
        assertEquals(2, vo.getYoy().size());
        assertEquals(2, vo.getProvinces().size());
        assertEquals(12, vo.getMonthlyTrend().size());
        System.out.println("✓ 测试通过: 数据大屏聚合 - KPI=" + vo.getKpi().getTotalArchives() + ", 省份=" + vo.getProvinces().size());
    }
}
