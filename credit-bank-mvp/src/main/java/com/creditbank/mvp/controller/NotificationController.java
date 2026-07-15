package com.creditbank.mvp.controller;

import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.dto.NotificationPageDTO;
import com.creditbank.mvp.dto.PublishNotificationRequest;
import com.creditbank.mvp.dto.PublishNotificationResult;
import com.creditbank.mvp.service.NotificationService;
import com.creditbank.mvp.util.CurrentUserUtil;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public Result<NotificationPageDTO> list(@RequestParam(defaultValue = "1") int page,
                                            @RequestParam(defaultValue = "20") int size,
                                            @RequestParam(defaultValue = "ALL") String readStatus,
                                            @RequestParam(required = false) String category) {
        return Result.ok(notificationService.listForUser(
                CurrentUserUtil.getCurrentUserId(), page, size, readStatus, category));
    }

    @GetMapping("/unread-count")
    public Result<Map<String, Long>> unreadCount() {
        return Result.ok(Collections.singletonMap(
                "unreadCount", notificationService.unreadCount(CurrentUserUtil.getCurrentUserId())));
    }

    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(CurrentUserUtil.getCurrentUserId(), id);
        return Result.ok();
    }

    @PutMapping("/read-all")
    public Result<Map<String, Integer>> markAllRead() {
        int count = notificationService.markAllRead(CurrentUserUtil.getCurrentUserId());
        return Result.ok(Collections.singletonMap("updated", count));
    }

    @PostMapping("/publish")
    public Result<PublishNotificationResult> publish(@RequestBody PublishNotificationRequest request) {
        return Result.ok(notificationService.publishManualNotice(
                CurrentUserUtil.getCurrentUserId(), request));
    }
}
