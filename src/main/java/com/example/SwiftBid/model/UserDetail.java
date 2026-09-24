package com.example.SwiftBid.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * Extended profile information for a user (1-1 with {@link User}).
 * Kept separate from the core {@code users} table so authentication-critical
 * data stays small and profile data can evolve independently.
 */
@Setter
@Getter
@Entity
@Table(name = "user_details")
public class UserDetail {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "address")
    private String address;

    @Column(name = "bio", columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_url")
    private String avatarUrl;

    public UserDetail() {
    }

    public UserDetail(User user) {
        this.user = user;
        this.userId = user.getId();
    }
}
