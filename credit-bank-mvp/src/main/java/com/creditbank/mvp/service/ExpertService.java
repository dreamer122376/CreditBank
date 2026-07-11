package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.mapper.SysUserMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ExpertService {

    private static final String ROLE_EXPERT = "expert";

    private final SysUserMapper sysUserMapper;

    public ExpertService(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    public List<SysUser> list() {
        return sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getRole, ROLE_EXPERT)
                        .orderByDesc(SysUser::getId));
    }

    public SysUser create(SysUser user) {
        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new BizException("登录账号不能为空");
        }
        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new BizException("密码不能为空");
        }
        Long count = sysUserMapper.selectCount(
                new LambdaQueryWrapper<SysUser>().eq(SysUser::getUsername, user.getUsername()));
        if (count > 0) {
            throw new BizException("登录账号已存在：" + user.getUsername());
        }
        user.setId(null);
        user.setRole(ROLE_EXPERT);
        if (user.getBalance() == null) {
            user.setBalance(0);
        }
        if (user.getStatus() == null) {
            user.setStatus(1);
        }
        sysUserMapper.insert(user);
        return sysUserMapper.selectById(user.getId());
    }

    public SysUser update(SysUser user) {
        SysUser exist = getExpert(user.getId());
        // 管理接口只维护资料字段，不允许在这里改角色 / 余额 / 密码
        exist.setRealName(user.getRealName());
        exist.setPhone(user.getPhone());
        exist.setEmail(user.getEmail());
        exist.setExpertField(user.getExpertField());
        sysUserMapper.updateById(exist);
        return exist;
    }

    /** 冻结 / 解冻：status 1正常，0冻结 */
    public SysUser changeStatus(Long id, Integer status) {
        SysUser exist = getExpert(id);
        if (status == null || (status != 0 && status != 1)) {
            throw new BizException("非法的账号状态：" + status);
        }
        exist.setStatus(status);
        sysUserMapper.updateById(exist);
        return exist;
    }

    private SysUser getExpert(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null || !ROLE_EXPERT.equals(user.getRole())) {
            throw new BizException("专家不存在：" + id);
        }
        return user;
    }
}
