package com.example.auth_service.service;

import com.example.auth_service.exception.TaskAlreadyExistsException;
import com.example.auth_service.exception.TaskNotFoundException;
import com.example.auth_service.model.Task;
import com.example.auth_service.model.TaskStatus;
import com.example.auth_service.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис для управления задачами.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;

    /**
     * Создать новую задачу.
     *
     * @param task задача для создания
     * @return созданная задача
     * @throws TaskAlreadyExistsException если задача с таким заголовком уже существует в рамках этого объекта
     */
    public Task createTask(Task task) {
        log.info("Попытка создания новой задачи: {}", task.getTitle());

        if (taskRepository.existsByTitleAndRealEstateObjectId(task.getTitle(), task.getRealEstateObject().getId())) {
            log.warn("Задача с названием '{}' уже существует для объекта с ID {}", task.getTitle(), task.getRealEstateObject().getId());
            throw new TaskAlreadyExistsException("Задача с таким названием уже существует для данного объекта недвижимости");
        }

        Task savedTask = taskRepository.save(task);
        log.info("Задача успешно создана: {}", savedTask);
        return savedTask;
    }


    /**
     * Получить список всех задач.
     *
     * @return список задач
     */
    public List<Task> getAllTasks() {
        log.info("Запрос на получение всех задач");
        return taskRepository.findAll();
    }

    /**
     * Получить задачу по ID.
     *
     * @param id идентификатор задачи
     * @return найденная задача
     * @throws TaskNotFoundException если задача не найдена
     */
    public Task getTaskById(Long id) {
        log.info("Запрос на получение задачи с ID: {}", id);
        return taskRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Задача с ID {} не найдена", id);
                    throw new TaskNotFoundException("Задача не найдена");
                });
    }


    /**
     * Обновить существующую задачу.
     *
     * @param id          идентификатор задачи
     * @param updatedTask обновленные данные задачи
     * @return обновленная задача
     * @throws TaskNotFoundException если задача не найдена
     */
    @Transactional
    public Task updateTask(Long id, Task updatedTask) {
        log.info("Попытка обновления задачи с ID {}", id);

        return taskRepository.findById(id)
                .map(task -> {
                    task.setTitle(updatedTask.getTitle());
                    task.setDescription(updatedTask.getDescription());
                    task.setStatus(updatedTask.getStatus());
                    task.setDeadline(updatedTask.getDeadline());

                    Task savedTask = taskRepository.save(task);
                    log.info("Задача с ID {} успешно обновлена", id);
                    return savedTask;
                })
                .orElseThrow(() -> {
                    log.warn("Задача с ID {} не найдена", id);
                    return new TaskNotFoundException("Задача не найдена");
                });
    }

    /**
     * Удалить задачу по ID.
     *
     * @param id идентификатор задачи
     * @throws TaskNotFoundException если задача не найдена
     */
    public void deleteTask(Long id) {
        log.info("Попытка удаления задачи с ID {}", id);

        if (!taskRepository.existsById(id)) {
            log.warn("Попытка удаления несуществующей задачи с ID {}", id);
            throw new TaskNotFoundException("Задача не найдена");
        }

        taskRepository.deleteById(id);
        log.info("Задача с ID {} успешно удалена", id);
    }

    /**
     * Получить список задач по статусу.
     *
     * @param status статус задач
     * @return список задач с указанным статусом
     */
    public List<Task> getTasksByStatus(TaskStatus status) {
        log.info("Запрос на получение задач со статусом: {}", status);
        return taskRepository.findByStatus(status);
    }

    /**
     * Получить список задач для объекта недвижимости.
     *
     * @param objectId ID объекта недвижимости
     * @return список задач, привязанных к объекту
     */
    public List<Task> getTasksByObjectId(Long objectId) {
        log.info("Запрос на получение задач для объекта с ID: {}", objectId);
        return taskRepository.findByRealEstateObjectId(objectId);
    }

    /**
     * Получить список задач по статусу и объекту недвижимости.
     *
     * @param status   статус задачи
     * @param objectId ID объекта недвижимости
     * @return список задач с указанным статусом, привязанных к объекту
     */
    public List<Task> getTasksByStatusAndObject(TaskStatus status, Long objectId) {
        log.info("Запрос на получение задач со статусом {} для объекта с ID {}", status, objectId);
        return taskRepository.findByStatusAndRealEstateObjectId(status, objectId);
    }
}
