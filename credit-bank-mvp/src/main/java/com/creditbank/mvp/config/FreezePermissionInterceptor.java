package com.creditbank.mvp.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 冻结用户权限拦截器。
 * 冻结用户只允许 GET 请求 + 白名单中的 POST 接口，
 * 其他写操作统一返回 403。
 */
public class FreezePermissionInterceptor implements HandlerInterceptor {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /** 冻结用户额外的 POST 白名单（登录/注册/提交解冻申诉） */
    private static final Set<String> POST_WHITELIST = Set.of(
            "/api/user/login",
            "/api/user/register",
            "/api/application/submit"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        Boolean frozen = (Boolean) request.getAttribute("frozen");
        if (frozen == null || !frozen) {
            return true;
        }

        String method = request.getMethod();
        String path = request.getRequestURI();

        // GET 请求放行（只读）
        if ("GET".equals(method)) {
            return true;
        }

        // POST 白名单放行（登录/注册/提交申诉）
        if ("POST".equals(method) && POST_WHITELIST.contains(path)) {
            return true;
        }

        // 其他所有写操作（POST/PUT/DELETE）拒绝
        response.setStatus(403);
        response.setContentType("application/json;charset=UTF-8");
        Map<String, Object> body = new HashMap<>();
        body.put("code", 403);
        body.put("message", "账户已被冻结，无法执行此操作。请提交解冻申诉。");
        body.put("data", null);
        response.getWriter().write(objectMapper.writeValueAsString(body));
        return false;
    }
}
