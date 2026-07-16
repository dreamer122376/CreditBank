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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
        when(sysUserMapper.update(any(), any())).thenReturn(1);
        when(userOpLogMapper.insert(any())).thenReturn(1);

        userService.resetPassword(1L, "newPassword", operator);

        verify(sysUserMapper).update(any(), any());
        verify(userOpLogMapper).insert(any());
        System.out.println("✓ 测试通过: 重置密码成功 - 用户ID=" + user.getId() + ", 密码已加密存储");

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

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserOpLog> result = userService.getOpLogs(1, 10, null, null, null, null, null, null);

        assertNotNull(result);
        assertEquals(2, result.getRecords().size());
        System.out.println("✓ 测试通过: 获取操作日志成功 - 日志数量=" + result.getRecords().size() + ", 最新日志ID=" + result.getRecords().get(0).getId() + ", 操作=" + result.getRecords().get(0).getAction());
    }

    @Test
    @DisplayName("listByOrg - 机构管理员只查看本机构学生")
    void testListByOrg() {
        SysUser student1 = new SysUser();
        student1.setId(1L);
        student1.setUsername("s1");
        student1.setRole("student");
        student1.setOrgId(1L);

        SysUser student2 = new SysUser();
        student2.setId(2L);
        student2.setUsername("s2");
        student2.setRole("student");
        student2.setOrgId(2L);

        when(sysUserMapper.selectList(any())).thenReturn(Collections.singletonList(student1));

        List<SysUser> result = userService.listByOrg(1L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        System.out.println("✓ 测试通过: 机构管理员查看本机构学生列表 - 数量=" + result.size());
    }

    @Test
    @DisplayName("listAuditorCandidates - 管理员返回所有审核人")
    void testListAuditorCandidatesAdmin() {
        SysUser admin = new SysUser();
        admin.setId(1L);
        admin.setRole("admin");
        admin.setStatus(1);

        SysUser expert = new SysUser();
        expert.setId(2L);
        expert.setRole("expert");
        expert.setStatus(1);

        when(sysUserMapper.selectList(any())).thenReturn(Arrays.asList(admin, expert));

        List<SysUser> result = userService.listAuditorCandidates(null, 1L, "admin");

        assertNotNull(result);
        assertEquals(2, result.size());
        System.out.println("✓ 测试通过: 管理员查询审核候选人 - 数量=" + result.size());
    }

    @Test
    @DisplayName("listAuditorCandidates - 机构管理员只返回自己和本机构专家")
    void testListAuditorCandidatesOrgAdmin() {
        SysUser orgAdmin = new SysUser();
        orgAdmin.setId(1L);
        orgAdmin.setRole("org_admin");
        orgAdmin.setStatus(1);
        orgAdmin.setOrgId(1L);

        SysUser expert = new SysUser();
        expert.setId(2L);
        expert.setRole("expert");
        expert.setStatus(1);
        expert.setOrgId(1L);

        when(sysUserMapper.selectList(any())).thenReturn(Arrays.asList(orgAdmin, expert));

        List<SysUser> result = userService.listAuditorCandidates(1L, 1L, "org_admin");

        assertNotNull(result);
        assertEquals(2, result.size());
        System.out.println("✓ 测试通过: 机构管理员查询审核候选人 - 数量=" + result.size());
    }

    @Test
    @DisplayName("getOpLogs - 按模块筛选")
    void testGetOpLogsFilterByModule() {
        UserOpLog log = new UserOpLog();
        log.setId(1L);
        log.setModule(UserOpLog.MODULE_PROJECT);
        log.setAction(UserOpLog.ACTION_PROJECT_AUDIT);

        @SuppressWarnings("unchecked")
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserOpLog> page = mock(
                com.baomidou.mybatisplus.extension.plugins.pagination.Page.class);
        when(page.getRecords()).thenReturn(Collections.singletonList(log));
        when(userOpLogMapper.selectPage(any(), any())).thenReturn(page);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserOpLog> result =
                userService.getOpLogs(1, 10, null, UserOpLog.MODULE_PROJECT, null, null, null, null);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        assertEquals(UserOpLog.MODULE_PROJECT, result.getRecords().get(0).getModule());
        System.out.println("✓ 测试通过: 操作日志按模块筛选成功 - 模块=" + result.getRecords().get(0).getModule());
    }

    @Test
    @DisplayName("getOpLogs - 机构管理员只能查看本机构学生相关日志")
    void testGetOpLogsOrgAdminIsolation() {
        SysUser student = new SysUser();
        student.setId(10L);
        student.setRole("student");
        student.setOrgId(1L);

        UserOpLog log = new UserOpLog();
        log.setId(1L);
        log.setTargetUserId(10L);

        @SuppressWarnings("unchecked")
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserOpLog> page = mock(
                com.baomidou.mybatisplus.extension.plugins.pagination.Page.class);
        when(page.getRecords()).thenReturn(Collections.singletonList(log));
        when(sysUserMapper.selectList(any())).thenReturn(Collections.singletonList(student));
        when(userOpLogMapper.selectPage(any(), any())).thenReturn(page);

        com.baomidou.mybatisplus.extension.plugins.pagination.Page<UserOpLog> result =
                userService.getOpLogs(1, 10, null, null, null, null, null, 1L);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        assertEquals(10L, result.getRecords().get(0).getTargetUserId());
        System.out.println("✓ 测试通过: 机构管理员操作日志隔离成功 - 只能查看本机构学生日志");
    }

    @Test
    @DisplayName("batchUpdateStatus - 返回统计结果")
    void testBatchUpdateStatusReturnsResult() {
        SysUser user = new SysUser();
        user.setId(3L);
        user.setUsername("student");
        user.setRole("student");
        user.setStatus(1);

        SysUser operator = new SysUser();
        operator.setId(2L);
        operator.setRealName("Admin");

        when(sysUserMapper.selectById(3L)).thenReturn(user);
        when(sysUserMapper.updateById(any())).thenReturn(1);
        when(userOpLogMapper.insert(any())).thenReturn(1);

        Map<String, Object> result = userService.batchUpdateStatus(Collections.singletonList(3L), 0, operator);

        assertNotNull(result);
        assertEquals(1, result.get("total"));
        assertEquals(1, result.get("processed"));
        assertEquals(0, result.get("skipped"));
        System.out.println("✓ 测试通过: 批量更新状态返回统计结果 - total=" + result.get("total") + ", processed=" + result.get("processed"));
    }
}