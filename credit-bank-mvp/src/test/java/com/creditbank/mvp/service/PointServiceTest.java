package com.creditbank.mvp.service;

import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.*;
import com.creditbank.mvp.mapper.CreditRuleMapper;
import com.creditbank.mvp.mapper.OrganizationMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.mapper.TransactionLogMapper;
import com.creditbank.mvp.mapper.UserOpLogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("PointService 单元测试")
class PointServiceTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private CreditRuleMapper creditRuleMapper;

    @Mock
    private TransactionLogMapper transactionLogMapper;

    @Mock
    private CampaignService campaignService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserOpLogMapper userOpLogMapper;

    @Mock
    private RedisService redisService;

    @Mock
    private OrganizationMapper organizationMapper;

    @Mock
    private NotificationService notificationService;

    private PointService pointService;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        MockitoAnnotations.openMocks(this);
        pointService = new PointService(sysUserMapper, creditRuleMapper, transactionLogMapper,
                userOpLogMapper, campaignService, passwordEncoder, redisService,
                organizationMapper, notificationService);
        System.out.println("========== 开始执行: " + testInfo.getDisplayName() + " ==========");
    }

    @Test
    @DisplayName("login - 用户名不存在")
    void testLoginUserNotFound() {
        when(sysUserMapper.selectOne(any())).thenReturn(null);

        BizException exception = assertThrows(BizException.class, () ->
                pointService.login("nonexistent", "password"));
        assertEquals("用户不存在", exception.getMessage());
        System.out.println("✓ 测试通过: 用户名不存在时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("login - 冻结用户也能登录")
    void testLoginFrozenUserCanLogin() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("test");
        user.setPassword("encryptedPassword");
        user.setStatus(0);

        when(sysUserMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("password", "encryptedPassword")).thenReturn(true);
        when(sysUserMapper.updateById(any())).thenReturn(1);

        SysUser result = pointService.login("test", "password");

        assertNotNull(result);
        assertEquals(0, result.getStatus());
        System.out.println("✓ 测试通过: 冻结用户可以登录（权限由拦截器控制）- 用户ID=" + result.getId() + ", 状态=" + result.getStatus());
    }

    @Test
    @DisplayName("login - 密码错误")
    void testLoginWrongPassword() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("test");
        user.setPassword("encryptedPassword");
        user.setStatus(1);

        when(sysUserMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("password", "encryptedPassword")).thenReturn(false);

        BizException exception = assertThrows(BizException.class, () ->
                pointService.login("test", "password"));
        assertEquals("密码错误", exception.getMessage());
        System.out.println("✓ 测试通过: 密码错误时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("login - 登录成功")
    void testLoginSuccess() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("test");
        user.setPassword("encryptedPassword");
        user.setStatus(1);

        when(sysUserMapper.selectOne(any())).thenReturn(user);
        when(passwordEncoder.matches("password", "encryptedPassword")).thenReturn(true);
        when(sysUserMapper.updateById(any())).thenReturn(1);

        SysUser result = pointService.login("test", "password");

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test", result.getUsername());
        assertNotNull(result.getLastLoginAt());
        verify(sysUserMapper).updateById(user);
        System.out.println("✓ 测试通过: 用户登录成功 - 用户ID=" + result.getId() + ", 用户名=" + result.getUsername() + ", 最后登录时间=" + result.getLastLoginAt());
    }

    @Test
    @DisplayName("register - 用户名已存在")
    void testRegisterUsernameExists() {
        SysUser existing = new SysUser();
        existing.setId(1L);
        existing.setUsername("test");

        when(sysUserMapper.selectOne(any())).thenReturn(existing);

        BizException exception = assertThrows(BizException.class, () ->
                pointService.register("test", "password", "Test User", "student", 1L, null));
        assertEquals("用户名已存在", exception.getMessage());
        System.out.println("✓ 测试通过: 用户名已存在时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("register - 注册成功")
    void testRegisterSuccess() {
        when(sysUserMapper.selectOne(any())).thenReturn(null);
        when(passwordEncoder.encode("password")).thenReturn("encryptedPassword");
        when(sysUserMapper.insert(any())).thenReturn(1);

        SysUser result = pointService.register("test", "password", "Test User", "student", 1L, null);

        assertNotNull(result);
        assertEquals("test", result.getUsername());
        assertEquals("encryptedPassword", result.getPassword());
        assertEquals("Test User", result.getRealName());
        assertEquals("student", result.getRole());
        assertEquals(0, result.getBalance());
        assertEquals(1, result.getStatus());
        assertNotNull(result.getCreatedAt());
        verify(sysUserMapper).insert(any());
        System.out.println("✓ 测试通过: 用户注册成功 - 用户名=" + result.getUsername() + ", 角色=" + result.getRole() + ", 初始积分=" + result.getBalance() + ", 状态=" + result.getStatus());
    }

    @Test
    @DisplayName("earn - 用户不存在")
    void testEarnUserNotFound() {
        when(sysUserMapper.selectById(1L)).thenReturn(null);

        BizException exception = assertThrows(BizException.class, () ->
                pointService.earn(1L, "project_complete", 1L));
        assertEquals("用户不存在：1", exception.getMessage());
        System.out.println("✓ 测试通过: 用户不存在时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("earn - 积分规则不存在")
    void testEarnRuleNotFound() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setBalance(0);

        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(creditRuleMapper.selectOne(any())).thenReturn(null);

        BizException exception = assertThrows(BizException.class, () ->
                pointService.earn(1L, "project_complete", 1L));
        assertEquals("积分规则不存在或已停用：project_complete", exception.getMessage());
        System.out.println("✓ 测试通过: 积分规则不存在时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("earn - 获取积分成功（无活动加成）")
    void testEarnSuccessWithoutCampaign() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setBalance(100);

        CreditRule rule = new CreditRule();
        rule.setId(1L);
        rule.setEventCode("project_complete");
        rule.setEventName("完成项目");
        rule.setCreditValue(50);
        rule.setIsEnabled(1);

        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(creditRuleMapper.selectOne(any())).thenReturn(rule);
        when(campaignService.getEnrolledMultiplierCampaign(1L)).thenReturn(null);
        when(sysUserMapper.updateById(any())).thenReturn(1);
        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(transactionLogMapper.insert(any())).thenReturn(1);
        when(userOpLogMapper.insert(any())).thenReturn(1);

        SysUser result = pointService.earn(1L, "project_complete", 1L);

        assertNotNull(result);
        assertEquals(150, user.getBalance());
        verify(sysUserMapper).updateById(user);
        verify(transactionLogMapper).insert(any());
        verify(userOpLogMapper).insert(any());
        System.out.println("✓ 测试通过: 获取积分成功（无活动加成）- 原积分=100, 获得积分=50, 当前积分=" + user.getBalance());
    }

    @Test
    @DisplayName("earn - 获取积分成功（有活动加成）")
    void testEarnSuccessWithCampaign() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setBalance(100);

        CreditRule rule = new CreditRule();
        rule.setId(1L);
        rule.setEventCode("project_complete");
        rule.setEventName("完成项目");
        rule.setCreditValue(50);
        rule.setIsEnabled(1);

        Campaign campaign = new Campaign();
        campaign.setId(1L);
        campaign.setMultiplier(BigDecimal.valueOf(2.0));

        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(creditRuleMapper.selectOne(any())).thenReturn(rule);
        when(campaignService.getEnrolledMultiplierCampaign(1L)).thenReturn(campaign);
        when(sysUserMapper.updateById(any())).thenReturn(1);
        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(transactionLogMapper.insert(any())).thenReturn(1);
        when(userOpLogMapper.insert(any())).thenReturn(1);

        SysUser result = pointService.earn(1L, "project_complete", 1L);

        assertNotNull(result);
        assertEquals(200, user.getBalance());
        verify(sysUserMapper).updateById(user);
        verify(transactionLogMapper).insert(any());
        verify(userOpLogMapper).insert(any());
        System.out.println("✓ 测试通过: 获取积分成功（有活动加成×2）- 原积分=100, 基础积分=50, 翻倍后获得=100, 当前积分=" + user.getBalance());
    }

    @Test
    @DisplayName("getUser - 获取用户成功")
    void testGetUserSuccess() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("test");

        when(sysUserMapper.selectById(1L)).thenReturn(user);

        SysUser result = pointService.getUser(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test", result.getUsername());
        System.out.println("✓ 测试通过: 获取用户成功 - ID=" + result.getId() + ", 用户名=" + result.getUsername());
    }

    @Test
    @DisplayName("getUser - 用户不存在")
    void testGetUserNotFound() {
        when(sysUserMapper.selectById(1L)).thenReturn(null);

        BizException exception = assertThrows(BizException.class, () ->
                pointService.getUser(1L));
        assertEquals("用户不存在：1", exception.getMessage());
        System.out.println("✓ 测试通过: 用户不存在时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("listUsers - 获取用户列表")
    void testListUsers() {
        SysUser user1 = new SysUser();
        user1.setId(1L);
        user1.setUsername("user1");

        SysUser user2 = new SysUser();
        user2.setId(2L);
        user2.setUsername("user2");

        when(sysUserMapper.selectList(null)).thenReturn(Arrays.asList(user1, user2));

        List<SysUser> result = pointService.listUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        System.out.println("✓ 测试通过: 获取用户列表成功 - 用户数量=" + result.size());
    }

    @Test
    @DisplayName("listTransactions - 获取交易流水")
    void testListTransactions() {
        TransactionLog log1 = new TransactionLog();
        log1.setId(1L);
        log1.setUserId(1L);
        log1.setAmount(100);

        TransactionLog log2 = new TransactionLog();
        log2.setId(2L);
        log2.setUserId(1L);
        log2.setAmount(50);

        when(transactionLogMapper.selectList(any())).thenReturn(Arrays.asList(log2, log1));

        List<TransactionLog> result = pointService.listTransactions(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getId());
        System.out.println("✓ 测试通过: 获取交易流水成功 - 流水数量=" + result.size() + ", 最新流水ID=" + result.get(0).getId());
    }

    @Test
    @DisplayName("earn - 管理员手动加分成功")
    void testEarnAdminSuccess() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setBalance(100);
        user.setRealName("Student");
        user.setRole("student");

        CreditRule rule = new CreditRule();
        rule.setId(2L);
        rule.setEventCode("ADMIN");
        rule.setEventName("管理员手动加分");
        rule.setCreditValue(0);
        rule.setIsEnabled(1);

        SysUser operator = new SysUser();
        operator.setId(2L);
        operator.setRealName("Admin");

        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(creditRuleMapper.selectOne(any())).thenReturn(rule);
        when(sysUserMapper.selectById(2L)).thenReturn(operator);
        when(sysUserMapper.updateById(any())).thenReturn(1);
        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(transactionLogMapper.insert(any())).thenReturn(1);
        when(userOpLogMapper.insert(any())).thenReturn(1);

        SysUser result = pointService.earn(1L, "ADMIN", 2L, 200, "奖励");

        assertNotNull(result);
        assertEquals(300, user.getBalance());
        verify(transactionLogMapper).insert(any());
        verify(userOpLogMapper).insert(any());
        System.out.println("✓ 测试通过: 管理员手动加分成功 - 原积分=100, 加分=200, 当前积分=" + user.getBalance());
    }

    @Test
    @DisplayName("earn - 管理员手动加分超过5000")
    void testEarnAdminExceedLimit() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setBalance(100);

        CreditRule rule = new CreditRule();
        rule.setId(2L);
        rule.setEventCode("ADMIN");
        rule.setEventName("管理员手动加分");
        rule.setIsEnabled(1);

        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(creditRuleMapper.selectOne(any())).thenReturn(rule);

        BizException exception = assertThrows(BizException.class, () ->
                pointService.earn(1L, "ADMIN", 1L, 6000, "超限"));
        assertEquals("单次加分不能超过5000", exception.getMessage());
        System.out.println("✓ 测试通过: 管理员手动加分超过5000时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("earn - 管理员手动加分未指定积分值")
    void testEarnAdminNoValue() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setBalance(100);

        CreditRule rule = new CreditRule();
        rule.setId(2L);
        rule.setEventCode("ADMIN");
        rule.setEventName("管理员手动加分");
        rule.setIsEnabled(1);

        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(creditRuleMapper.selectOne(any())).thenReturn(rule);

        BizException exception = assertThrows(BizException.class, () ->
                pointService.earn(1L, "ADMIN", 1L, null));
        assertEquals("管理员手动加分必须指定积分值", exception.getMessage());
        System.out.println("✓ 测试通过: 管理员手动加分未指定积分值时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("rewardProjectCompletion - 项目完成发分成功（无活动翻倍）")
    void testRewardProjectCompletionWithoutCampaign() {
        SysUser student = new SysUser();
        student.setId(1L);
        student.setBalance(50);
        student.setRealName("Student");

        Project project = new Project();
        project.setId(1L);
        project.setName("Test Project");
        project.setCreditReward(100);

        when(sysUserMapper.selectById(1L)).thenReturn(student);
        when(campaignService.getEnrolledMultiplierCampaign(1L)).thenReturn(null);
        when(sysUserMapper.updateById(any())).thenReturn(1);
        when(transactionLogMapper.insert(any())).thenReturn(1);
        when(userOpLogMapper.insert(any())).thenReturn(1);
        when(sysUserMapper.selectById(1L)).thenReturn(student);

        SysUser result = pointService.rewardProjectCompletion(1L, project, 2L);

        assertNotNull(result);
        assertEquals(150, student.getBalance());
        verify(transactionLogMapper).insert(any());
        verify(userOpLogMapper).insert(any());
        System.out.println("✓ 测试通过: 项目完成发分成功（无活动翻倍）- 基础积分=100, 当前积分=" + student.getBalance());
    }

    @Test
    @DisplayName("rewardProjectCompletion - 项目完成发分（活动翻倍）")
    void testRewardProjectCompletionWithCampaign() {
        SysUser student = new SysUser();
        student.setId(1L);
        student.setBalance(50);
        student.setRealName("Student");

        Project project = new Project();
        project.setId(1L);
        project.setName("Test Project");
        project.setCreditReward(100);

        Campaign campaign = new Campaign();
        campaign.setId(1L);
        campaign.setMultiplier(BigDecimal.valueOf(2.5));

        when(sysUserMapper.selectById(1L)).thenReturn(student);
        when(campaignService.getEnrolledMultiplierCampaign(1L)).thenReturn(campaign);
        when(sysUserMapper.updateById(any())).thenReturn(1);
        when(transactionLogMapper.insert(any())).thenReturn(1);
        when(userOpLogMapper.insert(any())).thenReturn(1);
        when(sysUserMapper.selectById(1L)).thenReturn(student);

        SysUser result = pointService.rewardProjectCompletion(1L, project, 2L);

        assertNotNull(result);
        assertEquals(300, student.getBalance());
        verify(transactionLogMapper).insert(any());
        verify(userOpLogMapper).insert(any());
        System.out.println("✓ 测试通过: 项目完成发分（活动翻倍×2.5）- 基础积分=100, 翻倍后=250, 当前积分=" + student.getBalance());
    }

    @Test
    @DisplayName("rewardProjectCompletion - 项目无积分奖励直接返回null")
    void testRewardProjectCompletionNoReward() {
        Project project = new Project();
        project.setId(1L);
        project.setName("No Reward Project");
        project.setCreditReward(0);

        SysUser result = pointService.rewardProjectCompletion(1L, project, 2L);

        assertNull(result);
        verify(sysUserMapper, never()).selectById(any());
        System.out.println("✓ 测试通过: 项目无积分奖励时直接返回null");
    }
}
