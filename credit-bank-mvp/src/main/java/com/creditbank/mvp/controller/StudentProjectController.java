package com.creditbank.mvp.controller;

import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.dto.ProjectEnrollmentDTO;
import com.creditbank.mvp.entity.StudentProject;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.service.StudentProjectService;
import com.creditbank.mvp.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 学生项目关系 REST 接口（旧接口保留，兼容已有前端页面）。
 * 新功能建议优先使用 {@link ProjectController}。
 */
@RestController
@RequestMapping("/api/student-projects")
public class StudentProjectController {

    private final StudentProjectService studentProjectService;
    private final UserService userService;

    public StudentProjectController(StudentProjectService studentProjectService,
                                    UserService userService) {
        this.studentProjectService = studentProjectService;
        this.userService = userService;
    }

    @GetMapping("/my-projects")
    public Result<List<ProjectEnrollmentDTO>> getMyProjects(@RequestHeader("X-Operator-Id") Long studentId) {
        return Result.ok(studentProjectService.getMyProjects(studentId));
    }

    @GetMapping("/my-detail")
    public Result<SysUser> getMyDetail(@RequestHeader("X-Operator-Id") Long studentId) {
        return Result.ok(userService.getUser(studentId));
    }

    @PostMapping("/register")
    public Result<StudentProject> register(@RequestParam Long projectId,
                                           @RequestHeader("X-Operator-Id") Long studentId) {
        return Result.ok(studentProjectService.enroll(studentId, projectId));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id,
                                     @RequestParam String status,
                                     @RequestHeader("X-Operator-Id") Long operatorId) {
        SysUser operator = userService.getUser(operatorId);
        studentProjectService.updateEnrollmentStatus(id, status, operator);
        return Result.ok();
    }

    @GetMapping("/project/{projectId}/students")
    public Result<List<SysUser>> getStudentsByProject(@PathVariable Long projectId) {
        return Result.ok(studentProjectService.getStudentsByProjectId(projectId));
    }
}
