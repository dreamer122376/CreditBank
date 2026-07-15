package com.creditbank.mvp.service;

import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.NotificationRecipient;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.entity.SystemNotification;
import com.creditbank.mvp.dto.PublishNotificationRequest;
import com.creditbank.mvp.dto.PublishNotificationResult;
import com.creditbank.mvp.mapper.NotificationRecipientMapper;
import com.creditbank.mvp.mapper.OrganizationMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.mapper.SystemNotificationMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class NotificationServiceTest {

    private SystemNotificationMapper notificationMapper;
    private NotificationRecipientMapper recipientMapper;
    private SysUserMapper sysUserMapper;
    private OrganizationMapper organizationMapper;
    private NotificationService service;

    @BeforeEach
    void setUp() {
        notificationMapper = mock(SystemNotificationMapper.class);
        recipientMapper = mock(NotificationRecipientMapper.class);
        sysUserMapper = mock(SysUserMapper.class);
        organizationMapper = mock(OrganizationMapper.class);
        service = new NotificationService(notificationMapper, recipientMapper, sysUserMapper, organizationMapper);
    }

    @Test
    void welcomeNotificationIsRoleSpecificAndUnread() {
        SysUser student = new SysUser();
        student.setId(7L);
        student.setRole("student");
        when(sysUserMapper.selectById(7L)).thenReturn(student);
        when(notificationMapper.selectCount(any())).thenReturn(0L);
        when(notificationMapper.insert(any())).thenAnswer(invocation -> {
            SystemNotification value = invocation.getArgument(0);
            value.setId(101L);
            return 1;
        });

        service.ensureWelcomeNotification(7L);

        ArgumentCaptor<SystemNotification> message = ArgumentCaptor.forClass(SystemNotification.class);
        verify(notificationMapper).insert(message.capture());
        assertEquals("WELCOME:7:v1", message.getValue().getDedupeKey());
        assertTrue(message.getValue().getContent().contains("积分商城"));

        ArgumentCaptor<NotificationRecipient> recipient = ArgumentCaptor.forClass(NotificationRecipient.class);
        verify(recipientMapper).insert(recipient.capture());
        assertEquals(7L, recipient.getValue().getUserId());
        assertNull(recipient.getValue().getReadAt());
    }

    @Test
    void welcomeNotificationDoesNotDuplicate() {
        SysUser user = new SysUser();
        user.setId(3L);
        user.setRole("org_admin");
        when(sysUserMapper.selectById(3L)).thenReturn(user);
        when(notificationMapper.selectCount(any())).thenReturn(1L);

        service.ensureWelcomeNotification(3L);

        verify(notificationMapper, never()).insert(any());
        verify(recipientMapper, never()).insert(any());
    }

    @Test
    void userCannotMarkAnotherUsersNotificationRead() {
        BizException error = assertThrows(BizException.class, () -> service.markRead(5L, 99L));

        assertEquals("通知不存在或不属于当前用户", error.getMessage());
    }

    @Test
    void organizationAdminIsForcedToOwnOrganizationScope() {
        SysUser orgAdmin = new SysUser();
        orgAdmin.setId(2L);
        orgAdmin.setRole("org_admin");
        orgAdmin.setOrgId(8L);
        orgAdmin.setStatus(1);
        SysUser student = new SysUser();
        student.setId(3L);
        student.setRole("student");
        student.setOrgId(8L);
        student.setStatus(1);
        when(sysUserMapper.selectById(2L)).thenReturn(orgAdmin);
        when(sysUserMapper.selectList(any())).thenReturn(java.util.Arrays.asList(orgAdmin, student));
        when(notificationMapper.selectCount(any())).thenReturn(0L);
        when(notificationMapper.insert(any())).thenAnswer(invocation -> {
            SystemNotification value = invocation.getArgument(0);
            value.setId(201L);
            return 1;
        });

        PublishNotificationRequest request = new PublishNotificationRequest();
        request.setScopeType("ALL");
        request.setTitle("本机构通知");
        request.setContent("请本机构用户及时查看。 ");

        PublishNotificationResult result = service.publishManualNotice(2L, request);

        assertEquals("ORG", result.getScopeType());
        assertEquals(8L, result.getOrgId());
        assertEquals(2, result.getRecipientCount());
        ArgumentCaptor<SystemNotification> message = ArgumentCaptor.forClass(SystemNotification.class);
        verify(notificationMapper).insert(message.capture());
        assertEquals("8", message.getValue().getScopeValue());
    }

    @Test
    void importantNoticeRequiresExplicitConfirmation() {
        NotificationRecipient recipient = new NotificationRecipient();
        recipient.setId(12L);
        recipient.setNotificationId(50L);
        recipient.setUserId(7L);
        SystemNotification notification = new SystemNotification();
        notification.setId(50L);
        notification.setLevel("WARNING");
        notification.setStatus("PUBLISHED");
        when(recipientMapper.selectOne(any())).thenReturn(recipient);
        when(notificationMapper.selectById(50L)).thenReturn(notification);

        service.confirmImportantNotice(7L, 50L);

        assertNotNull(recipient.getReadAt());
        assertNotNull(recipient.getConfirmedAt());
        verify(recipientMapper).updateById(recipient);
    }

    @Test
    void organizationAdminCannotRevokePlatformAdminsNotice() {
        SysUser orgAdmin = new SysUser();
        orgAdmin.setId(2L);
        orgAdmin.setRole("org_admin");
        orgAdmin.setOrgId(8L);
        SystemNotification notification = new SystemNotification();
        notification.setId(60L);
        notification.setSourceType("MANUAL_NOTICE");
        notification.setScopeType("ORG");
        notification.setScopeValue("8");
        notification.setActorId(1L);
        notification.setStatus("PUBLISHED");
        when(sysUserMapper.selectById(2L)).thenReturn(orgAdmin);
        when(notificationMapper.selectById(60L)).thenReturn(notification);

        BizException error = assertThrows(BizException.class,
                () -> service.revokeManualNotice(2L, 60L));

        assertEquals("只能撤回自己发布的本机构通知", error.getMessage());
        verify(notificationMapper, never()).updateById(any());
    }

    @Test
    void archiveTaskArchivesExpiredAndStaleInfoNotices() {
        when(notificationMapper.update(isNull(), any())).thenReturn(2, 1);

        assertEquals(3, service.archiveDueNotifications());
        verify(notificationMapper, times(2)).update(isNull(), any());
    }
}
