package com.example.SwiftBid.service;

import java.util.Set;

public interface AccountService {
    void becomeSeller(String username);
    Set<String> getCurrentUserRoles(String username);
}