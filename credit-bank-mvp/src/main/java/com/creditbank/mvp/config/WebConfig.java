package com.creditbank.mvp.config;

import com.creditbank.mvp.mapper.SysUserMapper;
import com.creditbank.mvp.util.JwtUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final SysUserMapper sysUserMapper;
    private final JwtUtil jwtUtil;

    public WebConfig(SysUserMapper sysUserMapper, JwtUtil jwtUtil) {
        this.sysUserMapper = sysUserMapper;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String uploadPath = Paths.get(System.getProperty("user.dir"), "uploads").toAbsolutePath().toUri().toString();
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPath);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // JWT 认证拦截器：验证 token，写入用户信息和冻结标记
        registry.addInterceptor(new JwtAuthenticationInterceptor(jwtUtil, sysUserMapper))
                .addPathPatterns("/**");
        // 角色权限拦截器：根据用户角色限制访问权限
        registry.addInterceptor(new RoleAuthorizationInterceptor())
                .addPathPatterns("/**");
        // 冻结权限拦截器：冻结用户只允许 GET + 白名单 POST
        registry.addInterceptor(new FreezePermissionInterceptor())
                .addPathPatterns("/**");
    }
}
