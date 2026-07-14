package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.dto.OrganizationAuditResult;
import com.creditbank.mvp.entity.Organization;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.mapper.OrganizationMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrganizationService {

    /** 机构状态：0待审核，1启用，2禁用 */
    public static final int STATUS_PENDING = 0;
    public static final int STATUS_ENABLED = 1;
    public static final int STATUS_DISABLED = 2;

    private static final String DEFAULT_ADMIN_PASSWORD = "123456";

    private final OrganizationMapper organizationMapper;
    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    public OrganizationService(OrganizationMapper organizationMapper,
                               SysUserMapper sysUserMapper,
                               PasswordEncoder passwordEncoder) {
        this.organizationMapper = organizationMapper;
        this.sysUserMapper = sysUserMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Organization> list() {
        List<Organization> orgs = organizationMapper.selectList(
                new LambdaQueryWrapper<Organization>().orderByDesc(Organization::getId));
        for (Organization org : orgs) {
            SysUser admin = sysUserMapper.selectOne(
                    new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getOrgId, org.getId())
                            .eq(SysUser::getRole, "org_admin")
                            .last("LIMIT 1"));
            org.setCreditPool(admin != null && admin.getBalance() != null ? admin.getBalance() : 0);
        }
        return orgs;
    }

    public Organization create(Organization org) {
        if (org.getName() == null || org.getName().trim().isEmpty()) {
            throw new BizException("机构名称不能为空");
        }
        org.setId(null);
        org.setStatus(STATUS_PENDING);
        organizationMapper.insert(org);
        return organizationMapper.selectById(org.getId());
    }

    public Organization update(Organization org) {
        Organization exist = organizationMapper.selectById(org.getId());
        if (exist == null) {
            throw new BizException("机构不存在：" + org.getId());
        }
        organizationMapper.updateById(org);
        return organizationMapper.selectById(org.getId());
    }

    /** 入驻审核 / 启停：只允许流转到 1启用 或 2禁用 */
    @Transactional(rollbackFor = Exception.class)
    public OrganizationAuditResult changeStatus(Long id, Integer status) {
        Organization exist = organizationMapper.selectById(id);
        if (exist == null) {
            throw new BizException("机构不存在：" + id);
        }
        if (status == null || (status != STATUS_ENABLED && status != STATUS_DISABLED)) {
            throw new BizException("非法的机构状态：" + status);
        }

        boolean isAuditPass = exist.getStatus() == STATUS_PENDING && status == STATUS_ENABLED;
        boolean isDisable = status == STATUS_DISABLED;
        boolean isReEnable = exist.getStatus() == STATUS_DISABLED && status == STATUS_ENABLED;
        exist.setStatus(status);
        organizationMapper.updateById(exist);

        if (isDisable) {
            sysUserMapper.update(null,
                    new LambdaUpdateWrapper<SysUser>()
                            .eq(SysUser::getOrgId, id)
                            .set(SysUser::getStatus, 0)
                            .set(SysUser::getFrozenAt, LocalDateTime.now()));
        } else if (isReEnable) {
            sysUserMapper.update(null,
                    new LambdaUpdateWrapper<SysUser>()
                            .eq(SysUser::getOrgId, id)
                            .set(SysUser::getStatus, 1)
                            .set(SysUser::getFrozenAt, null));
        }

        if (isAuditPass) {
            SysUser existingAdmin = sysUserMapper.selectOne(
                    new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getOrgId, id)
                            .eq(SysUser::getRole, "org_admin")
                            .last("LIMIT 1"));
            if (existingAdmin != null) {
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

            return OrganizationAuditResult.of(exist, true, username, DEFAULT_ADMIN_PASSWORD,
                    "机构管理员账号已生成，请妥善保管账号密码");
        }

        return OrganizationAuditResult.of(exist, false, null, null, "状态更新成功");
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

    private boolean isUsernameAvailable(String username) {
        return sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, username)) == 0;
    }
}
