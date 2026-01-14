package com.example.auth_service.controller;

import com.example.auth_service.dto.AssignResponsibleRequest;
import com.example.auth_service.dto.TaskCreateDTO;
import com.example.auth_service.dto.TaskDTO;
import com.example.auth_service.dto.TaskUpdateDTO;
import com.example.auth_service.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaskControllerTest {

    private TaskService taskService;
    private TaskController taskController;

    @BeforeEach
    void setUp() {
        taskService = mock(TaskService.class);
        taskController = new TaskController(taskService);
    }

    @Test
    @DisplayName("Создание задачи: возвращает созданную задачу с корректным Location")
    void createTask_shouldReturnCreatedTask() {
        TaskCreateDTO createDTO = new TaskCreateDTO();
        TaskDTO taskDTO = new TaskDTO();
        taskDTO.setId(1L);
        when(taskService.createTask(createDTO)).thenReturn(taskDTO);

        ResponseEntity<TaskDTO> response = taskController.createTask(createDTO);

        assertEquals(taskDTO, response.getBody());
        assertEquals(201, response.getStatusCodeValue());
        assertEquals("/tasks/1", response.getHeaders().getLocation().toString());
        verify(taskService).createTask(createDTO);
    }

    @Test
    @DisplayName("Получение всех задач: возвращает список всех задач")
    void getAllTasks_shouldReturnList() {
        List<TaskDTO> tasks = List.of(new TaskDTO());
        when(taskService.getAllTasks()).thenReturn(tasks);

        ResponseEntity<List<TaskDTO>> response = taskController.getAllTasks();

        assertEquals(tasks, response.getBody());
        verify(taskService).getAllTasks();
    }

    @Test
    @DisplayName("Получение задачи по ID: возвращает корректную задачу")
    void getTaskById_shouldReturnTask() {
        TaskDTO taskDTO = new TaskDTO();
        when(taskService.getTaskById(1L)).thenReturn(taskDTO);

        ResponseEntity<TaskDTO> response = taskController.getTaskById(1L);

        assertEquals(taskDTO, response.getBody());
        verify(taskService).getTaskById(1L);
    }

    @Test
    @DisplayName("Обновление задачи: возвращает обновленную задачу")
    void updateTask_shouldReturnUpdatedTask() {
        TaskUpdateDTO updateDTO = new TaskUpdateDTO();
        TaskDTO taskDTO = new TaskDTO();
        when(taskService.updateTask(1L, updateDTO)).thenReturn(taskDTO);

        ResponseEntity<TaskDTO> response = taskController.updateTask(1L, updateDTO);

        assertEquals(taskDTO, response.getBody());
        verify(taskService).updateTask(1L, updateDTO);
    }

    @Test
    @DisplayName("Удаление задачи: вызывает сервис и возвращает статус 204")
    void deleteTask_shouldCallService() {
        ResponseEntity<Void> response = taskController.deleteTask(1L);

        assertEquals(204, response.getStatusCodeValue());
        verify(taskService).deleteTask(1L);
    }

    @Test
    @DisplayName("Назначение ответственного за задачу: вызывает сервис и возвращает статус 200")
    void assignResponsible_shouldCallService() {
        AssignResponsibleRequest request = new AssignResponsibleRequest();
        request.setUserId(2L);

        ResponseEntity<?> response = taskController.assignResponsible(1L, request);

        assertEquals(200, response.getStatusCodeValue());
        verify(taskService).assignResponsible(1L, 2L);
    }

    @Test
    @DisplayName("Снятие ответственного с задачи: вызывает сервис и возвращает статус 200")
    void removeResponsible_shouldCallService() {
        ResponseEntity<?> response = taskController.removeResponsible(1L);

        assertEquals(200, response.getStatusCodeValue());
        verify(taskService).removeResponsible(1L);
    }

    @Test
    @DisplayName("Получение задач по ID объекта: возвращает список задач объекта")
    void getTasksByObjectId_shouldReturnList() {
        List<TaskDTO> tasks = List.of(new TaskDTO());
        when(taskService.getTasksByObjectId(1L)).thenReturn(tasks);

        ResponseEntity<List<TaskDTO>> response = taskController.getTasksByObjectId(1L);

        assertEquals(tasks, response.getBody());
        verify(taskService).getTasksByObjectId(1L);
    }

    @Test
    @DisplayName("Статистика статусов задач объекта: возвращает карту с количеством задач по статусам")
    void getTaskStatusStats_shouldReturnMap() {
        Map<String, Integer> stats = Map.of("NEW", 2, "DONE", 1);
        when(taskService.getTaskStatusStatsRecursive(1L)).thenReturn(stats);

        ResponseEntity<Map<String, Integer>> response = taskController.getTaskStatusStats(1L);

        assertEquals(stats, response.getBody());
        verify(taskService).getTaskStatusStatsRecursive(1L);
    }
}
