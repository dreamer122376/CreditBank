package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.dto.EnrolledStudentDTO;
import com.creditbank.mvp.dto.ProjectDetailDTO;
import com.creditbank.mvp.dto.ProjectListDTO;
import com.creditbank.mvp.entity.Organization;
import com.creditbank.mvp.entity.Project;
import com.creditbank.mvp.entity.Application;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 项目核心业务服务。
 * 负责 project 主表的 CRUD、审核、上下架，以及项目详情/列表的组装。
 */
@Service
public class ProjectService {

    public static final int STATUS_PENDING = 0;   // 待审核
    public static final int STATUS_APPROVED = 1;  // 已上架
    public static final int STATUS_REJECTED = 2;  // 已驳回
    public static final int STATUS_OFFLINE = 3;   // 已下架
    public static final int STATUS_IN_REVIEW = 4; // 审核中（申请流程中）

    /** 项目上架认证标准 ID */
    public static final long PROJECT_UP_STANDARD_ID = 6L;

    private static final Map<Integer, String> STATUS_NAME = new HashMap<>();

    static {
        STATUS_NAME.put(STATUS_PENDING, "待审核");
        STATUS_NAME.put(STATUS_APPROVED, "已上架");
        STATUS_NAME.put(STATUS_REJECTED, "已驳回");
        STATUS_NAME.put(STATUS_OFFLINE, "已下架");
        STATUS_NAME.put(STATUS_IN_REVIEW, "审核中");
    }

    private final ProjectMapper projectMapper;
    private final OrganizationMapper organizationMapper;
    private final SysUserMapper sysUserMapper;
    private final StudentProjectMapper studentProjectMapper;
    private final UserOpLogMapper userOpLogMapper;
    private final ApplicationService applicationService;

    public ProjectService(ProjectMapper projectMapper,
                          OrganizationMapper organizationMapper,
                          SysUserMapper sysUserMapper,
                          StudentProjectMapper studentProjectMapper,
                          UserOpLogMapper userOpLogMapper,
                          ApplicationService applicationService) {
        this.projectMapper = projectMapper;
        this.organizationMapper = organizationMapper;
        this.sysUserMapper = sysUserMapper;
        this.studentProjectMapper = studentProjectMapper;
        this.userOpLogMapper = userOpLogMapper;
        this.applicationService = applicationService;
    }

    // ==================== 查询 ====================

    /**
     * 根据 ID 查询项目，不存在则抛业务异常。
     */
    public Project getById(Long id) {
        Project project = projectMapper.selectById(id);
        if (project == null) {
            throw new BizException("项目不存在：" + id);
        }
        return project;
    }

    /**
     * 项目详情（含机构、专家、已报名学生）。
     */
    public ProjectDetailDTO getDetail(Long id, Long currentStudentId) {
        Project project = getById(id);
        ProjectDetailDTO dto = toDetailDTO(project);

        // 填充已报名学生列表（排除已取消）
        List<StudentProject> enrollments = studentProjectMapper.selectByProjectId(id).stream()
                .filter(e -> !StudentProject.STATUS_CANCELLED.equals(e.getStatus()))
                .collect(Collectors.toList());
        if (!enrollments.isEmpty()) {
            List<Long> studentIds = enrollments.stream()
                    .map(StudentProject::getStudentId)
                    .distinct()
                    .collect(Collectors.toList());
            List<SysUser> students = sysUserMapper.selectBatchIds(studentIds);
            Map<Long, SysUser> userMap = students.stream()
                    .collect(Collectors.toMap(SysUser::getId, u -> u));

            List<EnrolledStudentDTO> enrolledStudents = enrollments.stream().map(e -> {
                SysUser user = userMap.get(e.getStudentId());
                if (user == null) {
                    return null;
                }
                EnrolledStudentDTO es = new EnrolledStudentDTO();
                es.setEnrollmentId(e.getId());
                es.setStudentId(user.getId());
                es.setUsername(user.getUsername());
                es.setRealName(user.getRealName());
                es.setStatus(e.getStatus());
                es.setEnrolledAt(e.getCreatedAt());
                return es;
            }).filter(Objects::nonNull).collect(Collectors.toList());
            dto.setEnrolledStudents(enrolledStudents);

            // 学生端查看时，标记自己是否已报名及报名状态
            if (currentStudentId != null) {
                dto.setEnrolled(studentIds.contains(currentStudentId));
                for (StudentProject e : enrollments) {
                    if (currentStudentId.equals(e.getStudentId())) {
                        dto.setEnrollmentStatus(e.getStatus());
                        break;
                    }
                }
            }
        } else {
            dto.setEnrolledStudents(new ArrayList<>());
            if (currentStudentId != null) {
                dto.setEnrolled(false);
            }
        }

        return dto;
    }

