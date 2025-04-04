package com.example.auth_service.service.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
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
    
    @Value("${spring.mail.username}")
    private String fromEmail;

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

    /**
     * Отправляет письмо со ссылкой для сброса пароля.
     *
     * @param to Email получателя.
     * @param resetLink Ссылка для сброса пароля.
     * @throws MessagingException Если произошла ошибка при отправке письма.
     */
    public void sendPasswordResetEmail(String to, String resetLink) throws MessagingException {
        String subject = "Сброс пароля - RealEstate PRO";
        String content = String.format("""
            <html>
                <body>
                    <h2>Сброс пароля</h2>
                    <p>Вы запросили сброс пароля для вашего аккаунта в RealEstate PRO.</p>
                    <p>Для сброса пароля перейдите по следующей ссылке:</p>
                    <p><a href="%s">Сбросить пароль</a></p>
                    <p>Ссылка действительна в течение 1 часа.</p>
                    <p>Если вы не запрашивали сброс пароля, проигнорируйте это письмо.</p>
                </body>
            </html>
            """, resetLink);

        sendEmail(to, subject, content);
    }

    /**
     * Отправляет email с указанным содержимым.
     *
     * @param to      Email получателя.
     * @param subject Тема письма.
     * @param content Содержимое письма (HTML).
     * @throws MessagingException Если произошла ошибка при отправке письма.
     */
    public void sendEmail(String to, String subject, String content) throws MessagingException {
        log.info("Отправка письма на адрес: {}", to);

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true); // true означает, что содержимое - HTML

        javaMailSender.send(message);
        log.info("Письмо успешно отправлено на адрес: {}", to);
    }
}
