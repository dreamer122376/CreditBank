package com.creditbank.mvp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creditbank.mvp.entity.Project;

/**
 * 项目数据访问层。继承 BaseMapper 后自带 selectById / insert / updateById / selectList 等方法。
 */
public interface ProjectMapper extends BaseMapper<Project> {
}
