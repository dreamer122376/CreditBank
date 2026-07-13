package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.dto.ProjectDetailDTO;
import com.creditbank.mvp.dto.ProjectEnrollmentDTO;
import com.creditbank.mvp.entity.Organization;
import com.creditbank.mvp.entity.Project;
import com.creditbank.mvp.entity.StudentProject;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.mapper.OrganizationMapper;
import com.creditbank.mvp.mapper.StudentProjectMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 学生项目关系服务。
 * 负责学生报名、取消报名、我的项目列表、报名状态更新等。
 * 项目主表相关查询委托给 {@link ProjectService}。
 */
@Service
public class StudentProjectService {

    private static final String ENROLLMENT_STATUS_ENROLLED = "已报名";
    private static final String ENROLLMENT_STATUS_IN_PROGRESS = "进行中";
    private static final String ENROLLMENT_STATUS_COMPLETED = "已完成";
    private static final String ENROLLMENT_STATUS_CANCELLED = "已取消";

    private final StudentProjectMapper studentProjectMapper;
    private final ProjectService projectService;
    private final SysUserMapper sysUserMapper;
    private final OrganizationMapper organizationMapper;

    public StudentProjectService(StudentProjectMapper studentProjectMapper,
                                 ProjectService projectService,
                                 SysUserMapper sysUserMapper,
                                 OrganizationMapper organizationMapper) {
        this.studentProjectMapper = studentProjectMapper;
        this.projectService = projectService;
        this.sysUserMapper = sysUserMapper;
        this.organizationMapper = organizationMapper;
    }

    // ==================== 学生端：我的项目 ====================

