package com.example.auth_service.controller;

import com.example.auth_service.dto.AssignResponsibleRequest;
import com.example.auth_service.dto.TaskCreateDTO;
import com.example.auth_service.dto.TaskDTO;
import com.example.auth_service.dto.TaskUpdateDTO;
import com.example.auth_service.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.Map;

/**
 * Контроллер для управления задачами, связанными с объектами недвижимости.
 *
 * <p>Предоставляет операции CRUD для задач:</p>
 * <ul>
 *   <li>Создание задач</li>
 *   <li>Получение задач (по ID, все задачи, по объекту)</li>
 *   <li>Обновление задач</li>
 *   <li>Удаление задач</li>
 *   <li>Назначение и удаление ответственного пользователя</li>
 *   <li>Получение статистики задач по статусам</li>
 * </ul>
 *
 * <p>Контроллер использует {@link TaskService} для делегирования всей бизнес-логики.</p>
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
     * @param taskCreateDTO DTO с данными для создания задачи
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
    public ResponseEntity<Page<TaskDTO>> getAllTasks(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(taskService.getAllTasks(PageRequest.of(page, size)));
    }

    /**
     * Получает задачу по её идентификатору.
     *
     * @param id идентификатор задачи
     * @return {@link ResponseEntity} с найденной задачей
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    /**
     * Обновляет существующую задачу.
     *
     * @param id идентификатор обновляемой задачи
     * @param taskUpdateDTO DTO с новыми данными задачи
     * @return {@link ResponseEntity} с обновлённой задачей
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Long id, @Valid @RequestBody TaskUpdateDTO taskUpdateDTO) {
        return ResponseEntity.ok(taskService.updateTask(id, taskUpdateDTO));
    }

    /**
     * Удаляет задачу по её идентификатору.
     *
     * @param id идентификатор задачи
     * @return {@link ResponseEntity} с HTTP статусом 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Назначает ответственного пользователя на задачу.
     *
     * @param id идентификатор задачи
     * @param request DTO с ID пользователя для назначения
     * @return {@link ResponseEntity} с HTTP статусом 200 OK
     */
    @PutMapping("/{id}/assign-responsible")
    public ResponseEntity<?> assignResponsible(@PathVariable Long id, @RequestBody AssignResponsibleRequest request) {
        taskService.assignResponsible(id, request.getUserId());
        return ResponseEntity.ok().build();
    }

    /**
     * Удаляет назначенного ответственного пользователя с задачи.
     *
     * @param id идентификатор задачи
     * @return {@link ResponseEntity} с HTTP статусом 200 OK
     */
    @PutMapping("/{id}/remove-responsible")
    public ResponseEntity<?> removeResponsible(@PathVariable Long id) {
        taskService.removeResponsible(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Получает все задачи для конкретного объекта недвижимости.
     *
     * @param objectId ID объекта недвижимости
     * @return {@link ResponseEntity} со списком задач для указанного объекта
     */
    @GetMapping("/object/{objectId}")
    public ResponseEntity<Page<TaskDTO>> getTasksByObjectId(
            @PathVariable Long objectId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(taskService.getTasksByObjectId(objectId, PageRequest.of(page, size)));
    }

    /**
     * Получает статистику задач по статусам для объекта и всех его потомков.
     *
     * @param objectId ID объекта недвижимости
     * @return {@link ResponseEntity} с {@link java.util.Map} где ключ — статус задачи, значение — количество задач
     */
    @GetMapping("/object/{objectId}/status-stats")
    public ResponseEntity<Map<String, Integer>> getTaskStatusStats(@PathVariable Long objectId) {
        java.util.Map<String, Integer> stats = taskService.getTaskStatusStatsRecursive(objectId);
        return ResponseEntity.ok(stats);
    }
}
