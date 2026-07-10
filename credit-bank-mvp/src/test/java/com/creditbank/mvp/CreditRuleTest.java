package com.creditbank.mvp;

import com.creditbank.mvp.entity.CreditRule;
import com.creditbank.mvp.mapper.CreditRuleMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class CreditRuleTest {

    @Autowired
    private CreditRuleMapper creditRuleMapper;

    @Test
    public void testSelectById() {
        CreditRule rule = creditRuleMapper.selectById(2L);
        System.out.println("查询结果:");
        System.out.println("id: " + rule.getId());
        System.out.println("eventCode: " + rule.getEventCode());
        System.out.println("eventName: " + rule.getEventName());
        System.out.println("creditValue: " + rule.getCreditValue());
        System.out.println("isEnabled: " + rule.getIsEnabled());
        System.out.println("createdAt: " + rule.getCreatedAt());
        System.out.println("projectId: " + rule.getProjectId());
    }

    @Test
    public void testSelectList() {
        List<CreditRule> rules = creditRuleMapper.selectList(null);
        System.out.println("总记录数: " + rules.size());
        for (CreditRule rule : rules) {
            System.out.println("id=" + rule.getId() + ", eventCode=" + rule.getEventCode() + ", eventName=" + rule.getEventName() + ", creditValue=" + rule.getCreditValue());
        }
    }
}
