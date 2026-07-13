package com.creditbank.mvp.controller;

import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.dto.PointOverviewDTO;
import com.creditbank.mvp.dto.StatsSummaryDTO;
import com.creditbank.mvp.dto.TodoItemDTO;
import com.creditbank.mvp.entity.TransactionLog;
import com.creditbank.mvp.service.StatsService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final StatsService statsService;

    public StatsController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/summary")
    public Result<StatsSummaryDTO> summary(@RequestParam(required = false) String role,
                                           @RequestParam(required = false) Long userId) {
        return Result.ok(statsService.getSummary(role, userId));
    }

    @GetMapping("/point-overview")
    public Result<List<PointOverviewDTO>> pointOverview(@RequestParam(defaultValue = "7") int days) {
        return Result.ok(statsService.getPointOverview(days));
    }

    @GetMapping("/todo-list")
    public Result<List<TodoItemDTO>> todoList(@RequestParam(required = false) String role,
                                              @RequestParam(required = false) Long userId,
                                              @RequestParam(defaultValue = "10") int limit) {
        return Result.ok(statsService.getTodoList(role, userId, limit));
    }

    @GetMapping("/recent-transactions")
    public Result<List<TransactionLog>> recentTransactions(@RequestParam Long userId,
                                                           @RequestParam(defaultValue = "5") int limit) {
        return Result.ok(statsService.getRecentTransactions(userId, limit));
    }

    @GetMapping("/point-trend")
    public Result<List<java.util.Map<String, Object>>> pointTrend(@RequestParam Long userId,
                                                                  @RequestParam(defaultValue = "7") int days) {
        return Result.ok(statsService.getPointTrend(userId, days));
    }
}
