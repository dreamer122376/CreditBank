package com.creditbank.mvp;

import com.creditbank.mvp.entity.Project;
import com.creditbank.mvp.entity.StudentProject;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.service.StudentProjectService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class StudentProjectTest {

    @Autowired
    private StudentProjectService studentProjectService;

    @Test
    public void testGetStudentProjects() {
        Long studentId = 6L;
        SysUser student = studentProjectService.getStudentWithProjects(studentId);
        
        if (student == null) {
            System.out.println("未找到学生(id=" + studentId + ")");
            return;
        }
        
        System.out.println("学生信息: " + student.getRealName() + " (" + student.getUsername() + ")");
        System.out.println("报名的项目列表:");
        
        List<StudentProject> spList = student.getStudentProjects();
        if (spList == null || spList.isEmpty()) {
            System.out.println("  暂无报名项目");
            return;
        }
        
        for (StudentProject sp : spList) {
            String projectName = findProjectName(student.getProjects(), sp.getProjectId());
            System.out.println("  - 项目名称: " + projectName);
            System.out.println("    阶段状态: " + sp.getStatus());
            System.out.println("    报名时间: " + sp.getCreatedAt());
            System.out.println();
        }
    }

    private String findProjectName(List<Project> projects, Long projectId) {
        if (projects == null) {
            return "未知项目(id=" + projectId + ")";
        }
        return projects.stream()
                .filter(p -> p.getId().equals(projectId))
                .map(Project::getName)
                .findFirst()
                .orElse("未知项目(id=" + projectId + ")");
    }
}
