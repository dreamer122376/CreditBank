package com.creditbank.mvp.service;

import com.creditbank.mvp.entity.Project;
import com.creditbank.mvp.entity.StudentProject;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.mapper.ProjectMapper;
import com.creditbank.mvp.mapper.StudentProjectMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class StudentProjectService {

    private final StudentProjectMapper studentProjectMapper;
    private final ProjectMapper projectMapper;
    private final SysUserMapper sysUserMapper;

    public StudentProjectService(StudentProjectMapper studentProjectMapper,
                                 ProjectMapper projectMapper,
                                 SysUserMapper sysUserMapper) {
        this.studentProjectMapper = studentProjectMapper;
        this.projectMapper = projectMapper;
        this.sysUserMapper = sysUserMapper;
    }

    public List<Project> getProjectsByStudentId(Long studentId) {
        List<StudentProject> spList = studentProjectMapper.selectByStudentId(studentId);
        if (spList.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> projectIds = spList.stream()
                .map(StudentProject::getProjectId)
                .distinct()
                .collect(Collectors.toList());
        return projectMapper.selectBatchIds(projectIds);
    }

    public List<SysUser> getStudentsByProjectId(Long projectId) {
        List<StudentProject> spList = studentProjectMapper.selectByProjectId(projectId);
        if (spList.isEmpty()) {
            return new ArrayList<>();
        }
        List<Long> studentIds = spList.stream()
                .map(StudentProject::getStudentId)
                .distinct()
                .collect(Collectors.toList());
        return sysUserMapper.selectBatchIds(studentIds);
    }

    public Project getProjectWithStudents(Long projectId) {
        Project project = projectMapper.selectById(projectId);
        if (project == null) {
            return null;
        }
        List<StudentProject> spList = studentProjectMapper.selectByProjectId(projectId);
        project.setStudentProjects(spList);

        if (!spList.isEmpty()) {
            List<Long> studentIds = spList.stream()
                    .map(StudentProject::getStudentId)
                    .distinct()
                    .collect(Collectors.toList());
            project.setStudents(sysUserMapper.selectBatchIds(studentIds));
        }
        return project;
    }

    public SysUser getStudentWithProjects(Long studentId) {
        SysUser user = sysUserMapper.selectById(studentId);
        if (user == null) {
            return null;
        }
        List<StudentProject> spList = studentProjectMapper.selectByStudentId(studentId);
        user.setStudentProjects(spList);

        if (!spList.isEmpty()) {
            List<Long> projectIds = spList.stream()
                    .map(StudentProject::getProjectId)
                    .distinct()
                    .collect(Collectors.toList());
            user.setProjects(projectMapper.selectBatchIds(projectIds));
        }
        return user;
    }

    public StudentProject registerProject(Long studentId, Long projectId) {
        StudentProject sp = new StudentProject();
        sp.setStudentId(studentId);
        sp.setProjectId(projectId);
        sp.setStatus("已报名");
        studentProjectMapper.insert(sp);
        return sp;
    }

    public void updateStatus(Long id, String status) {
        StudentProject sp = studentProjectMapper.selectById(id);
        if (sp != null) {
            sp.setStatus(status);
            studentProjectMapper.updateById(sp);
        }
    }
}
