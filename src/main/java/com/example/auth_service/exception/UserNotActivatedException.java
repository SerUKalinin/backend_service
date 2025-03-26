package com.example.auth_service.exception;

public class UserNotActivatedException extends RuntimeException{
    public UserNotActivatedException(String message) {
        super(message);
    }
}
