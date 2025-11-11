package com.example.SwiftBid.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.SwiftBid.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
}
