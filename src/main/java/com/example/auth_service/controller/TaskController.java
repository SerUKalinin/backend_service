package com.example.auth_service.controller;

import com.example.auth_service.dto.AssignResponsibleRequest;
import com.example.auth_service.dto.TaskCreateDTO;
import com.example.auth_service.dto.TaskDTO;
import com.example.auth_service.dto.TaskUpdateDTO;
import com.example.auth_service.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

/**
 * Контроллер для управления задачами, связанными с объектами недвижимости.
 *
 * <p>Предоставляет операции CRUD для задач, включая:</p>
 * <ul>
 *   <li>Создание задач</li>
 *   <li>Получение задач (по ID, по объекту, все задачи)</li>
 *   <li>Обновление задач</li>
 *   <li>Удаление задач</li>
 *   <li>Назначение и удаление ответственного пользователя</li>
 *   <li>Получение статистики задач по статусам</li>
 * </ul>
 *
 * <p>Все операции делегируются {@link TaskService}, который реализует бизнес-логику.</p>
 */
@Slf4j
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    /**
     * Сервис для работы с задачами, включая CRUD и бизнес-логику.
     */
    private final TaskService taskService;

    /**
     * Создаёт новую задачу.
     *
     * @param taskCreateDTO DTO с данными для создания задачи; обязательные поля должны быть заполнены
     * @return {@link ResponseEntity} с созданной задачей и HTTP статусом 201 Created
     */
    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody TaskCreateDTO taskCreateDTO) {
        TaskDTO taskDTO = taskService.createTask(taskCreateDTO);
        return ResponseEntity.created(URI.create("/tasks/" + taskDTO.getId())).body(taskDTO);
    }

    /**
     * Получает список всех задач.
     *
     * @return {@link ResponseEntity} со списком всех задач
     */
    @GetMapping
    public ResponseEntity<List<TaskDTO>> getAllTasks() {
        List<TaskDTO> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    /**
     * Получает задачу по её идентификатору.
     *
     * @param id идентификатор задачи; должен существовать в системе
     * @return {@link ResponseEntity} с найденной задачей
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    /**
     * Обновляет существующую задачу.
     *
     * @param id идентификатор обновляемой задачи; должен существовать в системе
     * @param taskUpdateDTO DTO с новыми данными задачи; обязательные поля должны быть заполнены
     * @return {@link ResponseEntity} с обновлённой задачей
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Long id, @Valid @RequestBody TaskUpdateDTO taskUpdateDTO) {
        return ResponseEntity.ok(taskService.updateTask(id, taskUpdateDTO));
    }

    /**
     * Удаляет задачу по её идентификатору.
     *
     * @param id идентификатор задачи; должен существовать в системе
     * @return {@link ResponseEntity} с HTTP статусом 204 No Content при успешном удалении
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Назначает ответственного пользователя на задачу.
     *
     * @param id идентификатор задачи; должен существовать в системе
     * @param request DTO с ID пользователя для назначения; пользователь должен существовать в системе
     * @return {@link ResponseEntity} с HTTP статусом 200 OK при успешном назначении
     */
    @PutMapping("/{id}/assign-responsible")
    public ResponseEntity<?> assignResponsible(@PathVariable Long id, @RequestBody AssignResponsibleRequest request) {
        taskService.assignResponsible(id, request.getUserId());
        return ResponseEntity.ok().build();
    }

    /**
     * Удаляет назначенного ответственного пользователя с задачи.
     *
     * @param id идентификатор задачи; должен существовать в системе
     * @return {@link ResponseEntity} с HTTP статусом 200 OK при успешном удалении
     */
    @PutMapping("/{id}/remove-responsible")
    public ResponseEntity<?> removeResponsible(@PathVariable Long id) {
        taskService.removeResponsible(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Получает все задачи для конкретного объекта недвижимости.
     *
     * @param objectId ID объекта недвижимости; должен существовать в системе
     * @return {@link ResponseEntity} со списком задач для указанного объекта
     */
    @GetMapping("/object/{objectId}")
    public ResponseEntity<List<TaskDTO>> getTasksByObjectId(@PathVariable Long objectId) {
        log.info("Получение задач для объекта с ID: {}", objectId);
        return ResponseEntity.ok(taskService.getTasksByObjectId(objectId));
    }

    /**
     * Получает статистику задач по статусам для объекта и всех его потомков.
     *
     * @param objectId ID объекта недвижимости; должен существовать в системе
     * @return {@link ResponseEntity} с {@link java.util.Map}, где ключ — статус задачи, значение — количество задач
     */
    @GetMapping("/object/{objectId}/status-stats")
    public ResponseEntity<java.util.Map<String, Integer>> getTaskStatusStats(@PathVariable Long objectId) {
        java.util.Map<String, Integer> stats = taskService.getTaskStatusStatsRecursive(objectId);
        return ResponseEntity.ok(stats);
    }
}
