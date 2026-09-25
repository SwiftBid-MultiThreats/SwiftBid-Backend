package com.example.SwiftBid.service;

public interface MailService {

    /** FR-NOTIF-01: email containing the password-reset link. */
    void sendPasswordResetEmail(String toEmail, String resetLink);
}
