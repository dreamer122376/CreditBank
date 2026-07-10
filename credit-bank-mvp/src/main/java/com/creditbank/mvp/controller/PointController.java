package com.creditbank.mvp.controller;

import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.dto.EarnRequest;
import com.creditbank.mvp.entity.Account;
import com.creditbank.mvp.entity.PointRule;
import com.creditbank.mvp.entity.PointTransaction;
import com.creditbank.mvp.service.PointService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 积分相关的 REST 接口。Controller 只做"收参数、调 Service、包 Result"，
 * 不写业务逻辑——业务都在 Service 里。这是分层的关键纪律。
 */
@RestController
@RequestMapping("/api/points")
public class PointController {

    private final PointService pointService;

    public PointController(PointService pointService) {
        this.pointService = pointService;
    }

    /** 加分：完成一件事，触发一条积分规则 */
    @PostMapping("/earn")
    public Result<Account> earn(@RequestBody EarnRequest req) {
        return Result.ok(pointService.earn(req.getAccountId(), req.getRuleCode()));
    }

    /** 所有账户 */
    @GetMapping("/accounts")
    public Result<List<Account>> accounts() {
        return Result.ok(pointService.listAccounts());
    }

    /** 单个账户（含余额） */
    @GetMapping("/account/{id}")
    public Result<Account> account(@PathVariable Long id) {
        return Result.ok(pointService.getAccount(id));
    }

    /** 某账户的积分流水 */
    @GetMapping("/account/{id}/transactions")
    public Result<List<PointTransaction>> transactions(@PathVariable Long id) {
        return Result.ok(pointService.listTransactions(id));
    }

    /** 所有启用的积分规则 */
    @GetMapping("/rules")
    public Result<List<PointRule>> rules() {
        return Result.ok(pointService.listRules());
    }
}
