package com.creditbank.mvp.controller;

import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.service.ExpertService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expert")
public class ExpertController {

    private final ExpertService expertService;

    public ExpertController(ExpertService expertService) {
        this.expertService = expertService;
    }

    @GetMapping("/list")
    public Result<List<SysUser>> list() {
        return Result.ok(expertService.list());
    }

    @PostMapping("/create")
    public Result<SysUser> create(@RequestBody SysUser user) {
        return Result.ok(expertService.create(user));
    }

    @PostMapping("/update")
    public Result<SysUser> update(@RequestBody SysUser user) {
        return Result.ok(expertService.update(user));
    }

    @PostMapping("/{id}/status")
    public Result<SysUser> changeStatus(@PathVariable Long id, @RequestBody StatusRequest req) {
        return Result.ok(expertService.changeStatus(id, req.getStatus()));
    }

    public static class StatusRequest {
        private Integer status;

        public Integer getStatus() {
            return status;
        }

        public void setStatus(Integer status) {
            this.status = status;
        }
    }
}
