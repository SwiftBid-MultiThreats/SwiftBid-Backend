package com.example.SwiftBid.payload.user;

public record UpdateUserDetailRequest(
        String fullName,
        String phoneNumber,
        String address
) {}