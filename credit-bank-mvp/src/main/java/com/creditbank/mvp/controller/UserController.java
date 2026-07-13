package com.creditbank.mvp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.entity.UserOpLog;
import com.creditbank.mvp.service.UserService;
import com.creditbank.mvp.util.CurrentUserUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户列表、详情、编辑、冻结/解冻、重置密码、操作日志")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "用户列表", description = "获取所有用户列表，按ID倒序排列")
    public Result<List<SysUser>> list() {
        return Result.ok(userService.listUsers());
    }

    @GetMapping("/{id}")
    @Operation(summary = "用户详情", description = "根据用户ID获取用户详细信息")
    public Result<SysUser> get(@Parameter(description = "用户ID") @PathVariable Long id) {
        return Result.ok(userService.getUser(id));
    }

    @PutMapping("/{id}")
    @Operation(summary = "编辑用户信息", description = "编辑指定用户的基本信息（不包括密码）")
    public Result<SysUser> update(@Parameter(description = "用户ID") @PathVariable Long id,
                                   @Parameter(description = "用户更新信息") @RequestBody SysUser update) {
        Long operatorId = CurrentUserUtil.getCurrentUserId();
        SysUser operator = userService.getUser(operatorId);
        return Result.ok(userService.updateUser(id, update, operator));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "冻结/解冻用户", description = "冻结或解冻指定用户账户")
    public Result<SysUser> updateStatus(@Parameter(description = "用户ID") @PathVariable Long id,
                                         @Parameter(description = "{\"status\": 0或1}") @RequestBody Map<String, Integer> body) {
        Integer status = body.get("status");
        if (status == null || (status != 0 && status != 1)) {
            throw new BizException("status 必须为 0（冻结）或 1（解冻）");
        }
        Long operatorId = CurrentUserUtil.getCurrentUserId();
        SysUser operator = userService.getUser(operatorId);
        return Result.ok(userService.updateStatus(id, status, operator));
    }

    @PutMapping("/batch-status")
    @Operation(summary = "批量冻结/解冻", description = "批量冻结或解冻多个用户账户")
    public Result<?> batchUpdateStatus(@Parameter(description = "{\"ids\": [1,2,3], \"status\": 0或1}") @RequestBody Map<String, Object> body) {
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

    @PutMapping("/{id}/reset-pw")
    @Operation(summary = "重置密码", description = "管理员重置指定用户的密码")
    public Result<?> resetPassword(@Parameter(description = "用户ID") @PathVariable Long id,
                                    @Parameter(description = "{\"newPassword\": \"新密码\"}") @RequestBody Map<String, String> body) {
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new BizException("新密码不能为空");
        }
        Long operatorId = CurrentUserUtil.getCurrentUserId();
        SysUser operator = userService.getUser(operatorId);
        userService.resetPassword(id, newPassword, operator);
        return Result.ok();
    }

    @GetMapping("/op-logs")
    @Operation(summary = "操作日志", description = "分页查询用户操作日志")
    public Result<Page<UserOpLog>> opLogs(@Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
                                           @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int size) {
        return Result.ok(userService.getOpLogs(page, size));
    }
}
