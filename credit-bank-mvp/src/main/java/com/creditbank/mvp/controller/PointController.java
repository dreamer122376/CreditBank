package com.creditbank.mvp.controller;

import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.entity.TransactionLog;
import com.creditbank.mvp.service.PointService;
import com.creditbank.mvp.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@Tag(name = "认证与积分", description = "用户登录、注册、积分获取和流水查询接口")
public class PointController {

    private final PointService pointService;
    private final JwtUtil jwtUtil;

    public PointController(PointService pointService, JwtUtil jwtUtil) {
        this.pointService = pointService;
        this.jwtUtil = jwtUtil;
    }

    @PostMapping("/user/login")
    @Operation(summary = "用户登录", description = "使用用户名和密码登录系统，返回JWT令牌和用户信息")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest req) {
        SysUser user = pointService.login(req.getUsername(), req.getPassword());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return Result.ok(result);
    }

    @PostMapping("/user/register")
    @Operation(summary = "用户注册", description = "注册新用户，返回JWT令牌和用户信息")
    public Result<Map<String, Object>> register(@RequestBody RegisterRequest req) {
        SysUser user = pointService.register(req.getUsername(), req.getPassword(), req.getRealName(),
                req.getRole(), req.getOrgId(), req.getExpertField());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return Result.ok(result);
    }

    @PostMapping("/points/earn")
    @Operation(summary = "获取积分", description = "根据事件代码获取积分，支持活动倍率加成")
    public Result<SysUser> earn(@RequestBody EarnRequest req) {
        return Result.ok(pointService.earn(req.getUserId(), req.getEventCode()));
    }

    @GetMapping("/user/{id}/transactions")
    @Operation(summary = "查询交易流水", description = "查询指定用户的积分交易流水列表")
    public Result<List<TransactionLog>> transactions(@Parameter(description = "用户ID") @PathVariable Long id) {
        return Result.ok(pointService.listTransactions(id));
    }

    public static class LoginRequest {
        private String username;
        private String password;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }

    public static class EarnRequest {
        private Long userId;
        private String eventCode;

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        public String getEventCode() { return eventCode; }
        public void setEventCode(String eventCode) { this.eventCode = eventCode; }
    }

    public static class RegisterRequest {
        private String username;
        private String password;
        private String realName;
        private String role;
        private Long orgId;
        private String expertField;

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getRealName() {
            return realName;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public Long getOrgId() {
            return orgId;
        }

        public void setOrgId(Long orgId) {
            this.orgId = orgId;
        }

        public String getExpertField() {
            return expertField;
        }

        public void setExpertField(String expertField) {
            this.expertField = expertField;
        }
    }
}
