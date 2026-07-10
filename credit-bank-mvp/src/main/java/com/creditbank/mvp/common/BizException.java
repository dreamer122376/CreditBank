package com.creditbank.mvp.common;

/**
 * 业务异常。凡是可预期的业务错误（账户不存在、规则停用、并发冲突等）都抛这个，
 * 由 GlobalExceptionHandler 统一转换成友好的 Result 返回给前端。
 */
public class BizException extends RuntimeException {

    public BizException(String message) {
        super(message);
    }
}
