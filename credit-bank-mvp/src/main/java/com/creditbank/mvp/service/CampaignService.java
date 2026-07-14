package com.creditbank.mvp.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.creditbank.mvp.common.BizException;
import com.creditbank.mvp.entity.Campaign;
import com.creditbank.mvp.entity.CampaignEnrollment;
import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.entity.UserOpLog;
import com.creditbank.mvp.mapper.CampaignEnrollmentMapper;
import com.creditbank.mvp.mapper.CampaignMapper;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.mapper.UserOpLogMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 平台活动核心业务。
 * - 管理端：分页列表、详情、新增/编辑/删除
 * - 学生端：浏览进行中的活动
 * - 定时任务：根据起止时间自动刷新活动状态
 * - 积分活动互斥：同一时间只允许一个 multiplier>1.0 的活动处于进行中
 */
@Service
public class CampaignService {

    private final CampaignMapper campaignMapper;
    private final UserOpLogMapper userOpLogMapper;
    private final SysUserMapper sysUserMapper;

    // 构造函数在下面「报名管理」区

    /**
     * 分页查询活动列表，支持按状态筛选
     */
    public Page<Campaign> page(int pageNum, int pageSize, Integer status) {
        LambdaQueryWrapper<Campaign> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(Campaign::getStatus, status);
        }
        wrapper.orderByDesc(Campaign::getCreatedAt);
        return campaignMapper.selectPage(new Page<>(pageNum, pageSize), wrapper);
    }

    /**
     * 学生端：只查进行中的活动
     */
    public List<Campaign> listActive() {
        return campaignMapper.selectList(
                new LambdaQueryWrapper<Campaign>()
                        .eq(Campaign::getStatus, 1)
                        .orderByDesc(Campaign::getCreatedAt));
    }

    /**
     * 活动详情
     */
    public Campaign getById(Long id) {
        Campaign campaign = campaignMapper.selectById(id);
        if (campaign == null) {
            throw new BizException("活动不存在：" + id);
        }
        return campaign;
    }

    /**
     * 新增活动。
     * 校验规则：
     * 1. 结束时间必须晚于开始时间
     * 2. 若 multiplier > 1.0，检查是否已有进行中的积分活动
     */
    @Transactional(rollbackFor = Exception.class)
    public Campaign save(Campaign campaign, SysUser operator) {
        validateTime(campaign);
        if (campaign.getMultiplier() == null) {
            campaign.setMultiplier(BigDecimal.ONE);
        }
        // 根据起止时间设置初始状态
        campaign.setStatus(calcStatus(campaign.getStartTime(), campaign.getEndTime()));
        campaignMapper.insert(campaign);

        redisService.delete(CACHE_KEY_ACTIVE_MULTIPLIER);

        userOpLogMapper.insert(UserOpLog.createLog(
                operator.getId(), operator.getRealName(), null, null,
                UserOpLog.MODULE_CAMPAIGN, UserOpLog.ACTION_CREATE,
                "创建活动：" + campaign.getTitle()));
        return campaign;
    }

    /**
     * 编辑活动
     */
    @Transactional(rollbackFor = Exception.class)
    public Campaign update(Campaign campaign, SysUser operator) {
        Campaign existing = getById(campaign.getId());
        validateTime(campaign);
        if (campaign.getMultiplier() == null) {
            campaign.setMultiplier(BigDecimal.ONE);
        }
        // 重新计算状态
        campaign.setStatus(calcStatus(campaign.getStartTime(), campaign.getEndTime()));
        campaignMapper.updateById(campaign);

        redisService.delete(CACHE_KEY_ACTIVE_MULTIPLIER);

        userOpLogMapper.insert(UserOpLog.createLog(
                operator.getId(), operator.getRealName(), null, null,
                UserOpLog.MODULE_CAMPAIGN, UserOpLog.ACTION_UPDATE,
                "编辑活动：" + existing.getTitle()));
        return getById(campaign.getId());
    }

    /**
     * 删除活动
     */
    @Transactional(rollbackFor = Exception.class)
    public void delete(Long id, SysUser operator) {
        Campaign campaign = getById(id);
        campaignMapper.deleteById(id);

        redisService.delete(CACHE_KEY_ACTIVE_MULTIPLIER);

        userOpLogMapper.insert(UserOpLog.createLog(
                operator.getId(), operator.getRealName(), null, null,
                UserOpLog.MODULE_CAMPAIGN, UserOpLog.ACTION_DELETE,
                "删除活动：" + campaign.getTitle()));
    }

    // ==================== 定时任务 ====================

    /**
     * 每5分钟根据当前时间自动刷新所有活动状态
     */
    @Scheduled(cron = "0 */5 * * * ?")
    public void refreshStatus() {
        LocalDateTime now = LocalDateTime.now();

        // 未开始 → 进行中
        campaignMapper.update(null,
                new LambdaUpdateWrapper<Campaign>()
                        .set(Campaign::getStatus, 1)
                        .eq(Campaign::getStatus, 0)
                        .le(Campaign::getStartTime, now));

        // 进行中 → 已结束
        campaignMapper.update(null,
                new LambdaUpdateWrapper<Campaign>()
                        .set(Campaign::getStatus, 2)
                        .eq(Campaign::getStatus, 1)
                        .lt(Campaign::getEndTime, now));
    }

    // ==================== 积分联动 ====================

    /** Cache key for active multiplier campaign */
    private static final String CACHE_KEY_ACTIVE_MULTIPLIER = "campaign:active:multiplier";

    /**
     * 获取当前进行中的积分翻倍活动。
     * 只返回第一个匹配的（同一时间只允许一个积分活动）。
     * 结果缓存 5 分钟，活动状态变更时自动失效。
     */
    /**
     * 获取当前进行中的任一积分翻倍活动（用于前端展示等不需要用户维度的场景）。
     */
    public Campaign getActiveMultiplierCampaign() {
        LocalDateTime now = LocalDateTime.now();
        return campaignMapper.selectOne(
                new LambdaQueryWrapper<Campaign>()
                        .eq(Campaign::getStatus, 1)
                        .gt(Campaign::getMultiplier, BigDecimal.ONE)
                        .le(Campaign::getStartTime, now)
                        .ge(Campaign::getEndTime, now)
                        .last("limit 1"));
    }

    /**
     * 获取指定用户已报名的进行中积分翻倍活动。
     * 同一时间学生只能报名一个积分活动，所以最多返回一个。
     */
    public Campaign getEnrolledMultiplierCampaign(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        List<Campaign> activeMultiplierCampaigns = campaignMapper.selectList(
                new LambdaQueryWrapper<Campaign>()
                        .eq(Campaign::getStatus, 1)
                        .gt(Campaign::getMultiplier, BigDecimal.ONE)
                        .le(Campaign::getStartTime, now)
                        .ge(Campaign::getEndTime, now));
        for (Campaign c : activeMultiplierCampaigns) {
            if (isEnrolled(c.getId(), userId)) {
                return c;
            }
        }
        return null;
    }

    // ==================== 报名管理 ====================

    private final CampaignEnrollmentMapper enrollmentMapper;
    private final RedisService redisService;

    public CampaignService(CampaignMapper campaignMapper,
                           CampaignEnrollmentMapper enrollmentMapper,
                           UserOpLogMapper userOpLogMapper,
                           SysUserMapper sysUserMapper,
                           RedisService redisService) {
        this.campaignMapper = campaignMapper;
        this.enrollmentMapper = enrollmentMapper;
        this.userOpLogMapper = userOpLogMapper;
        this.sysUserMapper = sysUserMapper;
        this.redisService = redisService;
    }

    /** 参加活动 */
    public void enroll(Long campaignId, Long userId) {
        CampaignEnrollment exist = enrollmentMapper.selectOne(
                new LambdaQueryWrapper<CampaignEnrollment>()
                        .eq(CampaignEnrollment::getCampaignId, campaignId)
                        .eq(CampaignEnrollment::getUserId, userId));
        if (exist != null) {
            throw new BizException("已报名该活动");
        }
        Campaign campaign = getById(campaignId);

        // 积分活动互斥：学生同一时间只能参加一个积分翻倍活动
        if (campaign.getMultiplier().compareTo(BigDecimal.ONE) > 0) {
            Campaign alreadyEnrolled = getEnrolledMultiplierCampaign(userId);
            if (alreadyEnrolled != null && !alreadyEnrolled.getId().equals(campaignId)) {
                throw new BizException("您已参加积分活动「" + alreadyEnrolled.getTitle()
                        + "」，请先退出再参加新的积分活动");
            }
        }
        SysUser user = sysUserMapper.selectById(userId);
        CampaignEnrollment e = new CampaignEnrollment();
        e.setCampaignId(campaignId);
        e.setUserId(userId);
        e.setEnrolledAt(LocalDateTime.now());
        enrollmentMapper.insert(e);
        redisService.delete("campaign:enrolled:" + campaignId + ":" + userId);

        userOpLogMapper.insert(UserOpLog.createLog(
                userId, user != null ? user.getRealName() : String.valueOf(userId), null, null,
                UserOpLog.MODULE_ENROLL, UserOpLog.ACTION_CAMPAIGN_ENROLL,
                "报名活动：" + campaign.getTitle()));
    }

    /** 退出活动 */
    public void leave(Long campaignId, Long userId) {
        CampaignEnrollment exist = enrollmentMapper.selectOne(
                new LambdaQueryWrapper<CampaignEnrollment>()
                        .eq(CampaignEnrollment::getCampaignId, campaignId)
                        .eq(CampaignEnrollment::getUserId, userId));
        if (exist == null) {
            throw new BizException("未报名该活动");
        }
        Campaign campaign = getById(campaignId);
        SysUser user = sysUserMapper.selectById(userId);
        enrollmentMapper.deleteById(exist.getId());
        redisService.delete("campaign:enrolled:" + campaignId + ":" + userId);

        userOpLogMapper.insert(UserOpLog.createLog(
                userId, user != null ? user.getRealName() : String.valueOf(userId), null, null,
                UserOpLog.MODULE_ENROLL, UserOpLog.ACTION_CAMPAIGN_LEAVE,
                "取消报名活动：" + campaign.getTitle()));
    }

    /** 检查用户是否报名了指定活动 */
    public boolean isEnrolled(Long campaignId, Long userId) {
        String key = "campaign:enrolled:" + campaignId + ":" + userId;
        Object cached = redisService.get(key);
        if (cached instanceof Boolean) {
            return (Boolean) cached;
        }
        boolean enrolled = enrollmentMapper.selectCount(
                new LambdaQueryWrapper<CampaignEnrollment>()
                        .eq(CampaignEnrollment::getCampaignId, campaignId)
                        .eq(CampaignEnrollment::getUserId, userId)) > 0;
        redisService.set(key, enrolled, 10, TimeUnit.MINUTES);
        return enrolled;
    }

    // ==================== 内部方法 ====================

    private void validateTime(Campaign c) {
        if (c.getStartTime() == null || c.getEndTime() == null) {
            throw new BizException("开始时间和结束时间不能为空");
        }
        if (!c.getEndTime().isAfter(c.getStartTime())) {
            throw new BizException("结束时间必须晚于开始时间");
        }
    }

    /**
     * 根据起止时间和当前时间计算活动状态
     */
    private int calcStatus(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(start)) return 0;  // 未开始
        if (now.isAfter(end)) return 2;     // 已结束
        return 1;                            // 进行中
    }
}
