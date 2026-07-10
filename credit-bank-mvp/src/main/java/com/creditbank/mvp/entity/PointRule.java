package com.creditbank.mvp.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.math.BigDecimal;

/**
 * 积分规则。对应表 t_point_rule。
 * 这是"数据驱动规则"的最小雏形：规则（哪种行为奖励多少分）存在表里，
 * 运营改分值只改数据、不用改代码。真实系统会在这里扩展触发条件、上下限、时段等。
 */
@TableName("t_point_rule")
public class PointRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 规则编码，业务里用它来触发，例如 COURSE_DONE */
    private String ruleCode;

    private String ruleName;

    /** 命中该规则一次奖励的积分 */
    private BigDecimal points;

    /** 1 启用，0 停用 */
    private Integer enabled;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getRuleCode() {
        return ruleCode;
    }

    public void setRuleCode(String ruleCode) {
        this.ruleCode = ruleCode;
    }

    public String getRuleName() {
        return ruleName;
    }

    public void setRuleName(String ruleName) {
        this.ruleName = ruleName;
    }

    public BigDecimal getPoints() {
        return points;
    }

    public void setPoints(BigDecimal points) {
        this.points = points;
    }

    public Integer getEnabled() {
        return enabled;
    }

    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }
}
