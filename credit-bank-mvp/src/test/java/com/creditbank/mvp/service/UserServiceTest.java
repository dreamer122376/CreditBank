package com.creditbank.mvp.service;

import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.entity.UserOpLog;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.mapper.UserOpLogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("UserService 单元测试")
class UserServiceTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private UserOpLogMapper userOpLogMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(sysUserMapper, userOpLogMapper, passwordEncoder);
        System.out.println("========== 开始执行: " + testInfo.getDisplayName() + " ==========");
    }

    @Test
    @DisplayName("getUser - 获取用户成功")
    void testGetUserSuccess() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("test");
        user.setRealName("Test User");

        when(sysUserMapper.selectById(1L)).thenReturn(user);

        SysUser result = userService.getUser(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test", result.getUsername());
        System.out.println("✓ 测试通过: 获取用户成功 - ID=" + result.getId() + ", 用户名=" + result.getUsername() + ", 真实姓名=" + result.getRealName());
    }

    @Test
    @DisplayName("getUser - 用户不存在")
    void testGetUserNotFound() {
        when(sysUserMapper.selectById(1L)).thenReturn(null);

        BizException exception = assertThrows(BizException.class, () ->
                userService.getUser(1L));
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

        when(sysUserMapper.selectList(any())).thenReturn(Arrays.asList(user2, user1));

        List<SysUser> result = userService.listUsers();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(2L, result.get(0).getId());
        System.out.println("✓ 测试通过: 获取用户列表成功 - 用户数量=" + result.size() + ", 第一个用户ID=" + result.get(0).getId());
    }

    @Test
    @DisplayName("updateStatus - 冻结用户成功")
    void testUpdateStatusFreezeSuccess() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("student");
        user.setRealName("Student");
        user.setRole("student");
        user.setStatus(1);

        SysUser operator = new SysUser();
        operator.setId(2L);
        operator.setRealName("Admin");

        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(sysUserMapper.updateById(any())).thenReturn(1);
        when(userOpLogMapper.insert(any())).thenReturn(1);

        SysUser result = userService.updateStatus(1L, 0, operator);

        assertNotNull(result);
        assertEquals(0, result.getStatus());
        verify(sysUserMapper).updateById(user);
        verify(userOpLogMapper).insert(any());
        System.out.println("✓ 测试通过: 冻结用户成功 - 用户ID=" + result.getId() + ", 状态=" + result.getStatus() + " (0=冻结)");
    }

    @Test
    @DisplayName("updateStatus - 解冻用户成功")
    void testUpdateStatusUnfreezeSuccess() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("student");
        user.setRealName("Student");
        user.setRole("student");
        user.setStatus(0);

        SysUser operator = new SysUser();
        operator.setId(2L);
        operator.setRealName("Admin");

        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(sysUserMapper.updateById(any())).thenReturn(1);
        when(userOpLogMapper.insert(any())).thenReturn(1);

        SysUser result = userService.updateStatus(1L, 1, operator);

        assertNotNull(result);
        assertEquals(1, result.getStatus());
        verify(sysUserMapper).updateById(user);
        verify(userOpLogMapper).insert(any());
        System.out.println("✓ 测试通过: 解冻用户成功 - 用户ID=" + result.getId() + ", 状态=" + result.getStatus() + " (1=正常)");
    }

    @Test
    @DisplayName("updateStatus - 不能冻结自己")
    void testUpdateStatusCannotFreezeSelf() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setRealName("Admin");
        user.setRole("admin");
        user.setStatus(1);

        SysUser operator = new SysUser();
        operator.setId(1L);
        operator.setRealName("Admin");

        when(sysUserMapper.selectById(1L)).thenReturn(user);

        BizException exception = assertThrows(BizException.class, () ->
                userService.updateStatus(1L, 0, operator));
        assertEquals("不能冻结自己的账户", exception.getMessage());
        System.out.println("✓ 测试通过: 不能冻结自己时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("updateStatus - 不能冻结系统管理员")
    void testUpdateStatusCannotFreezeAdmin() {
        SysUser adminUser = new SysUser();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setRealName("System Admin");
        adminUser.setRole("admin");
        adminUser.setStatus(1);

        SysUser operator = new SysUser();
        operator.setId(2L);
        operator.setRealName("Operator");

        when(sysUserMapper.selectById(1L)).thenReturn(adminUser);

        BizException exception = assertThrows(BizException.class, () ->
                userService.updateStatus(1L, 0, operator));
        assertEquals("不能冻结系统管理员账户", exception.getMessage());
        System.out.println("✓ 测试通过: 不能冻结系统管理员时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("batchUpdateStatus - 批量冻结成功")
    void testBatchUpdateStatusFreezeSuccess() {
        SysUser user1 = new SysUser();
        user1.setId(1L);
        user1.setUsername("student1");
        user1.setRealName("Student1");
        user1.setRole("student");
        user1.setStatus(1);

        SysUser user2 = new SysUser();
        user2.setId(2L);
        user2.setUsername("student2");
        user2.setRealName("Student2");
        user2.setRole("student");
        user2.setStatus(1);

        SysUser operator = new SysUser();
        operator.setId(3L);
        operator.setRealName("Admin");

        when(sysUserMapper.selectById(1L)).thenReturn(user1);
        when(sysUserMapper.selectById(2L)).thenReturn(user2);
        when(sysUserMapper.updateById(any())).thenReturn(1);
        when(userOpLogMapper.insert(any())).thenReturn(1);

        userService.batchUpdateStatus(Arrays.asList(1L, 2L), 0, operator);

        assertEquals(0, user1.getStatus());
        assertEquals(0, user2.getStatus());
        verify(sysUserMapper, times(2)).updateById(any());
        verify(userOpLogMapper).insert(any());
        System.out.println("✓ 测试通过: 批量冻结成功 - 批量冻结2个用户, 用户1状态=" + user1.getStatus() + ", 用户2状态=" + user2.getStatus());
    }

    @Test
    @DisplayName("batchUpdateStatus - 跳过管理员和自己")
    void testBatchUpdateStatusSkipAdminAndSelf() {
        SysUser adminUser = new SysUser();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setRealName("Admin");
        adminUser.setRole("admin");

        SysUser selfUser = new SysUser();
        selfUser.setId(2L);
        selfUser.setUsername("operator");
        selfUser.setRealName("Operator");
        selfUser.setRole("org_admin");

        SysUser normalUser = new SysUser();
        normalUser.setId(3L);
        normalUser.setUsername("student");
        normalUser.setRealName("Student");
        normalUser.setRole("student");
        normalUser.setStatus(1);

        SysUser operator = new SysUser();
        operator.setId(2L);
        operator.setRealName("Operator");

        when(sysUserMapper.selectById(1L)).thenReturn(adminUser);
        when(sysUserMapper.selectById(2L)).thenReturn(selfUser);
        when(sysUserMapper.selectById(3L)).thenReturn(normalUser);
        when(sysUserMapper.updateById(any())).thenReturn(1);
        when(userOpLogMapper.insert(any())).thenReturn(1);

        userService.batchUpdateStatus(Arrays.asList(1L, 2L, 3L), 0, operator);

        assertEquals(0, normalUser.getStatus());
        verify(sysUserMapper).updateById(normalUser);
        verify(userOpLogMapper).insert(any());
        System.out.println("✓ 测试通过: 批量冻结跳过管理员和自己 - 跳过2个用户, 只冻结普通用户(ID=3), 状态=" + normalUser.getStatus());
    }

    @Test
    @DisplayName("resetPassword - 重置密码成功")
    void testResetPasswordSuccess() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("student");
        user.setRealName("Student");
        user.setRole("student");
        user.setPassword("oldPassword");

        SysUser operator = new SysUser();
        operator.setId(2L);
        operator.setRealName("Admin");

        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(passwordEncoder.encode("newPassword")).thenReturn("encryptedNewPassword");
        when(sysUserMapper.updateById(any())).thenReturn(1);
        when(userOpLogMapper.insert(any())).thenReturn(1);

        userService.resetPassword(1L, "newPassword", operator);

        assertEquals("encryptedNewPassword", user.getPassword());
        verify(sysUserMapper).updateById(user);
        verify(userOpLogMapper).insert(any());
        System.out.println("✓ 测试通过: 重置密码成功 - 用户ID=" + user.getId() + ", 旧密码=oldPassword, 新密码(加密后)=" + user.getPassword());
    }

    @Test
    @DisplayName("resetPassword - 不能重置自己的密码")
    void testResetPasswordCannotResetSelf() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("admin");
        user.setRealName("Admin");
        user.setRole("admin");

        SysUser operator = new SysUser();
        operator.setId(1L);
        operator.setRealName("Admin");

        when(sysUserMapper.selectById(1L)).thenReturn(user);

        BizException exception = assertThrows(BizException.class, () ->
                userService.resetPassword(1L, "newPassword", operator));
        assertEquals("不能重置密码自己的账户", exception.getMessage());
        System.out.println("✓ 测试通过: 不能重置自己密码时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("resetPassword - 不能重置系统管理员密码")
    void testResetPasswordCannotResetAdmin() {
        SysUser adminUser = new SysUser();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setRealName("System Admin");
        adminUser.setRole("admin");

        SysUser operator = new SysUser();
        operator.setId(2L);
        operator.setRealName("Operator");

        when(sysUserMapper.selectById(1L)).thenReturn(adminUser);

        BizException exception = assertThrows(BizException.class, () ->
                userService.resetPassword(1L, "newPassword", operator));
        assertEquals("不能重置密码系统管理员账户", exception.getMessage());
        System.out.println("✓ 测试通过: 不能重置系统管理员密码时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("updateUser - 更新用户信息成功")
    void testUpdateUserSuccess() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("student");
        user.setRealName("Old Name");
        user.setPhone("13800000000");
        user.setEmail("old@test.com");
        user.setRole("student");
        user.setOrgId(1L);

        SysUser update = new SysUser();
        update.setRealName("New Name");
        update.setPhone("13900000000");

        SysUser operator = new SysUser();
        operator.setId(2L);
        operator.setRealName("Admin");

        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(sysUserMapper.updateById(any())).thenReturn(1);
        when(userOpLogMapper.insert(any())).thenReturn(1);

        SysUser result = userService.updateUser(1L, update, operator);

        assertNotNull(result);
        assertEquals("New Name", result.getRealName());
        assertEquals("13900000000", result.getPhone());
        assertEquals("old@test.com", result.getEmail());
        verify(sysUserMapper).updateById(user);
        verify(userOpLogMapper).insert(any());
        System.out.println("✓ 测试通过: 更新用户信息成功 - 用户ID=" + result.getId() + ", 真实姓名: Old Name→" + result.getRealName() + ", 手机号: 13800000000→" + result.getPhone());
    }

    @Test
    @DisplayName("getOpLogs - 获取操作日志")
    void testGetOpLogs() {
        UserOpLog log1 = new UserOpLog();
        log1.setId(1L);
        log1.setOperatorId(1L);
        log1.setAction("UPDATE");

        UserOpLog log2 = new UserOpLog();
        log2.setId(2L);
        log2.setOperatorId(1L);
        log2.setAction("FREEZE");

        @SuppressWarnings("unchecked")
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserOpLog> page = mock(
                com.baomidou.mybatisplus.extension.plugins.pagination.Page.class);
        when(page.getRecords()).thenReturn(Arrays.asList(log2, log1));
        when(userOpLogMapper.selectPage(any(), any())).thenReturn(page);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserOpLog> result = userService.getOpLogs(1, 10);

        assertNotNull(result);
        assertEquals(2, result.getRecords().size());
        System.out.println("✓ 测试通过: 获取操作日志成功 - 日志数量=" + result.getRecords().size() + ", 最新日志ID=" + result.getRecords().get(0).getId() + ", 操作=" + result.getRecords().get(0).getAction());
    }
}