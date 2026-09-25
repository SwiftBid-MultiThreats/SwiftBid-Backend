package com.example.SwiftBid.service.impl;

import com.example.SwiftBid.service.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;

    @Override
    public void sendPasswordResetEmail(String toEmail, String resetLink) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(toEmail);
            message.setSubject("SwiftBid - Đặt lại mật khẩu");
            message.setText("""
                    Bạn (hoặc ai đó) đã yêu cầu đặt lại mật khẩu cho tài khoản SwiftBid.
                    Nhấn vào liên kết sau để đặt mật khẩu mới (link có hiệu lực trong 30 phút):

                    %s

                    Nếu bạn không yêu cầu, hãy bỏ qua email này.
                    """.formatted(resetLink));
            mailSender.send(message);
        } catch (Exception e) {
            // Don't let a mail transport failure surface to the client (FR-AUTH-04 always
            // returns a generic success message); log for operators to follow up.
            log.error("Không thể gửi email đặt lại mật khẩu tới {}: {}", toEmail, e.getMessage());
        }
    }
}
