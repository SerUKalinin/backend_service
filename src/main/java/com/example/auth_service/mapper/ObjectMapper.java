package com.example.auth_service.mapper;

import com.example.auth_service.dto.ObjectResponseDto;
import com.example.auth_service.model.ObjectEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public class ObjectMapper {

    public ObjectResponseDto toDto(ObjectEntity entity) {
        ObjectResponseDto dto = new ObjectResponseDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setObjectType(entity.getObjectType());
        dto.setParentId(entity.getParent() != null ? entity.getParent().getId() : null);
        return dto;
    }

    public ObjectEntity toEntity(ObjectResponseDto dto) {
        ObjectEntity entity = new ObjectEntity();
        entity.setId(dto.getId());
        entity.setName(dto.getName());
        entity.setObjectType(dto.getObjectType());
        return entity;
    }
}
