package com.creditbank.mvp.config;

import java.util.List;
import java.util.Set;

/**
 * 权限认证相关常量。
 * 统一管理所有与权限、路径白名单相关的常量，避免分散定义导致不一致。
 */
public final class AuthConstants {

    private AuthConstants() {
    }

    // ==================== 公开路径（无需认证） ====================

    /** 完全匹配的公开路径 */
    public static final Set<String> PUBLIC_PATHS = Set.of(
            "/api/user/login",
            "/api/user/register",
            "/api/user/test-login",
            "/api/user/test-users",
            "/api/student-cert/verify",
            "/api/application/submit",
            "/api/organization/list"
    );

    /** 前缀匹配的公开路径 */
    public static final List<String> PUBLIC_PATH_PREFIXES = List.of(
            "/api/application/org-register-status",
            "/api/stats/dashboard"
    );

    /** GET 请求免认证的文件访问路径前缀 */
    public static final List<String> FILE_ACCESS_PREFIXES = List.of(
            "/api/files/preview/",
            "/api/files/download/",
            "/api/files/view/"
    );

    // ==================== 冻结用户 POST 白名单 ====================

    /** 冻结用户允许的 POST 请求路径（精确匹配） */
    public static final Set<String> FROZEN_POST_WHITELIST = Set.of(
            "/api/user/login",
            "/api/user/register",
            "/api/user/test-login",
            "/api/application/submit"
    );

    /** 冻结用户允许的 POST 请求正则路径（重新提交申诉） */
    public static final String FROZEN_POST_RESUBMIT_REGEX = "/api/application/\\d+/resubmit";

    // ==================== Swagger / 静态资源 ====================

    /** Swagger 路径 */
    public static final String[] SWAGGER_PATHS = {
            "/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**"
    };

    /** 静态资源路径 */
    public static final String[] STATIC_RESOURCE_PATHS = {
            "/static/**", "/uploads/**"
    };
}
