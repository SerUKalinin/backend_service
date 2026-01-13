package com.example.auth_service.service;

import com.example.auth_service.dto.TaskCreateDTO;
import com.example.auth_service.dto.TaskDTO;
import com.example.auth_service.dto.TaskUpdateDTO;
import com.example.auth_service.exception.TaskNotFoundException;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Сервис для управления задачами (Task).
 * <p>
 * Предоставляет методы для создания, чтения, обновления, удаления и статистики задач.
 * Использует MapStruct для маппинга между DTO и сущностью Task.
 * </p>
 */
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

    /**
     * Создаёт новую задачу в системе.
     *
     * @param dto {@link TaskCreateDTO} с данными для создания задачи
     * @return {@link TaskDTO} с данными созданной задачи
     * @throws EntityNotFoundException если объект недвижимости не найден
     */
    @Transactional
    public TaskDTO createTask(TaskCreateDTO dto) {
        log.info("Создание задачи: {}", dto);

        ObjectEntity object = objectRepository.findById(dto.getRealEstateObjectId())
                .orElseThrow(() -> new EntityNotFoundException("Объект недвижимости не найден"));

        User creator = getCurrentUser();

        Task task = taskMapper.toEntity(dto);
        task.setRealEstateObject(object);
        task.setCreatedBy(creator);
        task.setStatus(TaskStatus.NEW);

        taskRepository.save(task);

        return taskMapper.toDto(task);
    }

    /**
     * Получает все задачи в системе.
     *
     * @return список {@link TaskDTO} всех задач
     */
    public List<TaskDTO> getAllTasks() {
        return taskRepository.findAll()
                .stream()
                .map(taskMapper::toDto)
                .toList();
    }

    /**
     * Получает задачу по её идентификатору.
     *
     * @param id идентификатор задачи
     * @return {@link TaskDTO} с данными задачи
     * @throws TaskNotFoundException если задача не найдена
     */
    public TaskDTO getTaskById(Long id) {
        return taskMapper.toDto(findTask(id));
    }

    /**
     * Получает список задач, связанных с конкретным объектом недвижимости.
     *
     * @param objectId идентификатор объекта недвижимости
     * @return список {@link TaskDTO} задач для данного объекта
     */
    public List<TaskDTO> getTasksByObjectId(Long objectId) {
        log.info("Получение задач для объекта {}", objectId);
        return taskRepository.findByRealEstateObjectId(objectId)
                .stream()
                .map(taskMapper::toDto)
                .toList();
    }

    /**
     * Обновляет данные существующей задачи.
     *
     * @param id  идентификатор задачи
     * @param dto {@link TaskUpdateDTO} с новыми данными задачи
     * @return {@link TaskDTO} обновлённой задачи
     * @throws TaskNotFoundException если задача не найдена
     */
    @Transactional
    public TaskDTO updateTask(Long id, TaskUpdateDTO dto) {
        Task task = findTask(id);
        taskMapper.updateTaskFromDto(dto, task);
        return taskMapper.toDto(task);
    }

    /**
     * Назначает ответственного пользователя на задачу.
     *
     * @param taskId идентификатор задачи
     * @param userId идентификатор пользователя
     * @throws EntityNotFoundException если задача или пользователь не найдены
     */
    @Transactional
    public void assignResponsible(Long taskId, Long userId) {
        Task task = findTask(taskId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
        task.setResponsibleUser(user);
    }

    /**
     * Убирает ответственного пользователя с задачи.
     *
     * @param taskId идентификатор задачи
     * @throws TaskNotFoundException если задача не найдена
     */
    @Transactional
    public void removeResponsible(Long taskId) {
        findTask(taskId).setResponsibleUser(null);
    }

    /**
     * Удаляет задачу по идентификатору.
     *
     * @param id идентификатор задачи
     * @throws TaskNotFoundException если задача не найдена
     */
    @Transactional
    public void deleteTask(Long id) {
        if (!taskRepository.existsById(id)) {
            throw new TaskNotFoundException("Задача не найдена");
        }
        taskRepository.deleteById(id);
    }

    /**
     * Получает статистику задач по статусам для объекта и всех его потомков.
     *
     * @param objectId идентификатор объекта недвижимости
     * @return {@link Map} с ключом статус задачи и значением количество задач в этом статусе
     */
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

    /**
     * Ищет задачу по идентификатору.
     *
     * @param id идентификатор задачи
     * @return {@link Task} найденная задача
     * @throws TaskNotFoundException если задача не найдена
     */
    private Task findTask(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException("Задача не найдена"));
    }

    /**
     * Получает текущего аутентифицированного пользователя.
     *
     * @return {@link User} текущий пользователь
     * @throws EntityNotFoundException если пользователь не найден
     */
    private User getCurrentUser() {
        String username = securityService.getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("Пользователь не найден"));
    }

    /**
     * Получает список идентификаторов всех потомков объекта недвижимости.
     *
     * @param parentId идентификатор родительского объекта
     * @return список идентификаторов всех потомков
     */
    private List<Long> getAllDescendantIds(Long parentId) {
        List<Long> result = new ArrayList<>();
        for (ObjectEntity child : objectRepository.findByParentId(parentId)) {
            result.add(child.getId());
            result.addAll(getAllDescendantIds(child.getId()));
        }
        return result;
    }
}
