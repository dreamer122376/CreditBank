package com.creditbank.mvp.service;

import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.dto.OrganizationAuditResult;
import com.creditbank.mvp.entity.Organization;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.mapper.OrganizationMapper;
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
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("OrganizationService 单元测试")
class OrganizationServiceTest {

    @Mock
    private OrganizationMapper organizationMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private NotificationService notificationService;

    @Mock
    private UserOpLogMapper userOpLogMapper;

    private OrganizationService organizationService;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        MockitoAnnotations.openMocks(this);
        organizationService = new OrganizationService(organizationMapper, sysUserMapper, passwordEncoder, notificationService, userOpLogMapper);
        System.out.println("========== 开始执行: " + testInfo.getDisplayName() + " ==========");
    }

    @Test
    @DisplayName("list - 获取机构列表并计算积分池")
    void testList() {
        Organization org = new Organization();
        org.setId(1L);
        org.setName("Computer School");

        SysUser admin = new SysUser();
        admin.setId(10L);
        admin.setRole("org_admin");
        admin.setOrgId(1L);
        admin.setBalance(500);

        when(organizationMapper.selectList(any())).thenReturn(Collections.singletonList(org));
        when(sysUserMapper.selectOne(any())).thenReturn(admin);

        List<Organization> result = organizationService.list();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(500, result.get(0).getCreditPool());
        System.out.println("✓ 测试通过: 获取机构列表成功 - 机构=" + result.get(0).getName() + ", 积分池=" + result.get(0).getCreditPool());
    }

    @Test
    @DisplayName("create - 新增机构成功")
    void testCreateSuccess() {
        Organization org = new Organization();
        org.setName("Computer School");
        org.setContactPerson("John");

        Organization saved = new Organization();
        saved.setId(1L);
        saved.setName("Computer School");
        saved.setStatus(Organization.STATUS_PENDING);

        when(organizationMapper.insert(any())).thenAnswer(inv -> {
            Organization arg = inv.getArgument(0);
            arg.setId(1L);
            return 1;
        });
        when(organizationMapper.selectById(1L)).thenReturn(saved);

        Organization result = organizationService.create(org);

        assertNotNull(result);
        assertEquals(Organization.STATUS_PENDING, result.getStatus());
        assertEquals("Computer School", result.getName());
        System.out.println("✓ 测试通过: 新增机构成功 - ID=" + result.getId() + ", 状态=待审核");
    }

    @Test
    @DisplayName("create - 机构名称不能为空")
    void testCreateNameEmpty() {
        Organization org = new Organization();
        org.setName("  ");

        BizException exception = assertThrows(BizException.class, () -> organizationService.create(org));
        assertEquals("机构名称不能为空", exception.getMessage());
        System.out.println("✓ 测试通过: 机构名称为空时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("changeStatus - 审核通过并自动生成管理员账号")
    void testChangeStatusAuditPassCreateAdmin() {
        Organization org = new Organization();
        org.setId(1L);
        org.setName("Computer School");
        org.setContactPerson("John");
        org.setContactPhone("13800000000");
        org.setStatus(Organization.STATUS_PENDING);

        when(organizationMapper.selectById(1L)).thenReturn(org);
        when(organizationMapper.updateById(any())).thenReturn(1);
        when(sysUserMapper.selectOne(any())).thenReturn(null);
        when(sysUserMapper.selectCount(any())).thenReturn(0L);
        when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");
        when(sysUserMapper.insert(any())).thenReturn(1);

        OrganizationAuditResult result = organizationService.changeStatus(1L, Organization.STATUS_ENABLED);

        assertNotNull(result);
        assertTrue(result.isCreated());
        assertNotNull(result.getAdminUsername());
        assertEquals("123456", result.getAdminPassword());
        assertEquals(Organization.STATUS_ENABLED, result.getOrganization().getStatus());
        verify(sysUserMapper).insert(any());
        System.out.println("✓ 测试通过: 机构审核通过并生成管理员账号 - 账号=" + result.getAdminUsername());
    }

    @Test
    @DisplayName("changeStatus - 审核通过但机构已存在管理员账号")
    void testChangeStatusAuditPassExistingAdmin() {
        Organization org = new Organization();
        org.setId(1L);
        org.setName("Computer School");
        org.setStatus(Organization.STATUS_PENDING);

        SysUser existingAdmin = new SysUser();
        existingAdmin.setId(10L);
        existingAdmin.setUsername("existing_admin");
        existingAdmin.setRole("org_admin");

        when(organizationMapper.selectById(1L)).thenReturn(org);
        when(organizationMapper.updateById(any())).thenReturn(1);
        when(sysUserMapper.selectOne(any())).thenReturn(existingAdmin);

        OrganizationAuditResult result = organizationService.changeStatus(1L, Organization.STATUS_ENABLED);

        assertNotNull(result);
        assertFalse(result.isCreated());
        assertEquals("existing_admin", result.getAdminUsername());
        verify(sysUserMapper, never()).insert(any());
        System.out.println("✓ 测试通过: 机构审核通过时已存在管理员账号 - 未重复创建");
    }

    @Test
    @DisplayName("changeStatus - 禁用机构并冻结其下所有用户")
    void testChangeStatusDisableFreezeUsers() {
        Organization org = new Organization();
        org.setId(1L);
        org.setName("Computer School");
        org.setStatus(Organization.STATUS_ENABLED);

        when(organizationMapper.selectById(1L)).thenReturn(org);
        when(organizationMapper.updateById(any())).thenReturn(1);
        when(sysUserMapper.update(any(), any())).thenReturn(1);

        OrganizationAuditResult result = organizationService.changeStatus(1L, Organization.STATUS_DISABLED);

        assertNotNull(result);
        assertEquals(Organization.STATUS_DISABLED, result.getOrganization().getStatus());
        verify(sysUserMapper).update(any(), any());
        System.out.println("✓ 测试通过: 禁用机构成功并冻结用户 - 机构状态=" + result.getOrganization().getStatus());
    }

    @Test
    @DisplayName("changeStatus - 重新启用机构并解冻用户")
    void testChangeStatusReEnable() {
        Organization org = new Organization();
        org.setId(1L);
        org.setName("Computer School");
        org.setStatus(Organization.STATUS_DISABLED);

        when(organizationMapper.selectById(1L)).thenReturn(org);
        when(organizationMapper.updateById(any())).thenReturn(1);
        when(sysUserMapper.update(any(), any())).thenReturn(1);

        OrganizationAuditResult result = organizationService.changeStatus(1L, Organization.STATUS_ENABLED);

        assertNotNull(result);
        assertEquals(Organization.STATUS_ENABLED, result.getOrganization().getStatus());
        verify(sysUserMapper).update(any(), any());
        System.out.println("✓ 测试通过: 重新启用机构成功 - 机构状态=" + result.getOrganization().getStatus());
    }

    @Test
    @DisplayName("changeStatus - 非法状态抛出异常")
    void testChangeStatusInvalidStatus() {
        Organization org = new Organization();
        org.setId(1L);
        org.setStatus(Organization.STATUS_PENDING);

        when(organizationMapper.selectById(1L)).thenReturn(org);

        BizException exception = assertThrows(BizException.class, () ->
                organizationService.changeStatus(1L, 99));
        assertEquals("非法的机构状态：99", exception.getMessage());
        System.out.println("✓ 测试通过: 非法机构状态时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("changeStatus - 机构不存在")
    void testChangeStatusOrgNotFound() {
        when(organizationMapper.selectById(1L)).thenReturn(null);

        BizException exception = assertThrows(BizException.class, () ->
                organizationService.changeStatus(1L, Organization.STATUS_ENABLED));
        assertEquals("机构不存在：1", exception.getMessage());
        System.out.println("✓ 测试通过: 机构不存在时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("reject - 拒绝入驻申请成功")
    void testRejectSuccess() {
        Organization org = new Organization();
        org.setId(1L);
        org.setStatus(Organization.STATUS_PENDING);

        when(organizationMapper.selectById(1L)).thenReturn(org);
        when(organizationMapper.updateById(any())).thenReturn(1);

        organizationService.reject(1L, "信息不完整");

        assertEquals(Organization.STATUS_REJECTED, org.getStatus());
        assertEquals("信息不完整", org.getRejectReason());
        System.out.println("✓ 测试通过: 拒绝入驻申请成功 - 状态=已拒绝, 原因=" + org.getRejectReason());
    }

    @Test
    @DisplayName("reject - 非待审核状态不可拒绝")
    void testRejectNotPending() {
        Organization org = new Organization();
        org.setId(1L);
        org.setStatus(Organization.STATUS_ENABLED);

        when(organizationMapper.selectById(1L)).thenReturn(org);

        BizException exception = assertThrows(BizException.class, () ->
                organizationService.reject(1L, "原因"));
        assertEquals("只有待审核状态的机构才能拒绝", exception.getMessage());
        System.out.println("✓ 测试通过: 非待审核状态拒绝时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("reject - 拒绝原因不能为空")
    void testRejectReasonEmpty() {
        Organization org = new Organization();
        org.setId(1L);
        org.setStatus(Organization.STATUS_PENDING);

        when(organizationMapper.selectById(1L)).thenReturn(org);

        BizException exception = assertThrows(BizException.class, () ->
                organizationService.reject(1L, "  "));
        assertEquals("拒绝原因不能为空", exception.getMessage());
        System.out.println("✓ 测试通过: 拒绝原因为空时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("update - 更新机构信息成功")
    void testUpdateSuccess() {
        Organization exist = new Organization();
        exist.setId(1L);
        exist.setName("Old Name");

        Organization update = new Organization();
        update.setId(1L);
        update.setName("New Name");

        when(organizationMapper.selectById(1L)).thenReturn(exist);
        when(organizationMapper.updateById(any())).thenReturn(1);
        when(organizationMapper.selectById(1L)).thenReturn(update);

        Organization result = organizationService.update(update);

        assertNotNull(result);
        assertEquals("New Name", result.getName());
        System.out.println("✓ 测试通过: 更新机构信息成功 - 新名称=" + result.getName());
    }
}