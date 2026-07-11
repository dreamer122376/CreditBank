package com.creditbank.mvp.mapper;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creditbank.mvp.entity.StudentProject;

import java.util.List;

/**
 * 学生报名项目关系数据访问层。继承 BaseMapper 后自带基础CRUD方法。
 */
public interface StudentProjectMapper extends BaseMapper<StudentProject> {

    default List<StudentProject> selectByStudentId(Long studentId) {
        return selectList(new LambdaQueryWrapper<StudentProject>()
                .eq(StudentProject::getStudentId, studentId));
    }

    default List<StudentProject> selectByProjectId(Long projectId) {
        return selectList(new LambdaQueryWrapper<StudentProject>()
                .eq(StudentProject::getProjectId, projectId));
    }
}
