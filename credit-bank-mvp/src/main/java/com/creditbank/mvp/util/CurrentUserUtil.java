package com.creditbank.mvp.util;

import com.creditbank.mvp.config.JwtAuthenticationInterceptor;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

public class CurrentUserUtil {

    public static Long getCurrentUserId() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return (Long) request.getAttribute(JwtAuthenticationInterceptor.CURRENT_USER_ID);
        }
        return null;
    }

    public static String getCurrentUserRole() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            return (String) request.getAttribute(JwtAuthenticationInterceptor.CURRENT_USER_ROLE);
        }
        return null;
    }
}