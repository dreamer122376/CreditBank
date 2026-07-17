package com.creditbank.mvp.config;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 角色授权拦截器。
 * 根据请求路径和 HTTP 方法，判断当前用户角色是否有权限访问。
 * 权限分三级：公开路径 → 需登录路径 → 管理员/机构管理员路径。
 * 额外注释
 */
public class RoleAuthorizationInterceptor implements HandlerInterceptor {

    /** 仅管理员（admin）可访问的路径及方法 */
    private static final Map<String, List<String>> ADMIN_ONLY_PATHS = Map.ofEntries(
            Map.entry("/api/users/create", List.of("POST")),
            Map.entry("/api/users/{id}/status", List.of("PUT")),
            Map.entry("/api/users/batch-status", List.of("PUT")),
            Map.entry("/api/users/{id}/reset-pw", List.of("PUT")),
            Map.entry("/api/users/{id}", List.of("PUT")),
            Map.entry("/api/organization/create", List.of("POST")),
            Map.entry("/api/organization/update", List.of("POST")),
            Map.entry("/api/organization/{id}/status", List.of("POST")),
            Map.entry("/api/credit-rule/{id}/adjust", List.of("POST")),
            Map.entry("/api/campaigns", List.of("POST")),
            Map.entry("/api/campaigns/{id}", List.of("PUT", "DELETE")),
            Map.entry("/api/expert-cert/audit", List.of("POST")),
            Map.entry("/api/cert-audit/audit", List.of("POST")),
            Map.entry("/api/stats", List.of("GET"))
    );

    private static final Map<String, List<String>> ADMIN_OR_ORG_ADMIN_PATHS = Map.ofEntries(
            Map.entry("/api/projects", List.of("POST")),
            Map.entry("/api/projects/{id}", List.of("PUT")),
            Map.entry("/api/projects/{id}/audit", List.of("POST")),
            Map.entry("/api/projects/{id}/offline", List.of("POST")),
            Map.entry("/api/expert/create", List.of("POST")),
            Map.entry("/api/expert/update", List.of("POST")),
            Map.entry("/api/expert/{id}/status", List.of("POST")),
            Map.entry("/api/expert/{id}/assign", List.of("POST")),
            Map.entry("/api/exchange-rule/create", List.of("POST")),
            Map.entry("/api/exchange-rule/update", List.of("POST")),
            Map.entry("/api/exchange-rule/{id}/toggle", List.of("POST")),
            Map.entry("/api/notifications/publish", List.of("POST")),
            Map.entry("/api/notifications/published", List.of("GET")),
            Map.entry("/api/notifications/{id}/revoke", List.of("PUT")),
            Map.entry("/api/credit-rule/update", List.of("POST")),
            Map.entry("/api/credit-rule/{id}/toggle", List.of("POST")),
            Map.entry("/api/credit-rule/{id}", List.of("DELETE")),
            // 转换规则管理（admin / org_admin）
            Map.entry("/api/conversion-rule/create", List.of("POST")),
            Map.entry("/api/conversion-rule/update", List.of("POST")),
            Map.entry("/api/conversion-rule/{id}", List.of("DELETE")),
            Map.entry("/api/conversion-rule/{id}/toggle", List.of("POST")),
            // 转换申请审核（admin / org_admin）
            Map.entry("/api/conversion-application/{id}/audit", List.of("POST")),
            Map.entry("/api/cert-standard/create", List.of("POST")),
            Map.entry("/api/cert-standard/update", List.of("POST")),
            Map.entry("/api/cert-standard/{id}/toggle", List.of("POST")),
            Map.entry("/api/cert-audit-flow/save/{certStandardId}", List.of("POST")),
            Map.entry("/api/cert-audit-flow/{certStandardId}", List.of("DELETE")),
            Map.entry("/api/student-cert/{id}/submit", List.of("POST")),
            Map.entry("/api/student-cert/{id}/cancel", List.of("POST")),
            Map.entry("/api/student-project/{id}/submit", List.of("POST")),
            Map.entry("/api/student-project/{id}/cancel", List.of("POST")),
            Map.entry("/api/sign-in/create", List.of("POST")),
            Map.entry("/api/sign-in/{id}/verify", List.of("POST"))
    );

