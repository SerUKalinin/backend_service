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
 * Сервис для отправки электронных писем пользователям.
 *
 * <p>Отвечает за отправку кодов подтверждения при регистрации и ссылок для сброса пароля.
 * Использует {@link JavaMailSender} для работы с SMTP-сервером.</p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    /**
     * Компонент Spring для отправки email сообщений.
     */
    private final JavaMailSender javaMailSender;

    /**
     * Email отправителя, подставляется из конфигурации приложения.
     */
    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Отправляет письмо с кодом подтверждения на указанный адрес.
     *
     * @param to    Email получателя, должен быть корректным
     * @param code  Код подтверждения для активации аккаунта
     * @throws MessagingException Если произошла ошибка при отправке письма
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
     * Отправляет письмо с ссылкой для сброса пароля пользователя.
     *
     * @param to        Email получателя, должен быть корректным
     * @param resetLink Ссылка для сброса пароля, должна быть валидной и доступной пользователю
     * @throws MessagingException Если произошла ошибка при отправке письма
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
     * Отправляет email с указанной темой и содержимым (HTML).
     *
     * @param to      Email получателя, должен быть корректным
     * @param subject Тема письма, не должна быть пустой
     * @param content HTML-содержимое письма
     * @throws MessagingException Если произошла ошибка при отправке письма
     */
    public void sendEmail(String to, String subject, String content) throws MessagingException {
        log.info("Отправка письма на адрес: {}", to);

        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);

        javaMailSender.send(message);
        log.info("Письмо успешно отправлено на адрес: {}", to);
    }
}
