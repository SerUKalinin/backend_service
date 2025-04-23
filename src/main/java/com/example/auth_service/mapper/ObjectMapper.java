package com.example.auth_service.mapper;

import com.example.auth_service.dto.ObjectResponseDto;
import com.example.auth_service.model.ObjectEntity;
import org.mapstruct.Mapper;

/**
 * Маппер для преобразования между сущностью {@link ObjectEntity} и DTO {@link ObjectResponseDto}.
 * Использует библиотеку MapStruct для автоматического создания методов преобразования.
 */
@Mapper(componentModel = "spring")
public class ObjectMapper {

    /**
     * Преобразует сущность {@link ObjectEntity} в DTO {@link ObjectResponseDto}.
     *
     * @param entity сущность, которую нужно преобразовать
     * @return преобразованный объект DTO
     */
    public ObjectResponseDto toDto(ObjectEntity entity) {
        if (entity == null) {
            return null;
        }

        ObjectResponseDto dto = new ObjectResponseDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setObjectType(entity.getObjectType());
        dto.setParentId(entity.getParent() != null ? entity.getParent().getId() : null);
        dto.setCreatedAt(entity.getCreatedAt());

        // Преобразуем данные о пользователе, создавшем объект
        if (entity.getCreatedBy() != null) {
            dto.setCreatedById(entity.getCreatedBy().getId());
            dto.setCreatedByFirstName(entity.getCreatedBy().getFirstName());
            dto.setCreatedByLastName(entity.getCreatedBy().getLastName());
        }

        // Преобразуем данные о пользователе, ответственном за объект
        if (entity.getResponsibleUser() != null) {
            dto.setResponsibleUserId(entity.getResponsibleUser().getId());
            dto.setResponsibleUserFirstName(entity.getResponsibleUser().getFirstName());
            dto.setResponsibleUserLastName(entity.getResponsibleUser().getLastName());
            // Понимание ролей в виде строки
            dto.setResponsibleUserRole(entity.getResponsibleUser().getRoles() != null ?
                    entity.getResponsibleUser().getRoles().toString() : "ROLE_USER");
        }

        return dto;
    }

    /**
     * Преобразует DTO {@link ObjectResponseDto} в сущность {@link ObjectEntity}.
     *
     * @param dto DTO, которое нужно преобразовать
     * @return преобразованная сущность
     */
    public ObjectEntity toEntity(ObjectResponseDto dto) {
        if (dto == null) {
            return null;
        }

        ObjectEntity entity = new ObjectEntity();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setObjectType(dto.getObjectType());
        // Можно добавить дополнительные поля в объект сущности, если необходимо
        return entity;
    }
}
