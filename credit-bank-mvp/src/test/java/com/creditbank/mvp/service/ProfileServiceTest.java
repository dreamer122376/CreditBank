package com.creditbank.mvp.service;

import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.Organization;
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

    @Test
    @DisplayName("getProfile - 获取个人信息成功")
    void testGetProfileSuccess() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("student");
        user.setRealName("Student");
        user.setOrgId(1L);

        Organization org = new Organization();
        org.setId(1L);
        org.setName("Computer School");

        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(organizationMapper.selectById(1L)).thenReturn(org);

        SysUser result = profileService.getProfile(1L);

        assertNotNull(result);
        assertEquals("Computer School", result.getOrgName());
        System.out.println("✓ 测试通过: 获取个人信息成功 - 用户名=" + result.getUsername() + ", 机构=" + result.getOrgName());
    }

    @Test
    @DisplayName("getProfile - 用户不存在")
    void testGetProfileUserNotFound() {
        when(sysUserMapper.selectById(1L)).thenReturn(null);

        BizException exception = assertThrows(BizException.class, () ->
                profileService.getProfile(1L));
        assertEquals("用户不存在：1", exception.getMessage());
        System.out.println("✓ 测试通过: 获取个人信息用户不存在时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("updateProfile - 更新个人信息成功")
    void testUpdateProfileSuccess() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("student");
        user.setRealName("Old Name");
        user.setPhone("13800000000");
        user.setEmail("old@test.com");

        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(sysUserMapper.updateById(any())).thenReturn(1);

        SysUser result = profileService.updateProfile(1L, "New Name", "13900000000", "new@test.com");

        assertNotNull(result);
        assertEquals("New Name", result.getRealName());
        assertEquals("13900000000", result.getPhone());
        assertEquals("new@test.com", result.getEmail());
        verify(sysUserMapper).updateById(user);
        System.out.println("✓ 测试通过: 更新个人信息成功 - 真实姓名=" + result.getRealName() + ", 手机号=" + result.getPhone());
    }

    @Test
    @DisplayName("updateProfile - 真实姓名为空时不更新姓名")
    void testUpdateProfileEmptyName() {
        SysUser user = new SysUser();
        user.setId(1L);
        user.setUsername("student");
        user.setRealName("Old Name");
        user.setPhone("13800000000");

        when(sysUserMapper.selectById(1L)).thenReturn(user);
        when(sysUserMapper.updateById(any())).thenReturn(1);

        SysUser result = profileService.updateProfile(1L, "   ", "13900000000", null);

        assertNotNull(result);
        assertEquals("Old Name", result.getRealName());
        assertEquals("13900000000", result.getPhone());
        System.out.println("✓ 测试通过: 真实姓名为空时不更新姓名 - 姓名保持=" + result.getRealName());
    }
}