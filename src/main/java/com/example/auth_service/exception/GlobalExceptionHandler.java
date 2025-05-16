package com.example.auth_service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.security.authentication.BadCredentialsException;

/**
 * Глобальный обработчик исключений для обработки специфических ошибок в приложении.
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Обрабатывает исключение, связанное с ошибками аутентификации и авторизации.
     *
     * @param ex Исключение {@link AuthException}
     * @return Ответ с кодом 401 UNAUTHORIZED и сообщением об ошибке
     */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<String> handleAuthException(AuthException ex) {
        log.error("Ошибка аутентификации: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    /**
     * Обрабатывает исключения, возникающие при попытке создания сущности, которая уже существует.
     *
     * @param ex Исключение {@link EmailAlreadyExistsException}, {@link UserAlreadyExistsException}, {@link TaskAlreadyExistsException}
     * @return Ответ с кодом 409 CONFLICT и сообщением об ошибке
     */
    @ExceptionHandler({
            EmailAlreadyExistsException.class,
            UserAlreadyExistsException.class,
            TaskAlreadyExistsException.class
    })
    public ResponseEntity<String> handleConflictExceptions(RuntimeException ex) {
        log.error("Конфликт при создании сущности: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    /**
     * Обрабатывает исключения, возникающие при отсутствии запрашиваемой сущности в базе данных.
     *
     * @param ex Исключение {@link UserNotFoundException}, {@link ObjectNotFoundException}, {@link TaskNotFoundException}
     * @return Ответ с кодом 404 NOT FOUND и сообщением об ошибке
     */
    @ExceptionHandler({
            UserNotFoundException.class,
            ObjectNotFoundException.class,
            TaskNotFoundException.class
    })
    public ResponseEntity<String> handleNotFoundExceptions(RuntimeException ex) {
        log.error("Не найдена сущность: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Обрабатывает исключение, возникающее при попытке входа неактивированного пользователя.
     *
     * @param ex Исключение {@link UserNotActivatedException}
     * @return Ответ с кодом 403 FORBIDDEN и сообщением об ошибке
     */
    @ExceptionHandler(UserNotActivatedException.class)
    public ResponseEntity<String> handleUserNotActivatedException(UserNotActivatedException ex) {
        log.error("Неактивированный пользователь: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    /**
     * Обрабатывает исключение, возникающее при неверном коде подтверждения.
     *
     * @param ex Исключение {@link InvalidConfirmationCodeException}
     * @return Ответ с кодом 400 BAD REQUEST и сообщением об ошибке
     */
    @ExceptionHandler(InvalidConfirmationCodeException.class)
    public ResponseEntity<String> handleInvalidConfirmationCodeException(InvalidConfirmationCodeException ex) {
        log.error("Неверный код подтверждения: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Обрабатывает исключение, связанное с некорректной конфигурацией CORS.
     *
     * @param ex Исключение {@link InvalidCorsConfigurationException}
     * @return Ответ с кодом 500 INTERNAL SERVER ERROR и сообщением об ошибке
     */
    @ExceptionHandler(InvalidCorsConfigurationException.class)
    public ResponseEntity<String> handleInvalidCorsConfigurationException(InvalidCorsConfigurationException ex) {
        log.error("Некорректная конфигурация CORS: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    /**
     * Обрабатывает исключение, связанное с ошибками конфигурации Redis.
     *
     * @param ex Исключение {@link RedisConfigurationException}
     * @return Ответ с кодом 500 INTERNAL SERVER ERROR и сообщением об ошибке
     */
    @ExceptionHandler(RedisConfigurationException.class)
    public ResponseEntity<String> handleRedisConfigurationException(RedisConfigurationException ex) {
        log.error("Ошибка конфигурации Redis: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    /**
     * Обрабатывает любые неожиданные исключения, не предусмотренные в других методах.
     *
     * @param ex Общее исключение {@link Exception}
     * @return Ответ с кодом 500 INTERNAL SERVER ERROR и сообщением "Внутренняя ошибка сервера"
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        log.error("Неизвестная ошибка: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Внутренняя ошибка сервера");
    }

    /**
     * Обрабатывает исключение, возникающее при неверных учетных данных.
     *
     * @param ex Исключение {@link BadCredentialsException}
     * @return Ответ с кодом 401 UNAUTHORIZED и сообщением об ошибке
     */
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentialsException(BadCredentialsException ex) {
        log.error("Ошибка аутентификации: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Неверный логин или пароль");
    }

    /**
     * Обрабатывает исключение, возникающее при превышении лимита запросов.
     *
     * @param ex Исключение {@link RateLimitExceededException}
     * @return Ответ с кодом 429 TOO MANY REQUESTS и сообщением об ошибке
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<String> handleRateLimitExceededException(RateLimitExceededException ex) {
        log.warn("Превышен лимит запросов: {}", ex.getMessage());
        
        String message = ex.getMessage();
        if (message.contains("register_user")) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Превышен лимит попыток регистрации (3 попытки в час). Пожалуйста, подождите.");
        } else if (message.contains("register_admin")) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Превышен лимит попыток регистрации администратора (1 попытка в час). Пожалуйста, подождите.");
        } else if (message.contains("login")) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Превышен лимит попыток входа (5 попыток в минуту). Пожалуйста, подождите.");
        } else if (message.contains("logout")) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Превышен лимит запросов на выход (10 запросов в минуту). Пожалуйста, подождите.");
        } else if (message.contains("verify_email")) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Превышен лимит попыток подтверждения email (3 попытки в минуту). Пожалуйста, подождите.");
        } else if (message.contains("resend_verification")) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Превышен лимит запросов на повторную отправку кода (3 запроса в час). Пожалуйста, подождите.");
        } else if (message.contains("refresh_token")) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Превышен лимит запросов на обновление токена (10 запросов в минуту). Пожалуйста, подождите.");
        } else if (message.contains("validate_token")) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Превышен лимит запросов на проверку токена (10 запросов в минуту). Пожалуйста, подождите.");
        } else if (message.contains("forgot_password")) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Превышен лимит запросов на сброс пароля (3 запроса в час). Пожалуйста, подождите.");
        } else if (message.contains("reset_password")) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Превышен лимит попыток сброса пароля (3 попытки в час). Пожалуйста, подождите.");
        } else {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                    .body("Превышен лимит запросов. Пожалуйста, подождите некоторое время перед следующей попыткой.");
        }
    }
}
