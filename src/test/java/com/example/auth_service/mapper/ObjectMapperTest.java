package com.example.auth_service.mapper;

import com.example.auth_service.dto.ObjectRequestDto;
import com.example.auth_service.dto.ObjectResponseDto;
import com.example.auth_service.model.ObjectEntity;
import com.example.auth_service.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class ObjectMapperTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = Mappers.getMapper(ObjectMapper.class);
    }

    @Test
    void toDto_shouldMapAllFields() {
        User createdBy = new User();
        createdBy.setId(1L);
        createdBy.setFirstName("Ivan");
        createdBy.setLastName("Ivanov");

        User responsibleUser = new User();
        responsibleUser.setId(2L);
        responsibleUser.setFirstName("Petr");
        responsibleUser.setLastName("Petrov");

        ObjectEntity parent = new ObjectEntity();
        parent.setId(10L);

        ObjectEntity entity = ObjectEntity.builder()
                .id(100L)
                .name("Test Object")
                .parent(parent)
                .createdBy(createdBy)
                .responsibleUser(responsibleUser)
                .build();

        ObjectResponseDto dto = objectMapper.toDto(entity);

        assertNotNull(dto);
        assertEquals(100L, dto.getId());
        assertEquals("Test Object", dto.getName());
        assertEquals(10L, dto.getParentId());
        assertEquals(1L, dto.getCreatedById());
        assertEquals("Ivan", dto.getCreatedByFirstName());
        assertEquals("Ivanov", dto.getCreatedByLastName());
        assertEquals(2L, dto.getResponsibleUserId());
        assertEquals("Petr", dto.getResponsibleUserFirstName());
        assertEquals("Petrov", dto.getResponsibleUserLastName());
        // поле responsibleUserRole игнорируется, проверять не нужно
    }

    @Test
    void toEntity_shouldMapFields() {
        ObjectRequestDto dto = new ObjectRequestDto();
        dto.setName("New Object");

        ObjectEntity entity = objectMapper.toEntity(dto);

        assertNotNull(entity);
        assertEquals("New Object", entity.getName());
        // Игнорируемые поля должны быть null
        assertNull(entity.getId());
        assertNull(entity.getParent());
        assertNull(entity.getCreatedBy());
        assertNull(entity.getResponsibleUser());
    }

    @Test
    void updateEntityFromDto_shouldUpdateFields() {
        ObjectRequestDto dto = new ObjectRequestDto();
        dto.setName("Updated Object");

        ObjectEntity entity = ObjectEntity.builder()
                .id(100L)
                .name("Old Name")
                .build();

        objectMapper.updateEntityFromDto(dto, entity);

        assertEquals("Updated Object", entity.getName());
        // остальные поля остаются без изменений
        assertEquals(100L, entity.getId());
    }
}
