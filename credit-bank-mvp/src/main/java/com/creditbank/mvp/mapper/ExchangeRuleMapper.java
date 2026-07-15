package com.creditbank.mvp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creditbank.mvp.entity.ExchangeRule;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface ExchangeRuleMapper extends BaseMapper<ExchangeRule> {

    /** 条件扣减库存：库存不足时影响行数为 0，调用方据此判定失败，避免并发超卖 */
    @Update("UPDATE exchange_rule SET stock = stock - 1 " +
            "WHERE id = #{id} AND stock > 0")
    int deductStock(@Param("id") Long id);
}
