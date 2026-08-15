package com.stockmonitor.repository;

import com.stockmonitor.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository // 标记为 Spring 组件
public interface UserRepository extends JpaRepository<User, Integer> {
    // 根据手机号查找用户
    Optional<User> findByPhoneNumber(String phoneNumber);

    // 可以在这里添加其他查询方法，Spring Data JPA 会自动实现
    // 例如：Optional<User> findByStatus(User.UserStatus status);
}