package com.creditbank.mvp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.entity.UserOpLog;
import com.creditbank.mvp.service.UserService;
import com.creditbank.mvp.util.CurrentUserUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 用户管理 REST 接口。
 * 包括用户列表、详情、编辑、冻结/解冻、重置密码、操作日志。
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ==================== 用户 CRUD ====================

    /** 用户列表 */
    @GetMapping
    public Result<List<SysUser>> list() {
        return Result.ok(userService.listUsers());
    }

    /** 用户详情 */
    @GetMapping("/{id}")
    public Result<SysUser> get(@PathVariable Long id) {
        return Result.ok(userService.getUser(id));
    }

    /** 编辑用户信息 */
    @PutMapping("/{id}")
    public Result<SysUser> update(@PathVariable Long id,
                                   @RequestBody SysUser update) {
        Long operatorId = CurrentUserUtil.getCurrentUserId();
        SysUser operator = userService.getUser(operatorId);
        return Result.ok(userService.updateUser(id, update, operator));
    }

    // ==================== 状态操作 ====================

    /** 冻结/解冻单个用户 */
    @PutMapping("/{id}/status")
    public Result<SysUser> updateStatus(@PathVariable Long id,
                                         @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
            throw new BizException("status 必须为 0（冻结）或 1（解冻）");
        }
        Long operatorId = CurrentUserUtil.getCurrentUserId();
        SysUser operator = userService.getUser(operatorId);
        return Result.ok(userService.updateStatus(id, status, operator));
    }

    /** 批量冻结/解冻 */
    @PutMapping("/batch-status")
    public Result<?> batchUpdateStatus(@RequestBody Map<String, Object> body) {
        @SuppressWarnings("unchecked")
        List<Long> ids = ((List<Number>) body.get("ids")).stream()
                .map(Number::longValue).collect(java.util.stream.Collectors.toList());
        Integer status = (Integer) body.get("status");
        if (ids == null || ids.isEmpty()) {
            throw new BizException("ids 不能为空");
        }
        if (status == null || (status != 0 && status != 1)) {
            throw new BizException("status 必须为 0（冻结）或 1（解冻）");
        }
        Long operatorId = CurrentUserUtil.getCurrentUserId();
        SysUser operator = userService.getUser(operatorId);
        userService.batchUpdateStatus(ids, status, operator);
        return Result.ok();
    }

    // ==================== 重置密码 ====================

    /** 管理员重置用户密码 */
    @PutMapping("/{id}/reset-pw")
    public Result<?> resetPassword(@PathVariable Long id,
                                    @RequestBody Map<String, String> body) {
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new BizException("新密码不能为空");
        }
        Long operatorId = CurrentUserUtil.getCurrentUserId();
        SysUser operator = userService.getUser(operatorId);
        userService.resetPassword(id, newPassword, operator);
        return Result.ok();
    }

    // ==================== 操作日志 ====================

    /** 操作日志列表（分页） */
    @GetMapping("/op-logs")
    public Result<Page<UserOpLog>> opLogs(@RequestParam(defaultValue = "1") int page,
                                           @RequestParam(defaultValue = "10") int size) {
        return Result.ok(userService.getOpLogs(page, size));
    }
}
