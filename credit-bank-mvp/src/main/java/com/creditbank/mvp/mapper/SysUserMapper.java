package com.creditbank.mvp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creditbank.mvp.entity.SysUser;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface SysUserMapper extends BaseMapper<SysUser> {

    /** 条件扣减积分：余额不足时影响行数为 0，调用方据此判定失败，避免并发超扣 */
    @Update("UPDATE sys_user SET balance = balance - #{amount} " +
            "WHERE id = #{userId} AND balance >= #{amount}")
    int deductBalance(@Param("userId") Long userId, @Param("amount") int amount);
}
