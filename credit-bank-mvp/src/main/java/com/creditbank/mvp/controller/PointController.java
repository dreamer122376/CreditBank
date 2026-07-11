package com.creditbank.mvp.controller;

import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.entity.TransactionLog;
import com.creditbank.mvp.service.PointService;
import com.creditbank.mvp.util.JwtUtil;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class PointController {

    private final PointService pointService;
    private final JwtUtil jwtUtil;

    public PointController(PointService pointService, JwtUtil jwtUtil) {
        this.pointService = pointService;
        this.jwtUtil = jwtUtil;
    }

    // ==================== 认证 ====================

    @PostMapping("/user/login")
    public Result<Map<String, Object>> login(@RequestBody LoginRequest req) {
        SysUser user = pointService.login(req.getUsername(), req.getPassword());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return Result.ok(result);
    }

    @PostMapping("/user/register")
    public Result<Map<String, Object>> register(@RequestBody RegisterRequest req) {
        SysUser user = pointService.register(req.getUsername(), req.getPassword(), req.getRealName(),
                req.getRole(), req.getOrgId(), req.getExpertField());
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        Map<String, Object> result = new HashMap<>();
        result.put("token", token);
        result.put("user", user);
        return Result.ok(result);
    }

    // ==================== 积分 ====================

    @PostMapping("/points/earn")
    public Result<SysUser> earn(@RequestBody EarnRequest req) {
        return Result.ok(pointService.earn(req.getUserId(), req.getEventCode()));
    }

    // ==================== 流水 ====================

    @GetMapping("/user/{id}/transactions")
    public Result<List<TransactionLog>> transactions(@PathVariable Long id) {
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
