package com.example.auth_service.service;

import com.example.auth_service.dto.TaskCreateDTO;
import com.example.auth_service.dto.TaskDTO;
import com.example.auth_service.dto.TaskUpdateDTO;
import com.example.auth_service.exception.TaskNotFoundException;
import com.example.auth_service.model.ObjectEntity;
import com.example.auth_service.model.Task;
import com.example.auth_service.model.TaskStatus;
import com.example.auth_service.repository.ObjectRepository;
import com.example.auth_service.repository.TaskRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.model.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.HashMap;

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
    private final ObjectRepository objectRepository;

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

    public void assignResponsible(Long taskId, Long userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        task.setResponsibleUser(user);
        taskRepository.save(task);
    }

    public void removeResponsible(Long taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));
        task.setResponsibleUser(null);
        taskRepository.save(task);
    }

    /**
     * Получить все задачи для конкретного объекта недвижимости.
     *
     * @param objectId ID объекта недвижимости
     * @return список задач для данного объекта
     */
    public List<TaskDTO> getTasksByObjectId(Long objectId) {
        log.info("Получение задач для объекта с ID: {}", objectId);
        return taskRepository.findByRealEstateObjectId(objectId).stream()
                .map(task -> {
                    TaskDTO dto = modelMapper.map(task, TaskDTO.class);
                    if (task.getCreatedBy() != null) {
                        dto.setCreatedByFirstName(task.getCreatedBy().getFirstName());
                        dto.setCreatedByLastName(task.getCreatedBy().getLastName());
                    }
                    if (task.getResponsibleUser() != null) {
                        dto.setResponsibleUserFirstName(task.getResponsibleUser().getFirstName());
                        dto.setResponsibleUserLastName(task.getResponsibleUser().getLastName());
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    // Получить статистику задач по статусам для объекта и всех его потомков
    public Map<String, Integer> getTaskStatusStatsRecursive(Long objectId) {
        List<Long> allObjectIds = getAllDescendantIds(objectId);
        allObjectIds.add(objectId);

        List<Task> tasks = taskRepository.findByRealEstateObjectIdIn(allObjectIds);
        Map<String, Integer> statusCounts = new HashMap<>();
        for (TaskStatus status : TaskStatus.values()) {
            statusCounts.put(status.name(), 0);
        }
        for (Task task : tasks) {
            String status = task.getStatus().name();
            statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
        }
        return statusCounts;
    }

    // Рекурсивно получить все id потомков
    private List<Long> getAllDescendantIds(Long parentId) {
        List<Long> result = new ArrayList<>();
        List<ObjectEntity> children = objectRepository.findByParentId(parentId);
        for (ObjectEntity child : children) {
            result.add(child.getId());
            result.addAll(getAllDescendantIds(child.getId()));
        }
        return result;
    }
}
