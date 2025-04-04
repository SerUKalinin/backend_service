package com.example.auth_service.dto;

import com.example.auth_service.model.ObjectType;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * DTO для ответа с информацией об объекте недвижимости.
 */
@Data
public class ObjectResponseDto {
    private Long id;
    private String name;
    private ObjectType objectType;
    private Long parentId;
    private LocalDateTime createdAt;
    
    // Информация о создателе объекта
    private Long createdById;
    private String createdByFirstName;
    private String createdByLastName;
}