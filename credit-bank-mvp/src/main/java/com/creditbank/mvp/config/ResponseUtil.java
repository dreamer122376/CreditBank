package com.creditbank.mvp.config;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 拦截器响应工具类。
 * 统一管理 ObjectMapper 和错误响应写入，避免各拦截器重复创建。
 */
public final class ResponseUtil {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    private ResponseUtil() {
    }

    /**
     * 写入 JSON 格式的错误响应
     *
     * @param response HttpServletResponse
     * @param status   HTTP 状态码
     * @param message  错误消息
     */
    public static void writeError(HttpServletResponse response, int status, String message) {
        try {
            response.setStatus(status);
            response.setContentType("application/json;charset=UTF-8");
            Map<String, Object> body = new HashMap<>();
            body.put("code", status);
            body.put("message", message);
            body.put("data", null);
            response.getWriter().write(objectMapper.writeValueAsString(body));
        } catch (Exception e) {
            // 写入响应失败时静默处理，避免二次异常
        }
    }
}
