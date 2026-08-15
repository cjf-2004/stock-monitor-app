package com.stockmonitor.controller;

import com.stockmonitor.entity.User; // 导入你的 User 实体
import com.stockmonitor.entity.User.UserStatus;
import com.stockmonitor.payload.ApiResponse;
import com.stockmonitor.repository.UserRepository;
import com.stockmonitor.service.UserService;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// IMPORTANT: 如果你不想单独创建UserController，也可以把这个方法加到AuthController里，
// 但通常不推荐，因为AuthController的职责是认证和授权流程本身，而不是获取用户数据。
@RestController
@RequestMapping("/api/user") // 新的请求路径前缀
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;
    public UserController(UserService userService, UserRepository userRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
    }

    /**
     * 获取当前认证用户的个人资料。
     * 直接返回数据库中的 User 实体记录。
     * WARNING: 直接返回 User 实体会暴露 'passwordEncode' 字段，生产环境不推荐此做法。
     */
    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
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

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                 .body(new ApiResponse(false, "认证信息中未包含电话号码。"));
        }

        // 3. 根据电话号码从数据库中查询用户记录
        Optional<User> user = userRepository.findByPhoneNumber(phoneNumber);
        System.out.println("phoneNumber"+ phoneNumber);
        // 4. 判断用户是否存在并返回结果
        if (!user.isPresent()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(new ApiResponse(false, "未找到对应的用户资料。"));
        }
        userService.updateUserLastActiveTimeAndStatus(user.get().getPhoneNumber() , UserStatus.LOG_IN);
        

        // 5. 直接返回 User 实体 (包含所有字段，包括 passwordEncode)
        // 再次强调：生产环境中应该使用 DTO 过滤掉敏感信息！
        //return ResponseEntity.ok(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(new ApiResponse(true, "返回用户信息", user));
    }
}