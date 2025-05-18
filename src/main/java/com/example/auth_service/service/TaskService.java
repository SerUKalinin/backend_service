package com.example.auth_service.service;

import com.example.auth_service.dto.TaskCreateDTO;
import com.example.auth_service.dto.TaskDTO;
import com.example.auth_service.dto.TaskUpdateDTO;
import com.example.auth_service.exception.TaskNotFoundException;
import com.example.auth_service.model.ObjectEntity;
import com.example.auth_service.model.Task;
import com.example.auth_service.model.TaskStatus;
import com.example.auth_service.repository.TaskRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис для управления задачами.
 * Предоставляет функциональность для создания, получения, обновления и удаления задач.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ModelMapper modelMapper;
    private final UserRepository userRepository;

    /**
     * Создание новой задачи.
     *
     * @param taskCreateDTO DTO объекта для создания задачи.
     * @return TaskDTO объект созданной задачи.
     */
    public TaskDTO createTask(TaskCreateDTO taskCreateDTO) {
        log.info("Создание задачи: {}", taskCreateDTO);

        Task task = new Task();
        task.setTitle(taskCreateDTO.getTitle());
        task.setDescription(taskCreateDTO.getDescription());
        task.setDeadline(taskCreateDTO.getDeadline());
        task.setStatus(TaskStatus.NEW);
        task.setRealEstateObject(new ObjectEntity(taskCreateDTO.getRealEstateObjectId()));

        // Устанавливаем автора задачи
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден: " + username));
        task.setCreatedBy(user);

        Task savedTask = taskRepository.save(task);
        TaskDTO dto = modelMapper.map(savedTask, TaskDTO.class);
        if (savedTask.getCreatedBy() != null) {
            dto.setCreatedByFirstName(savedTask.getCreatedBy().getFirstName());
            dto.setCreatedByLastName(savedTask.getCreatedBy().getLastName());
        }
        return dto;
    }

    /**
     * Получить все задачи.
     *
     * @return Список всех задач в формате TaskDTO.
     */
    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll().stream()
                .map(task -> modelMapper.map(task, TaskDTO.class))
                .toList();
    }

    /**
     * Получить задачу по её идентификатору.
     *
     * @param id Идентификатор задачи.
     * @return TaskDTO объект найденной задачи.
     * @throws TaskNotFoundException Если задача с указанным идентификатором не найдена.
     */
    public TaskDTO getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Задача не найдена"));
        TaskDTO dto = modelMapper.map(task, TaskDTO.class);
        if (task.getCreatedBy() != null) {
            dto.setCreatedByFirstName(task.getCreatedBy().getFirstName());
            dto.setCreatedByLastName(task.getCreatedBy().getLastName());
        }
        return dto;
    }

    /**
     * Обновить существующую задачу.
     *
     * @param id Идентификатор задачи.
     * @param taskUpdateDTO DTO с новыми данными для обновления задачи.
     * @return TaskDTO объект обновленной задачи.
     * @throws TaskNotFoundException Если задача с указанным идентификатором не найдена.
     */
    @Transactional
    public TaskDTO updateTask(Long id, TaskUpdateDTO taskUpdateDTO) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Задача не найдена"));

        modelMapper.map(taskUpdateDTO, task);
        return modelMapper.map(taskRepository.save(task), TaskDTO.class);
    }

    /**
     * Удалить задачу по её идентификатору.
     *
     * @param id Идентификатор задачи.
     * @throws TaskNotFoundException Если задача с указанным идентификатором не найдена.
     */
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException("Задача не найдена");
        }
        taskRepository.deleteById(id);
    }
}
