package com.example.SwiftBid.service.impl;

import com.example.SwiftBid.service.MailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.Locale;

@Slf4j
@Service
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;
    private final String frontendUrl;

    public MailServiceImpl(JavaMailSender mailSender, @Value("${app.frontend-url:http://localhost:3000}") String frontendUrl) {
        this.mailSender = mailSender;
        this.frontendUrl = frontendUrl;
    }

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        send(toEmail, "SwiftBid - Đặt lại mật khẩu", """
                Bạn (hoặc ai đó) đã yêu cầu đặt lại mật khẩu cho tài khoản SwiftBid.
                Nhấn vào liên kết sau để đặt mật khẩu mới (link có hiệu lực trong 30 phút):

                %s

                Nếu bạn không yêu cầu, hãy bỏ qua email này.
                """.formatted(resetLink));
    }

    @Override
    public void sendAuctionWonEmail(String toEmail, String productName, BigDecimal winningAmount, Long auctionId) {
        send(toEmail, "SwiftBid - Chúc mừng! Bạn đã thắng phiên đấu giá", """
                Chúc mừng! Bạn đã thắng phiên đấu giá cho sản phẩm "%s" với giá %s.

                Xem chi tiết tại: %s

                Vui lòng liên hệ người bán để hoàn tất giao dịch.
                """.formatted(productName, formatCurrency(winningAmount), auctionLink(auctionId)));
    }

    @Override
    public void sendAuctionEndedEmailToSeller(String toEmail, String productName, String winnerUsername,
                                               BigDecimal winningAmount, Long auctionId) {
        String body = winnerUsername != null
                ? """
                Phiên đấu giá cho sản phẩm "%s" của bạn đã kết thúc.
                Người thắng: %s với giá %s.

                Xem chi tiết tại: %s
                """.formatted(productName, winnerUsername, formatCurrency(winningAmount), auctionLink(auctionId))
                : """
                Phiên đấu giá cho sản phẩm "%s" của bạn đã kết thúc mà không có người đặt giá.

                Xem chi tiết tại: %s
                """.formatted(productName, auctionLink(auctionId));
        send(toEmail, "SwiftBid - Phiên đấu giá của bạn đã kết thúc", body);
    }

    private void send(String toEmail, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
        } catch (Exception e) {
            // Never let a mail transport failure surface to the caller — these notifications are
            // best-effort side effects, not part of the auction/bid transaction itself.
            log.error("Không thể gửi email '{}' tới {}: {}", subject, toEmail, e.getMessage());
        }
    }

    private String auctionLink(Long auctionId) {
        return frontendUrl + "/auctions/" + auctionId;
    }

    private String formatCurrency(BigDecimal amount) {
        NumberFormat format = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        return format.format(amount);
    }
}
