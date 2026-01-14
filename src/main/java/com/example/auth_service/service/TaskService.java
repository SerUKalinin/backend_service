package com.example.auth_service.service;

import com.example.auth_service.dto.TaskCreateDTO;
import com.example.auth_service.dto.TaskDTO;
import com.example.auth_service.dto.TaskUpdateDTO;
import com.example.auth_service.exception.ObjectNotFoundException;
import com.example.auth_service.exception.TaskNotFoundException;
import com.example.auth_service.exception.UserNotFoundException;
import com.example.auth_service.mapper.TaskMapper;
import com.example.auth_service.model.ObjectEntity;
import com.example.auth_service.model.Task;
import com.example.auth_service.model.TaskStatus;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.ObjectRepository;
import com.example.auth_service.repository.TaskRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.security.SecurityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    private final TaskMapper taskMapper;
    private final SecurityService securityService;

    /** Создаёт новую задачу */
    @Transactional
    public TaskDTO createTask(TaskCreateDTO dto) {
        log.info("Создание задачи: {}", dto);

        ObjectEntity object = objectRepository.findById(dto.getRealEstateObjectId())
                .orElseThrow(() -> new ObjectNotFoundException("Объект недвижимости не найден"));

        User creator = getCurrentUser();

        Task task = taskMapper.toEntity(dto);
        task.setRealEstateObject(object);
        task.setCreatedBy(creator);
        task.setStatus(TaskStatus.NEW);

        taskRepository.save(task);
        log.info("Задача создана с ID: {}", task.getId());

        return taskMapper.toDto(task);
    }

    /** Получает все задачи с пагинацией */
    public Page<TaskDTO> getAllTasks(Pageable pageable) {
        return taskRepository.findAll(pageable)
                .map(taskMapper::toDto);
    }

    /** Получает задачи для конкретного объекта с пагинацией */
    public Page<TaskDTO> getTasksByObjectId(Long objectId, Pageable pageable) {
        log.info("Получение задач для объекта {}", objectId);
        return taskRepository.findByRealEstateObjectId(objectId, pageable)
                .map(taskMapper::toDto);
    }

    /** Получает задачу по ID */
    public TaskDTO getTaskById(Long id) {
        return taskMapper.toDto(findTask(id));
    }

    /** Обновляет задачу */
    @Transactional
    public TaskDTO updateTask(Long id, TaskUpdateDTO dto) {
        Task task = findTask(id);
        taskMapper.updateTaskFromDto(dto, task);
        taskRepository.save(task);
        log.info("Задача {} обновлена", id);
        return taskMapper.toDto(task);
    }

    /** Назначает ответственного */
    @Transactional
    public void assignResponsible(Long taskId, Long userId) {
        Task task = findTask(taskId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден"));
        task.setResponsibleUser(user);
        taskRepository.save(task);
        log.info("Пользователь {} назначен ответственным за задачу {}", userId, taskId);
    }

    /** Убирает ответственного */
    @Transactional
    public void removeResponsible(Long taskId) {
        Task task = findTask(taskId);
        task.setResponsibleUser(null);
        taskRepository.save(task);
        log.info("Ответственный убран с задачи {}", taskId);
    }

    /** Удаляет задачу */
    @Transactional
    public void deleteTask(Long id) {
        Task task = findTask(id);
        taskRepository.delete(task);
        log.info("Задача {} удалена", id);
    }

    /** Получает статистику задач по статусам для объекта и всех его потомков */
    public Map<String, Integer> getTaskStatusStatsRecursive(Long objectId) {
        List<Long> ids = new ArrayList<>();
        ids.add(objectId);
        ids.addAll(getAllDescendantIds(objectId));

        return taskRepository.findByRealEstateObjectIdIn(ids, Pageable.unpaged())
                .stream()
                .collect(Collectors.groupingBy(
                        task -> task.getStatus().name(),
                        Collectors.collectingAndThen(Collectors.counting(), Long::intValue)
                ));
    }

    /** Ищет задачу по ID */
    private Task findTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Задача не найдена: " + id));
    }

    /** Получает текущего пользователя */
    private User getCurrentUser() {
        String username = securityService.getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("Пользователь не найден: " + username));
    }

    /** Итеративно получает ID всех потомков объекта */
    private List<Long> getAllDescendantIds(Long parentId) {
        List<Long> result = new ArrayList<>();
        Deque<Long> stack = new ArrayDeque<>();
        stack.push(parentId);

        while (!stack.isEmpty()) {
            Long currentId = stack.pop();
            List<ObjectEntity> children = objectRepository.findByParentId(currentId);
            for (ObjectEntity child : children) {
                result.add(child.getId());
                stack.push(child.getId());
            }
        }
        return result;
    }
}
