package com.creditbank.mvp.config;

import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.util.JwtUtil;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT 认证拦截器。
 * 从请求头中提取 Bearer Token，验证用户身份，并将用户信息（ID、角色、冻结状态）
 * 设置到 request 属性中，供后续拦截器和控制器使用。
 * 额外注释
 */
public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final SysUserMapper sysUserMapper;

    /** 存储在 request 属性中的当前用户 ID */
    public static final String CURRENT_USER_ID = "currentUserId";
    /** 存储在 request 属性中的当前用户角色 */
    public static final String CURRENT_USER_ROLE = "currentUserRole";

    public JwtAuthenticationInterceptor(JwtUtil jwtUtil, SysUserMapper sysUserMapper) {
        this.jwtUtil = jwtUtil;
        this.sysUserMapper = sysUserMapper;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {
        String path = request.getRequestURI();

        // 公开路径放行
        if (AuthConstants.PUBLIC_PATHS.contains(path)) {
            return true;
        }
        for (String prefix : AuthConstants.PUBLIC_PATH_PREFIXES) {
            if (path.startsWith(prefix)) {
                return true;
            }
        }
        // 文件预览/下载/查看由浏览器直接打开（window.open / <img>），带不上 Authorization 头；
        // 文件名为随机串且 /uploads/** 本就公开，放行不会扩大暴露面
        if ("GET".equals(request.getMethod())
                && AuthConstants.FILE_ACCESS_PREFIXES.stream().anyMatch(path::startsWith)) {
            return true;
        }
        if (!path.startsWith("/api/")) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ResponseUtil.writeError(response, 401, "请先登录");
            return false;
        }

        String token = authHeader.substring(7);
        try {
            if (!jwtUtil.validateToken(token)) {
                throw new RuntimeException("Token已过期");
            }

            Long userId = jwtUtil.getUserIdFromToken(token);
            String role = jwtUtil.getRoleFromToken(token);

            SysUser user = sysUserMapper.selectById(userId);
            if (user == null) {
                ResponseUtil.writeError(response, 401, "用户不存在");
                return false;
            }

            request.setAttribute(CURRENT_USER_ID, userId);
            request.setAttribute(CURRENT_USER_ROLE, role);
            // 冻结标记：由 FreezePermissionInterceptor 根据此标记做读写控制
            request.setAttribute("frozen", user.getStatus() != null && user.getStatus() == 0);

        } catch (Exception e) {
            ResponseUtil.writeError(response, 401, "登录已失效，请重新登录");
            return false;
        }

        return true;
    }
}
