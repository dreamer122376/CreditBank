package com.creditbank.mvp.controller;

import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.service.SignInService;
import com.creditbank.mvp.util.CurrentUserUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "签到打卡", description = "用户签到打卡相关接口")
public class SignInController {

    private final SignInService signInService;

    public SignInController(SignInService signInService) {
        this.signInService = signInService;
    }

    @PostMapping("/sign-in")
    @Operation(summary = "签到打卡", description = "用户进行签到打卡，获得积分奖励")
    public Result<Map<String, Object>> signIn() {
        Long userId = CurrentUserUtil.getCurrentUserId();
        if (userId == null) {
            throw new IllegalArgumentException("用户未登录");
        }
        return Result.ok(signInService.signIn(userId));
    }

    @GetMapping("/sign-in/status")
    @Operation(summary = "获取签到状态", description = "获取当前用户的签到状态、连续签到天数等信息")
    public Result<Map<String, Object>> getSignInStatus() {
        Long userId = CurrentUserUtil.getCurrentUserId();
        if (userId == null) {
            throw new IllegalArgumentException("用户未登录");
        }
        return Result.ok(signInService.getSignInStatus(userId));
    }

    @GetMapping("/sign-in/history")
    @Operation(summary = "获取签到历史", description = "获取当前用户最近的签到记录")
    public Result<Map<String, Object>> getSignInHistory(@RequestParam(defaultValue = "30") int limit) {
        Long userId = CurrentUserUtil.getCurrentUserId();
        if (userId == null) {
            throw new IllegalArgumentException("用户未登录");
        }
        return Result.ok(Map.of(
                "records", signInService.getSignInHistory(userId, limit)
        ));
    }
}