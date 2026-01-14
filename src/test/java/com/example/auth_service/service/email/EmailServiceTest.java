package com.example.auth_service.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.Mockito.contains;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    @DisplayName("Отправка кода подтверждения: проверяет вызов JavaMailSender")
    void sendConfirmationCode_shouldCallJavaMailSender() throws MessagingException {
        String to = "user@example.com";
        String code = "123456";

        emailService.sendConfirmationCode(to, code);

        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    @DisplayName("Отправка ссылки для сброса пароля: делегирует вызов sendEmail")
    void sendPasswordResetEmail_shouldCallSendEmail() throws MessagingException {
        EmailService spyService = spy(emailService);
        String to = "user@example.com";
        String link = "http://localhost/reset?token=abc";

        spyService.sendPasswordResetEmail(to, link);

        verify(spyService).sendEmail(eq(to), contains("Сброс пароля"), contains(link));
    }

    @Test
    @DisplayName("Отправка произвольного письма: вызывает JavaMailSender.send()")
    void sendEmail_shouldCallJavaMailSender() throws Exception {
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendEmail("to@example.com", "subject", "content");

        verify(javaMailSender).send(mimeMessage);
    }
}
