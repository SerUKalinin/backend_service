package com.example.auth_service.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

/**
 * Сервис для отправки электронных писем.
 * Содержит метод для отправки письма с кодом подтверждения.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender javaMailSender;

    /**
     * Отправка письма с кодом подтверждения.
     *
     * @param to      Адрес получателя.
     * @param code    Код подтверждения.
     * @throws MessagingException Ошибка при отправке письма.
     */
    public void sendConfirmationCode(String to, String code) throws MessagingException {
        log.info("Попытка отправки письма с кодом подтверждения на адрес: {}", to);

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        try {
            helper.setTo(to);
            helper.setSubject("Код подтверждения для регистрации");
            helper.setText("Ваш код подтверждения: " + code);

            javaMailSender.send(message);

            log.info("Письмо с кодом подтверждения успешно отправлено на адрес: {}", to);
        } catch (MessagingException e) {
            log.error("Ошибка при отправке письма на адрес: {}", to, e);
            throw e;
        }
    }
}
