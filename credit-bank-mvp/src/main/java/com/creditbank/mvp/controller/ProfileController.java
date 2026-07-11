package com.creditbank.mvp.controller;

import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.service.ProfileService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping("/{userId}")
    public Result<SysUser> get(@PathVariable Long userId) {
        return Result.ok(profileService.getProfile(userId));
    }

    @PostMapping("/update")
    public Result<SysUser> update(@RequestBody UpdateRequest req) {
        return Result.ok(profileService.updateProfile(
                req.getUserId(), req.getRealName(), req.getPhone(), req.getEmail()));
    }

    @PostMapping("/password")
    public Result<Void> changePassword(@RequestBody PasswordRequest req) {
        profileService.changePassword(req.getUserId(), req.getOldPassword(), req.getNewPassword());
        return Result.ok();
    }

    public static class UpdateRequest {
        private Long userId;
        private String realName;
        private String phone;
        private String email;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getRealName() {
            return realName;
        }

        public void setRealName(String realName) {
            this.realName = realName;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    public static class PasswordRequest {
        private Long userId;
        private String oldPassword;
        private String newPassword;

        public Long getUserId() {
            return userId;
        }

        public void setUserId(Long userId) {
            this.userId = userId;
        }

        public String getOldPassword() {
            return oldPassword;
        }

        public void setOldPassword(String oldPassword) {
            this.oldPassword = oldPassword;
        }

        public String getNewPassword() {
            return newPassword;
        }

        public void setNewPassword(String newPassword) {
            this.newPassword = newPassword;
        }
    }
}
