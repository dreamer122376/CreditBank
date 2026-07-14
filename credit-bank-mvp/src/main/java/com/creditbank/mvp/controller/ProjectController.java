package com.creditbank.mvp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.dto.ProjectDetailDTO;
import com.creditbank.mvp.dto.ProjectEnrollmentDTO;
import com.creditbank.mvp.dto.ProjectListDTO;
import com.creditbank.mvp.entity.Project;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.service.ProjectService;
import com.creditbank.mvp.service.StudentProjectService;
import com.creditbank.mvp.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目模块 REST 接口。
 *
 * 学生端：浏览已上架项目、查看详情、报名、查看我的项目、取消报名
 * 机构端：创建/编辑本机构项目、查看本机构项目
 * 管理端：分页列表、审核、下架
 */
@RestController
@RequestMapping("/api/projects")
public class ProjectController {

    private final ProjectService projectService;
    private final StudentProjectService studentProjectService;
    private final UserService userService;

    public ProjectController(ProjectService projectService,
                             StudentProjectService studentProjectService,
                             UserService userService) {
        this.projectService = projectService;
        this.studentProjectService = studentProjectService;
        this.userService = userService;
    }

    // ==================== 学生端 ====================

    /** 学生端：查询所有已上架项目 */
    @GetMapping("/active")
    public Result<List<ProjectListDTO>> listActive(@RequestParam(required = false) Long studentId) {
        return Result.ok(projectService.listActive(studentId));
    }

    /** 项目详情（学生端查看时可选传 studentId 标记是否已报名） */
    @GetMapping("/{id}")
    public Result<ProjectDetailDTO> detail(@PathVariable Long id,
                                           @RequestParam(required = false) Long studentId) {
        return Result.ok(projectService.getDetail(id, studentId));
    }

    /** 学生报名项目 */
    @PostMapping("/{id}/enroll")
    public Result<?> enroll(@PathVariable Long id,
                            @RequestHeader("X-Operator-Id") Long studentId) {
        studentProjectService.enroll(studentId, id);
        return Result.ok();
    }

    /** 学生取消报名 */
    @DeleteMapping("/{id}/enroll")
    public Result<?> cancelEnroll(@PathVariable Long id,
                                  @RequestHeader("X-Operator-Id") Long studentId) {
        studentProjectService.cancelEnrollment(studentId, id);
        return Result.ok();
    }

    /** 我的项目列表 */
    @GetMapping("/my")
    public Result<List<ProjectEnrollmentDTO>> myProjects(@RequestHeader("X-Operator-Id") Long studentId) {
        return Result.ok(studentProjectService.getMyProjects(studentId));
    }

    // ==================== 机构端 ====================

    /** 机构端：本机构项目列表 */
    @GetMapping("/org")
    public Result<List<ProjectListDTO>> listByOrg(@RequestHeader("X-Operator-Id") Long operatorId) {
        SysUser operator = userService.getUser(operatorId);
        return Result.ok(projectService.listByOrgId(operator.getOrgId()));
    }

    /** 机构端：创建项目 */
    @PostMapping
    public Result<Project> create(@RequestBody Project project,
                                  @RequestHeader("X-Operator-Id") Long operatorId) {
        SysUser operator = userService.getUser(operatorId);
        return Result.ok(projectService.create(project, operator));
    }

    /** 机构端：编辑项目 */
    @PutMapping("/{id}")
    public Result<Project> update(@PathVariable Long id,
                                  @RequestBody ProjectUpdateRequest req,
                                  @RequestHeader("X-Operator-Id") Long operatorId) {
        SysUser operator = userService.getUser(operatorId);
        Project project = new Project();
        project.setName(req.getName());
        project.setDescription(req.getDescription());
        project.setCreditReward(req.getCreditReward());
        project.setCreditPrice(req.getCreditPrice());
        project.setExpertId(req.getExpertId());
        return Result.ok(projectService.update(id, project, operator, req.isCancelStudents()));
    }

    // ==================== 管理端 ====================

    /** 管理端：项目分页列表 */
    @GetMapping
    public Result<Page<ProjectListDTO>> page(
            @RequestHeader("X-Operator-Id") Long operatorId,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        SysUser operator = userService.getUser(operatorId);
        return Result.ok(projectService.page(operator.getRole(), operator.getOrgId(), status, page, size));
    }

    /** 管理端：审核项目 */
    @PostMapping("/{id}/audit")
    public Result<Project> audit(@PathVariable Long id,
                                 @RequestBody AuditRequest req,
                                 @RequestHeader("X-Operator-Id") Long operatorId) {
        SysUser operator = userService.getUser(operatorId);
        return Result.ok(projectService.audit(id, req.isApprove(), req.getReason(), operator));
    }

    /** 管理端：下架项目 */
    @PostMapping("/{id}/offline")
    public Result<Project> offline(@PathVariable Long id,
                                   @RequestHeader("X-Operator-Id") Long operatorId) {
        SysUser operator = userService.getUser(operatorId);
        return Result.ok(projectService.offline(id, operator));
    }

    public static class AuditRequest {
        private boolean approve;
        private String reason;

        public boolean isApprove() {
            return approve;
        }

        public void setApprove(boolean approve) {
            this.approve = approve;
        }

        public String getReason() {
            return reason;
        }

        public void setReason(String reason) {
            this.reason = reason;
        }
    }

    public static class ProjectUpdateRequest {
        private String name;
        private String description;
        private Integer creditReward;
        private Integer creditPrice;
        private Long expertId;
        private boolean cancelStudents;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public Integer getCreditReward() {
            return creditReward;
        }

        public void setCreditReward(Integer creditReward) {
            this.creditReward = creditReward;
        }

        public Integer getCreditPrice() {
            return creditPrice;
        }

        public void setCreditPrice(Integer creditPrice) {
            this.creditPrice = creditPrice;
        }

        public Long getExpertId() {
            return expertId;
        }

        public void setExpertId(Long expertId) {
            this.expertId = expertId;
        }

        public boolean isCancelStudents() {
            return cancelStudents;
        }

        public void setCancelStudents(boolean cancelStudents) {
            this.cancelStudents = cancelStudents;
        }
    }
}
