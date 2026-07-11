package com.creditbank.mvp.controller;

import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.entity.Project;
import com.creditbank.mvp.entity.StudentProject;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.service.StudentProjectService;
import com.creditbank.mvp.util.CurrentUserUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student-projects")
public class StudentProjectController {

    private final StudentProjectService studentProjectService;

    public StudentProjectController(StudentProjectService studentProjectService) {
        this.studentProjectService = studentProjectService;
    }

    @GetMapping("/my-projects")
    public Result<List<Project>> getMyProjects() {
        Long studentId = CurrentUserUtil.getCurrentUserId();
        return Result.ok(studentProjectService.getProjectsByStudentId(studentId));
    }

    @GetMapping("/my-detail")
    public Result<SysUser> getMyDetail() {
        Long studentId = CurrentUserUtil.getCurrentUserId();
        return Result.ok(studentProjectService.getStudentWithProjects(studentId));
    }

    @PostMapping("/register")
    public Result<StudentProject> register(@RequestParam Long projectId) {
        Long studentId = CurrentUserUtil.getCurrentUserId();
        return Result.ok(studentProjectService.registerProject(studentId, projectId));
    }

    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestParam String status) {
        studentProjectService.updateStatus(id, status);
        return Result.ok();
    }

    @GetMapping("/project/{projectId}/students")
    public Result<List<SysUser>> getStudentsByProject(@PathVariable Long projectId) {
        return Result.ok(studentProjectService.getStudentsByProjectId(projectId));
    }
}