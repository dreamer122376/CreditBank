package com.creditbank.mvp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.entity.Campaign;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.service.CampaignService;
import com.creditbank.mvp.service.UserService;
import com.creditbank.mvp.util.CurrentUserUtil;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 平台活动管理 REST 接口。
 * - /api/campaigns：管理端 CRUD
 * - /api/campaigns/active：学生端查询进行中的活动
 */
@RestController
@RequestMapping("/api/campaigns")
public class CampaignController {

    private final CampaignService campaignService;
    private final UserService userService;

    public CampaignController(CampaignService campaignService,
                              UserService userService) {
        this.campaignService = campaignService;
        this.userService = userService;
    }

    /** 分页列表（管理端），支持 ?page=1&size=10&status=1 筛选 */
    @GetMapping
    public Result<Page<Campaign>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer status) {
        return Result.ok(campaignService.page(page, size, status));
    }

    /** 学生端：查询所有进行中的活动 */
    @GetMapping("/active")
    public Result<List<Campaign>> listActive() {
        return Result.ok(campaignService.listActive());
    }

    /** 活动详情 */
    @GetMapping("/{id}")
    public Result<Campaign> detail(@PathVariable Long id) {
        return Result.ok(campaignService.getById(id));
    }

    /** 新增活动 */
    @PostMapping
    public Result<Campaign> create(@RequestBody Campaign campaign) {
        SysUser operator = userService.getUser(CurrentUserUtil.getCurrentUserId());
        return Result.ok(campaignService.save(campaign, operator));
    }

    /** 编辑活动 */
    @PutMapping("/{id}")
    public Result<Campaign> update(@PathVariable Long id, @RequestBody Campaign campaign) {
        SysUser operator = userService.getUser(CurrentUserUtil.getCurrentUserId());
        campaign.setId(id);
        return Result.ok(campaignService.update(campaign, operator));
    }

    /** 删除活动 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        SysUser operator = userService.getUser(CurrentUserUtil.getCurrentUserId());
        campaignService.delete(id, operator);
        return Result.ok();
    }

    // ==================== 报名 ====================

    /** 参加活动 */
    @PostMapping("/{id}/enroll")
    public Result<Void> enroll(@PathVariable Long id) {
        Long userId = CurrentUserUtil.getCurrentUserId();
        campaignService.enroll(id, userId);
        return Result.ok();
    }

    /** 退出活动 */
    @DeleteMapping("/{id}/enroll")
    public Result<Void> leave(@PathVariable Long id) {
        Long userId = CurrentUserUtil.getCurrentUserId();
        campaignService.leave(id, userId);
        return Result.ok();
    }

    /** 查询当前用户是否已报名 */
    @GetMapping("/{id}/enrolled")
    public Result<Boolean> isEnrolled(@PathVariable Long id) {
        Long userId = CurrentUserUtil.getCurrentUserId();
        return Result.ok(campaignService.isEnrolled(id, userId));
    }
}
