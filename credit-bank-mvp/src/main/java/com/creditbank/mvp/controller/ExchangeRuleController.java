package com.creditbank.mvp.controller;

import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.entity.ExchangeRule;
import com.creditbank.mvp.service.ExchangeRuleService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/exchange-rule")
public class ExchangeRuleController {

    private final ExchangeRuleService exchangeRuleService;

    public ExchangeRuleController(ExchangeRuleService exchangeRuleService) {
        this.exchangeRuleService = exchangeRuleService;
    }

    @GetMapping("/list")
    public Result<List<ExchangeRule>> list() {
        return Result.ok(exchangeRuleService.list());
    }

    @PostMapping("/create")
    public Result<ExchangeRule> create(@RequestBody ExchangeRule rule) {
        return Result.ok(exchangeRuleService.create(rule));
    }

    @PostMapping("/update")
    public Result<ExchangeRule> update(@RequestBody ExchangeRule rule) {
        return Result.ok(exchangeRuleService.update(rule));
    }

    @PostMapping("/{id}/toggle")
    public Result<ExchangeRule> toggle(@PathVariable Long id) {
        return Result.ok(exchangeRuleService.toggle(id));
    }
}
