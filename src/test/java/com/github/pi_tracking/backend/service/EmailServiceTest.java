package com.github.pi_tracking.backend.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender emailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService();
        // Use reflection to set the emailSender field
        try {
            java.lang.reflect.Field field = EmailService.class.getDeclaredField("emailSender");
            field.setAccessible(true);
            field.set(emailService, emailSender);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set emailSender field", e);
        }
    }

    @Test
    void sendEmail_ShouldSendEmail() {
        // Arrange
        String to = "test@example.com";
        String subject = "Test Subject";
        String text = "Test Message";

        // Act
        emailService.sendEmail(to, subject, text);

        // Assert
        verify(emailSender).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendEmail_ShouldSetCorrectEmailProperties() {
        // Arrange
        String to = "test@example.com";
        String subject = "Test Subject";
        String text = "Test Message";

        // Act
        emailService.sendEmail(to, subject, text);

        // Assert
        verify(emailSender).send(argThat((SimpleMailMessage message) ->
            message.getTo()[0].equals(to) &&
            message.getSubject().equals(subject) &&
            message.getText().equals(text)
        ));
    }
} 