    /**
     * 查询学生已报名的项目列表（含项目基本信息和报名状态）。
     */
    public List<ProjectEnrollmentDTO> getMyProjects(Long studentId) {
        SysUser student = sysUserMapper.selectById(studentId);
        if (student == null) {
            throw new BizException("学生不存在：" + studentId);
        }

        List<StudentProject> enrollments = studentProjectMapper.selectByStudentId(studentId);
        if (enrollments.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> projectIds = enrollments.stream()
                .map(StudentProject::getProjectId)
                .distinct()
                .collect(Collectors.toList());

        List<Project> projects = new ArrayList<>();
        for (Long pid : projectIds) {
            Project p = projectService.getById(pid);
            if (p != null) {
                projects.add(p);
            }
        }

        Map<Long, Project> projectMap = projects.stream()
                .collect(Collectors.toMap(Project::getId, p -> p));

        return enrollments.stream().map(enrollment -> {
            Project project = projectMap.get(enrollment.getProjectId());
            if (project == null) {
                return null;
            }
            return toEnrollmentDTO(enrollment, project);
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 查询项目下的报名学生列表。
     */
    public List<SysUser> getStudentsByProjectId(Long projectId) {
        List<StudentProject> enrollments = studentProjectMapper.selectByProjectId(projectId);
        if (enrollments.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> studentIds = enrollments.stream()
                .map(StudentProject::getStudentId)
                .distinct()
                .collect(Collectors.toList());
        return sysUserMapper.selectBatchIds(studentIds);
    }

    /**
     * 项目详情（供学生端查看，会标记当前学生是否已报名）。
     */
    public ProjectDetailDTO getProjectDetailForStudent(Long projectId, Long studentId) {
        return projectService.getDetail(projectId, studentId);
    }

    // ==================== 报名/取消报名 ====================

    /**
     * 学生报名项目。
     */
    @Transactional(rollbackFor = Exception.class)
    public StudentProject enroll(Long studentId, Long projectId) {
        SysUser student = sysUserMapper.selectById(studentId);
        if (student == null) {
            throw new BizException("学生不存在：" + studentId);
        }
        if (!"student".equals(student.getRole())) {
            throw new BizException("只有学生可以报名项目");
        }

        Project project = projectService.getById(projectId);
        if (project.getStatus() != ProjectService.STATUS_APPROVED) {
            throw new BizException("项目未上架，不可报名");
        }

        StudentProject existing = studentProjectMapper.selectOne(
                new LambdaQueryWrapper<StudentProject>()
                        .eq(StudentProject::getStudentId, studentId)
                        .eq(StudentProject::getProjectId, projectId));
        if (existing != null) {
            throw new BizException("已报名该项目");
        }

        StudentProject enrollment = new StudentProject();
        enrollment.setStudentId(studentId);
        enrollment.setProjectId(projectId);
        enrollment.setStatus(ENROLLMENT_STATUS_ENROLLED);
        enrollment.setCreatedAt(LocalDateTime.now());
        studentProjectMapper.insert(enrollment);
        return enrollment;
    }

    /**
     * 学生取消报名（只有"已报名"状态可以取消）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelEnrollment(Long studentId, Long projectId) {
        StudentProject enrollment = studentProjectMapper.selectOne(
                new LambdaQueryWrapper<StudentProject>()
                        .eq(StudentProject::getStudentId, studentId)
                        .eq(StudentProject::getProjectId, projectId));
        if (enrollment == null) {
            throw new BizException("未报名该项目");
        }
        if (!ENROLLMENT_STATUS_ENROLLED.equals(enrollment.getStatus())) {
            throw new BizException("当前状态不可取消报名");
        }
        enrollment.setStatus(ENROLLMENT_STATUS_CANCELLED);
        studentProjectMapper.updateById(enrollment);
    }

    // ==================== 机构/管理端：状态管理 ====================

    /**
     * 更新报名状态（机构/管理员操作）。
     * 允许流转：已报名 → 进行中 → 已完成
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateEnrollmentStatus(Long enrollmentId, String newStatus, SysUser operator) {
        if (!isOrgAdminOrAdmin(operator)) {
            throw new BizException("无权操作");
        }

        StudentProject enrollment = studentProjectMapper.selectById(enrollmentId);
        if (enrollment == null) {
            throw new BizException("报名记录不存在：" + enrollmentId);
        }

        String current = enrollment.getStatus();
        if (ENROLLMENT_STATUS_CANCELLED.equals(current)) {
            throw new BizException("已取消的报名不可修改状态");
        }

        if (ENROLLMENT_STATUS_ENROLLED.equals(current) && ENROLLMENT_STATUS_IN_PROGRESS.equals(newStatus)) {
            enrollment.setStatus(newStatus);
        } else if (ENROLLMENT_STATUS_IN_PROGRESS.equals(current) && ENROLLMENT_STATUS_COMPLETED.equals(newStatus)) {
            enrollment.setStatus(newStatus);
        } else {
            throw new BizException("非法的状态流转：" + current + " → " + newStatus);
        }

        studentProjectMapper.updateById(enrollment);
    }

    // ==================== 内部工具 ====================

    private boolean isOrgAdminOrAdmin(SysUser operator) {
        return operator != null && ("org_admin".equals(operator.getRole()) || "admin".equals(operator.getRole()));
    }

    private ProjectEnrollmentDTO toEnrollmentDTO(StudentProject enrollment, Project project) {
        ProjectEnrollmentDTO dto = new ProjectEnrollmentDTO();
        dto.setEnrollmentId(enrollment.getId());
        dto.setProjectId(project.getId());
        dto.setProjectName(project.getName());
        dto.setDescription(project.getDescription());
        dto.setCreditReward(project.getCreditReward());
        dto.setCreditPrice(project.getCreditPrice());
        dto.setStatus(enrollment.getStatus());
        dto.setEnrolledAt(enrollment.getCreatedAt());
        dto.setProjectCreatedAt(project.getCreatedAt());

        if (project.getOrgId() != null) {
            Organization org = organizationMapper.selectById(project.getOrgId());
            dto.setOrgName(org != null ? org.getName() : "");
        }
        if (project.getExpertId() != null) {
            SysUser expert = sysUserMapper.selectById(project.getExpertId());
            dto.setExpertName(expert != null ? expert.getRealName() : "");
        }

        return dto;
    }
}
