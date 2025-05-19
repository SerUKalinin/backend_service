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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Контроллер для управления задачами, связанными с объектами недвижимости.
 * <p>
 * Предоставляет CRUD-операции: создание, получение, обновление и удаление задач.
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final ModelMapper modelMapper;

    /**
     * Создать новую задачу.
     *
     * @param taskCreateDTO DTO с данными для создания задачи
     * @return созданная задача
     */
    @PostMapping
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody TaskCreateDTO taskCreateDTO) {
        TaskDTO taskDTO = taskService.createTask(taskCreateDTO);
        return ResponseEntity.created(URI.create("/tasks/" + taskDTO.getId())).body(taskDTO);
    }

    /**
     * Получить список всех задач.
     *
     * @return список всех задач
     */
    @GetMapping
    public ResponseEntity<List<TaskDTO>> getAllTasks() {
        List<TaskDTO> tasks = taskService.getAllTasks();
        return ResponseEntity.ok(tasks);
    }

    /**
     * Получить задачу по её идентификатору.
     *
     * @param id идентификатор задачи
     * @return найденная задача
     */
    @GetMapping("/{id}")
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    /**
     * Обновить существующую задачу.
     *
     * @param id             идентификатор обновляемой задачи
     * @param taskUpdateDTO  DTO с новыми данными задачи
     * @return обновлённая задача
     */
    @PutMapping("/{id}")
    public ResponseEntity<TaskDTO> updateTask(@PathVariable Long id, @Valid @RequestBody TaskUpdateDTO taskUpdateDTO) {
        return ResponseEntity.ok(taskService.updateTask(id, taskUpdateDTO));
    }

    /**
     * Удалить задачу по идентификатору.
     *
     * @param id идентификатор задачи
     * @return HTTP 204 No Content в случае успешного удаления
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/assign-responsible")
    public ResponseEntity<?> assignResponsible(@PathVariable Long id, @RequestBody AssignResponsibleRequest request) {
        taskService.assignResponsible(id, request.getUserId());
        return ResponseEntity.ok().build();
    }

    @PutMapping("/{id}/remove-responsible")
    public ResponseEntity<?> removeResponsible(@PathVariable Long id) {
        taskService.removeResponsible(id);
        return ResponseEntity.ok().build();
    }

    /**
     * Получить все задачи для конкретного объекта недвижимости.
     *
     * @param objectId ID объекта недвижимости
     * @return список задач для данного объекта
     */
    @GetMapping("/object/{objectId}")
    public ResponseEntity<List<TaskDTO>> getTasksByObjectId(@PathVariable Long objectId) {
        log.info("Получение задач для объекта с ID: {}", objectId);
        return ResponseEntity.ok(taskService.getTasksByObjectId(objectId));
    }
}
