package com.example.SwiftBid.service;

import java.math.BigDecimal;

public interface MailService {

    /** FR-NOTIF-01: email containing the password-reset link. */
    void sendPasswordResetEmail(String toEmail, String resetLink);

    /** FR-NOTIF-02: sent to the winning bidder when their auction completes. */
    void sendAuctionWonEmail(String toEmail, String productName, BigDecimal winningAmount, Long auctionId);

    /** FR-NOTIF-02: sent to the seller when their auction completes (with or without a winner). */
    void sendAuctionEndedEmailToSeller(String toEmail, String productName, String winnerUsername,
                                        BigDecimal winningAmount, Long auctionId);
}
