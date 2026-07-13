package com.creditbank.mvp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.entity.Project;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.mapper.ProjectMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.service.StudentProjectService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/project")
public class ProjectController {

    private final ProjectMapper projectMapper;
    private final SysUserMapper sysUserMapper;
    private final StudentProjectService studentProjectService;

    public ProjectController(ProjectMapper projectMapper,
                             SysUserMapper sysUserMapper,
                             StudentProjectService studentProjectService) {
        this.projectMapper = projectMapper;
        this.sysUserMapper = sysUserMapper;
        this.studentProjectService = studentProjectService;
    }

    @GetMapping("/list")
    public Result<List<Project>> list() {
        return Result.ok(projectMapper.selectList(
                new LambdaQueryWrapper<Project>().orderByDesc(Project::getId)));
    }

    /** 机构管理员：查询本机构下的项目 */
    @GetMapping("/my-org")
    public Result<List<Project>> listByOrg(@RequestHeader(value = "X-Operator-Id", required = false) Long userId) {
        if (userId == null) {
            return Result.ok(projectMapper.selectList(
                    new LambdaQueryWrapper<Project>().orderByDesc(Project::getId)));
        }
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null || user.getOrgId() == null) {
            return Result.ok(projectMapper.selectList(
                    new LambdaQueryWrapper<Project>().orderByDesc(Project::getId)));
        }
        List<Project> projects = projectMapper.selectList(
                new LambdaQueryWrapper<Project>()
                        .eq(Project::getOrgId, user.getOrgId())
                        .orderByDesc(Project::getId));
        return Result.ok(projects);
    }

    @GetMapping("/{id}")
    public Result<Project> detail(@PathVariable Long id) {
        return Result.ok(projectMapper.selectById(id));
    }

    /** 项目详情（含报名学生列表） */
    @GetMapping("/{id}/detail")
    public Result<Project> detailWithStudents(@PathVariable Long id) {
        return Result.ok(studentProjectService.getProjectWithStudents(id));
    }

    /** 新增项目 */
    @PostMapping("/create")
    public Result<Project> create(@RequestBody Project project,
                                  @RequestHeader(value = "X-Operator-Id", required = false) Long userId) {
        if (userId != null) {
            SysUser user = sysUserMapper.selectById(userId);
            if (user != null && user.getOrgId() != null) {
                project.setOrgId(user.getOrgId());
            }
        }
        if (project.getStatus() == null) {
            project.setStatus(0);
        }
        project.setCreatedAt(LocalDateTime.now());
        project.setUpdatedAt(LocalDateTime.now());
        projectMapper.insert(project);
        return Result.ok(project);
    }

    /** 编辑项目 */
    @PostMapping("/update")
    public Result<Project> update(@RequestBody Project project) {
        Project existing = projectMapper.selectById(project.getId());
        if (existing == null) {
            return Result.fail("项目不存在");
        }
        if (project.getName() != null) existing.setName(project.getName());
        if (project.getDescription() != null) existing.setDescription(project.getDescription());
        if (project.getStatus() != null) existing.setStatus(project.getStatus());
        if (project.getExpertId() != null) existing.setExpertId(project.getExpertId());
        existing.setUpdatedAt(LocalDateTime.now());
        projectMapper.updateById(existing);
        return Result.ok(existing);
    }

    /** 删除项目 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        projectMapper.deleteById(id);
        return Result.ok();
    }
}
