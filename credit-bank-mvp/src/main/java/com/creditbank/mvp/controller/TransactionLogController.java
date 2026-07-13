package com.creditbank.mvp.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.creditbank.mvp.common.Result;
import com.creditbank.mvp.entity.TransactionLog;
import com.creditbank.mvp.service.TransactionLogService;
import com.creditbank.mvp.util.CurrentUserUtil;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
@RequestMapping("/api/transactions")
public class TransactionLogController {

    private final TransactionLogService transactionLogService;

    public TransactionLogController(TransactionLogService transactionLogService) {
        this.transactionLogService = transactionLogService;
    }

    @GetMapping
    public Result<Page<TransactionLog>> page(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String bizType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        Long operatorId = CurrentUserUtil.getCurrentUserId();
        return Result.ok(transactionLogService.page(operatorId, page, size, userId, bizType, startTime, endTime));
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> export(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String bizType,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime) {
        Long operatorId = CurrentUserUtil.getCurrentUserId();
        byte[] csvData = transactionLogService.exportToCsv(operatorId, userId, bizType, startTime, endTime);
        
        String filename = "transactions_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
                .body(csvData);
    }

    @GetMapping("/{id}")
    public Result<TransactionLog> detail(@PathVariable Long id) {
        Long operatorId = CurrentUserUtil.getCurrentUserId();
        return Result.ok(transactionLogService.getById(operatorId, id));
    }
}