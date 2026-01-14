package com.example.auth_service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Глобальный обработчик исключений для REST-контроллеров приложения.
 *
 * <p>Обеспечивает централизованную обработку различных типов исключений,
 * логирует их и возвращает корректный HTTP-статус и сообщение клиенту.</p>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Обрабатывает ошибки аутентификации и авторизации.
     *
     * @param ex исключение {@link AuthException}
     * @return Ответ с HTTP 401 UNAUTHORIZED и сообщением об ошибке
     */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<String> handleAuthException(AuthException ex) {
        log.error("Ошибка аутентификации: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    /**
     * Обрабатывает конфликты при создании сущностей, которые уже существуют.
     *
     * @param ex исключения {@link EmailAlreadyExistsException}, {@link UserAlreadyExistsException}, {@link TaskAlreadyExistsException}
     * @return Ответ с HTTP 409 CONFLICT и сообщением об ошибке
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
     * Обрабатывает ошибки отсутствия сущности.
     *
     * @param ex исключения {@link UserNotFoundException}, {@link ObjectNotFoundException}, {@link TaskNotFoundException}, {@link FileNotFoundException}
     * @return Ответ с HTTP 404 NOT FOUND и сообщением об ошибке
     */
    @ExceptionHandler({
            UserNotFoundException.class,
            ObjectNotFoundException.class,
            TaskNotFoundException.class,
            FileNotFoundException.class
    })
    public ResponseEntity<String> handleNotFoundExceptions(RuntimeException ex) {
        log.error("Сущность не найдена: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Обрабатывает ошибки некорректных данных.
     *
     * @param ex исключения {@link InvalidDataException}, {@link InvalidConfirmationCodeException}, {@link InvalidFileException}
     * @return Ответ с HTTP 400 BAD REQUEST и сообщением об ошибке
     */
    @ExceptionHandler({
            InvalidDataException.class,
            InvalidConfirmationCodeException.class,
            InvalidFileException.class
    })
    public ResponseEntity<String> handleInvalidDataExceptions(RuntimeException ex) {
        log.error("Некорректные данные: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }

    /**
     * Обрабатывает попытки входа неактивированного пользователя.
     *
     * @param ex исключение {@link UserNotActivatedException}
     * @return Ответ с HTTP 403 FORBIDDEN и сообщением об ошибке
     */
    @ExceptionHandler(UserNotActivatedException.class)
    public ResponseEntity<String> handleUserNotActivatedException(UserNotActivatedException ex) {
        log.error("Попытка входа неактивированного пользователя: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    /**
     * Обрабатывает превышение лимита запросов.
     *
     * @param ex исключение {@link RateLimitExceededException}
     * @return Ответ с HTTP 429 TOO MANY REQUESTS и сообщением об ошибке
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<String> handleRateLimitExceededException(RateLimitExceededException ex) {
        log.error("Превышен лимит запросов: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ex.getMessage());
    }

    /**
     * Обрабатывает системные ошибки приложения.
     *
     * @param ex исключения {@link InvalidCorsConfigurationException}, {@link RedisConfigurationException}, {@link FileStorageException}
     * @return Ответ с HTTP 500 INTERNAL SERVER ERROR и сообщением об ошибке
     */
    @ExceptionHandler({
            InvalidCorsConfigurationException.class,
            RedisConfigurationException.class,
            FileStorageException.class
    })
    public ResponseEntity<String> handleSystemExceptions(RuntimeException ex) {
        log.error("Системная ошибка: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
    }

    /**
     * Обрабатывает все неожиданные исключения, не учтенные в других обработчиках.
     *
     * @param ex общее исключение {@link Exception}
     * @return Ответ с HTTP 500 INTERNAL SERVER ERROR и сообщением "Внутренняя ошибка сервера"
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        log.error("Неизвестная ошибка: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Внутренняя ошибка сервера");
    }
}
