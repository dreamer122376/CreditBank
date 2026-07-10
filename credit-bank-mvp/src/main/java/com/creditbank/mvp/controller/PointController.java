package com.creditbank.mvp.controller;

import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.entity.CreditRule;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.entity.TransactionLog;
import com.creditbank.mvp.service.PointService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class PointController {

    private final PointService pointService;

    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    @PostMapping("/user/login")
    public Result<SysUser> login(@RequestBody LoginRequest req) {
        return Result.ok(pointService.login(req.getUsername(), req.getPassword()));
    }

    @GetMapping("/user/list")
    public Result<List<SysUser>> users() {
        return Result.ok(pointService.listUsers());
    }

    @GetMapping("/user/{id}")
    public Result<SysUser> user(@PathVariable Long id) {
        return Result.ok(pointService.getUser(id));
    }

    @GetMapping("/user/{id}/transactions")
    public Result<List<TransactionLog>> transactions(@PathVariable Long id) {
        return Result.ok(pointService.listTransactions(id));
    }

    @GetMapping("/credit-rule/list")
    public Result<List<CreditRule>> rules() {
        return Result.ok(pointService.listRules());
    }

    @PostMapping("/points/earn")
    public Result<SysUser> earn(@RequestBody EarnRequest req) {
        return Result.ok(pointService.earn(req.getUserId(), req.getEventCode()));
    }

    @PostMapping("/user/register")
    public Result<SysUser> register(@RequestBody RegisterRequest req) {
        return Result.ok(pointService.register(req.getUsername(), req.getPassword(), req.getRealName(),
                req.getRole(), req.getOrgId(), req.getExpertField()));
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

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getEventCode() {
            return eventCode;
        }

        public void setEventCode(String eventCode) {
            this.eventCode = eventCode;
        }
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
