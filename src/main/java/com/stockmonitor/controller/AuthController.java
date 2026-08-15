package com.stockmonitor.controller;

import com.stockmonitor.entity.User;
import com.stockmonitor.payload.ApiResponse;
import com.stockmonitor.payload.JwtResponse; // 导入新增的 DTO
import com.stockmonitor.payload.LoginRequest;
import com.stockmonitor.payload.LogoutRequest;
import com.stockmonitor.payload.RefreshTokenRequest; // 导入新增的 DTO
import com.stockmonitor.payload.RegisterRequest;
import com.stockmonitor.service.UserService;
import com.stockmonitor.security.jwt.JwtTokenProvider; // 导入 JWT 工具类
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager; // 导入认证管理器
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService; // 导入 UserDetailsService
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final AuthenticationManager authenticationManager; // Spring Security 认证管理器
    private final JwtTokenProvider tokenProvider; // JWT 工具类
    private final UserDetailsService userDetailsService; // 用于刷新token时加载UserDetails

    public AuthController(UserService userService,
                          AuthenticationManager authenticationManager,
                          JwtTokenProvider tokenProvider,
                          UserDetailsService userDetailsService) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            // 在 UserService 中进行密码加密
            User newUser = userService.registerUser(request.phoneNumber, request.password);
            return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "用户注册成功！",newUser.getUserId()) );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false, "注册失败，服务器内部错误。"));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // 1. 使用 AuthenticationManager 进行认证
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.phoneNumber, // 使用 phoneNumber 作为用户名
                            request.password
                    )
            );

            // 2. 将认证信息设置到安全上下文
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 3. 生成 Access Token 和 Refresh Token
            String accessToken = tokenProvider.createAccessToken(authentication);
            String refreshToken = tokenProvider.createRefreshToken(authentication);

            // 4. 更新用户的最后活跃时间 (lastActiveTime) 和状态
            userService.updateUserLastActiveTimeAndStatus(request.phoneNumber, User.UserStatus.LOG_IN);

            // 5. 返回 JWT 响应
            return ResponseEntity.ok(new JwtResponse(accessToken, refreshToken,"Bearer"));

        } catch (org.springframework.security.core.AuthenticationException e) {
            // 认证失败，例如用户名不存在或密码错误
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("登录失败: " + e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("登录失败，服务器内部错误。");
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
                // 1. 从 Spring Security 上下文中获取当前认证信息
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // 防御性编程：虽然 Spring Security 过滤器会处理未认证请求，但这里再次检查确保安全
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                 .body(new ApiResponse(false, "用户未认证，请先登录。"));
        }

        // 2. 从认证主体中提取用户名 (即手机号)
        String phoneNumber = null;
        Object principal = authentication.getPrincipal();

        if (principal instanceof UserDetails) {
            phoneNumber = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) { // 适用于一些简单认证场景
            phoneNumber = (String) principal;
        } else {
            // 如果 principal 类型不是预期的，可能是配置问题
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(new ApiResponse(false, "无法从认证上下文中获取用户信息。"));
        }
        try {
            // 实际登出操作：使 refresh token 失效 (可选，复杂场景需要存储refresh token)
            // 更常见的做法是让客户端删除其存储的 token , 在应用方已完成
            // 这里我们只更新用户状态和活跃时间
            userService.updateUserLastActiveTimeAndStatus(phoneNumber, User.UserStatus.LOG_OUT);
            // 清除安全上下文 (在无状态JWT场景中通常不需要，但为了逻辑完整性)
            SecurityContextHolder.clearContext();
            return ResponseEntity.ok(new ApiResponse(true, "登出成功"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ApiResponse(false,"登出失败，服务器内部错误。"));
        }
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (tokenProvider.validateRefreshToken(refreshToken)) {
            String username = tokenProvider.getUsernameFromRefreshToken(refreshToken);
            if (username != null) {
                // 加载 UserDetails 以创建新的 Access Token
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());

                String newAccessToken = tokenProvider.createAccessToken(authentication);
                // 重新生成一个新的 Refresh Token，以实现滑动窗口的“记住我”效果
                // 这样只要用户在15天内打开过APP，刷新过token，就可以继续保持登录状态
                String newRefreshToken = tokenProvider.createRefreshToken(authentication);

                // 更新用户的最后活跃时间 (lastActiveTime) 和状态
                userService.updateUserLastActiveTimeAndStatus(username, User.UserStatus.LOG_IN);

                return ResponseEntity.ok(new JwtResponse(newAccessToken, newRefreshToken,"Bearer"));
            }
        }
        // Refresh token 无效或过期，要求重新登录
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("无效或过期的刷新令牌，请重新登录。");
    }
}