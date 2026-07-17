package com.creditbank.mvp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Spring Security 安全配置。
 * 禁用 CSRF（由自定义 JWT 拦截器代替），设置无状态会话策略（所有状态由 Token 维护）。
 * 额外注释
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * 提供 BCrypt 密码编码器，用于密码加密存储和验证
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF：使用 JWT 验证，不需要 CSRF 保护
            .csrf().disable()
            // 无状态会话：不创建 HttpSession，所有认证信息来自 JWT Token
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }
}