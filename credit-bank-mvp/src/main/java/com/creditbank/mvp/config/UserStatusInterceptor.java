package com.creditbank.mvp.config;

import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * 拦截器：检查当前登录用户是否已被冻结。
 * 被冻结的用户所有 API 请求返回 403。
 * 跳过登录和注册接口。
 */
public class UserStatusInterceptor implements HandlerInterceptor {

    private final SysUserMapper sysUserMapper;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public UserStatusInterceptor(SysUserMapper sysUserMapper) {
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String path = request.getRequestURI();

        // 跳过无需验证的接口
        if (path.equals("/api/user/login") || path.equals("/api/user/register")) {
            return true;
        }
        // 静态资源和 Swagger 放行
        if (!path.startsWith("/api/")) {
            return true;
        }

        String operatorIdStr = request.getHeader("X-Operator-Id");
        if (operatorIdStr == null || operatorIdStr.isEmpty()) {
            return true; // 没有带操作人ID的请求，暂时放行
        }

        try {
            Long operatorId = Long.parseLong(operatorIdStr);
            SysUser user = sysUserMapper.selectById(operatorId);
            if (user != null && user.getStatus() != null && user.getStatus() == 0) {
                response.setStatus(403);
                response.setContentType("application/json;charset=UTF-8");
                Map<String, Object> body = new HashMap<>();
                body.put("code", 403);
                body.put("message", "账户已被冻结，请联系管理员");
                body.put("data", null);
                response.getWriter().write(objectMapper.writeValueAsString(body));
                return false;
            }
        } catch (NumberFormatException ignored) {
        }

        return true;
    }
}
