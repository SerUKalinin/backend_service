package com.example.auth_service.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Глобальный обработчик исключений приложения.
 *
 * <p>Отвечает за централизованную обработку всех исключений, возникающих в контроллерах,
 * и формирует корректные HTTP-ответы для клиента с соответствующим статусом и сообщением.</p>
 *
 * <p>Позволяет логировать ошибки и стандартизировать обработку ошибок на уровне всего приложения.</p>
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * Обрабатывает исключения аутентификации и авторизации.
     *
     * @param ex исключение {@link AuthException}, выбрасываемое при проблемах входа пользователя
     * @return {@link ResponseEntity} с HTTP статусом 401 UNAUTHORIZED и сообщением ошибки
     */
    @ExceptionHandler(AuthException.class)
    public ResponseEntity<String> handleAuthException(AuthException ex) {
        log.error("Ошибка аутентификации: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ex.getMessage());
    }

    /**
     * Обрабатывает исключения конфликтов при создании сущностей, которые уже существуют.
     *
     * @param ex исключения {@link EmailAlreadyExistsException}, {@link UserAlreadyExistsException}, {@link TaskAlreadyExistsException}
     * @return {@link ResponseEntity} с HTTP статусом 409 CONFLICT и сообщением ошибки
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
     * Обрабатывает исключения отсутствия сущности в системе.
     *
     * @param ex исключения {@link UserNotFoundException}, {@link ObjectNotFoundException}, {@link TaskNotFoundException}, {@link FileNotFoundException}
     * @return {@link ResponseEntity} с HTTP статусом 404 NOT FOUND и сообщением ошибки
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
     * Обрабатывает исключения, связанные с некорректными данными пользователя или системы.
     *
     * @param ex исключения {@link InvalidDataException}, {@link InvalidConfirmationCodeException}, {@link InvalidFileException}
     * @return {@link ResponseEntity} с HTTP статусом 400 BAD REQUEST и сообщением ошибки
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
     * Обрабатывает исключение при попытке входа пользователя, который не активирован.
     *
     * @param ex исключение {@link UserNotActivatedException}
     * @return {@link ResponseEntity} с HTTP статусом 403 FORBIDDEN и сообщением ошибки
     */
    @ExceptionHandler(UserNotActivatedException.class)
    public ResponseEntity<String> handleUserNotActivatedException(UserNotActivatedException ex) {
        log.error("Неактивированный пользователь: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ex.getMessage());
    }

    /**
     * Обрабатывает исключения, связанные с превышением лимита запросов.
     *
     * @param ex исключение {@link RateLimitExceededException}
     * @return {@link ResponseEntity} с HTTP статусом 429 TOO MANY REQUESTS и сообщением ошибки
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<String> handleRateLimitExceededException(RateLimitExceededException ex) {
        log.error("Превышен лимит запросов: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(ex.getMessage());
    }

    /**
     * Обрабатывает системные исключения приложения.
     *
     * @param ex исключения {@link InvalidCorsConfigurationException}, {@link RedisConfigurationException}, {@link FileStorageException}
     * @return {@link ResponseEntity} с HTTP статусом 500 INTERNAL SERVER ERROR и сообщением ошибки
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
     * Обрабатывает все неожиданные исключения, не предусмотренные другими методами.
     *
     * @param ex любое {@link Exception}, возникшее в контроллерах
     * @return {@link ResponseEntity} с HTTP статусом 500 INTERNAL SERVER ERROR и стандартным сообщением "Внутренняя ошибка сервера"
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleGenericException(Exception ex) {
        log.error("Неизвестная ошибка: {}", ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Внутренняя ошибка сервера");
    }
}
