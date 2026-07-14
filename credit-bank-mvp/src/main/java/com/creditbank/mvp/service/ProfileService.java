package com.creditbank.mvp.service;

import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.Organization;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.mapper.OrganizationMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 个人资料自助管理：只允许改自己的免审字段（姓名/手机/邮箱）和密码。
 * 角色、余额、专家领域等敏感字段一律不在这里改——
 * 专家领域走 EXPERT_CERT 认证审批，其余归用户管理模块。
 */
@Service
public class ProfileService {

    private final SysUserMapper sysUserMapper;
    private final OrganizationMapper organizationMapper;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(SysUserMapper sysUserMapper, OrganizationMapper organizationMapper, PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.organizationMapper = organizationMapper;
        this.passwordEncoder = passwordEncoder;
    }

    public SysUser getProfile(Long userId) {
        SysUser user = sysUserMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在：" + userId);
        }
        if (user.getOrgId() != null) {
            Organization org = organizationMapper.selectById(user.getOrgId());
            if (org != null) {
                user.setOrgName(org.getName());
            }
        }
        return user;
    }

    /** 白名单更新：只接收姓名/手机/邮箱 */
    public SysUser updateProfile(Long userId, String realName, String phone, String email) {
        SysUser user = getProfile(userId);
        if (realName != null && !realName.trim().isEmpty()) {
            user.setRealName(realName.trim());
        }
        user.setPhone(phone);
        user.setEmail(email);
        sysUserMapper.updateById(user);
        return user;
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        SysUser user = getProfile(userId);
        if (oldPassword == null || !passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new BizException("原密码不正确");
        }
        if (newPassword == null || newPassword.length() < 6) {
            throw new BizException("新密码至少 6 位");
        }
        sysUserMapper.update(
                null,
                new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<SysUser>()
                        .eq("id", userId)
                        .set("password", passwordEncoder.encode(newPassword))
        );
    }
}
