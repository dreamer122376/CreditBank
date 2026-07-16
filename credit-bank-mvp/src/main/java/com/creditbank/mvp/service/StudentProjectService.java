package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.dto.ProjectDetailDTO;
import com.creditbank.mvp.dto.ProjectEnrollmentDTO;
import com.creditbank.mvp.dto.StudentProjectAuditDTO;
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

    private final StudentProjectMapper studentProjectMapper;
    private final ProjectService projectService;
    private final ProjectMapper projectMapper;
    private final SysUserMapper sysUserMapper;
    private final OrganizationMapper organizationMapper;
    private final UserOpLogMapper userOpLogMapper;
    private final PointService pointService;
    private final NotificationService notificationService;

    public StudentProjectService(StudentProjectMapper studentProjectMapper,
                                 ProjectService projectService,
                                 ProjectMapper projectMapper,
                                 SysUserMapper sysUserMapper,
                                 OrganizationMapper organizationMapper,
                                 UserOpLogMapper userOpLogMapper,
                                 PointService pointService,
                                 NotificationService notificationService) {
        this.studentProjectMapper = studentProjectMapper;
        this.projectService = projectService;
        this.projectMapper = projectMapper;
        this.sysUserMapper = sysUserMapper;
        this.organizationMapper = organizationMapper;
        this.userOpLogMapper = userOpLogMapper;
        this.pointService = pointService;
        this.notificationService = notificationService;
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
            if (StudentProject.STATUS_CANCELLED.equals(existing.getStatus())) {
                existing.setStatus(StudentProject.STATUS_ENROLLED);
                studentProjectMapper.updateById(existing);
                userOpLogMapper.insert(UserOpLog.createLog(
                        studentId, student.getRealName(), null, null,
                        UserOpLog.MODULE_ENROLL, UserOpLog.ACTION_PROJECT_ENROLL,
                        "重新报名项目：" + project.getName()));
                return existing;
            }
            throw new BizException("已报名该项目");
        }

        StudentProject enrollment = new StudentProject();
        enrollment.setStudentId(studentId);
        enrollment.setProjectId(projectId);
        enrollment.setStatus(StudentProject.STATUS_ENROLLED);
        enrollment.setCreatedAt(LocalDateTime.now());
        studentProjectMapper.insert(enrollment);

        userOpLogMapper.insert(UserOpLog.createLog(
                studentId, student.getRealName(), null, null,
                UserOpLog.MODULE_ENROLL, UserOpLog.ACTION_PROJECT_ENROLL,
                "报名项目：" + project.getName()));
        return enrollment;
    }

    /**
     * 学生取消报名（"已报名"或"进行中"状态可以取消）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelEnrollment(Long studentId, Long projectId) {
        SysUser student = sysUserMapper.selectById(studentId);
        if (student == null) {
            throw new BizException("学生不存在：" + studentId);
        }
        StudentProject enrollment = studentProjectMapper.selectOne(
                new LambdaQueryWrapper<StudentProject>()
                        .eq(StudentProject::getStudentId, studentId)
                        .eq(StudentProject::getProjectId, projectId));
        if (enrollment == null) {
            throw new BizException("未报名该项目");
        }
        if (!StudentProject.STATUS_ENROLLED.equals(enrollment.getStatus())
                && !StudentProject.STATUS_IN_PROGRESS.equals(enrollment.getStatus())) {
            throw new BizException("当前状态不可取消报名");
        }
        Project project = projectService.getById(projectId);
        enrollment.setStatus(StudentProject.STATUS_CANCELLED);
        studentProjectMapper.updateById(enrollment);

        userOpLogMapper.insert(UserOpLog.createLog(
                studentId, student.getRealName(), null, null,
                UserOpLog.MODULE_ENROLL, UserOpLog.ACTION_PROJECT_LEAVE,
                "取消报名项目：" + project.getName()));
    }

    // ==================== 机构/管理端：状态管理 ====================

    /**
     * 更新报名状态（机构/管理员操作）。
     * 允许流转：已报名 → 进行中 → 待审核 → 已完成/进行中
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

        if ("org_admin".equals(operator.getRole()) && operator.getOrgId() != null) {
            Project project = projectMapper.selectById(enrollment.getProjectId());
            if (project == null || !operator.getOrgId().equals(project.getOrgId())) {
                throw new BizException("无权审核非本机构项目的报名");
            }
        }

        String current = enrollment.getStatus();
        if (StudentProject.STATUS_CANCELLED.equals(current)) {
            throw new BizException("已取消的报名不可修改状态");
        }

        boolean valid = false;
        if (StudentProject.STATUS_ENROLLED.equals(current) && StudentProject.STATUS_IN_PROGRESS.equals(newStatus)) {
            valid = true;
        } else if (StudentProject.STATUS_IN_PROGRESS.equals(current) && StudentProject.STATUS_PENDING_REVIEW.equals(newStatus)) {
            valid = true;
        } else if (StudentProject.STATUS_PENDING_REVIEW.equals(current)
                && (StudentProject.STATUS_COMPLETED.equals(newStatus) || StudentProject.STATUS_IN_PROGRESS.equals(newStatus))) {
            valid = true;
        }
        if (!valid) {
            throw new BizException("非法的状态流转：" + current + " → " + newStatus);
        }

        enrollment.setStatus(newStatus);
        studentProjectMapper.updateById(enrollment);

        // 审核操作记日志
        if (StudentProject.STATUS_PENDING_REVIEW.equals(current)) {
            Project project = projectService.getById(enrollment.getProjectId());
            String projectName = project != null ? project.getName() : "未知项目";
            String auditAction = StudentProject.STATUS_COMPLETED.equals(newStatus)
                    ? UserOpLog.ACTION_PROJECT_APPROVE : UserOpLog.ACTION_PROJECT_REJECT;
            String auditDetail = (StudentProject.STATUS_COMPLETED.equals(newStatus) ? "通过" : "驳回")
                    + "项目完成申请：" + projectName;
            userOpLogMapper.insert(UserOpLog.createLog(
                    operator.getId(), operator.getRealName(),
                    enrollment.getStudentId(), null,
                    UserOpLog.MODULE_PROJECT, auditAction,
                    auditDetail));
        }

        // 审核通过（待审核 → 已完成）时自动发放积分奖励
        if (StudentProject.STATUS_PENDING_REVIEW.equals(current) && StudentProject.STATUS_COMPLETED.equals(newStatus)) {
            Project project = projectService.getById(enrollment.getProjectId());
            if (project != null) {
                pointService.rewardProjectCompletion(enrollment.getStudentId(), project, operator.getId());
                // 通知学生审核通过
                notificationService.sendToUser(
                        "PROJECT_APPROVED", NotificationService.CATEGORY_APPLICATION, "SUCCESS",
                        "项目审核通过",
                        "您的项目「" + project.getName() + "」完成申请已审核通过，积分已发放。",
                        "/my-projects",
                        "PROJECT", project.getId(),
                        "project_approved_" + enrollment.getId(),
                        enrollment.getStudentId(), operator.getId());
            }
        }
        // 审核驳回时通知学生
        if (StudentProject.STATUS_PENDING_REVIEW.equals(current) && StudentProject.STATUS_IN_PROGRESS.equals(newStatus)) {
            Project project = projectService.getById(enrollment.getProjectId());
            if (project != null) {
                notificationService.sendToUser(
                        "PROJECT_REJECTED", NotificationService.CATEGORY_APPLICATION, "WARNING",
                        "项目审核驳回",
                        "您的项目「" + project.getName() + "」完成申请已被驳回，可重新提交。",
                        "/my-projects",
                        "PROJECT", project.getId(),
                        "project_rejected_" + enrollment.getId(),
                        enrollment.getStudentId(), operator.getId());
            }
        }
    }

    /**
     * 学生提交完成申请，状态从 进行中 → 待审核。
     */
    @Transactional(rollbackFor = Exception.class)
    public void submitForReview(Long studentId, Long enrollmentId) {
        StudentProject enrollment = studentProjectMapper.selectById(enrollmentId);
        if (enrollment == null) {
            throw new BizException("报名记录不存在：" + enrollmentId);
        }
        if (!enrollment.getStudentId().equals(studentId)) {
            throw new BizException("无权操作");
        }
        if (!StudentProject.STATUS_IN_PROGRESS.equals(enrollment.getStatus())) {
            throw new BizException("当前状态不可提交完成");
        }
        enrollment.setStatus(StudentProject.STATUS_PENDING_REVIEW);
        studentProjectMapper.updateById(enrollment);

        Project project = projectService.getById(enrollment.getProjectId());
        SysUser student = sysUserMapper.selectById(studentId);
        userOpLogMapper.insert(UserOpLog.createLog(
                studentId, student != null ? student.getRealName() : String.valueOf(studentId),
                null, null,
                UserOpLog.MODULE_ENROLL, UserOpLog.ACTION_PROJECT_SUBMIT,
                "提交项目完成申请：" + (project != null ? project.getName() : "")));

        // 通知机构管理员审核
        if (project != null && project.getOrgId() != null && student != null) {
            SysUser orgAdmin = sysUserMapper.selectOne(
                    new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getOrgId, project.getOrgId())
                            .eq(SysUser::getRole, "org_admin")
                            .last("LIMIT 1"));
            if (orgAdmin != null) {
                notificationService.sendToUser(
                        "PROJECT_REVIEW", NotificationService.CATEGORY_APPLICATION, "INFO",
                        "项目完成申请待审核",
                        "学生「" + student.getRealName() + "」提交了项目「" + project.getName() + "」的完成申请，请及时审核。",
                        "/projects/manage",
                        "PROJECT", project.getId(),
                        "project_review_" + enrollment.getId(),
                        orgAdmin.getId(), studentId);
            }
        }
    }

    /**
     * 查询待审核的报名列表（机构管理员只能查看本机构项目下的报名）。
     */
    public List<StudentProjectAuditDTO> getPendingAuditEnrollments(SysUser operator) {
        if (!isOrgAdminOrAdmin(operator)) {
            throw new BizException("无权操作");
        }

        System.out.println("[DEBUG] getPendingAuditEnrollments - operator: " + operator.getRealName() + ", role: " + operator.getRole() + ", orgId: " + operator.getOrgId());

        LambdaQueryWrapper<StudentProject> query = new LambdaQueryWrapper<StudentProject>()
                .eq(StudentProject::getStatus, StudentProject.STATUS_PENDING_REVIEW);

        if ("org_admin".equals(operator.getRole()) && operator.getOrgId() != null) {
            List<Project> orgProjects = projectMapper.selectList(
                    new LambdaQueryWrapper<Project>()
                            .eq(Project::getOrgId, operator.getOrgId()));
            System.out.println("[DEBUG] org_admin projects count: " + orgProjects.size());
            if (orgProjects.isEmpty()) {
                return new ArrayList<>();
            }
            List<Long> projectIds = orgProjects.stream()
                    .map(Project::getId)
                    .collect(Collectors.toList());
            System.out.println("[DEBUG] projectIds: " + projectIds);
            query.in(StudentProject::getProjectId, projectIds);
        }

        List<StudentProject> enrollments = studentProjectMapper.selectList(query);
        System.out.println("[DEBUG] enrollments count: " + enrollments.size());
        if (enrollments.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> projectIds = enrollments.stream()
                .map(StudentProject::getProjectId)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, Project> projectMap = projectMapper.selectBatchIds(projectIds)
                .stream()
                .collect(Collectors.toMap(Project::getId, p -> p));

        Map<Long, SysUser> studentMap = sysUserMapper.selectBatchIds(
                enrollments.stream().map(StudentProject::getStudentId).distinct().collect(Collectors.toList())
        ).stream().collect(Collectors.toMap(SysUser::getId, u -> u));

        Map<Long, Organization> orgMap = new java.util.HashMap<>();
        for (Project project : projectMap.values()) {
            if (project.getOrgId() != null && !orgMap.containsKey(project.getOrgId())) {
                Organization org = organizationMapper.selectById(project.getOrgId());
                if (org != null) {
                    orgMap.put(project.getOrgId(), org);
                }
            }
        }

        return enrollments.stream().map(enrollment -> {
            StudentProjectAuditDTO dto = new StudentProjectAuditDTO();
            dto.setId(enrollment.getId());
            dto.setStudentId(enrollment.getStudentId());
            dto.setStudentName(studentMap.get(enrollment.getStudentId()) != null
                    ? studentMap.get(enrollment.getStudentId()).getRealName()
                    : "");
            dto.setProjectId(enrollment.getProjectId());
            dto.setProjectName(projectMap.get(enrollment.getProjectId()) != null
                    ? projectMap.get(enrollment.getProjectId()).getName()
                    : "");
            Project project = projectMap.get(enrollment.getProjectId());
            if (project != null) {
                dto.setOrgId(project.getOrgId());
                dto.setOrgName(orgMap.get(project.getOrgId()) != null
                        ? orgMap.get(project.getOrgId()).getName()
                        : "");
            }
            dto.setStatus(enrollment.getStatus());
            dto.setCreatedAt(enrollment.getCreatedAt());
            return dto;
        }).collect(Collectors.toList());
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
