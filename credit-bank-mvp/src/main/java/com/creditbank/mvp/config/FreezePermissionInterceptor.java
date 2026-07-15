package com.creditbank.mvp.config;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Set;

/**
 * 冻结用户权限拦截器。
 * 冻结用户只允许 GET 请求 + 白名单中的 POST 接口，
 * 其他写操作统一返回 403。
 */
public class FreezePermissionInterceptor implements HandlerInterceptor {

    /** 冻结用户额外的 POST 白名单（登录/注册/提交解冻申诉） */
    private static final Set<String> POST_WHITELIST = Set.of(
            "/api/user/login",
            "/api/user/register",
            "/api/user/test-login",
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

        // 阅读通知只改变个人阅读状态，不属于业务写操作。
        if ("PUT".equals(method) && (path.equals("/api/notifications/read-all")
                || path.matches("/api/notifications/\\d+/(read|confirm)"))) {
            return true;
        }

        // POST 白名单放行（登录/注册/提交申诉）
        if ("POST".equals(method) && (POST_WHITELIST.contains(path) || path.matches(AuthConstants.FROZEN_POST_RESUBMIT_REGEX))) {
            return true;
        }

        // 其他所有写操作（POST/PUT/DELETE）拒绝
        ResponseUtil.writeError(response, 403, "账户已被冻结，无法执行此操作。请提交解冻申诉。");
        return false;
    }
}
