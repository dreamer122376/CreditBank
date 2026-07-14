package com.creditbank.mvp.config;

import com.creditbank.mvp.entity.SysUser;
import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

public class JwtAuthenticationInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final SysUserMapper sysUserMapper;
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static final String CURRENT_USER_ID = "currentUserId";
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
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            Map<String, Object> body = new HashMap<>();
            body.put("code", 401);
            body.put("message", "请先登录");
            body.put("data", null);
            response.getWriter().write(objectMapper.writeValueAsString(body));
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
                response.setStatus(401);
                response.setContentType("application/json;charset=UTF-8");
                Map<String, Object> body = new HashMap<>();
                body.put("code", 401);
                body.put("message", "用户不存在");
                body.put("data", null);
                response.getWriter().write(objectMapper.writeValueAsString(body));
                return false;
            }

            request.setAttribute(CURRENT_USER_ID, userId);
            request.setAttribute(CURRENT_USER_ROLE, role);
            // 冻结标记：由 FreezePermissionInterceptor 根据此标记做读写控制
            request.setAttribute("frozen", user.getStatus() != null && user.getStatus() == 0);

        } catch (Exception e) {
            response.setStatus(401);
            response.setContentType("application/json;charset=UTF-8");
            Map<String, Object> body = new HashMap<>();
            body.put("code", 401);
            body.put("message", "登录已失效，请重新登录");
            body.put("data", null);
            response.getWriter().write(objectMapper.writeValueAsString(body));
            return false;
        }

        return true;
    }
}
