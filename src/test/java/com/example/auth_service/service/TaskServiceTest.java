package com.example.auth_service.service;

import com.example.auth_service.dto.TaskCreateDTO;
import com.example.auth_service.dto.TaskDTO;
import com.example.auth_service.dto.TaskUpdateDTO;
import com.example.auth_service.mapper.TaskMapper;
import com.example.auth_service.model.ObjectEntity;
import com.example.auth_service.model.Task;
import com.example.auth_service.model.TaskStatus;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.ObjectRepository;
import com.example.auth_service.repository.TaskRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.security.SecurityService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    private TaskRepository taskRepository;
    private ObjectRepository objectRepository;
    private UserRepository userRepository;
    private TaskMapper taskMapper;
    private SecurityService securityService;

    private TaskService taskService;

    @BeforeEach
    void setUp() {
        taskRepository = mock(TaskRepository.class);
        objectRepository = mock(ObjectRepository.class);
        userRepository = mock(UserRepository.class);
        taskMapper = mock(TaskMapper.class);
        securityService = mock(SecurityService.class);

        taskService = new TaskService(taskRepository, objectRepository, userRepository, taskMapper, securityService);
    }

    @Test
    @DisplayName("Создание задачи: успешно сохраняется с корректными полями")
    void createTask_shouldSaveTaskSuccessfully() {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setRealEstateObjectId(1L);

        ObjectEntity object = new ObjectEntity();
        object.setId(1L);
        User user = new User();
        user.setUsername("user");

        Task task = new Task();
        TaskDTO taskDTO = new TaskDTO();

        when(objectRepository.findById(1L)).thenReturn(Optional.of(object));
        when(securityService.getCurrentUsername()).thenReturn("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(taskMapper.toEntity(dto)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(taskDTO);

        TaskDTO result = taskService.createTask(dto);

        assertNotNull(result);
        verify(taskRepository).save(task);
        assertEquals(object, task.getRealEstateObject());
        assertEquals(user, task.getCreatedBy());
        assertEquals(TaskStatus.NEW, task.getStatus());
    }

    @Test
    @DisplayName("Создание задачи: выбрасывает исключение если объект не найден")
    void createTask_shouldThrowException_whenObjectNotFound() {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setRealEstateObjectId(1L);

        when(objectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> taskService.createTask(dto));
    }

    @Test
    @DisplayName("Получение задачи по ID: возвращает TaskDTO")
    void getTaskById_shouldReturnTaskDTO() {
        Task task = new Task();
        TaskDTO taskDTO = new TaskDTO();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskMapper.toDto(task)).thenReturn(taskDTO);

        TaskDTO result = taskService.getTaskById(1L);

        assertNotNull(result);
        verify(taskMapper).toDto(task);
    }

    @Test
    @DisplayName("Обновление задачи: корректно обновляет поля")
    void updateTask_shouldUpdateTask() {
        TaskUpdateDTO dto = new TaskUpdateDTO();
        Task task = new Task();
        TaskDTO taskDTO = new TaskDTO();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskMapper.toDto(task)).thenReturn(taskDTO);

        TaskDTO result = taskService.updateTask(1L, dto);

        assertNotNull(result);
        verify(taskMapper).updateTaskFromDto(dto, task);
    }

    @Test
    @DisplayName("Назначение ответственного: корректно устанавливает пользователя")
    void assignResponsible_shouldSetUser() {
        Task task = new Task();
        User user = new User();
        user.setId(2L);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));

        taskService.assignResponsible(1L, 2L);

        assertEquals(user, task.getResponsibleUser());
    }

    @Test
    @DisplayName("Удаление ответственного: корректно устанавливает null")
    void removeResponsible_shouldSetNull() {
        Task task = new Task();
        task.setResponsibleUser(new User());

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.removeResponsible(1L);

        assertNull(task.getResponsibleUser());
    }

    @Test
    @DisplayName("Удаление задачи: успешно удаляет существующую задачу")
    void deleteTask_shouldDeleteTask() {
        when(taskRepository.existsById(1L)).thenReturn(true);

        taskService.deleteTask(1L);

        verify(taskRepository).deleteById(1L);
    }

    @Test
    @DisplayName("Удаление задачи: выбрасывает исключение если задача не найдена")
    void deleteTask_shouldThrow_whenNotFound() {
        when(taskRepository.existsById(1L)).thenReturn(false);

        assertThrows(RuntimeException.class, () -> taskService.deleteTask(1L));
    }

    @Test
    @DisplayName("Статистика задач по статусу: корректно считает задачи рекурсивно")
    void getTaskStatusStatsRecursive_shouldReturnStats() {
        ObjectEntity parent = new ObjectEntity();
        parent.setId(1L);

        Task task1 = new Task();
        task1.setStatus(TaskStatus.NEW);
        Task task2 = new Task();
        task2.setStatus(TaskStatus.IN_PROGRESS);

        when(objectRepository.findByParentId(1L)).thenReturn(Collections.emptyList());
        when(taskRepository.findByRealEstateObjectIdIn(List.of(1L)))
                .thenReturn(List.of(task1, task2));

        Map<String, Integer> stats = taskService.getTaskStatusStatsRecursive(1L);

        assertEquals(2, stats.size());
        assertEquals(1, stats.get("NEW"));
        assertEquals(1, stats.get("IN_PROGRESS"));
    }
}