    /** 引用 AuthConstants 中的公开路径 */
    private static final Set<String> PUBLIC_PATHS = AuthConstants.PUBLIC_PATHS;

    /** 仅需登录（任意角色）即可访问的路径，用于角色拦截前的初步过滤 */
    private static final List<String> AUTHENTICATED_ONLY_PATHS = List.of(
            "/api/profile/**",
            "/api/points/earn",
            "/api/user/{id}/transactions",
            "/api/student-cert/**",
            "/api/projects/active",
            "/api/projects/{id}",
            "/api/projects/my",
            "/api/projects/{id}/enroll",
            "/api/projects/org",
            "/api/campaigns/active",
            "/api/campaigns/{id}",
            "/api/campaigns/{id}/enroll",
            "/api/campaigns/{id}/enrolled",
            "/api/credit-rule/list",
            "/api/credit-rule/{id}",
            "/api/cert-standard/list",
            "/api/cert-standard/{id}",
            "/api/organization/list",
            "/api/users",
            "/api/users/{id}",
            "/api/users/op-logs",
            "/api/transactions/**",
            "/api/expert/**",
            "/api/expert-cert/**",
            "/api/cert-audit/**",
            "/api/application/**",
            "/api/exchange-rule/**",
            "/api/notifications/**",
            "/api/sign-in/**",
            // 成果转换模块 - 仅登录后可访问
            "/api/conversion-rule/**",
            "/api/conversion-application/**"
    );

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // 公开路径直接放行
        if (isPublicPath(path)) {
            return true;
        }

        String role = (String) request.getAttribute(JwtAuthenticationInterceptor.CURRENT_USER_ROLE);
        if (role == null) {
            return true;
        }

        // 检查是否为仅管理员路径
        if (isAdminOnlyPath(path, method) && !"admin".equals(role)) {
            ResponseUtil.writeError(response, 403, "无权限，仅管理员可操作");
            return false;
        }

        // 检查是否为管理员或机构管理员路径
        if (isAdminOrOrgAdminPath(path, method)) {
            boolean allowed = "admin".equals(role) || "org_admin".equals(role);
            // 专家角色也允许审核操作
            if (!allowed && "expert".equals(role) && path.contains("/audit")) {
                allowed = true;
            }
            if (!allowed) {
                ResponseUtil.writeError(response, 403, "无权限，仅管理员或机构管理员可操作");
                return false;
            }
        }

        return true;
    }

    /**
     * 判断是否为公开路径
     */
    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.contains(path);
    }

    /**
     * 判断是否为仅管理员路径（同时匹配路径和方法）
     */
    private boolean isAdminOnlyPath(String path, String method) {
        for (Map.Entry<String, List<String>> entry : ADMIN_ONLY_PATHS.entrySet()) {
            String pattern = entry.getKey();
            List<String> methods = entry.getValue();
            if (matchesPath(path, pattern) && methods.contains(method)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否为管理员或机构管理员路径（同时匹配路径和方法）
     */
    private boolean isAdminOrOrgAdminPath(String path, String method) {
        for (Map.Entry<String, List<String>> entry : ADMIN_OR_ORG_ADMIN_PATHS.entrySet()) {
            String pattern = entry.getKey();
            List<String> methods = entry.getValue();
            if (matchesPath(path, pattern) && methods.contains(method)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 路径匹配工具方法。
     * 支持 {id} 通配符匹配（如 /api/users/{id}/status），
     * 同时限制 {id} 部分不能包含斜杠，避免误匹配多级路径。
     */
    private boolean matchesPath(String path, String pattern) {
        if (pattern.contains("{id}")) {
            String prefix = pattern.substring(0, pattern.indexOf("{id}"));
            String suffix = pattern.substring(pattern.indexOf("{id}") + 4);
            if (!path.startsWith(prefix) || !path.endsWith(suffix)) {
                return false;
            }
            // {id} 部分不能包含斜杠，避免 /api/campaigns/1/enroll 误匹配 /api/campaigns/{id}
            String idPart = path.substring(prefix.length(), path.length() - suffix.length());
            return !idPart.contains("/");
        }
        return path.equals(pattern);
    }
}
