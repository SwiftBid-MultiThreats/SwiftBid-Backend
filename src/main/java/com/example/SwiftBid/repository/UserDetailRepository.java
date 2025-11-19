package com.example.SwiftBid.repository;

import com.example.SwiftBid.model.UserDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserDetailRepository extends JpaRepository<UserDetail, Long> {
    // Không cần thêm phương thức nào, JpaRepository là đủ
}