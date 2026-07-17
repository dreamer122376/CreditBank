package com.creditbank.mvp.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.creditbank.mvp.entity.ConversionRule;
import org.apache.ibatis.annotations.Mapper;

/**
 * 转换规则 Mapper。
 * 额外注释
 */
@Mapper
public interface ConversionRuleMapper extends BaseMapper<ConversionRule> {
}