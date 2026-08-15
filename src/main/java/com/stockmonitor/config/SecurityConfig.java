package com.stockmonitor.config;

import com.stockmonitor.security.CustomUserDetailsService; // 你的自定义UserDetailsService
import com.stockmonitor.security.jwt.JwtAuthenticationEntryPoint;
import com.stockmonitor.security.jwt.JwtAuthenticationFilter;
import com.stockmonitor.security.jwt.JwtTokenProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy; // 导入 SessionCreationPolicy
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter; // 导入 UsernamePasswordAuthenticationFilter

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true) // 启用方法级别的安全注解，如 @PreAuthorize
public class SecurityConfig {

    private final JwtAuthenticationEntryPoint unauthorizedHandler; // 处理未经认证的请求
    private final JwtTokenProvider tokenProvider; // JWT 工具类
    private final CustomUserDetailsService customUserDetailsService; // 你的自定义 UserDetailsService

    // 构造函数注入依赖
    public SecurityConfig(JwtAuthenticationEntryPoint unauthorizedHandler,
                          JwtTokenProvider tokenProvider,
                          CustomUserDetailsService customUserDetailsService) {
        this.unauthorizedHandler = unauthorizedHandler;
        this.tokenProvider = tokenProvider;
        this.customUserDetailsService = customUserDetailsService;
    }

    // 定义密码编码器 Bean
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 定义认证管理器 Bean
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    // 定义 JWT 认证过滤器 Bean
    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(tokenProvider, customUserDetailsService);
    }

    // 配置安全过滤器链
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable) // 禁用 CSRF，因为我们使用 JWT 是无状态的
            .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler)) // 配置认证入口点，处理未认证的请求
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // 禁用会话管理，使应用程序无状态
            .authorizeHttpRequests(authorize -> authorize
                .requestMatchers("/api/auth/**").permitAll() // 允许所有 /api/auth/** 路径下的请求无需认证
                .anyRequest().authenticated() // 其他所有请求都需要认证
            );

        // 在 Spring Security 的认证过滤器链中添加 JWT 过滤器
        // 确保它在 UsernamePasswordAuthenticationFilter 之前执行
        http.addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // 配置 DaoAuthenticationProvider 来使用自定义的 UserDetailsService 和 PasswordEncoder
    @Bean
    public DaoAuthenticationProvider daoAuthenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(customUserDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}