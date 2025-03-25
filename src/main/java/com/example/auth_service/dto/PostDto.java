package com.example.auth_service.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO для передачи данных поста.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {

    /**
     * Контент поста.
     * Не может быть пустым.
     */
    @NotBlank(message = "Контент поста не может быть пустым")
    private String content;
}
