package com.creditbank.mvp.service;

import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.Campaign;
import com.creditbank.mvp.entity.CreditRule;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.entity.TransactionLog;
import com.creditbank.mvp.mapper.CreditRuleMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.mapper.TransactionLogMapper;
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

    private PointService pointService;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        MockitoAnnotations.openMocks(this);
        pointService = new PointService(sysUserMapper, creditRuleMapper, transactionLogMapper, campaignService, passwordEncoder);
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
    @DisplayName("login - 账户已冻结")
    void testLoginAccountFrozen() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("test");
        user.setPassword("encryptedPassword");
        user.setStatus(0);

        when(sysUserMapper.selectOne(any())).thenReturn(user);

        BizException exception = assertThrows(BizException.class, () ->
                pointService.login("test", "password"));
        assertEquals("账户已被冻结，请联系管理员", exception.getMessage());
        System.out.println("✓ 测试通过: 账户冻结时正确抛出异常 - " + exception.getMessage());
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
                pointService.earn(1L, "project_complete"));
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
                pointService.earn(1L, "project_complete"));
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
        when(campaignService.getActiveMultiplierCampaign()).thenReturn(null);
        when(sysUserMapper.updateById(any())).thenReturn(1);
        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(transactionLogMapper.insert(any())).thenReturn(1);

        SysUser result = pointService.earn(1L, "project_complete");

        assertNotNull(result);
        assertEquals(150, user.getBalance());
        verify(sysUserMapper).updateById(user);
        verify(transactionLogMapper).insert(any());
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
        when(campaignService.getActiveMultiplierCampaign()).thenReturn(campaign);
        when(campaignService.isEnrolled(1L, 1L)).thenReturn(true);
        when(sysUserMapper.updateById(any())).thenReturn(1);
        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(transactionLogMapper.insert(any())).thenReturn(1);

        SysUser result = pointService.earn(1L, "project_complete");

        assertNotNull(result);
        assertEquals(200, user.getBalance());
        verify(sysUserMapper).updateById(user);
        verify(transactionLogMapper).insert(any());
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
}