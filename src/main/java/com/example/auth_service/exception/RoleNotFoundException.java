package com.example.auth_service.exception;

/**
 * Исключение, выбрасываемое, если роль с данным типом не найдена.
 */
public class RoleNotFoundException extends RuntimeException {
    public RoleNotFoundException(String message) {
        super(message);
    }
}
