package com.creditbank.mvp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.entity.Campaign;
import com.creditbank.mvp.service.CampaignService;
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

    public CampaignController(CampaignService campaignService) {
        this.campaignService = campaignService;
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
        return Result.ok(campaignService.save(campaign));
    }

    /** 编辑活动 */
    @PutMapping("/{id}")
    public Result<Campaign> update(@PathVariable Long id, @RequestBody Campaign campaign) {
        campaign.setId(id);
        return Result.ok(campaignService.update(campaign));
    }

    /** 删除活动 */
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        campaignService.delete(id);
        return Result.ok();
    }
}
