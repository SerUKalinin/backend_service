package com.example.auth_service.controller;

import com.example.auth_service.model.Task;
import com.example.auth_service.model.TaskStatus;
import com.example.auth_service.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для управления задачами.
 * Позволяет создавать, получать, обновлять и удалять задачи.
 */
@Slf4j
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {
    private final TaskService taskService;

    /**
     * Создаёт новую задачу.
     *
     * @param task Данные новой задачи.
     * @return Созданная задача.
     */
    @PostMapping
    public ResponseEntity<Task> createTask(@RequestBody Task task) {
        log.info("Создание новой задачи: {}", task);
        Task createdTask = taskService.createTask(task);
        log.info("Задача успешно создана: {}", createdTask);
        return ResponseEntity.ok(createdTask);
    }

    /**
     * Получает список всех задач.
     *
     * @return Список задач.
     */
    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        log.info("Получение списка всех задач");
        List<Task> tasks = taskService.getAllTasks();
        log.info("Найдено {} задач", tasks.size());
        return ResponseEntity.ok(tasks);
    }

    /**
     * Получает задачу по её ID.
     *
     * @param id Идентификатор задачи.
     * @return Задача, если найдена, иначе 404 Not Found.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Task> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    /**
     * Обновляет задачу по её ID.
     *
     * @param id          Идентификатор задачи.
     * @param updatedTask Обновлённые данные задачи.
     * @return Обновлённая задача.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Task> updateTask(@PathVariable Long id, @RequestBody Task updatedTask) {
        log.info("Обновление задачи с ID {}: новые данные: {}", id, updatedTask);
        Task task = taskService.updateTask(id, updatedTask);
        log.info("Задача обновлена: {}", task);
        return ResponseEntity.ok(task);
    }

    /**
     * Удаляет задачу по её ID.
     *
     * @param id Идентификатор задачи.
     * @return HTTP 204 No Content.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        log.info("Удаление задачи с ID {}", id);
        taskService.deleteTask(id);
        log.info("Задача с ID {} успешно удалена", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Получает список задач по их статусу.
     *
     * @param status Статус задач (например, NEW, IN_PROGRESS, DONE).
     * @return Список задач с указанным статусом.
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<Task>> getTasksByStatus(@PathVariable TaskStatus status) {
        log.info("Получение списка задач со статусом {}", status);
        List<Task> tasks = taskService.getTasksByStatus(status);
        log.info("Найдено {} задач со статусом {}", tasks.size(), status);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Получает список задач, связанных с объектом недвижимости.
     *
     * @param objectId Идентификатор объекта недвижимости.
     * @return Список задач, относящихся к указанному объекту.
     */
    @GetMapping("/object/{objectId}")
    public ResponseEntity<List<Task>> getTasksByObjectId(@PathVariable Long objectId) {
        log.info("Получение списка задач для объекта ID {}", objectId);
        List<Task> tasks = taskService.getTasksByObjectId(objectId);
        log.info("Найдено {} задач для объекта ID {}", tasks.size(), objectId);
        return ResponseEntity.ok(tasks);
    }

    /**
     * Получает список задач по статусу и объекту недвижимости.
     *
     * @param status   Статус задачи.
     * @param objectId Идентификатор объекта недвижимости.
     * @return Список задач с указанным статусом и объектом.
     */
    @GetMapping("/status/{status}/object/{objectId}")
    public ResponseEntity<List<Task>> getTasksByStatusAndObjectId(@PathVariable TaskStatus status, @PathVariable Long objectId) {
        log.info("Получение списка задач со статусом {} для объекта ID {}", status, objectId);
        List<Task> tasks = taskService.getTasksByStatusAndObject(status, objectId);
        log.info("Найдено {} задач со статусом {} для объекта ID {}", tasks.size(), status, objectId);
        return ResponseEntity.ok(tasks);
    }
}
