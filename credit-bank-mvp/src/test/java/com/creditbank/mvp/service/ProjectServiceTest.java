package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.dto.ProjectDetailDTO;
import com.creditbank.mvp.dto.ProjectListDTO;
import com.creditbank.mvp.entity.Application;
import com.creditbank.mvp.entity.Organization;
import com.creditbank.mvp.entity.Project;
import com.creditbank.mvp.entity.StudentProject;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.entity.UserOpLog;
import com.creditbank.mvp.mapper.OrganizationMapper;
import com.creditbank.mvp.mapper.ProjectMapper;
import com.creditbank.mvp.mapper.StudentProjectMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.mapper.UserOpLogMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("ProjectService 单元测试")
class ProjectServiceTest {

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private OrganizationMapper organizationMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private StudentProjectMapper studentProjectMapper;

    @Mock
    private UserOpLogMapper userOpLogMapper;

    @Mock
    private ApplicationService applicationService;

    private ProjectService projectService;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        MockitoAnnotations.openMocks(this);
        projectService = new ProjectService(projectMapper, organizationMapper, sysUserMapper,
                studentProjectMapper, userOpLogMapper, applicationService);
        System.out.println("========== 开始执行: " + testInfo.getDisplayName() + " ==========");
    }

    private Project mockProject(Long id, String name, Integer status, Long orgId) {
        Project p = new Project();
        p.setId(id);
        p.setName(name);
        p.setDescription("desc");
        p.setCreditReward(100);
        p.setCreditPrice(0);
        p.setStatus(status);
        p.setOrgId(orgId);
        return p;
    }

    @Test
    @DisplayName("getById - 获取项目成功")
    void testGetByIdSuccess() {
        Project p = mockProject(1L, "Project A", ProjectService.STATUS_APPROVED, 1L);
        when(projectMapper.selectById(1L)).thenReturn(p);

        Project result = projectService.getById(1L);

        assertNotNull(result);
        assertEquals("Project A", result.getName());
        System.out.println("✓ 测试通过: 获取项目成功 - ID=" + result.getId() + ", 名称=" + result.getName());
    }

    @Test
    @DisplayName("getById - 项目不存在")
    void testGetByIdNotFound() {
        when(projectMapper.selectById(1L)).thenReturn(null);

        BizException exception = assertThrows(BizException.class, () -> projectService.getById(1L));
        assertEquals("项目不存在：1", exception.getMessage());
        System.out.println("✓ 测试通过: 项目不存在时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("getDetail - 获取项目详情成功")
    void testGetDetail() {
        Project p = mockProject(1L, "Project A", ProjectService.STATUS_APPROVED, 1L);
        Organization org = new Organization();
        org.setId(1L);
        org.setName("Computer School");

        StudentProject enrollment = new StudentProject();
        enrollment.setId(1L);
        enrollment.setStudentId(100L);
        enrollment.setProjectId(1L);
        enrollment.setStatus(StudentProject.STATUS_ENROLLED);

        SysUser student = new SysUser();
        student.setId(100L);
        student.setUsername("s100");
        student.setRealName("Student");

        when(projectMapper.selectById(1L)).thenReturn(p);
        when(organizationMapper.selectById(1L)).thenReturn(org);
        when(studentProjectMapper.selectByProjectId(1L)).thenReturn(Collections.singletonList(enrollment));
        when(sysUserMapper.selectBatchIds(Collections.singletonList(100L))).thenReturn(Collections.singletonList(student));

        ProjectDetailDTO result = projectService.getDetail(1L, 100L);

        assertNotNull(result);
        assertEquals("Project A", result.getName());
        assertEquals(1, result.getEnrolledStudents().size());
        assertTrue(result.getEnrolled());
        System.out.println("✓ 测试通过: 获取项目详情成功 - 已报名学生=" + result.getEnrolledStudents().size());
    }

    @Test
    @DisplayName("page - 管理员查看全部项目")
    void testPageAdmin() {
        Project p = mockProject(1L, "Project A", ProjectService.STATUS_PENDING, 1L);

        @SuppressWarnings("unchecked")
        Page<Project> page = mock(Page.class);
        when(page.getCurrent()).thenReturn(1L);
        when(page.getSize()).thenReturn(10L);
        when(page.getTotal()).thenReturn(1L);
        when(page.getRecords()).thenReturn(Collections.singletonList(p));
        when(projectMapper.selectPage(any(), any())).thenReturn(page);
        when(organizationMapper.selectBatchIds(any())).thenReturn(Collections.emptyList());

        Page<ProjectListDTO> result = projectService.page("admin", null, null, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        System.out.println("✓ 测试通过: 管理员分页查看项目 - 数量=" + result.getRecords().size());
    }

    @Test
    @DisplayName("page - 机构管理员只看本机构项目")
    void testPageOrgAdmin() {
        Project p = mockProject(1L, "Project A", ProjectService.STATUS_PENDING, 1L);

        @SuppressWarnings("unchecked")
        Page<Project> page = mock(Page.class);
        when(page.getCurrent()).thenReturn(1L);
        when(page.getSize()).thenReturn(10L);
        when(page.getTotal()).thenReturn(1L);
        when(page.getRecords()).thenReturn(Collections.singletonList(p));
        when(projectMapper.selectPage(any(), any())).thenReturn(page);
        when(organizationMapper.selectBatchIds(any())).thenReturn(Collections.emptyList());

        Page<ProjectListDTO> result = projectService.page("org_admin", 1L, null, 1, 10);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        System.out.println("✓ 测试通过: 机构管理员分页查看本机构项目 - 数量=" + result.getRecords().size());
    }

    @Test
    @DisplayName("listActive - 学生查看已上架项目")
    void testListActiveStudent() {
        Project p = mockProject(1L, "Project A", ProjectService.STATUS_APPROVED, 1L);
        SysUser student = new SysUser();
        student.setId(100L);
        student.setOrgId(1L);

        when(sysUserMapper.selectById(100L)).thenReturn(student);
        when(projectMapper.selectList(any())).thenReturn(Collections.singletonList(p));
        when(studentProjectMapper.selectList(any())).thenReturn(Collections.emptyList());

        List<ProjectListDTO> result = projectService.listActive(100L);

        assertNotNull(result);
        assertEquals(1, result.size());
        System.out.println("✓ 测试通过: 学生查看已上架项目 - 数量=" + result.size());
    }

    @Test
    @DisplayName("create - 机构管理员创建项目成功")
    void testCreateSuccess() {
        SysUser operator = new SysUser();
        operator.setId(10L);
        operator.setRole("org_admin");
        operator.setOrgId(1L);
        operator.setRealName("Org Admin");

        Project project = new Project();
        project.setName("New Project");
        project.setDescription("desc");
        project.setCreditReward(100);
        project.setCreditPrice(0);

        Project saved = mockProject(1L, "New Project", ProjectService.STATUS_IN_REVIEW, 1L);
        Application app = new Application();
        app.setId(100L);

        when(projectMapper.insert(any())).thenAnswer(inv -> {
            Project arg = inv.getArgument(0);
            arg.setId(1L);
            return 1;
        });
        when(projectMapper.selectById(1L)).thenReturn(saved);
        when(applicationService.submit(any())).thenReturn(app);
        when(projectMapper.updateById(any())).thenReturn(1);
        when(userOpLogMapper.insert(any())).thenReturn(1);

        Project result = projectService.create(project, operator);

        assertNotNull(result);
        assertEquals(ProjectService.STATUS_IN_REVIEW, result.getStatus());
        assertEquals(1L, result.getOrgId());
        verify(applicationService).submit(any());
        verify(userOpLogMapper).insert(any());
        System.out.println("✓ 测试通过: 机构管理员创建项目成功 - 状态=审核中");
    }

    @Test
    @DisplayName("create - 非机构管理员被拒绝")
    void testCreateNotOrgAdmin() {
        SysUser operator = new SysUser();
        operator.setId(10L);
        operator.setRole("student");

        Project project = new Project();
        project.setName("New Project");
        project.setCreditReward(100);
        project.setCreditPrice(0);

        BizException exception = assertThrows(BizException.class, () ->
                projectService.create(project, operator));
        assertEquals("只有机构管理员可以操作", exception.getMessage());
        System.out.println("✓ 测试通过: 非机构管理员创建项目被拒绝 - " + exception.getMessage());
    }

    @Test
    @DisplayName("create - 项目名称为空")
    void testCreateNameEmpty() {
        SysUser operator = new SysUser();
        operator.setId(10L);
        operator.setRole("org_admin");
        operator.setOrgId(1L);

        Project project = new Project();
        project.setName("  ");
        project.setCreditReward(100);
        project.setCreditPrice(0);

        BizException exception = assertThrows(BizException.class, () ->
                projectService.create(project, operator));
        assertEquals("项目名称不能为空", exception.getMessage());
        System.out.println("✓ 测试通过: 项目名称为空时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("audit - 管理员审核通过项目")
    void testAuditApprove() {
        SysUser operator = new SysUser();
        operator.setId(1L);
        operator.setRole("admin");
        operator.setRealName("Admin");

        Project p = mockProject(1L, "Project A", ProjectService.STATUS_PENDING, 1L);
        when(projectMapper.selectById(1L)).thenReturn(p);
        when(projectMapper.updateById(any())).thenReturn(1);
        when(userOpLogMapper.insert(any())).thenReturn(1);

        Project result = projectService.audit(1L, true, null, operator);

        assertNotNull(result);
        assertEquals(ProjectService.STATUS_APPROVED, result.getStatus());
        verify(userOpLogMapper).insert(any());
        System.out.println("✓ 测试通过: 管理员审核通过项目 - 状态=已通过");
    }

    @Test
    @DisplayName("audit - 管理员驳回项目")
    void testAuditReject() {
        SysUser operator = new SysUser();
        operator.setId(1L);
        operator.setRole("admin");
        operator.setRealName("Admin");

        Project p = mockProject(1L, "Project A", ProjectService.STATUS_PENDING, 1L);
        when(projectMapper.selectById(1L)).thenReturn(p);
        when(projectMapper.updateById(any())).thenReturn(1);
        when(userOpLogMapper.insert(any())).thenReturn(1);

        Project result = projectService.audit(1L, false, "不符合要求", operator);

        assertNotNull(result);
        assertEquals(ProjectService.STATUS_REJECTED, result.getStatus());
        System.out.println("✓ 测试通过: 管理员驳回项目 - 状态=已驳回");
    }

    @Test
    @DisplayName("audit - 非管理员被拒绝")
    void testAuditNotAdmin() {
        SysUser operator = new SysUser();
        operator.setId(10L);
        operator.setRole("org_admin");

        Project p = mockProject(1L, "Project A", ProjectService.STATUS_PENDING, 1L);
        when(projectMapper.selectById(1L)).thenReturn(p);

        BizException exception = assertThrows(BizException.class, () ->
                projectService.audit(1L, true, null, operator));
        assertEquals("只有系统管理员可以操作", exception.getMessage());
        System.out.println("✓ 测试通过: 非管理员审核项目被拒绝 - " + exception.getMessage());
    }

    @Test
    @DisplayName("audit - 非待审核状态不可审核")
    void testAuditInvalidStatus() {
        SysUser operator = new SysUser();
        operator.setId(1L);
        operator.setRole("admin");

        Project p = mockProject(1L, "Project A", ProjectService.STATUS_APPROVED, 1L);
        when(projectMapper.selectById(1L)).thenReturn(p);

        BizException exception = assertThrows(BizException.class, () ->
                projectService.audit(1L, true, null, operator));
        assertEquals("当前状态不可审核", exception.getMessage());
        System.out.println("✓ 测试通过: 非待审核状态审核时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("offline - 管理员下架已上架项目")
    void testOfflineSuccess() {
        SysUser operator = new SysUser();
        operator.setId(1L);
        operator.setRole("admin");
        operator.setRealName("Admin");

        Project p = mockProject(1L, "Project A", ProjectService.STATUS_APPROVED, 1L);
        when(projectMapper.selectById(1L)).thenReturn(p);
        when(projectMapper.updateById(any())).thenReturn(1);
        when(userOpLogMapper.insert(any())).thenReturn(1);

        Project result = projectService.offline(1L, operator);

        assertNotNull(result);
        assertEquals(ProjectService.STATUS_OFFLINE, result.getStatus());
        System.out.println("✓ 测试通过: 管理员下架项目成功 - 状态=已下架");
    }

    @Test
    @DisplayName("offline - 非已上架项目不可下架")
    void testOfflineInvalidStatus() {
        SysUser operator = new SysUser();
        operator.setId(1L);
        operator.setRole("admin");

        Project p = mockProject(1L, "Project A", ProjectService.STATUS_PENDING, 1L);
        when(projectMapper.selectById(1L)).thenReturn(p);

        BizException exception = assertThrows(BizException.class, () ->
                projectService.offline(1L, operator));
        assertEquals("只有已上架项目可以下架", exception.getMessage());
        System.out.println("✓ 测试通过: 非已上架项目下架时正确抛出异常 - " + exception.getMessage());
    }
}