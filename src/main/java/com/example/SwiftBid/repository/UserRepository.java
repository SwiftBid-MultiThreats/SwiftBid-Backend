package com.example.SwiftBid.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.SwiftBid.model.User;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    // Thêm 2 phương thức này:
    Optional<User> findByEmail(String email);

    Optional<User> findByResetToken(String resetToken);
}
