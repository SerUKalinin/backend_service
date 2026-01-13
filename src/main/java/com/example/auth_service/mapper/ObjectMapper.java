package com.example.auth_service.mapper;

import com.example.auth_service.dto.ObjectRequestDto;
import com.example.auth_service.dto.ObjectResponseDto;
import com.example.auth_service.model.ObjectEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ObjectMapper {

    /**
     * Преобразует сущность {@link ObjectEntity} в DTO {@link ObjectResponseDto}.
     *
     * @param entity сущность
     * @return DTO
     */
    default ObjectResponseDto toDto(ObjectEntity entity) {
        if (entity == null) return null;

        ObjectResponseDto dto = new ObjectResponseDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setObjectType(entity.getObjectType());
        dto.setParentId(entity.getParent() != null ? entity.getParent().getId() : null);
        dto.setCreatedAt(entity.getCreatedAt());

        if (entity.getCreatedBy() != null) {
            dto.setCreatedById(entity.getCreatedBy().getId());
            dto.setCreatedByFirstName(entity.getCreatedBy().getFirstName());
            dto.setCreatedByLastName(entity.getCreatedBy().getLastName());
        }

        if (entity.getResponsibleUser() != null) {
            dto.setResponsibleUserId(entity.getResponsibleUser().getId());
            dto.setResponsibleUserFirstName(entity.getResponsibleUser().getFirstName());
            dto.setResponsibleUserLastName(entity.getResponsibleUser().getLastName());
            dto.setResponsibleUserRole(entity.getResponsibleUser().getRoles() != null ?
                    entity.getResponsibleUser().getRoles().toString() : "ROLE_USER");
        }

        return dto;
    }

    /**
     * Преобразует DTO {@link ObjectRequestDto} в сущность {@link ObjectEntity}.
     *
     * @param dto DTO
     * @return сущность
     */
    default ObjectEntity toEntity(ObjectRequestDto dto) {
        if (dto == null) return null;

        ObjectEntity entity = new ObjectEntity();
        entity.setName(dto.getName());
        entity.setObjectType(dto.getObjectType());
        return entity;
    }

    /**
     * Преобразует список сущностей в список DTO
     *
     * @param entities список сущностей
     * @return список DTO
     */
    default List<ObjectResponseDto> toDtoList(List<ObjectEntity> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Обновляет существующую сущность данными из DTO.
     *
     * @param dto    DTO с новыми данными
     * @param entity сущность для обновления
     */
    default void updateEntityFromDto(ObjectRequestDto dto, @MappingTarget ObjectEntity entity) {
        if (dto == null || entity == null) return;
        entity.setName(dto.getName());
        entity.setObjectType(dto.getObjectType());
    }
}
