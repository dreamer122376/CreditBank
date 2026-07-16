package com.creditbank.mvp.service;

import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.CreditRule;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.entity.TransactionLog;
import com.creditbank.mvp.entity.UserOpLog;
import com.creditbank.mvp.mapper.CreditRuleMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.mapper.TransactionLogMapper;
import com.creditbank.mvp.mapper.UserOpLogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("SignInService 单元测试")
class SignInServiceTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private CreditRuleMapper creditRuleMapper;

    @Mock
    private TransactionLogMapper transactionLogMapper;

    @Mock
    private RedisService redisService;

    @Mock
    private UserOpLogMapper userOpLogMapper;

    private SignInService signInService;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        MockitoAnnotations.openMocks(this);
        signInService = new SignInService(sysUserMapper, creditRuleMapper, transactionLogMapper,
                redisService, userOpLogMapper);
        System.out.println("========== 开始执行: " + testInfo.getDisplayName() + " ==========");
    }

    private SysUser mockUser(Long id, Integer balance) {
        SysUser user = new SysUser();
        user.setId(id);
        user.setBalance(balance);
        user.setRealName("User" + id);
        return user;
    }

    private CreditRule mockRule() {
        CreditRule rule = new CreditRule();
        rule.setId(1L);
        rule.setEventCode("ATTENDANCE");
        rule.setEventName("签到打卡");
        rule.setCreditValue(5);
        rule.setIsEnabled(1);
        return rule;
    }

    private void mockSignInRule() {
        when(creditRuleMapper.selectOne(any())).thenReturn(mockRule());
    }

    @Test
    @DisplayName("signIn - 用户首次签到成功")
    void testSignInSuccess() {
        SysUser user = mockUser(100L, 100);
        when(sysUserMapper.selectById(100L)).thenReturn(user);
        when(redisService.get("sign:today:100")).thenReturn(null);
        mockSignInRule();
        when(transactionLogMapper.selectCount(any())).thenReturn(0L);
        when(sysUserMapper.updateById(any())).thenReturn(1);
        when(transactionLogMapper.insert(any())).thenReturn(1);
        doNothing().when(redisService).set(anyString(), any(), anyLong(), any(TimeUnit.class));
        when(userOpLogMapper.insert(any())).thenReturn(1);

        Map<String, Object> result = signInService.signIn(100L);

        assertNotNull(result);
        assertTrue((Boolean) result.get("success"));
        assertEquals(5, result.get("creditEarned"));
        assertEquals(105, result.get("newBalance"));
        assertEquals(0, result.get("streak"));
        verify(sysUserMapper).updateById(user);
        verify(transactionLogMapper).insert(any());
        verify(userOpLogMapper).insert(any());
        System.out.println("✓ 测试通过: 首次签到成功 - 获得积分=" + result.get("creditEarned") + ", 新余额=" + result.get("newBalance"));
    }

    @Test
    @DisplayName("signIn - 用户不存在抛出异常")
    void testSignInUserNotFound() {
        when(sysUserMapper.selectById(100L)).thenReturn(null);

        BizException exception = assertThrows(BizException.class, () -> signInService.signIn(100L));
        assertEquals("用户不存在", exception.getMessage());
        System.out.println("✓ 测试通过: 用户不存在时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("signIn - Redis缓存今日已签到抛出异常")
    void testSignInAlreadyCached() {
        SysUser user = mockUser(100L, 100);
        when(sysUserMapper.selectById(100L)).thenReturn(user);
        when(redisService.get("sign:today:100")).thenReturn(true);

        BizException exception = assertThrows(BizException.class, () -> signInService.signIn(100L));
        assertEquals("今日已签到", exception.getMessage());
        System.out.println("✓ 测试通过: Redis缓存已签到时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("signIn - 数据库记录今日已签到抛出异常")
    void testSignInAlreadyInDb() {
        SysUser user = mockUser(100L, 100);
        when(sysUserMapper.selectById(100L)).thenReturn(user);
        when(redisService.get("sign:today:100")).thenReturn(null);
        mockSignInRule();
        when(transactionLogMapper.selectCount(any())).thenReturn(1L);

        BizException exception = assertThrows(BizException.class, () -> signInService.signIn(100L));
        assertEquals("今日已签到", exception.getMessage());
        System.out.println("✓ 测试通过: 数据库已签到时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("signIn - 签到规则不存在抛出异常")
    void testSignInRuleNotFound() {
        SysUser user = mockUser(100L, 100);
        when(sysUserMapper.selectById(100L)).thenReturn(user);
        when(redisService.get("sign:today:100")).thenReturn(null);
        when(creditRuleMapper.selectOne(any())).thenReturn(null);

        BizException exception = assertThrows(BizException.class, () -> signInService.signIn(100L));
        assertEquals("签到规则不存在或已停用", exception.getMessage());
        System.out.println("✓ 测试通过: 签到规则不存在时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("getSignInStatus - 今日已签到状态")
    void testGetSignInStatusSigned() {
        SysUser user = mockUser(100L, 200);
        when(sysUserMapper.selectById(100L)).thenReturn(user);
        when(redisService.get("sign:today:100")).thenReturn(true);
        mockSignInRule();
        when(redisService.get("sign:streak:100")).thenReturn(3);

        Map<String, Object> result = signInService.getSignInStatus(100L);

        assertNotNull(result);
        assertTrue((Boolean) result.get("hasSignedIn"));
        assertEquals(3, result.get("streak"));
        assertEquals(200, result.get("currentBalance"));
        assertEquals(5, result.get("signInCredit"));
        System.out.println("✓ 测试通过: 已签到状态 - 连续天数=" + result.get("streak"));
    }

    @Test
    @DisplayName("getSignInStatus - 今日未签到状态")
    void testGetSignInStatusUnsigned() {
        SysUser user = mockUser(100L, 200);
        when(sysUserMapper.selectById(100L)).thenReturn(user);
        when(redisService.get("sign:today:100")).thenReturn(null);
        mockSignInRule();
        when(transactionLogMapper.selectCount(any())).thenReturn(0L);
        when(redisService.get("sign:streak:100")).thenReturn(null);
        when(transactionLogMapper.selectList(any())).thenReturn(Collections.emptyList());
        doNothing().when(redisService).set(anyString(), any(), anyLong(), any(TimeUnit.class));

        Map<String, Object> result = signInService.getSignInStatus(100L);

        assertNotNull(result);
        assertFalse((Boolean) result.get("hasSignedIn"));
        assertEquals(0, result.get("streak"));
        assertEquals(200, result.get("currentBalance"));
        System.out.println("✓ 测试通过: 未签到状态 - 连续天数=" + result.get("streak"));
    }

    @Test
    @DisplayName("getSignInHistory - 查询签到历史")
    void testGetSignInHistory() {
        mockSignInRule();
        TransactionLog log = new TransactionLog();
        log.setId(1L);
        log.setUserId(100L);
        log.setAmount(5);
        log.setBizType("DAILY");
        log.setRelatedRuleId(1L);
        log.setDescription("签到打卡");
        log.setCreatedAt(LocalDateTime.now().minusDays(1));

        when(transactionLogMapper.selectList(any())).thenReturn(Collections.singletonList(log));

        List<TransactionLog> result = signInService.getSignInHistory(100L, 10);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(5, result.get(0).getAmount());
        assertEquals("DAILY", result.get(0).getBizType());
        System.out.println("✓ 测试通过: 签到历史查询 - 数量=" + result.size());
    }

    @Test
    @DisplayName("getSignInHistory - 签到规则不存在返回空列表")
    void testGetSignInHistoryNoRule() {
        when(creditRuleMapper.selectOne(any())).thenReturn(null);

        List<TransactionLog> result = signInService.getSignInHistory(100L, 10);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        System.out.println("✓ 测试通过: 无签到规则时历史为空 - 数量=" + result.size());
    }
}
