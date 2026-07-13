package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.entity.UserOpLog;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.mapper.UserOpLogMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户管理业务。负责用户的编辑、冻结/解冻、重置密码、
 * 批量操作，以及操作日志的记录和查询。
 */
@Service
public class UserService {

    private final SysUserMapper sysUserMapper;
    private final UserOpLogMapper userOpLogMapper;
    private final PasswordEncoder passwordEncoder;

    public UserService(SysUserMapper sysUserMapper,
                       UserOpLogMapper userOpLogMapper,
                       PasswordEncoder passwordEncoder) {
        this.sysUserMapper = sysUserMapper;
        this.userOpLogMapper = userOpLogMapper;
        this.passwordEncoder = passwordEncoder;
    }

    // ==================== 查询 ====================

    public List<SysUser> listUsers() {
        return sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>()
                        .orderByDesc(SysUser::getId));
    }

    public SysUser getUser(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在：" + id);
        }
        return user;
    }

    // ==================== 编辑用户 ====================

    @Transactional(rollbackFor = Exception.class)
    public SysUser updateUser(Long id, SysUser update, SysUser operator) {
        SysUser user = getUser(id);
        if (update.getRealName() != null) user.setRealName(update.getRealName());
        if (update.getPhone() != null) user.setPhone(update.getPhone());
        if (update.getEmail() != null) user.setEmail(update.getEmail());
        if (update.getRole() != null) user.setRole(update.getRole());
        if (update.getOrgId() != null) user.setOrgId(update.getOrgId());
        sysUserMapper.updateById(user);

        writeLog(operator, user, "UPDATE", "编辑用户信息");
        return user;
    }

    // ==================== 状态操作 ====================

    @Transactional(rollbackFor = Exception.class)
    public SysUser updateStatus(Long id, Integer status, SysUser operator) {
        SysUser user = getUser(id);
        checkAdminProtection(operator, user, status == 0 ? "冻结" : "解冻");
        user.setStatus(status);
        sysUserMapper.updateById(user);

        String action = status == 1 ? "UNFREEZE" : "FREEZE";
        String detail = status == 1 ? "解冻账户" : "冻结账户";
        writeLog(operator, user, action, detail);
        return user;
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchUpdateStatus(List<Long> ids, Integer status, SysUser operator) {
        int skipped = 0;
        for (Long id : ids) {
            SysUser user = sysUserMapper.selectById(id);
            if (user == null) continue;
            // 批量操作中跳过受保护的管理员
            if ("admin".equals(user.getRole())) {
                skipped++;
                continue;
            }
            if (user.getId().equals(operator.getId())) {
                skipped++;
                continue;
            }
            user.setStatus(status);
            sysUserMapper.updateById(user);
        }
        String action = status == 1 ? "BATCH_UNFREEZE" : "BATCH_FREEZE";
        String detail = (status == 1 ? "批量解冻 " : "批量冻结 ") + ids.size() + " 个用户";
        if (skipped > 0) detail += "（跳过 " + skipped + " 个管理员）";
        UserOpLog log = buildLog(operator, null, action, detail);
        log.setDetail(detail);
        userOpLogMapper.insert(log);
    }

    // ==================== 重置密码 ====================

    @Transactional(rollbackFor = Exception.class)
    public void resetPassword(Long id, String newPassword, SysUser operator) {
        SysUser user = getUser(id);
        checkAdminProtection(operator, user, "重置密码");
        user.setPassword(passwordEncoder.encode(newPassword));
        sysUserMapper.updateById(user);
        writeLog(operator, user, "RESET_PW", "重置密码");
    }

    // ==================== 保护规则 ====================

    /**
     * 管理员保护：不能操作系统管理员（包括自己）。
     */
    private void checkAdminProtection(SysUser operator, SysUser target, String actionName) {
        if (target.getId().equals(operator.getId())) {
            throw new BizException("不能" + actionName + "自己的账户");
        }
        if ("admin".equals(target.getRole())) {
            throw new BizException("不能" + actionName + "系统管理员账户");
        }
    }

    // ==================== 操作日志 ====================

    public Page<UserOpLog> getOpLogs(int page, int size) {
        return userOpLogMapper.selectPage(
                new Page<>(page, size),
                new LambdaQueryWrapper<UserOpLog>()
                        .orderByDesc(UserOpLog::getCreatedAt));
    }

    // ==================== 内部工具 ====================

    private void writeLog(SysUser operator, SysUser target, String action, String detail) {
        UserOpLog log = buildLog(operator, target, action, detail);
        userOpLogMapper.insert(log);
    }

    private UserOpLog buildLog(SysUser operator, SysUser target, String action, String detail) {
        UserOpLog log = new UserOpLog();
        log.setOperatorId(operator.getId());
        log.setOperatorName(operator.getRealName());
        if (target != null) {
            log.setTargetUserId(target.getId());
            log.setTargetUserName(target.getRealName());
        }
        log.setAction(action);
        log.setDetail(detail);
        log.setCreatedAt(LocalDateTime.now());
        return log;
    }
}
