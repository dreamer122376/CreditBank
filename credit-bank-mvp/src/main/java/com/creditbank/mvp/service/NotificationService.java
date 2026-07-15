package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.dto.NotificationPageDTO;
import com.creditbank.mvp.dto.PublishNotificationRequest;
import com.creditbank.mvp.dto.PublishNotificationResult;
import com.creditbank.mvp.entity.NotificationRecipient;
import com.creditbank.mvp.entity.Organization;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.entity.SystemNotification;
import com.creditbank.mvp.mapper.NotificationRecipientMapper;
import com.creditbank.mvp.mapper.OrganizationMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.mapper.SystemNotificationMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NotificationService {

    public static final String CATEGORY_SYSTEM = "SYSTEM";
    public static final String CATEGORY_APPLICATION = "APPLICATION";
    public static final String CATEGORY_POINT = "POINT";
    public static final String CATEGORY_MALL = "MALL";

    private final SystemNotificationMapper notificationMapper;
    private final NotificationRecipientMapper recipientMapper;
    private final SysUserMapper sysUserMapper;
    private final OrganizationMapper organizationMapper;

    public NotificationService(SystemNotificationMapper notificationMapper,
                               NotificationRecipientMapper recipientMapper,
                               SysUserMapper sysUserMapper,
                               OrganizationMapper organizationMapper) {
        this.notificationMapper = notificationMapper;
        this.recipientMapper = recipientMapper;
        this.sysUserMapper = sysUserMapper;
        this.organizationMapper = organizationMapper;
    }

    public NotificationPageDTO listForUser(Long userId, int page, int size,
                                           String readStatus, String category) {
        requireUserId(userId);
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(size, 1), 100);
        String normalizedStatus = normalizeReadStatus(readStatus);
        NotificationPageDTO result = new NotificationPageDTO();
        result.setRecords(recipientMapper.selectPageForUser(
                userId, normalizedStatus, normalizeCategory(category),
                (safePage - 1) * safeSize, safeSize));
        result.setTotal(recipientMapper.countForUser(
                userId, normalizedStatus, normalizeCategory(category)));
        result.setUnreadCount(recipientMapper.countUnread(userId));
        result.setPage(safePage);
        result.setSize(safeSize);
        return result;
    }

    public long unreadCount(Long userId) {
        requireUserId(userId);
        return recipientMapper.countUnread(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long userId, Long notificationId) {
        requireUserId(userId);
        if (notificationId == null || recipientMapper.markRead(notificationId, userId) == 0) {
            throw new BizException("通知不存在或不属于当前用户");
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public int markAllRead(Long userId) {
        requireUserId(userId);
        return recipientMapper.markAllRead(userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public PublishNotificationResult publishManualNotice(Long actorId, PublishNotificationRequest request) {
        requireUserId(actorId);
        SysUser actor = sysUserMapper.selectById(actorId);
        if (actor == null || (!"admin".equals(actor.getRole()) && !"org_admin".equals(actor.getRole()))) {
            throw new BizException("仅系统管理员或机构管理员可以发布通知");
        }
        if (request == null) {
            throw new BizException("通知内容不能为空");
        }
        String title = trimRequired(request.getTitle(), "通知标题", 100);
        String content = trimRequired(request.getContent(), "通知内容", 500);
        String level = "WARNING".equalsIgnoreCase(request.getLevel()) ? "WARNING" : "INFO";

        String scopeType;
        Long targetOrgId = null;
        List<Long> recipients;
        if ("org_admin".equals(actor.getRole())) {
            if (actor.getOrgId() == null) {
                throw new BizException("当前机构管理员未绑定机构");
            }
            scopeType = "ORG";
            targetOrgId = actor.getOrgId();
            recipients = activeUsers(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getOrgId, targetOrgId), null);
        } else if ("ALL".equalsIgnoreCase(request.getScopeType())) {
            scopeType = "ALL";
            recipients = activeUsers(new LambdaQueryWrapper<SysUser>(), null);
        } else if ("ORG".equalsIgnoreCase(request.getScopeType())) {
            if (request.getOrgId() == null) {
                throw new BizException("请选择接收机构");
            }
            Organization organization = organizationMapper.selectById(request.getOrgId());
            if (organization == null) {
                throw new BizException("接收机构不存在");
            }
            scopeType = "ORG";
            targetOrgId = organization.getId();
            recipients = activeUsers(new LambdaQueryWrapper<SysUser>()
                    .eq(SysUser::getOrgId, targetOrgId), null);
        } else {
            throw new BizException("系统管理员请选择全平台或指定机构范围");
        }

        if (recipients.isEmpty()) {
            throw new BizException("当前范围内没有可接收通知的有效用户");
        }
        int recipientCount = publish(
                "MANUAL_NOTICE", scopeType,
                targetOrgId == null ? null : String.valueOf(targetOrgId),
                CATEGORY_SYSTEM, level, title, content,
                "MANUAL_NOTICE", null, null, actorId,
                "MANUAL_NOTICE:" + UUID.randomUUID(), recipients);
        return new PublishNotificationResult(scopeType, targetOrgId, recipientCount);
    }

    @Transactional(rollbackFor = Exception.class)
    public void ensureWelcomeNotification(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            return;
        }
        String content = welcomeContent(user.getRole());
        publish("WELCOME", "USER", String.valueOf(userId), CATEGORY_SYSTEM, "INFO",
                "欢迎使用学分银行", content, null, null, "/dashboard", null,
                "WELCOME:" + userId + ":v1", Collections.singletonList(userId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void ensureWelcomeNotificationsForAllUsers() {
        List<SysUser> users = sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>().orderByAsc(SysUser::getId));
        for (SysUser user : users) {
            ensureWelcomeNotification(user.getId());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void sendToUser(String eventCode, String category, String level,
                           String title, String content, String actionPath,
                           String sourceType, Long sourceId, String dedupeKey,
                           Long userId, Long actorId) {
        if (userId == null || sysUserMapper.selectById(userId) == null) {
            return;
        }
        publish(eventCode, "USER", String.valueOf(userId), category, level,
                title, content, sourceType, sourceId, actionPath, actorId,
                dedupeKey, Collections.singletonList(userId));
    }

    @Transactional(rollbackFor = Exception.class)
    public void sendToRole(String eventCode, String category, String level,
                           String title, String content, String actionPath,
                           String sourceType, Long sourceId, String dedupeKey,
                           String role, Long actorId) {
        List<Long> recipients = activeUsers(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRole, role), actorId);
        publish(eventCode, "ROLE", role, category, level, title, content,
                sourceType, sourceId, actionPath, actorId, dedupeKey, recipients);
    }

    @Transactional(rollbackFor = Exception.class)
    public void sendToOrgStudents(String eventCode, String category, String level,
                                  String title, String content, String actionPath,
                                  String sourceType, Long sourceId, String dedupeKey,
                                  Long orgId, Long actorId) {
        if (orgId == null) {
            return;
        }
        List<Long> recipients = activeUsers(new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getOrgId, orgId)
                .eq(SysUser::getRole, "student"), actorId);
        publish(eventCode, "ORG", String.valueOf(orgId), category, level,
                title, content, sourceType, sourceId, actionPath, actorId,
                dedupeKey, recipients);
    }

    @Transactional(rollbackFor = Exception.class)
    public void sendToAll(String eventCode, String category, String level,
                          String title, String content, String actionPath,
                          String sourceType, Long sourceId, String dedupeKey,
                          Long actorId) {
        List<Long> recipients = activeUsers(new LambdaQueryWrapper<SysUser>(), actorId);
        publish(eventCode, "ALL", null, category, level, title, content,
                sourceType, sourceId, actionPath, actorId, dedupeKey, recipients);
    }

    private List<Long> activeUsers(LambdaQueryWrapper<SysUser> wrapper, Long actorId) {
        wrapper.eq(SysUser::getStatus, 1).orderByAsc(SysUser::getId);
        return sysUserMapper.selectList(wrapper).stream()
                .map(SysUser::getId)
                .filter(id -> !Objects.equals(id, actorId))
                .collect(Collectors.toList());
    }

    private int publish(String eventCode, String scopeType, String scopeValue,
                         String category, String level, String title, String content,
                         String sourceType, Long sourceId, String actionPath, Long actorId,
                         String dedupeKey, List<Long> recipientIds) {
        if (recipientIds == null || recipientIds.isEmpty()) {
            return 0;
        }
        if (dedupeKey != null && notificationMapper.selectCount(
                new LambdaQueryWrapper<SystemNotification>()
                        .eq(SystemNotification::getDedupeKey, dedupeKey)) > 0) {
            return 0;
        }

        LocalDateTime now = LocalDateTime.now();
        SystemNotification notification = new SystemNotification();
        notification.setEventCode(eventCode);
        notification.setScopeType(scopeType);
        notification.setScopeValue(scopeValue);
        notification.setCategory(category);
        notification.setLevel(level);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setSourceType(sourceType);
        notification.setSourceId(sourceId);
        notification.setActionPath(actionPath);
        notification.setActorId(actorId);
        notification.setDedupeKey(dedupeKey);
        notification.setStatus("PUBLISHED");
        notification.setCreatedAt(now);
        notificationMapper.insert(notification);

        for (Long recipientId : recipientIds.stream().distinct().collect(Collectors.toList())) {
            NotificationRecipient recipient = new NotificationRecipient();
            recipient.setNotificationId(notification.getId());
            recipient.setUserId(recipientId);
            recipient.setCreatedAt(now);
            recipientMapper.insert(recipient);
        }
        return (int) recipientIds.stream().distinct().count();
    }

    private String welcomeContent(String role) {
        if ("admin".equals(role)) {
            return "你可以管理用户、机构、积分规则和审批任务；右上角铃铛用于查看全平台业务通知。";
        }
        if ("org_admin".equals(role)) {
            return "你可以管理本机构用户、项目、积分规则和兑换商品，并在工作台查看机构积分池。";
        }
        if ("student".equals(role)) {
            return "你可以报名项目、参加活动获取积分，在积分商城兑换商品，并申请学生证书认证。";
        }
        if ("expert".equals(role)) {
            return "你可以在业务审核中处理评审任务，并在“我的资质”查看认证信息。";
        }
        return "从工作台开始使用系统，右上角铃铛用于查看与你相关的业务通知。";
    }

    private void requireUserId(Long userId) {
        if (userId == null) {
            throw new BizException("请先登录");
        }
    }

    private String normalizeReadStatus(String value) {
        if ("UNREAD".equalsIgnoreCase(value)) return "UNREAD";
        if ("READ".equalsIgnoreCase(value)) return "READ";
        return "ALL";
    }

    private String normalizeCategory(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim().toUpperCase();
    }

    private String trimRequired(String value, String fieldName, int maxLength) {
        String normalized = value == null ? "" : value.trim();
        if (normalized.isEmpty()) {
            throw new BizException(fieldName + "不能为空");
        }
        if (normalized.length() > maxLength) {
            throw new BizException(fieldName + "不能超过 " + maxLength + " 个字符");
        }
        return normalized;
    }
}
