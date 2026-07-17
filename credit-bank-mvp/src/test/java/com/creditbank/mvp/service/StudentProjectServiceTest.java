package com.creditbank.mvp.service;

import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.dto.ProjectDetailDTO;
import com.creditbank.mvp.dto.ProjectEnrollmentDTO;
import com.creditbank.mvp.entity.Organization;
import com.creditbank.mvp.entity.Project;
import com.creditbank.mvp.entity.StudentProject;
import com.creditbank.mvp.entity.SysUser;
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

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("StudentProjectService 单元测试")
class StudentProjectServiceTest {

    @Mock
    private StudentProjectMapper studentProjectMapper;

    @Mock
    private ProjectService projectService;

    @Mock
    private SysUserMapper sysUserMapper;

    @Mock
    private OrganizationMapper organizationMapper;

    @Mock
    private UserOpLogMapper userOpLogMapper;

    @Mock
    private PointService pointService;

    @Mock
    private ProjectMapper projectMapper;

    @Mock
    private NotificationService notificationService;

    private StudentProjectService studentProjectService;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        MockitoAnnotations.openMocks(this);
        studentProjectService = new StudentProjectService(studentProjectMapper, projectService,
                projectMapper, sysUserMapper, organizationMapper, userOpLogMapper, pointService,
                notificationService);
        System.out.println("========== 开始执行: " + testInfo.getDisplayName() + " ==========");
    }

    private Project mockProject(Long id, Integer status, Long orgId) {
        Project p = new Project();
        p.setId(id);
        p.setName("Project " + id);
        p.setDescription("desc");
        p.setCreditReward(100);
        p.setCreditPrice(0);
        p.setStatus(status);
        p.setOrgId(orgId);
        return p;
    }

    @Test
    @DisplayName("getMyProjects - 获取我的项目列表")
    void testGetMyProjects() {
        SysUser student = new SysUser();
        student.setId(100L);
        student.setRole("student");

        StudentProject enrollment = new StudentProject();
        enrollment.setId(1L);
        enrollment.setStudentId(100L);
        enrollment.setProjectId(1L);
        enrollment.setStatus(StudentProject.STATUS_ENROLLED);

        Project project = mockProject(1L, ProjectService.STATUS_APPROVED, 1L);
        Organization org = new Organization();
        org.setId(1L);
        org.setName("Computer School");

        when(sysUserMapper.selectById(100L)).thenReturn(student);
        when(studentProjectMapper.selectByStudentId(100L)).thenReturn(Collections.singletonList(enrollment));
        when(projectService.getById(1L)).thenReturn(project);
        when(organizationMapper.selectById(1L)).thenReturn(org);

        List<ProjectEnrollmentDTO> result = studentProjectService.getMyProjects(100L);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Project 1", result.get(0).getProjectName());
        System.out.println("✓ 测试通过: 获取我的项目列表成功 - 数量=" + result.size());
    }

    @Test
    @DisplayName("enroll - 学生报名项目成功")
    void testEnrollSuccess() {
        SysUser student = new SysUser();
        student.setId(100L);
        student.setRole("student");
        student.setRealName("Student");

        Project project = mockProject(1L, ProjectService.STATUS_APPROVED, 1L);

        when(sysUserMapper.selectById(100L)).thenReturn(student);
        when(projectService.getById(1L)).thenReturn(project);
        when(studentProjectMapper.selectOne(any())).thenReturn(null);
        when(studentProjectMapper.insert(any())).thenReturn(1);
        when(userOpLogMapper.insert(any())).thenReturn(1);

        StudentProject result = studentProjectService.enroll(100L, 1L);

        assertNotNull(result);
        assertEquals(StudentProject.STATUS_IN_PROGRESS, result.getStatus());
        assertEquals(100L, result.getStudentId());
        assertEquals(1L, result.getProjectId());
        System.out.println("✓ 测试通过: 学生报名项目成功 - 状态=" + result.getStatus());
    }

    @Test
    @DisplayName("enroll - 非学生角色报名被拒绝")
    void testEnrollNotStudent() {
        SysUser orgAdmin = new SysUser();
        orgAdmin.setId(100L);
        orgAdmin.setRole("org_admin");

        when(sysUserMapper.selectById(100L)).thenReturn(orgAdmin);

        BizException exception = assertThrows(BizException.class, () ->
                studentProjectService.enroll(100L, 1L));
        assertEquals("只有学生可以报名项目", exception.getMessage());
        System.out.println("✓ 测试通过: 非学生报名被拒绝 - " + exception.getMessage());
    }

    @Test
    @DisplayName("enroll - 项目未上架不可报名")
    void testEnrollProjectNotApproved() {
        SysUser student = new SysUser();
        student.setId(100L);
        student.setRole("student");

        Project project = mockProject(1L, ProjectService.STATUS_PENDING, 1L);

        when(sysUserMapper.selectById(100L)).thenReturn(student);
        when(projectService.getById(1L)).thenReturn(project);

        BizException exception = assertThrows(BizException.class, () ->
                studentProjectService.enroll(100L, 1L));
        assertEquals("项目未上架，不可报名", exception.getMessage());
        System.out.println("✓ 测试通过: 项目未上架时报名被拒绝 - " + exception.getMessage());
    }

    @Test
    @DisplayName("enroll - 重复报名抛出异常")
    void testEnrollDuplicate() {
        SysUser student = new SysUser();
        student.setId(100L);
        student.setRole("student");

        Project project = mockProject(1L, ProjectService.STATUS_APPROVED, 1L);

        StudentProject existing = new StudentProject();
        existing.setId(1L);
        existing.setStudentId(100L);
        existing.setProjectId(1L);
        existing.setStatus(StudentProject.STATUS_ENROLLED);

        when(sysUserMapper.selectById(100L)).thenReturn(student);
        when(projectService.getById(1L)).thenReturn(project);
        when(studentProjectMapper.selectOne(any())).thenReturn(existing);

        BizException exception = assertThrows(BizException.class, () ->
                studentProjectService.enroll(100L, 1L));
        assertEquals("已报名该项目", exception.getMessage());
        System.out.println("✓ 测试通过: 重复报名时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("cancelEnrollment - 取消报名成功")
    void testCancelEnrollmentSuccess() {
        SysUser student = new SysUser();
        student.setId(100L);
        student.setRealName("Student");

        StudentProject enrollment = new StudentProject();
        enrollment.setId(1L);
        enrollment.setStudentId(100L);
        enrollment.setProjectId(1L);
        enrollment.setStatus(StudentProject.STATUS_ENROLLED);

        Project project = mockProject(1L, ProjectService.STATUS_APPROVED, 1L);

        when(sysUserMapper.selectById(100L)).thenReturn(student);
        when(studentProjectMapper.selectOne(any())).thenReturn(enrollment);
        when(projectService.getById(1L)).thenReturn(project);
        when(studentProjectMapper.updateById(any())).thenReturn(1);
        when(userOpLogMapper.insert(any())).thenReturn(1);

        studentProjectService.cancelEnrollment(100L, 1L);

        assertEquals(StudentProject.STATUS_CANCELLED, enrollment.getStatus());
        System.out.println("✓ 测试通过: 取消报名成功 - 状态=" + enrollment.getStatus());
    }

    @Test
    @DisplayName("cancelEnrollment - 当前状态不可取消报名")
    void testCancelEnrollmentInvalidStatus() {
        SysUser student = new SysUser();
        student.setId(100L);

        StudentProject enrollment = new StudentProject();
        enrollment.setId(1L);
        enrollment.setStudentId(100L);
        enrollment.setProjectId(1L);
        enrollment.setStatus(StudentProject.STATUS_COMPLETED);

        Project project = mockProject(1L, ProjectService.STATUS_APPROVED, 1L);

        when(sysUserMapper.selectById(100L)).thenReturn(student);
        when(studentProjectMapper.selectOne(any())).thenReturn(enrollment);
        when(projectService.getById(1L)).thenReturn(project);

        BizException exception = assertThrows(BizException.class, () ->
                studentProjectService.cancelEnrollment(100L, 1L));
        assertEquals("当前状态不可取消报名", exception.getMessage());
        System.out.println("✓ 测试通过: 已完成项目取消报名时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("updateEnrollmentStatus - 已报名→进行中")
    void testUpdateStatusEnrolledToInProgress() {
        SysUser operator = new SysUser();
        operator.setId(10L);
        operator.setRole("org_admin");

        StudentProject enrollment = new StudentProject();
        enrollment.setId(1L);
        enrollment.setStudentId(100L);
        enrollment.setProjectId(1L);
        enrollment.setStatus(StudentProject.STATUS_ENROLLED);

        when(studentProjectMapper.selectById(1L)).thenReturn(enrollment);
        when(studentProjectMapper.updateById(any())).thenReturn(1);

        studentProjectService.updateEnrollmentStatus(1L, StudentProject.STATUS_IN_PROGRESS, operator);

        assertEquals(StudentProject.STATUS_IN_PROGRESS, enrollment.getStatus());
        System.out.println("✓ 测试通过: 报名状态流转 已报名→进行中");
    }

    @Test
    @DisplayName("updateEnrollmentStatus - 进行中→待审核")
    void testUpdateStatusInProgressToPendingReview() {
        SysUser operator = new SysUser();
        operator.setId(10L);
        operator.setRole("org_admin");

        StudentProject enrollment = new StudentProject();
        enrollment.setId(1L);
        enrollment.setStudentId(100L);
        enrollment.setProjectId(1L);
        enrollment.setStatus(StudentProject.STATUS_IN_PROGRESS);

        when(studentProjectMapper.selectById(1L)).thenReturn(enrollment);
        when(studentProjectMapper.updateById(any())).thenReturn(1);

        studentProjectService.updateEnrollmentStatus(1L, StudentProject.STATUS_PENDING_REVIEW, operator);

        assertEquals(StudentProject.STATUS_PENDING_REVIEW, enrollment.getStatus());
        System.out.println("✓ 测试通过: 报名状态流转 进行中→待审核");
    }

    @Test
    @DisplayName("updateEnrollmentStatus - 待审核→已完成并自动发分")
    void testUpdateStatusPendingReviewToCompleted() {
        SysUser operator = new SysUser();
        operator.setId(10L);
        operator.setRole("org_admin");
        operator.setRealName("Org Admin");

        StudentProject enrollment = new StudentProject();
        enrollment.setId(1L);
        enrollment.setStudentId(100L);
        enrollment.setProjectId(1L);
        enrollment.setStatus(StudentProject.STATUS_PENDING_REVIEW);

        Project project = mockProject(1L, ProjectService.STATUS_APPROVED, 1L);
        project.setCreditReward(100);

        when(studentProjectMapper.selectById(1L)).thenReturn(enrollment);
        when(projectService.getById(1L)).thenReturn(project);
        when(studentProjectMapper.updateById(any())).thenReturn(1);
        when(userOpLogMapper.insert(any())).thenReturn(1);
        when(pointService.rewardProjectCompletion(100L, project, 10L)).thenReturn(new SysUser());

        studentProjectService.updateEnrollmentStatus(1L, StudentProject.STATUS_COMPLETED, operator);

        assertEquals(StudentProject.STATUS_COMPLETED, enrollment.getStatus());
        verify(pointService).rewardProjectCompletion(100L, project, 10L);
        System.out.println("✓ 测试通过: 报名状态流转 待审核→已完成, 自动发放积分=" + project.getCreditReward());
    }

    @Test
    @DisplayName("updateEnrollmentStatus - 待审核→进行中（驳回）")
    void testUpdateStatusPendingReviewToInProgress() {
        SysUser operator = new SysUser();
        operator.setId(10L);
        operator.setRole("org_admin");
        operator.setRealName("Org Admin");

        StudentProject enrollment = new StudentProject();
        enrollment.setId(1L);
        enrollment.setStudentId(100L);
        enrollment.setProjectId(1L);
        enrollment.setStatus(StudentProject.STATUS_PENDING_REVIEW);

        Project project = mockProject(1L, ProjectService.STATUS_APPROVED, 1L);

        when(studentProjectMapper.selectById(1L)).thenReturn(enrollment);
        when(projectService.getById(1L)).thenReturn(project);
        when(studentProjectMapper.updateById(any())).thenReturn(1);
        when(userOpLogMapper.insert(any())).thenReturn(1);

        studentProjectService.updateEnrollmentStatus(1L, StudentProject.STATUS_IN_PROGRESS, operator);

        assertEquals(StudentProject.STATUS_IN_PROGRESS, enrollment.getStatus());
        System.out.println("✓ 测试通过: 报名状态流转 待审核→进行中（驳回重修）");
    }

    @Test
    @DisplayName("updateEnrollmentStatus - 非法状态流转")
    void testUpdateStatusInvalidTransition() {
        SysUser operator = new SysUser();
        operator.setId(10L);
        operator.setRole("org_admin");

        StudentProject enrollment = new StudentProject();
        enrollment.setId(1L);
        enrollment.setStatus(StudentProject.STATUS_ENROLLED);

        when(studentProjectMapper.selectById(1L)).thenReturn(enrollment);

        BizException exception = assertThrows(BizException.class, () ->
                studentProjectService.updateEnrollmentStatus(1L, StudentProject.STATUS_COMPLETED, operator));
        assertEquals("非法的状态流转：已报名 → 已完成", exception.getMessage());
        System.out.println("✓ 测试通过: 非法状态流转时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("updateEnrollmentStatus - 非管理员/机构管理员无权操作")
    void testUpdateStatusNoPermission() {
        SysUser student = new SysUser();
        student.setId(100L);
        student.setRole("student");

        StudentProject enrollment = new StudentProject();
        enrollment.setId(1L);
        enrollment.setStatus(StudentProject.STATUS_ENROLLED);

        when(studentProjectMapper.selectById(1L)).thenReturn(enrollment);

        BizException exception = assertThrows(BizException.class, () ->
                studentProjectService.updateEnrollmentStatus(1L, StudentProject.STATUS_IN_PROGRESS, student));
        assertEquals("无权操作", exception.getMessage());
        System.out.println("✓ 测试通过: 学生无权限修改报名状态 - " + exception.getMessage());
    }

    @Test
    @DisplayName("submitForReview - 学生提交项目完成申请")
    void testSubmitForReview() {
        SysUser student = new SysUser();
        student.setId(100L);
        student.setRealName("Student");

        StudentProject enrollment = new StudentProject();
        enrollment.setId(1L);
        enrollment.setStudentId(100L);
        enrollment.setProjectId(1L);
        enrollment.setStatus(StudentProject.STATUS_IN_PROGRESS);

        Project project = mockProject(1L, ProjectService.STATUS_APPROVED, 1L);

        when(studentProjectMapper.selectById(1L)).thenReturn(enrollment);
        when(studentProjectMapper.updateById(any())).thenReturn(1);
        when(projectService.getById(1L)).thenReturn(project);
        when(sysUserMapper.selectById(100L)).thenReturn(student);
        when(userOpLogMapper.insert(any())).thenReturn(1);

        studentProjectService.submitForReview(100L, 1L);

        assertEquals(StudentProject.STATUS_PENDING_REVIEW, enrollment.getStatus());
        System.out.println("✓ 测试通过: 学生提交完成申请 - 状态=待审核");
    }

    @Test
    @DisplayName("submitForReview - 非本人报名记录无权操作")
    void testSubmitForReviewNotOwner() {
        StudentProject enrollment = new StudentProject();
        enrollment.setId(1L);
        enrollment.setStudentId(100L);
        enrollment.setStatus(StudentProject.STATUS_IN_PROGRESS);

        when(studentProjectMapper.selectById(1L)).thenReturn(enrollment);

        BizException exception = assertThrows(BizException.class, () ->
                studentProjectService.submitForReview(999L, 1L));
        assertEquals("无权操作", exception.getMessage());
        System.out.println("✓ 测试通过: 非本人提交完成申请被拒绝 - " + exception.getMessage());
    }

    @Test
    @DisplayName("getProjectDetailForStudent - 委托 ProjectService 获取详情")
    void testGetProjectDetailForStudent() {
        ProjectDetailDTO dto = new ProjectDetailDTO();
        dto.setId(1L);
        dto.setName("Project 1");

        when(projectService.getDetail(1L, 100L)).thenReturn(dto);

        ProjectDetailDTO result = studentProjectService.getProjectDetailForStudent(1L, 100L);

        assertNotNull(result);
        assertEquals("Project 1", result.getName());
        verify(projectService).getDetail(1L, 100L);
        System.out.println("✓ 测试通过: 学生端项目详情委托成功 - 名称=" + result.getName());
    }
}