package com.example.SwiftBid.controller;

import com.example.SwiftBid.dto.common.MessageResponse;
import com.example.SwiftBid.dto.contact.ContactRequest;
import com.example.SwiftBid.model.ContactMessage;
import com.example.SwiftBid.repository.ContactMessageRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** FR-STATIC-02 (extension): optional server-side persistence for the contact form. */
@RestController
@RequestMapping("/api/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactMessageRepository contactMessageRepository;

    @PostMapping
    public ResponseEntity<MessageResponse> submit(@Valid @RequestBody ContactRequest request) {
        ContactMessage message = new ContactMessage();
        message.setName(request.name());
        message.setEmail(request.email());
        message.setSubject(request.subject());
        message.setMessage(request.message());
        contactMessageRepository.save(message);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponse("Tin nhắn của bạn đã được gửi thành công!"));
    }
}
