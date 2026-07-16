package com.creditbank.mvp.service;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("CampaignService 单元测试")
class CampaignServiceTest {

    @Mock
    private CampaignMapper campaignMapper;

    @Mock
    private CampaignEnrollmentMapper enrollmentMapper;

    @Mock
    private UserOpLogMapper userOpLogMapper;

    @Mock
    private SysUserMapper sysUserMapper;

    private CampaignService campaignService;

    @BeforeEach
    void setUp(TestInfo testInfo) {
        MockitoAnnotations.openMocks(this);
        campaignService = new CampaignService(campaignMapper, enrollmentMapper, userOpLogMapper, sysUserMapper);
        System.out.println("========== 开始执行: " + testInfo.getDisplayName() + " ==========");
    }

    private Campaign mockCampaign(Long id, String title, BigDecimal multiplier, Integer status,
                                   LocalDateTime start, LocalDateTime end) {
        Campaign c = new Campaign();
        c.setId(id);
        c.setTitle(title);
        c.setMultiplier(multiplier);
        c.setStatus(status);
        c.setStartTime(start);
        c.setEndTime(end);
        return c;
    }

    @Test
    @DisplayName("page - 分页查询活动列表")
    void testPage() {
        Campaign c = mockCampaign(1L, "Double Points", BigDecimal.valueOf(2.0), 1,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

        @SuppressWarnings("unchecked")
        Page<Campaign> page = mock(Page.class);
        when(page.getRecords()).thenReturn(Collections.singletonList(c));
        when(campaignMapper.selectPage(any(), any())).thenReturn(page);

        Page<Campaign> result = campaignService.page(1, 10, 1);

        assertNotNull(result);
        assertEquals(1, result.getRecords().size());
        assertEquals("Double Points", result.getRecords().get(0).getTitle());
        System.out.println("✓ 测试通过: 分页查询活动列表 - 数量=" + result.getRecords().size());
    }

    @Test
    @DisplayName("listActive - 学生端查看进行中活动")
    void testListActive() {
        Campaign c = mockCampaign(1L, "Double Points", BigDecimal.valueOf(2.0), 1,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

        when(campaignMapper.selectList(any())).thenReturn(Collections.singletonList(c));

        List<Campaign> result = campaignService.listActive();

        assertNotNull(result);
        assertEquals(1, result.size());
        System.out.println("✓ 测试通过: 学生端查看进行中活动 - 数量=" + result.size());
    }

    @Test
    @DisplayName("getById - 活动不存在")
    void testGetByIdNotFound() {
        when(campaignMapper.selectById(1L)).thenReturn(null);

        BizException exception = assertThrows(BizException.class, () -> campaignService.getById(1L));
        assertEquals("活动不存在：1", exception.getMessage());
        System.out.println("✓ 测试通过: 活动不存在时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("save - 管理员创建活动成功")
    void testSaveSuccess() {
        SysUser operator = new SysUser();
        operator.setId(1L);
        operator.setRealName("Admin");

        Campaign campaign = new Campaign();
        campaign.setTitle("Double Points");
        campaign.setMultiplier(BigDecimal.valueOf(2.0));
        campaign.setStartTime(LocalDateTime.now().minusDays(1));
        campaign.setEndTime(LocalDateTime.now().plusDays(1));

        when(campaignMapper.insert(any())).thenAnswer(inv -> {
            Campaign arg = inv.getArgument(0);
            arg.setId(1L);
            return 1;
        });
        when(userOpLogMapper.insert(any())).thenReturn(1);

        Campaign result = campaignService.save(campaign, operator);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertNotNull(result.getStatus());
        verify(userOpLogMapper).insert(any());
        System.out.println("✓ 测试通过: 创建活动成功 - ID=" + result.getId() + ", 状态=" + result.getStatus());
    }

    @Test
    @DisplayName("save - 结束时间必须晚于开始时间")
    void testSaveInvalidTime() {
        SysUser operator = new SysUser();
        operator.setId(1L);

        Campaign campaign = new Campaign();
        campaign.setTitle("Double Points");
        campaign.setStartTime(LocalDateTime.now());
        campaign.setEndTime(LocalDateTime.now());

        BizException exception = assertThrows(BizException.class, () ->
                campaignService.save(campaign, operator));
        assertEquals("结束时间必须晚于开始时间", exception.getMessage());
        System.out.println("✓ 测试通过: 活动结束时间不合法时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("update - 编辑活动成功")
    void testUpdateSuccess() {
        SysUser operator = new SysUser();
        operator.setId(1L);
        operator.setRealName("Admin");

        Campaign existing = mockCampaign(1L, "Old Title", BigDecimal.ONE, 1,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

        Campaign update = mockCampaign(1L, "New Title", BigDecimal.valueOf(2.0), 1,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(2));

        // update() 先 getById() 取 existing，再 updateById，最后再次 getById() 返回
        when(campaignMapper.selectById(1L)).thenReturn(existing, update);
        when(campaignMapper.updateById(any())).thenReturn(1);
        when(userOpLogMapper.insert(any())).thenReturn(1);

        Campaign result = campaignService.update(update, operator);

        assertNotNull(result);
        assertEquals("New Title", result.getTitle());
        verify(userOpLogMapper).insert(any());
        System.out.println("✓ 测试通过: 编辑活动成功 - 新标题=" + result.getTitle());
    }

    @Test
    @DisplayName("delete - 删除活动成功")
    void testDeleteSuccess() {
        SysUser operator = new SysUser();
        operator.setId(1L);
        operator.setRealName("Admin");

        Campaign c = mockCampaign(1L, "Double Points", BigDecimal.valueOf(2.0), 2,
                LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(1));

        when(campaignMapper.selectById(1L)).thenReturn(c);
        when(campaignMapper.deleteById(1L)).thenReturn(1);
        when(userOpLogMapper.insert(any())).thenReturn(1);

        campaignService.delete(1L, operator);

        verify(campaignMapper).deleteById(1L);
        verify(userOpLogMapper).insert(any());
        System.out.println("✓ 测试通过: 删除活动成功 - 标题=" + c.getTitle());
    }

    @Test
    @DisplayName("enroll - 学生报名活动成功")
    void testEnrollSuccess() {
        Campaign c = mockCampaign(1L, "Double Points", BigDecimal.valueOf(2.0), 1,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

        SysUser user = new SysUser();
        user.setId(100L);
        user.setRealName("Student");

        when(enrollmentMapper.selectOne(any())).thenReturn(null);
        when(campaignMapper.selectById(1L)).thenReturn(c);
        when(sysUserMapper.selectById(100L)).thenReturn(user);
        when(enrollmentMapper.insert(any())).thenReturn(1);
        when(userOpLogMapper.insert(any())).thenReturn(1);

        campaignService.enroll(1L, 100L);

        verify(enrollmentMapper).insert(any());
        verify(userOpLogMapper).insert(any());
        System.out.println("✓ 测试通过: 学生报名活动成功 - 活动=" + c.getTitle());
    }

    @Test
    @DisplayName("enroll - 重复报名抛出异常")
    void testEnrollDuplicate() {
        CampaignEnrollment exist = new CampaignEnrollment();
        exist.setId(1L);
        exist.setCampaignId(1L);
        exist.setUserId(100L);

        when(enrollmentMapper.selectOne(any())).thenReturn(exist);

        BizException exception = assertThrows(BizException.class, () ->
                campaignService.enroll(1L, 100L));
        assertEquals("已报名该活动", exception.getMessage());
        System.out.println("✓ 测试通过: 重复报名活动时正确抛出异常 - " + exception.getMessage());
    }

    @Test
    @DisplayName("enroll - 积分翻倍活动互斥")
    void testEnrollMultiplierMutex() {
        Campaign current = mockCampaign(2L, "New Double", BigDecimal.valueOf(2.0), 1,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

        Campaign enrolled = mockCampaign(1L, "Old Double", BigDecimal.valueOf(2.0), 1,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

        CampaignEnrollment exist = new CampaignEnrollment();
        exist.setCampaignId(1L);
        exist.setUserId(100L);

        when(enrollmentMapper.selectOne(any())).thenReturn(null);
        when(campaignMapper.selectById(2L)).thenReturn(current);
        when(enrollmentMapper.selectList(any())).thenReturn(Collections.singletonList(exist));
        when(campaignMapper.selectById(1L)).thenReturn(enrolled);

        BizException exception = assertThrows(BizException.class, () ->
                campaignService.enroll(2L, 100L));
        assertTrue(exception.getMessage().contains("已参加积分活动"));
        System.out.println("✓ 测试通过: 积分翻倍活动互斥 - " + exception.getMessage());
    }

    @Test
    @DisplayName("leave - 退出活动成功")
    void testLeaveSuccess() {
        Campaign c = mockCampaign(1L, "Double Points", BigDecimal.valueOf(2.0), 1,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

        CampaignEnrollment exist = new CampaignEnrollment();
        exist.setId(1L);
        exist.setCampaignId(1L);
        exist.setUserId(100L);

        SysUser user = new SysUser();
        user.setId(100L);
        user.setRealName("Student");

        when(enrollmentMapper.selectOne(any())).thenReturn(exist);
        when(campaignMapper.selectById(1L)).thenReturn(c);
        when(sysUserMapper.selectById(100L)).thenReturn(user);
        when(enrollmentMapper.deleteById(1L)).thenReturn(1);
        when(userOpLogMapper.insert(any())).thenReturn(1);

        campaignService.leave(1L, 100L);

        verify(enrollmentMapper).deleteById(1L);
        System.out.println("✓ 测试通过: 退出活动成功 - 活动=" + c.getTitle());
    }

    @Test
    @DisplayName("isEnrolled - 检查用户是否已报名")
    void testIsEnrolled() {
        when(enrollmentMapper.selectCount(any())).thenReturn(1L);

        boolean result = campaignService.isEnrolled(1L, 100L);

        assertTrue(result);
        System.out.println("✓ 测试通过: 用户已报名活动 - isEnrolled=true");
    }

    @Test
    @DisplayName("getActiveMultiplierCampaign - 获取当前进行中的翻倍活动")
    void testGetActiveMultiplierCampaign() {
        Campaign c = mockCampaign(1L, "Double Points", BigDecimal.valueOf(2.0), 1,
                LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));

        when(campaignMapper.selectOne(any())).thenReturn(c);

        Campaign result = campaignService.getActiveMultiplierCampaign();

        assertNotNull(result);
        assertEquals(BigDecimal.valueOf(2.0), result.getMultiplier());
        System.out.println("✓ 测试通过: 获取当前翻倍活动成功 - 倍率=" + result.getMultiplier());
    }

}