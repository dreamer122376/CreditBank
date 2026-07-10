package com.creditbank.mvp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creditbank.mvp.entity.Account;

/**
 * 继承 BaseMapper 后，自动拥有 selectById / insert / updateById / selectList 等方法，
 * 简单场景一行 SQL 都不用写。复杂查询再自己加方法 + XML。
 */
public interface AccountMapper extends BaseMapper<Account> {
}
