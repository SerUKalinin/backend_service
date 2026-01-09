package com.example.auth_service.service;

import com.example.auth_service.dto.TaskCreateDTO;
import com.example.auth_service.dto.TaskDTO;
import com.example.auth_service.dto.TaskUpdateDTO;
import com.example.auth_service.exception.TaskNotFoundException;
import com.example.auth_service.model.ObjectEntity;
import com.example.auth_service.model.Task;
import com.example.auth_service.model.TaskStatus;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.ObjectRepository;
import com.example.auth_service.repository.TaskRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.security.SecurityService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {

    private final TaskRepository taskRepository;
    private final ObjectRepository objectRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final SecurityService securityService;

    /* ========================= CREATE ========================= */

    @Transactional
    public TaskDTO createTask(TaskCreateDTO dto) {
        log.info("Создание задачи: {}", dto);

        ObjectEntity object = objectRepository.findById(dto.getRealEstateObjectId())
                .orElseThrow(() -> new EntityNotFoundException("Объект недвижимости не найден"));

        User creator = getCurrentUser();

        Task task = Task.builder()
                .title(dto.getTitle())
                .description(dto.getDescription())
                .deadline(dto.getDeadline())
                .status(TaskStatus.NEW)
                .realEstateObject(object)
                .createdBy(creator)
                .build();

        taskRepository.save(task);
        return toDto(task);
    }

    /* ========================= READ ========================= */

    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(this::toDto)
                .toList();
    }

    public TaskDTO getTaskById(Long id) {
        return toDto(findTask(id));
    }

    public List<TaskDTO> getTasksByObjectId(Long objectId) {
        log.info("Получение задач для объекта {}", objectId);

        return taskRepository.findByRealEstateObjectId(objectId)
                .stream()
                .map(this::toDto)
                .toList();
    }

    /* ========================= UPDATE ========================= */

    @Transactional
    public TaskDTO updateTask(Long id, TaskUpdateDTO dto) {
        Task task = findTask(id);
        modelMapper.map(dto, task);
        return toDto(task);
    }

    @Transactional
    public void assignResponsible(Long taskId, Long userId) {
        Task task = findTask(taskId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
        task.setResponsibleUser(user);
    }

    @Transactional
    public void removeResponsible(Long taskId) {
        findTask(taskId).setResponsibleUser(null);
    }

    /* ========================= DELETE ========================= */

    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException("Задача не найдена");
        }
        taskRepository.deleteById(id);
    }

    /* ========================= STATISTICS ========================= */

    public Map<String, Integer> getTaskStatusStatsRecursive(Long objectId) {
        List<Long> ids = new ArrayList<>();
        ids.add(objectId);
        ids.addAll(getAllDescendantIds(objectId));

        return taskRepository.findByRealEstateObjectIdIn(ids).stream()
                .collect(Collectors.groupingBy(
                        task -> task.getStatus().name(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }

    /* ========================= HELPERS ========================= */

    private Task findTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Задача не найдена"));
    }

    private User getCurrentUser() {
        String username = securityService.getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
    }

    private TaskDTO toDto(Task task) {
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
    }

    private List<Long> getAllDescendantIds(Long parentId) {
        List<Long> result = new ArrayList<>();
        for (ObjectEntity child : objectRepository.findByParentId(parentId)) {
            result.add(child.getId());
            result.addAll(getAllDescendantIds(child.getId()));
        }
        return result;
    }
}
