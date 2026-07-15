package com.creditbank.mvp.service;

import com.creditbank.mvp.entity.CreditRule;
import com.creditbank.mvp.entity.Organization;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.entity.TransactionLog;
import com.creditbank.mvp.mapper.CreditRuleMapper;
import com.creditbank.mvp.mapper.OrganizationMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.mapper.TransactionLogMapper;
import com.creditbank.mvp.mapper.UserOpLogMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PointNotificationTest {

    @Test
    void adminGrantToOrganizationPublishesPlatformNotification() {
        SysUserMapper userMapper = mock(SysUserMapper.class);
        CreditRuleMapper ruleMapper = mock(CreditRuleMapper.class);
        TransactionLogMapper transactionMapper = mock(TransactionLogMapper.class);
        UserOpLogMapper opLogMapper = mock(UserOpLogMapper.class);
        CampaignService campaignService = mock(CampaignService.class);
        PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);
        RedisService redisService = mock(RedisService.class);
        OrganizationMapper organizationMapper = mock(OrganizationMapper.class);
        NotificationService notificationService = mock(NotificationService.class);

        SysUser admin = new SysUser();
        admin.setId(1L);
        admin.setRole("admin");
        admin.setRealName("系统管理员");

        SysUser orgAdmin = new SysUser();
        orgAdmin.setId(2L);
        orgAdmin.setRole("org_admin");
        orgAdmin.setOrgId(9L);
        orgAdmin.setRealName("机构管理员");
        orgAdmin.setBalance(500);

        CreditRule rule = new CreditRule();
        rule.setId(10L);
        rule.setEventCode("ADMIN");
        rule.setEventName("管理员手动加分");
        rule.setIsEnabled(1);

        Organization organization = new Organization();
        organization.setId(9L);
        organization.setName("测试学院");

        when(userMapper.selectById(2L)).thenReturn(orgAdmin);
        when(userMapper.selectById(1L)).thenReturn(admin);
        when(ruleMapper.selectOne(any())).thenReturn(rule);
        when(userMapper.updateById(any())).thenReturn(1);
        when(transactionMapper.insert(any())).thenAnswer(invocation -> {
            TransactionLog log = invocation.getArgument(0);
            log.setId(77L);
            return 1;
        });
        when(opLogMapper.insert(any())).thenReturn(1);
        when(organizationMapper.selectById(9L)).thenReturn(organization);

        PointService service = new PointService(userMapper, ruleMapper, transactionMapper,
                opLogMapper, campaignService, passwordEncoder, redisService,
                organizationMapper, notificationService);

        service.earn(2L, "ADMIN", 1L, 100);

        assertEquals(600, orgAdmin.getBalance());
        verify(notificationService).sendToAll(
                eq("ORG_POINTS_GRANTED"), eq(NotificationService.CATEGORY_POINT), eq("INFO"),
                eq("机构积分池已增加"), contains("测试学院"), isNull(),
                eq("TRANSACTION"), eq(77L), eq("ORG_POINTS_GRANTED:77"), eq(1L));
    }
}
