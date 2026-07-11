package com.creditbank.mvp;

import com.creditbank.mvp.entity.Project;
import com.creditbank.mvp.mapper.ProjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class ProjectTest {

    @Autowired
    private ProjectMapper projectMapper;

    @Test
    public void testSelectById() {
        Project project = projectMapper.selectById(4L);
        System.out.println("查询结果(id=4):");
        System.out.println("id: " + project.getId());
        System.out.println("orgId: " + project.getOrgId());
        System.out.println("expertId: " + project.getExpertId());
        System.out.println("name: " + project.getName());
        System.out.println("description: " + project.getDescription());
        System.out.println("status: " + project.getStatus());
        System.out.println("createdAt: " + project.getCreatedAt());
        System.out.println("updatedAt: " + project.getUpdatedAt());
    }
}
