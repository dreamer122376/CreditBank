package com.creditbank.mvp.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.entity.Project;
import com.creditbank.mvp.mapper.ProjectMapper;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/project")
public class ProjectController {

    private final ProjectMapper projectMapper;

    public ProjectController(ProjectMapper projectMapper) {
        this.projectMapper = projectMapper;
    }

    @GetMapping("/list")
    public Result<List<Project>> list() {
        return Result.ok(projectMapper.selectList(
                new LambdaQueryWrapper<Project>().orderByDesc(Project::getId)));
    }

    @GetMapping("/{id}")
    public Result<Project> detail(@PathVariable Long id) {
        return Result.ok(projectMapper.selectById(id));
    }
}