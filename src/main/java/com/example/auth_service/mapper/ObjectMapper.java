package com.example.auth_service.mapper;

import com.example.auth_service.dto.ObjectRequestDto;
import com.example.auth_service.dto.ObjectResponseDto;
import com.example.auth_service.model.ObjectEntity;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper для преобразования между сущностью {@link ObjectEntity} и DTO {@link ObjectRequestDto}/{@link ObjectResponseDto}.
 *
 * <p>Обеспечивает конвертацию данных между слоями приложения:
 * - преобразование сущностей в DTO для передачи клиенту,
 * - создание сущностей на основе DTO,
 * - обновление существующих сущностей новыми данными из DTO.</p>
 *
 * <p>Используется сервисным слоем для отделения модели данных от представления и
 * обеспечения чистой архитектуры.</p>
 */
@Mapper(componentModel = "spring")
public interface ObjectMapper {

    /**
     * Преобразует сущность {@link ObjectEntity} в DTO {@link ObjectResponseDto}.
     *
     * @param entity сущность для конвертации; не может быть null
     * @return DTO с полями, соответствующими сущности; возвращает null, если entity равен null
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
     * @param dto DTO с данными для создания сущности; не может быть null
     * @return новая сущность с заполненными полями; возвращает null, если dto равен null
     */
    default ObjectEntity toEntity(ObjectRequestDto dto) {
        if (dto == null) return null;

        ObjectEntity entity = new ObjectEntity();
        entity.setName(dto.getName());
        entity.setObjectType(dto.getObjectType());
        return entity;
    }

    /**
     * Преобразует список сущностей {@link ObjectEntity} в список DTO {@link ObjectResponseDto}.
     *
     * @param entities список сущностей; может быть пустым или null
     * @return список DTO; если entities равен null, возвращается пустой список
     */
    default List<ObjectResponseDto> toDtoList(List<ObjectEntity> entities) {
        if (entities == null) return List.of();
        return entities.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Обновляет существующую сущность {@link ObjectEntity} данными из DTO {@link ObjectRequestDto}.
     *
     * @param dto    DTO с новыми данными; если null, обновление не выполняется
     * @param entity сущность для обновления; не может быть null
     */
    default void updateEntityFromDto(ObjectRequestDto dto, @MappingTarget ObjectEntity entity) {
        if (dto == null || entity == null) return;
        entity.setName(dto.getName());
        entity.setObjectType(dto.getObjectType());
    }
}