    /**
     * 管理端/机构端项目分页列表。
     *
     * @param role    admin / org_admin / 其他
     * @param orgId   机构管理员传入本机构 ID
     * @param status  状态筛选，null 表示全部
     */
    public Page<ProjectListDTO> page(String role, Long orgId, Integer status, int pageNum, int pageSize) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<>();
        if ("org_admin".equals(role) && orgId != null) {
            wrapper.eq(Project::getOrgId, orgId);
        }
        if (status != null) {
            wrapper.eq(Project::getStatus, status);
        }
        wrapper.orderByDesc(Project::getCreatedAt);

        Page<Project> page = projectMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
        return toListDTOPage(page);
    }

    /**
     * 学生端：查询所有已上架项目。
     */
    public List<ProjectListDTO> listActive() {
        return listActive(null);
    }

    /**
     * 学生端：查询已上架项目（带报名状态）。
     * 仅显示学生所属机构的项目和通用项目（orgId 为 null）。
     */
    public List<ProjectListDTO> listActive(Long studentId) {
        LambdaQueryWrapper<Project> wrapper = new LambdaQueryWrapper<Project>()
                .eq(Project::getStatus, STATUS_APPROVED);
        if (studentId != null) {
            SysUser student = sysUserMapper.selectById(studentId);
            if (student != null && student.getOrgId() != null) {
                wrapper.and(w -> w.eq(Project::getOrgId, student.getOrgId())
                        .or().isNull(Project::getOrgId));
            }
        }
        wrapper.orderByDesc(Project::getCreatedAt);
        List<Project> projects = projectMapper.selectList(wrapper);
        List<ProjectListDTO> dtos = toListDTO(projects);
        
        if (studentId != null && !dtos.isEmpty()) {
            List<Long> projectIds = dtos.stream().map(ProjectListDTO::getId).collect(Collectors.toList());
            List<StudentProject> enrollments = studentProjectMapper.selectList(
                    new LambdaQueryWrapper<StudentProject>()
                            .eq(StudentProject::getStudentId, studentId)
                            .in(StudentProject::getProjectId, projectIds)
                            .ne(StudentProject::getStatus, StudentProject.STATUS_CANCELLED));
            
            Map<Long, String> enrollmentMap = new HashMap<>();
            for (StudentProject e : enrollments) {
                enrollmentMap.put(e.getProjectId(), e.getStatus());
            }
            
            for (ProjectListDTO dto : dtos) {
                String status = enrollmentMap.get(dto.getId());
                if (status != null) {
                    dto.setEnrolled(true);
                    dto.setEnrollmentStatus(status);
                } else {
                    dto.setEnrolled(false);
                }
            }
        }
        
        return dtos;
    }

    /**
     * 机构端：查询本机构所有项目（不分页，用于下拉选择等）。
     */
    public List<ProjectListDTO> listByOrgId(Long orgId) {
        List<Project> projects = projectMapper.selectList(
                new LambdaQueryWrapper<Project>()
                        .eq(Project::getOrgId, orgId)
                        .orderByDesc(Project::getCreatedAt));
        return toListDTO(projects);
    }

    // ==================== 创建/编辑 ====================

    /**
     * 机构创建项目并提交上架申请。
     */
    @Transactional(rollbackFor = Exception.class)
    public Project create(Project project, SysUser operator) {
        checkOrgAdmin(operator);
        validateProject(project);

        project.setId(null);
        project.setOrgId(operator.getOrgId());
        project.setStatus(STATUS_IN_REVIEW);
        projectMapper.insert(project);

        Application app = new Application();
        app.setBizType("PROJECT_UP");
        app.setBizKey(PROJECT_UP_STANDARD_ID);
        app.setApplicantId(operator.getId());
        app.setOrgId(operator.getOrgId());
        app.setFormData(buildProjectFormData(project));
        Application savedApp = applicationService.submit(app);

        project.setApplicationId(savedApp.getId());
        projectMapper.updateById(project);

        userOpLogMapper.insert(UserOpLog.createLog(
                operator.getId(), operator.getRealName(), null, null,
                UserOpLog.MODULE_PROJECT, UserOpLog.ACTION_CREATE,
                "创建项目并提交上架申请：" + project.getName()));
        return getById(project.getId());
    }

    /**
     * 机构编辑项目并重新提交审核。
     */
    @Transactional(rollbackFor = Exception.class)
    public Project update(Long id, Project update, SysUser operator, boolean cancelStudents) {
        checkOrgAdmin(operator);
        Project project = getById(id);

        if (!project.getOrgId().equals(operator.getOrgId())) {
            throw new BizException("只能编辑本机构的项目");
        }
        if (project.getStatus() == STATUS_IN_REVIEW) {
            throw new BizException("审核中的项目不允许编辑");
        }

        boolean isInProgress = project.getStatus() == STATUS_APPROVED || project.getStatus() == STATUS_OFFLINE;
        if (isInProgress && cancelStudents) {
            cancelEnrolledStudents(id);
        }

        if (update.getName() != null) project.setName(update.getName());
        if (update.getDescription() != null) project.setDescription(update.getDescription());
        if (update.getCreditReward() != null) project.setCreditReward(update.getCreditReward());
        if (update.getCreditPrice() != null) project.setCreditPrice(update.getCreditPrice());
        if (update.getExpertId() != null) project.setExpertId(update.getExpertId());

        validateProject(project);
        project.setStatus(STATUS_IN_REVIEW);
        projectMapper.updateById(project);

        if (project.getApplicationId() != null) {
            applicationService.resubmit(project.getApplicationId(), operator.getId(), buildProjectFormData(project));
        } else {
            Application app = new Application();
            app.setBizType("PROJECT_UP");
            app.setBizKey(PROJECT_UP_STANDARD_ID);
            app.setApplicantId(operator.getId());
            app.setOrgId(operator.getOrgId());
            app.setFormData(buildProjectFormData(project));
            Application savedApp = applicationService.submit(app);
            project.setApplicationId(savedApp.getId());
            projectMapper.updateById(project);
        }

        userOpLogMapper.insert(UserOpLog.createLog(
                operator.getId(), operator.getRealName(), null, null,
                UserOpLog.MODULE_PROJECT, UserOpLog.ACTION_UPDATE,
                "编辑项目并重新提交审核：" + project.getName()));
        return getById(project.getId());
    }

    private String buildProjectFormData(Project project) {
        return "{\"projectId\":" + project.getId() +
                ",\"standardId\":" + PROJECT_UP_STANDARD_ID +
                ",\"projectName\":\"" + escapeJson(project.getName()) + "\"}";
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private void cancelEnrolledStudents(Long projectId) {
        List<StudentProject> enrollments = studentProjectMapper.selectList(
                new LambdaQueryWrapper<StudentProject>()
                        .eq(StudentProject::getProjectId, projectId)
                        .ne(StudentProject::getStatus, StudentProject.STATUS_COMPLETED)
                        .ne(StudentProject::getStatus, StudentProject.STATUS_CANCELLED));
        for (StudentProject e : enrollments) {
            e.setStatus(StudentProject.STATUS_CANCELLED);
            studentProjectMapper.updateById(e);
        }
    }

    // ==================== 审核/上下架 ====================

    /**
     * 管理员审核项目。
     */
    @Transactional(rollbackFor = Exception.class)
    public Project audit(Long id, boolean approve, String reason, SysUser operator) {
        checkAdmin(operator);
        Project project = getById(id);

        if (project.getStatus() != STATUS_PENDING) {
            throw new BizException("当前状态不可审核");
        }

        if (approve) {
            project.setStatus(STATUS_APPROVED);
        } else {
            if (reason == null || reason.trim().isEmpty()) {
                throw new BizException("驳回时必须填写原因");
            }
            project.setStatus(STATUS_REJECTED);
        }
        projectMapper.updateById(project);

        String actionName = approve ? "通过" : "驳回";
        userOpLogMapper.insert(UserOpLog.createLog(
                operator.getId(), operator.getRealName(), null, null,
                UserOpLog.MODULE_PROJECT, UserOpLog.ACTION_PROJECT_AUDIT,
                actionName + "项目：" + project.getName() + (approve ? "" : "，原因：" + reason)));
        return project;
    }

    /**
     * 管理员下架已上架项目。
     */
    @Transactional(rollbackFor = Exception.class)
    public Project offline(Long id, SysUser operator) {
        checkAdmin(operator);
        Project project = getById(id);
        if (project.getStatus() != STATUS_APPROVED) {
            throw new BizException("只有已上架项目可以下架");
        }
        project.setStatus(STATUS_OFFLINE);
        projectMapper.updateById(project);

        userOpLogMapper.insert(UserOpLog.createLog(
                operator.getId(), operator.getRealName(), null, null,
                UserOpLog.MODULE_PROJECT, UserOpLog.ACTION_PROJECT_OFFLINE,
                "下架项目：" + project.getName()));
        return project;
    }

    // ==================== 内部工具 ====================

    private void validateProject(Project project) {
        if (project.getName() == null || project.getName().trim().isEmpty()) {
            throw new BizException("项目名称不能为空");
        }
        if (project.getCreditReward() == null || project.getCreditReward() < 0) {
            throw new BizException("积分奖励不能为负数");
        }
        if (project.getCreditPrice() == null || project.getCreditPrice() < 0) {
            throw new BizException("报名费用不能为负数");
        }
        if (project.getExpertId() != null) {
            SysUser expert = sysUserMapper.selectById(project.getExpertId());
            if (expert == null || !"expert".equals(expert.getRole())) {
                throw new BizException("负责专家不存在或角色不是专家");
            }
        }
    }

    private void checkOrgAdmin(SysUser operator) {
        if (operator == null || !"org_admin".equals(operator.getRole())) {
            throw new BizException("只有机构管理员可以操作");
        }
    }

    private void checkAdmin(SysUser operator) {
        if (operator == null || !"admin".equals(operator.getRole())) {
            throw new BizException("只有系统管理员可以操作");
        }
    }

    private Page<ProjectListDTO> toListDTOPage(Page<Project> page) {
        Page<ProjectListDTO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(toListDTO(page.getRecords()));
        return result;
    }

    private List<ProjectListDTO> toListDTO(List<Project> projects) {
        if (projects.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> orgIds = projects.stream()
                .map(Project::getOrgId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> orgNameMap = new HashMap<>();
        if (!orgIds.isEmpty()) {
            organizationMapper.selectBatchIds(orgIds)
                    .forEach(o -> orgNameMap.put(o.getId(), o.getName()));
        }

        List<Long> expertIds = projects.stream()
                .map(Project::getExpertId)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        Map<Long, String> expertNameMap = new HashMap<>();
        if (!expertIds.isEmpty()) {
            sysUserMapper.selectBatchIds(expertIds)
                    .forEach(u -> expertNameMap.put(u.getId(), u.getRealName()));
        }

        return projects.stream().map(p -> {
            ProjectListDTO dto = new ProjectListDTO();
            dto.setId(p.getId());
            dto.setName(p.getName());
            dto.setDescription(p.getDescription());
            dto.setCreditReward(p.getCreditReward());
            dto.setCreditPrice(p.getCreditPrice());
            dto.setStatus(p.getStatus());
            dto.setStatusName(STATUS_NAME.getOrDefault(p.getStatus(), "未知"));
            dto.setOrgId(p.getOrgId());
            dto.setOrgName(orgNameMap.getOrDefault(p.getOrgId(), ""));
            dto.setExpertId(p.getExpertId());
            dto.setExpertName(expertNameMap.getOrDefault(p.getExpertId(), ""));
            dto.setCreatedAt(p.getCreatedAt());
            dto.setUpdatedAt(p.getUpdatedAt());
            return dto;
        }).collect(Collectors.toList());
    }

    private ProjectDetailDTO toDetailDTO(Project p) {
        ProjectDetailDTO dto = new ProjectDetailDTO();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setCreditReward(p.getCreditReward());
        dto.setCreditPrice(p.getCreditPrice());
        dto.setStatus(p.getStatus());
        dto.setStatusName(STATUS_NAME.getOrDefault(p.getStatus(), "未知"));
        dto.setOrgId(p.getOrgId());
        dto.setExpertId(p.getExpertId());
        dto.setCreatedAt(p.getCreatedAt());
        dto.setUpdatedAt(p.getUpdatedAt());

        if (p.getOrgId() != null) {
            Organization org = organizationMapper.selectById(p.getOrgId());
            dto.setOrgName(org != null ? org.getName() : "");
        }
        if (p.getExpertId() != null) {
            SysUser expert = sysUserMapper.selectById(p.getExpertId());
            dto.setExpertName(expert != null ? expert.getRealName() : "");
        }

        return dto;
    }
}
