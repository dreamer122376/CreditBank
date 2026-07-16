package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.dto.OrganizationAuditResult;
import com.creditbank.mvp.entity.Organization;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.entity.UserOpLog;
import com.creditbank.mvp.mapper.OrganizationMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.mapper.UserOpLogMapper;
import com.creditbank.mvp.util.CurrentUserUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrganizationService {

    private static final String DEFAULT_ADMIN_PASSWORD = "123456";

    private final OrganizationMapper organizationMapper;
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;
    private final UserOpLogMapper userOpLogMapper;

    public OrganizationService(OrganizationMapper organizationMapper,
                               SysUserMapper sysUserMapper,
                               PasswordEncoder passwordEncoder,
                               NotificationService notificationService,
                               UserOpLogMapper userOpLogMapper) {
        this.organizationMapper = organizationMapper;
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
        this.userOpLogMapper = userOpLogMapper;
    }

    public List<Organization> list() {
        List<Organization> orgs = organizationMapper.selectList(
                new QueryWrapper<Organization>().orderByDesc("id"));
        for (Organization org : orgs) {
            SysUser admin = sysUserMapper.selectOne(
                    new QueryWrapper<SysUser>()
                            .eq("org_id", org.getId())
                            .eq("role", "org_admin")
                            .last("LIMIT 1"));
            org.setCreditPool(admin != null && admin.getBalance() != null ? admin.getBalance() : 0);
        }
        return orgs;
    }

    @Transactional(rollbackFor = Exception.class)
    public Organization create(Organization org) {
        if (org.getName() == null || org.getName().trim().isEmpty()) {
            throw new BizException("机构名称不能为空");
        }
        org.setId(null);
        org.setStatus(Organization.STATUS_PENDING);
        organizationMapper.insert(org);
        Organization saved = organizationMapper.selectById(org.getId());
        SysUser operator = currentOperator();
        // 操作日志：创建机构
        if (operator != null) {
            userOpLogMapper.insert(UserOpLog.createLog(
                    operator.getId(), operator.getRealName(),
                    operator.getId(), operator.getRealName(),
                    UserOpLog.MODULE_ORGANIZATION, UserOpLog.ACTION_CREATE,
                    "创建机构：" + buildOrgSummary(saved)));
        }
        return saved;
    }

    @Transactional(rollbackFor = Exception.class)
    public Organization update(Organization org) {
        Organization exist = organizationMapper.selectById(org.getId());
        if (exist == null) {
            throw new BizException("机构不存在：" + org.getId());
        }
        organizationMapper.updateById(org);
        Organization saved = organizationMapper.selectById(org.getId());
        SysUser operator = currentOperator();
        // 操作日志：更新机构
        if (operator != null) {
            userOpLogMapper.insert(UserOpLog.createLog(
                    operator.getId(), operator.getRealName(),
                    operator.getId(), operator.getRealName(),
                    UserOpLog.MODULE_ORGANIZATION, UserOpLog.ACTION_UPDATE,
                    "更新机构：" + buildOrgSummary(saved)));
        }
        return saved;
    }

    /** 入驻审核 / 启停：只允许流转到 1启用 或 2禁用 */
    @Transactional(rollbackFor = Exception.class)
    public OrganizationAuditResult changeStatus(Long id, Integer status) {
        Organization exist = organizationMapper.selectById(id);
        if (exist == null) {
            throw new BizException("机构不存在：" + id);
        }
        if (status == null || (status != Organization.STATUS_ENABLED && status != Organization.STATUS_DISABLED)) {
            throw new BizException("非法的机构状态：" + status);
        }

        boolean isAuditPass = exist.getStatus() == Organization.STATUS_PENDING && status == Organization.STATUS_ENABLED;
        boolean isDisable = status == Organization.STATUS_DISABLED;
        boolean isReEnable = exist.getStatus() == Organization.STATUS_DISABLED && status == Organization.STATUS_ENABLED;
        int prevStatus = exist.getStatus() == null ? -1 : exist.getStatus();
        exist.setStatus(status);
        organizationMapper.updateById(exist);

        if (isDisable) {
            sysUserMapper.update(null,
                    new UpdateWrapper<SysUser>()
                            .eq("org_id", id)
                            .set("status", 0)
                            .set("frozen_at", LocalDateTime.now()));
        } else if (isReEnable) {
            sysUserMapper.update(null,
                    new UpdateWrapper<SysUser>()
                            .eq("org_id", id)
                            .set("status", 1)
                            .set("frozen_at", null));
        }

        // 操作日志：机构状态变更（审核通过/停用/重新启用）
        SysUser operator = currentOperator();
        if (operator != null) {
            String action;
            String stageLabel;
            if (isAuditPass) {
                action = UserOpLog.ACTION_APPROVE;
                stageLabel = "审核通过机构入驻：";
            } else if (isDisable) {
                action = UserOpLog.ACTION_REVOKE;
                stageLabel = "停用机构（冻结全部用户）：";
            } else if (isReEnable) {
                action = UserOpLog.ACTION_UPDATE;
                stageLabel = "重新启用机构（解冻全部用户）：";
            } else {
                action = UserOpLog.ACTION_UPDATE;
                stageLabel = "机构状态变更(" + statusLabel(prevStatus) + "→" + statusLabel(status) + ")：";
            }
            // targetUserId = 该机构的 org_admin（若存在）
            SysUser orgAdmin = sysUserMapper.selectOne(
                    new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getOrgId, id)
                            .eq(SysUser::getRole, "org_admin")
                            .last("LIMIT 1"));
            userOpLogMapper.insert(UserOpLog.createLog(
                    operator.getId(), operator.getRealName(),
                    orgAdmin == null ? null : orgAdmin.getId(),
                    orgAdmin == null ? null : orgAdmin.getRealName(),
                    UserOpLog.MODULE_ORGANIZATION, action,
                    stageLabel + buildOrgSummary(exist)));
        }

        if (isAuditPass) {
            SysUser existingAdmin = sysUserMapper.selectOne(
                    new QueryWrapper<SysUser>()
                            .eq("org_id", id)
                            .eq("role", "org_admin")
                            .last("LIMIT 1"));
            if (existingAdmin != null) {
                notificationService.ensureWelcomeNotification(existingAdmin.getId());
                notifyOrganizationApproved(exist, existingAdmin);
                return OrganizationAuditResult.of(exist, false, existingAdmin.getUsername(), null,
                        "该机构已存在管理员账号，未重复创建");
            }

            String username = generateAdminUsername(exist.getName());
            SysUser admin = new SysUser();
            admin.setUsername(username);
            admin.setPassword(passwordEncoder.encode(DEFAULT_ADMIN_PASSWORD));
            admin.setRealName(exist.getContactPerson());
            admin.setPhone(exist.getContactPhone());
            admin.setRole("org_admin");
            admin.setOrgId(id);
            admin.setBalance(0);
            admin.setStatus(1);
            admin.setCreatedAt(LocalDateTime.now());
            sysUserMapper.insert(admin);
            notificationService.ensureWelcomeNotification(admin.getId());
            notifyOrganizationApproved(exist, admin);

            return OrganizationAuditResult.of(exist, true, username, DEFAULT_ADMIN_PASSWORD,
                    "机构管理员账号已生成，请妥善保管账号密码");
        }

        return OrganizationAuditResult.of(exist, false, null, null, "状态更新成功");
    }

    /** 拒绝机构入驻申请 */
    @Transactional(rollbackFor = Exception.class)
    public void reject(Long id, String reason) {
        Organization exist = organizationMapper.selectById(id);
        if (exist == null) {
            throw new BizException("机构不存在：" + id);
        }
        if (exist.getStatus() != Organization.STATUS_PENDING) {
            throw new BizException("只有待审核状态的机构才能拒绝");
        }
        if (reason == null || reason.trim().isEmpty()) {
            throw new BizException("拒绝原因不能为空");
        }
        exist.setStatus(Organization.STATUS_REJECTED);
        exist.setRejectReason(reason);
        organizationMapper.updateById(exist);
        SysUser operator = currentOperator();
        // 操作日志：驳回机构入驻申请
        if (operator != null) {
            userOpLogMapper.insert(UserOpLog.createLog(
                    operator.getId(), operator.getRealName(),
                    null, exist.getContactPerson(),
                    UserOpLog.MODULE_ORGANIZATION, UserOpLog.ACTION_REJECT,
                    "驳回机构入驻申请：" + buildOrgSummary(exist) + " · 原因：" + reason.trim()));
        }
    }

    private String generateAdminUsername(String orgName) {
        String base = orgName == null ? "org" : orgName.trim();
        String pinyin = base.replaceAll("[^a-zA-Z0-9\\u4e00-\\u9fa5]", "");
        if (pinyin.isEmpty()) {
            pinyin = "org";
        }
        String candidate = pinyin + "_admin";
        if (candidate.length() > 40) {
            candidate = candidate.substring(0, 40);
        }
        if (isUsernameAvailable(candidate)) {
            return candidate;
        }
        for (int i = 2; i <= 99; i++) {
            String suffix = "_" + i;
            String tryName = pinyin + "_admin";
            if (tryName.length() + suffix.length() > 50) {
                tryName = tryName.substring(0, 50 - suffix.length());
            }
            tryName = tryName + suffix;
            if (isUsernameAvailable(tryName)) {
                return tryName;
            }
        }
        throw new BizException("无法生成唯一的机构管理员账号，请检查机构名称");
    }

    private void notifyOrganizationApproved(Organization org, SysUser admin) {
        Long actorId = CurrentUserUtil.getCurrentUserId();
        notificationService.sendToUser(
                "ORG_REGISTER_APPROVED", NotificationService.CATEGORY_APPLICATION, "SUCCESS",
                "机构入驻已通过",
                "“" + org.getName() + "”已成功入驻，机构管理员账号已开通。",
                "/dashboard", "ORGANIZATION", org.getId(),
                "ORG_REGISTER_APPROVED:ORG:" + org.getId(), admin.getId(), actorId);
        notificationService.sendToAll(
                "ORG_JOINED", NotificationService.CATEGORY_SYSTEM, "INFO",
                "新机构正式入驻",
                "“" + org.getName() + "”已正式入驻学分银行。",
                null, "ORGANIZATION", org.getId(),
                "ORG_JOINED:" + org.getId(), actorId);
    }

    private boolean isUsernameAvailable(String username) {
        return sysUserMapper.selectCount(
                new QueryWrapper<SysUser>().eq("username", username)) == 0;
    }

    // --- 内部辅助方法 ---

    private SysUser currentOperator() {
        Long userId = CurrentUserUtil.getCurrentUserId();
        return userId == null ? null : sysUserMapper.selectById(userId);
    }

    // 操作日志详情
    private String buildOrgSummary(Organization org) {
        if (org == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("#").append(org.getId());
        if (org.getName() != null) {
            sb.append(" · 机构：").append(org.getName());
        }
        if (org.getContactPerson() != null && !org.getContactPerson().trim().isEmpty()) {
            sb.append(" · 联系人：").append(org.getContactPerson().trim());
        }
        if (org.getContactPhone() != null && !org.getContactPhone().trim().isEmpty()) {
            sb.append(" · 电话：").append(org.getContactPhone().trim());
        }
        if (org.getStatus() != null) {
            sb.append(" · ").append(statusLabel(org.getStatus()));
        }
        return sb.toString();
    }

    private String statusLabel(Integer status) {
        if (status == null) return "未知";
        if (status == Organization.STATUS_PENDING) return "待审核";
        if (status == Organization.STATUS_ENABLED) return "已启用";
        if (status == Organization.STATUS_DISABLED) return "已停用";
        if (status == Organization.STATUS_REJECTED) return "已驳回";
        return "状态" + status;
    }
}
