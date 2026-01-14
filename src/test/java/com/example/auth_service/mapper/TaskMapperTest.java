package com.example.auth_service.mapper;

import com.example.auth_service.dto.TaskCreateDTO;
import com.example.auth_service.dto.TaskDTO;
import com.example.auth_service.dto.TaskUpdateDTO;
import com.example.auth_service.model.ObjectEntity;
import com.example.auth_service.model.Task;
import com.example.auth_service.model.TaskStatus;
import com.example.auth_service.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для TaskMapper.
 */
class TaskMapperTest {

    private TaskMapper taskMapper;

    @BeforeEach
    void setUp() {
        taskMapper = Mappers.getMapper(TaskMapper.class);
    }

    @Test
    void toDto_shouldMapFields() {
        User creator = new User();
        creator.setFirstName("Ivan");
        creator.setLastName("Ivanov");

        User responsible = new User();
        responsible.setFirstName("Petr");
        responsible.setLastName("Petrov");

        ObjectEntity object = new ObjectEntity();
        object.setId(100L);

        Task task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setDescription("Description");
        task.setStatus(TaskStatus.NEW);
        task.setCreatedBy(creator);
        task.setResponsibleUser(responsible);
        task.setRealEstateObject(object);
        task.setAttachments(new ArrayList<>());

        TaskDTO dto = taskMapper.toDto(task);

        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("Test Task", dto.getTitle());
        assertEquals("Description", dto.getDescription());
        assertEquals(TaskStatus.NEW, dto.getStatus());
        assertEquals("Ivan", dto.getCreatedByFirstName());
        assertEquals("Ivanov", dto.getCreatedByLastName());
        assertEquals("Petr", dto.getResponsibleUserFirstName());
        assertEquals("Petrov", dto.getResponsibleUserLastName());
        assertEquals(100L, dto.getRealEstateObjectId());
    }

    @Test
    void toEntity_shouldMapFields() {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setTitle("New Task");
        dto.setDescription("Task Description");

        Task task = taskMapper.toEntity(dto);

        assertNotNull(task);
        assertEquals("New Task", task.getTitle());
        assertEquals("Task Description", task.getDescription());

        assertNull(task.getCreatedBy());
        assertNull(task.getResponsibleUser());
        assertNull(task.getRealEstateObject());
        assertTrue(task.getAttachments().isEmpty());

        assertEquals(TaskStatus.NEW, task.getStatus());
    }


    @Test
    void updateTaskFromDto_shouldUpdateOnlyAllowedFields() {
        TaskUpdateDTO dto = new TaskUpdateDTO();
        dto.setTitle("Updated Task");
        dto.setDescription("Updated Description");

        Task task = Task.builder()
                .title("Old Task")
                .description("Old Description")
                .status(TaskStatus.IN_PROGRESS)
                .build();

        taskMapper.updateTaskFromDto(dto, task);

        assertEquals("Updated Task", task.getTitle());
        assertEquals("Updated Description", task.getDescription());

        task.setStatus(TaskStatus.IN_PROGRESS);
    }


}
