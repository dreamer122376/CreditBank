package com.creditbank.mvp.util;

import com.creditbank.mvp.config.JwtAuthenticationInterceptor;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 当前用户工具类。
 * 通过 RequestContextHolder 从当前请求中获取 JWT 拦截器设置的用户信息，
 * 方便 Service 层等非 Controller 组件获取当前操作用户。
 * 额外注释
 */
public class CurrentUserUtil {

    /**
     * 获取当前登录用户的 ID
     *
     * @return 用户 ID，未登录时返回 null
     */
    public static Long getCurrentUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return (Long) request.getAttribute(JwtAuthenticationInterceptor.CURRENT_USER_ID);
        }
        return null;
    }

    /**
     * 获取当前登录用户的角色
     *
     * @return 角色字符串（admin/org_admin/user/expert），未登录时返回 null
     */
    public static String getCurrentUserRole() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return (String) request.getAttribute(JwtAuthenticationInterceptor.CURRENT_USER_ROLE);
        }
        return null;
    }
}