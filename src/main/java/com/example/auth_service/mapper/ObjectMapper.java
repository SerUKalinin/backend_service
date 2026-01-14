package com.example.auth_service.mapper;

import com.example.auth_service.dto.ObjectRequestDto;
import com.example.auth_service.dto.ObjectResponseDto;
import com.example.auth_service.model.ObjectEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

/**
 * Mapper для преобразования между сущностью {@link ObjectEntity} и DTO {@link ObjectRequestDto}/{@link ObjectResponseDto}.
 *
 * <p>Обеспечивает методы для конвертации сущностей в DTO, DTO в сущности,
 * а также обновления существующих сущностей на основе DTO.</p>
 */
@Mapper(componentModel = "spring")
public interface ObjectMapper {

    @Mapping(target = "parentId", source = "parent.id")
    @Mapping(target = "createdById", source = "createdBy.id")
    @Mapping(target = "createdByFirstName", source = "createdBy.firstName")
    @Mapping(target = "createdByLastName", source = "createdBy.lastName")
    @Mapping(target = "responsibleUserId", source = "responsibleUser.id")
    @Mapping(target = "responsibleUserFirstName", source = "responsibleUser.firstName")
    @Mapping(target = "responsibleUserLastName", source = "responsibleUser.lastName")
    @Mapping(target = "responsibleUserRole", ignore = true)
    ObjectResponseDto toDto(ObjectEntity entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "responsibleUser", ignore = true)
    ObjectEntity toEntity(ObjectRequestDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "parent", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "responsibleUser", ignore = true)
    void updateEntityFromDto(ObjectRequestDto dto, @MappingTarget ObjectEntity entity);
}
