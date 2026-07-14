package com.creditbank.mvp.service;

import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.mapper.OrganizationMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("ProfileService 单元测试")
class ProfileServiceTest {

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private OrganizationMapper organizationMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    private ProfileService profileService;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        MockitoAnnotations.openMocks(this);
        profileService = new ProfileService(sysUserMapper, organizationMapper, passwordEncoder);
        System.out.println("========== 开始执行: " + testInfo.getDisplayName() + " ==========");
    }

    @Test
    @DisplayName("changePassword - 修改密码成功")
    void testChangePasswordSuccess() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("test");
        user.setPassword("encodedOldPassword");

        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("encodedNewPassword");
        when(sysUserMapper.update(any(), any())).thenReturn(1);

        profileService.changePassword(1L, "oldPassword", "newPassword123");

        verify(sysUserMapper).update(any(), any());
        System.out.println("✓ 测试通过: 修改密码成功 - 新密码已加密存储");
    }

    @Test
    @DisplayName("changePassword - 原密码不正确")
    void testChangePasswordOldPasswordWrong() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("test");
        user.setPassword("encodedOldPassword");

        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(passwordEncoder.matches("wrongPassword", "encodedOldPassword")).thenReturn(false);

        BizException exception = assertThrows(BizException.class, () ->
                profileService.changePassword(1L, "wrongPassword", "newPassword123"));
        assertEquals("原密码不正确", exception.getMessage());
        System.out.println("✓ 测试通过: 原密码不正确时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("changePassword - 新密码太短")
    void testChangePasswordNewPasswordTooShort() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("test");
        user.setPassword("encodedOldPassword");

        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(passwordEncoder.matches("oldPassword", "encodedOldPassword")).thenReturn(true);

        BizException exception = assertThrows(BizException.class, () ->
                profileService.changePassword(1L, "oldPassword", "123"));
        assertEquals("新密码至少 6 位", exception.getMessage());
        System.out.println("✓ 测试通过: 新密码太短时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("changePassword - 用户不存在")
    void testChangePasswordUserNotFound() {
        when(sysUserMapper.selectById(1L)).thenReturn(null);

        BizException exception = assertThrows(BizException.class, () ->
                profileService.changePassword(1L, "oldPassword", "newPassword123"));
        assertEquals("用户不存在：1", exception.getMessage());
        System.out.println("✓ 测试通过: 用户不存在时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("changePassword - 原密码为空")
    void testChangePasswordOldPasswordNull() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("test");
        user.setPassword("encodedOldPassword");

        when(sysUserMapper.selectById(1L)).thenReturn(user);

        BizException exception = assertThrows(BizException.class, () ->
                profileService.changePassword(1L, null, "newPassword123"));
        assertEquals("原密码不正确", exception.getMessage());
        System.out.println("✓ 测试通过: 原密码为空时正确抛出异常 - " + exception.getMessage());
    }
}