package com.stockmonitor.service;

import com.stockmonitor.entity.User;
import com.stockmonitor.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder; // 导入 PasswordEncoder
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // 注入 PasswordEncoder

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public User registerUser(String phoneNumber, String password) {
        if (userRepository.findByPhoneNumber(phoneNumber).isPresent()) {
            throw new IllegalArgumentException("手机号已被注册。");
        }

        User newUser = new User();
        newUser.setPhoneNumber(phoneNumber);
        newUser.setPasswordEncode(passwordEncoder.encode(password)); // 注册时加密密码
        newUser.setStatus(User.UserStatus.LOG_OUT); // 注册时初始状态为登出
        newUser.setLastActiveTime(LocalDateTime.now()); // 初始活跃时间

        return userRepository.save(newUser);
    }

    @Transactional
    public User loginUser(String phoneNumber, String password) {
        Optional<User> optionalUser = userRepository.findByPhoneNumber(phoneNumber);

        if (optionalUser.isEmpty()) {
            throw new IllegalArgumentException("用户不存在。");
        }

        User user = optionalUser.get();
        // Spring Security 会在 AuthenticationManager 层面处理密码验证
        // 这里只是为了更新状态和活跃时间，实际业务逻辑可以移除密码校验
        // if (!passwordEncoder.matches(password, user.getPasswordEncode())) {
        //     throw new IllegalArgumentException("密码不正确。");
        // }

        // 登录成功，更新状态和最后活跃时间
        user.setStatus(User.UserStatus.LOG_IN);
        user.setLastActiveTime(LocalDateTime.now());
        return userRepository.save(user);
    }

    @Transactional
    public void logoutUser(String phoneNumber) {
        User user = userRepository.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在。"));
        user.setStatus(User.UserStatus.LOG_OUT);
        user.setLastActiveTime(LocalDateTime.now()); // 登出也更新活跃时间
        userRepository.save(user);
    }

    /**
     * 更新用户的最后活跃时间，并在登录或刷新token时调用。
     * @param phoneNumber 用户手机号
     * @param status 用户状态
     */
    @Transactional
    public void updateUserLastActiveTimeAndStatus(String phoneNumber, User.UserStatus status) {
        userRepository.findByPhoneNumber(phoneNumber).ifPresent(user -> {
            user.setLastActiveTime(LocalDateTime.now());
            user.setStatus(status);
            userRepository.save(user);
        });
    }

    // 您可能还需要一个用于加载用户的方法，供CustomUserDetailsService使用
    // 例如，在UserRepository中已有的 findByPhoneNumber
}