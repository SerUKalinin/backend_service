package com.example.auth_service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

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
     * @param ex Исключение {@link UserNotFoundException}, {@link ObjectNotFoundException}, {@link TaskNotFoundException}, {@link FileNotFoundException}
     * @return Ответ с кодом 404 NOT FOUND и сообщением об ошибке
     */
    @ExceptionHandler({
            UserNotFoundException.class,
            ObjectNotFoundException.class,
            TaskNotFoundException.class,
            FileNotFoundException.class
    })
    public ResponseEntity<String> handleNotFoundExceptions(RuntimeException ex) {
        log.error("Не найдена сущность: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Обрабатывает исключения, связанные с невалидными данными.
     *
     * @param ex Исключение {@link InvalidDataException}, {@link InvalidConfirmationCodeException}, {@link InvalidFileException}
     * @return Ответ с кодом 400 BAD REQUEST и сообщением об ошибке
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
     * Обрабатывает исключение, связанное с превышением лимита запросов.
     *
     * @param ex Исключение {@link RateLimitExceededException}
     * @return Ответ с кодом 429 TOO MANY REQUESTS и сообщением об ошибке
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<String> handleRateLimitExceededException(RateLimitExceededException ex) {
        log.error("Превышен лимит запросов: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ex.getMessage());
    }

    /**
     * Обрабатывает исключения, связанные с системными ошибками.
     *
     * @param ex Исключение {@link InvalidCorsConfigurationException}, {@link RedisConfigurationException}, {@link FileStorageException}
     * @return Ответ с кодом 500 INTERNAL SERVER ERROR и сообщением об ошибке
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
}
