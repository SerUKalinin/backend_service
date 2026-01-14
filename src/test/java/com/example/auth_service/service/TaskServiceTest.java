package com.example.auth_service.service;

import com.example.auth_service.dto.TaskCreateDTO;
import com.example.auth_service.dto.TaskDTO;
import com.example.auth_service.dto.TaskUpdateDTO;
import com.example.auth_service.exception.ObjectNotFoundException;
import com.example.auth_service.mapper.TaskMapper;
import com.example.auth_service.model.ObjectEntity;
import com.example.auth_service.model.Task;
import com.example.auth_service.model.TaskStatus;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.ObjectRepository;
import com.example.auth_service.repository.TaskRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.security.SecurityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ObjectRepository objectRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private SecurityService securityService;

    @InjectMocks
    private TaskService taskService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createTask_shouldCreateTask() {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setRealEstateObjectId(1L);

        ObjectEntity object = new ObjectEntity();
        object.setId(1L);

        User user = new User();
        user.setId(2L);

        Task task = new Task();
        TaskDTO taskDTO = new TaskDTO();

        when(objectRepository.findById(1L)).thenReturn(Optional.of(object));
        when(securityService.getCurrentUsername()).thenReturn("user");
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(user));
        when(taskMapper.toEntity(dto)).thenReturn(task);
        when(taskMapper.toDto(task)).thenReturn(taskDTO);
        when(taskRepository.save(task)).thenReturn(task);

        TaskDTO result = taskService.createTask(dto);

        assertNotNull(result);
        verify(taskRepository).save(task);
    }

    @Test
    void createTask_shouldThrowException_whenObjectNotFound() {
        TaskCreateDTO dto = new TaskCreateDTO();
        dto.setRealEstateObjectId(1L);

        when(objectRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ObjectNotFoundException.class, () -> taskService.createTask(dto));
    }

    @Test
    void getAllTasks_shouldReturnPage() {
        Task task = new Task();
        TaskDTO taskDTO = new TaskDTO();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> page = new PageImpl<>(List.of(task));

        when(taskRepository.findAll(pageable)).thenReturn(page);
        when(taskMapper.toDto(task)).thenReturn(taskDTO);

        Page<TaskDTO> result = taskService.getAllTasks(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(taskDTO, result.getContent().get(0));

        verify(taskRepository).findAll(pageable);
    }

    @Test
    void getTasksByObjectId_shouldReturnPage() {
        Task task = new Task();
        TaskDTO taskDTO = new TaskDTO();
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> page = new PageImpl<>(List.of(task));

        when(taskRepository.findByRealEstateObjectId(1L, pageable)).thenReturn(page);
        when(taskMapper.toDto(task)).thenReturn(taskDTO);

        Page<TaskDTO> result = taskService.getTasksByObjectId(1L, pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals(taskDTO, result.getContent().get(0));

        verify(taskRepository).findByRealEstateObjectId(1L, pageable);
    }

    @Test
    void getTaskById_shouldReturnTaskDTO() {
        Task task = new Task();
        TaskDTO taskDTO = new TaskDTO();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskMapper.toDto(task)).thenReturn(taskDTO);

        TaskDTO result = taskService.getTaskById(1L);

        assertEquals(taskDTO, result);
        verify(taskMapper).toDto(task);
    }

    @Test
    void updateTask_shouldUpdateTask() {
        TaskUpdateDTO dto = new TaskUpdateDTO();
        Task task = new Task();
        TaskDTO taskDTO = new TaskDTO();

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        doNothing().when(taskMapper).updateTaskFromDto(dto, task);
        when(taskMapper.toDto(task)).thenReturn(taskDTO);
        when(taskRepository.save(task)).thenReturn(task);

        TaskDTO result = taskService.updateTask(1L, dto);

        assertNotNull(result);
        verify(taskMapper).updateTaskFromDto(dto, task);
        verify(taskRepository).save(task);
    }

    @Test
    void assignResponsible_shouldAssignUser() {
        Task task = new Task();
        User user = new User();
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userRepository.findById(2L)).thenReturn(Optional.of(user));
        when(taskRepository.save(task)).thenReturn(task);

        taskService.assignResponsible(1L, 2L);

        assertEquals(user, task.getResponsibleUser());
        verify(taskRepository).save(task);
    }

    @Test
    void removeResponsible_shouldRemoveUser() {
        Task task = new Task();
        task.setResponsibleUser(new User());

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(taskRepository.save(task)).thenReturn(task);

        taskService.removeResponsible(1L);

        assertNull(task.getResponsibleUser());
        verify(taskRepository).save(task);
    }

    @Test
    void deleteTask_shouldDeleteTask() {
        Task task = new Task();
        task.setId(1L);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));

        taskService.deleteTask(1L);

        verify(taskRepository).delete(task);
    }

    @Test
    void getTaskStatusStatsRecursive_shouldReturnStats() {
        Task task1 = new Task();
        task1.setStatus(TaskStatus.NEW);
        Task task2 = new Task();
        task2.setStatus(TaskStatus.IN_PROGRESS);

        when(objectRepository.findByParentId(1L)).thenReturn(Collections.emptyList());
        when(taskRepository.findByRealEstateObjectIdIn(List.of(1L), Pageable.unpaged()))
                .thenReturn(new PageImpl<>(List.of(task1, task2)));

        Map<String, Integer> stats = taskService.getTaskStatusStatsRecursive(1L);

        assertEquals(2, stats.size());
        assertEquals(1, stats.get("NEW"));
        assertEquals(1, stats.get("IN_PROGRESS"));
    }
}
