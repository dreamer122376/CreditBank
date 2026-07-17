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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    /**
     * 机构管理员：只返回本机构学生。
     */
    public List<SysUser> listByOrg(Long orgId) {
        return sysUserMapper.selectList(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getOrgId, orgId)
                        .eq(SysUser::getRole, "student")
                        .orderByDesc(SysUser::getId));
    }

    /**
     * 获取审批流程可选的审核人员。
     * 管理员：返回所有管理员/机构管理员/专家（状态正常）。
     * 机构管理员：返回自己与本机构的专家（状态正常）。
     */
    public List<SysUser> listAuditorCandidates(Long orgId, Long selfId, String selfRole) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getStatus, 1)
                .in(SysUser::getRole, List.of("admin", "org_admin", "expert"))
                .orderByDesc(SysUser::getId);
        if ("org_admin".equals(selfRole)) {
            wrapper.and(w -> w.eq(SysUser::getId, selfId)
                    .or(w2 -> w2.eq(SysUser::getRole, "expert").eq(SysUser::getOrgId, orgId))
                    .or(w3 -> w3.eq(SysUser::getRole, "admin")));
        }
        return sysUserMapper.selectList(wrapper);
    }

    public SysUser getUser(Long id) {
        SysUser user = sysUserMapper.selectById(id);
        if (user == null) {
            throw new BizException("用户不存在：" + id);
        }
        return user;
    }

    public SysUser testLogin(String username) {
        SysUser user = sysUserMapper.selectOne(
                new LambdaQueryWrapper<SysUser>()
                        .eq(SysUser::getUsername, username));
        if (user == null) {
            throw new BizException("用户不存在");
        }
        user.setLastLoginAt(java.time.LocalDateTime.now());
        sysUserMapper.updateById(user);
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
        if (status == 0) {
            user.setFrozenAt(LocalDateTime.now());
            user.setFrozenBy(operator.getId());
        } else {
            user.setFrozenAt(null);
            user.setFrozenBy(null);
        }
        sysUserMapper.updateById(user);

        String action = status == 1 ? "UNFREEZE" : "FREEZE";
        String detail = status == 1 ? "解冻账户" : "冻结账户";
        writeLog(operator, user, action, detail);
        return user;
    }

    @Transactional(rollbackFor = Exception.class)
    public Map<String, Object> batchUpdateStatus(List<Long> ids, Integer status, SysUser operator) {
        int skipped = 0;
        int processed = 0;
        for (Long id : ids) {
            SysUser user = sysUserMapper.selectById(id);
            if (user == null) {
                skipped++;
                continue;
            }
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
            if (status == 0) {
                user.setFrozenAt(LocalDateTime.now());
                user.setFrozenBy(operator.getId());
            } else {
                user.setFrozenAt(null);
                user.setFrozenBy(null);
            }
            sysUserMapper.updateById(user);
            processed++;
        }
        String action = status == 1 ? "BATCH_UNFREEZE" : "BATCH_FREEZE";
        String detail = (status == 1 ? "批量解冻 " : "批量冻结 ") + processed + " 个用户";
        if (skipped > 0) detail += "（跳过 " + skipped + " 个）";
        UserOpLog log = buildLog(operator, null, action, detail);
        log.setDetail(detail);
        log.setModule(UserOpLog.MODULE_USER);
        userOpLogMapper.insert(log);

        Map<String, Object> result = new HashMap<>();
        result.put("total", ids.size());
        result.put("processed", processed);
        result.put("skipped", skipped);
        result.put("action", action);
        return result;
    }

    // ==================== 重置密码 ====================

    @Transactional(rollbackFor = Exception.class)
    /** 重置用户密码（禁止操作系统管理员和自己） */
    public void resetPassword(Long id, String newPassword, SysUser operator) {
        SysUser user = getUser(id);
        checkAdminProtection(operator, user, "重置密码");
        sysUserMapper.update(
                null,
                new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<SysUser>()
                        .eq("id", id)
                        .set("password", passwordEncoder.encode(newPassword))
        );
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

    public Page<UserOpLog> getOpLogs(int page, int size,
                                      String action, String module, String keyword,
                                      LocalDateTime startTime, LocalDateTime endTime,
                                      Long operatorId, Long orgId) {
        LambdaQueryWrapper<UserOpLog> wrapper = new LambdaQueryWrapper<>();
        if (action != null && !action.isEmpty()) {
            wrapper.eq(UserOpLog::getAction, action);
        }
        if (module != null && !module.isEmpty()) {
            wrapper.eq(UserOpLog::getModule, module);
        }
        if (keyword != null && !keyword.trim().isEmpty()) {
            String kw = keyword.trim();
            wrapper.and(w -> w.like(UserOpLog::getOperatorName, kw)
                    .or()
                    .like(UserOpLog::getTargetUserName, kw)
                    .or()
                    .like(UserOpLog::getDetail, kw));
        }
        if (startTime != null) {
            wrapper.ge(UserOpLog::getCreatedAt, startTime);
        }
        if (endTime != null) {
            wrapper.le(UserOpLog::getCreatedAt, endTime);
        }
        // 机构管理员：只能看到自己的操作 + 本机构学生的操作日志
        if (orgId != null) {
            List<Long> studentIds = sysUserMapper.selectList(
                    new LambdaQueryWrapper<SysUser>()
                            .eq(SysUser::getOrgId, orgId)
                            .eq(SysUser::getRole, "student"))
                    .stream()
                    .map(SysUser::getId)
                    .collect(Collectors.toList());
            if (studentIds.isEmpty()) {
                // 没有学生时，只能看自己的操作
                wrapper.eq(UserOpLog::getOperatorId, operatorId);
            } else {
                wrapper.and(w -> w.eq(UserOpLog::getOperatorId, operatorId)
                        .or()
                        .in(UserOpLog::getTargetUserId, studentIds));
            }
        }
        wrapper.orderByDesc(UserOpLog::getCreatedAt);
        return userOpLogMapper.selectPage(new Page<>(page, size), wrapper);
    }

    /**
     * 保存一条操作日志（供其他 Service 复用）。
     */
    public void saveLog(UserOpLog log) {
        if (log.getCreatedAt() == null) {
            log.setCreatedAt(LocalDateTime.now());
        }
        userOpLogMapper.insert(log);
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
        log.setModule(UserOpLog.MODULE_USER);
        log.setDetail(detail);
        log.setCreatedAt(LocalDateTime.now());
        return log;
    }
}
