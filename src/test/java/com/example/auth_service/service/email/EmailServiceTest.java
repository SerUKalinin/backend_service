package com.example.auth_service.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    private JavaMailSender javaMailSender;
    private EmailService emailService;
    private MimeMessage mimeMessage;

    @BeforeEach
    void setUp() {
        javaMailSender = mock(JavaMailSender.class);
        emailService = new EmailService(javaMailSender);
        mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        try {
            var field = EmailService.class.getDeclaredField("fromEmail");
            field.setAccessible(true);
            field.set(emailService, "test@example.com");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void sendConfirmationCode_shouldCallJavaMailSender() throws MessagingException {
        String to = "user@example.com";
        String code = "123456";

        emailService.sendConfirmationCode(to, code);

        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void sendPasswordResetEmail_shouldCallSendEmail() throws MessagingException {
        EmailService spyService = spy(emailService);
        String to = "user@example.com";
        String link = "http://localhost/reset?token=abc";

        spyService.sendPasswordResetEmail(to, link);

        verify(spyService).sendEmail(eq(to), contains("Сброс пароля"), contains(link));
    }

    @Test
    void sendEmail_shouldCallJavaMailSender() throws Exception {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendEmail("to@example.com", "subject", "content");

        verify(javaMailSender).send(mimeMessage);
    }
}
