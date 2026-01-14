package com.example.auth_service.controller;

import com.example.auth_service.dto.ObjectRequestDto;
import com.example.auth_service.dto.ObjectResponseDto;
import com.example.auth_service.model.ObjectType;
import com.example.auth_service.service.ObjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST-контроллер для управления объектами недвижимости.
 *
 * Предоставляет методы для CRUD операций, получения дочерних объектов,
 * управления ответственными пользователями и получения объектов по текущему пользователю.
 * Все операции делегируются {@link ObjectService}, который реализует бизнес-логику работы
 * с объектами недвижимости в системе.
 */
@Slf4j
@RestController
@RequestMapping("/real-estate-objects")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:63342")
public class ObjectController {

    /**
     * Сервис для управления объектами недвижимости.
     */
    private final ObjectService objectService;

    /**
     * Получение списка всех объектов недвижимости.
     *
     * @return {@link ResponseEntity} со списком {@link ObjectResponseDto} всех объектов
     */
    @GetMapping
    public ResponseEntity<List<ObjectResponseDto>> getAllObjects() {
        log.info("Получение всех объектов");
        List<ObjectResponseDto> objects = objectService.getAllObjects();
        return ResponseEntity.ok(objects);
    }

    /**
     * Получение объекта по его идентификатору.
     *
     * @param id ID объекта; должен существовать в системе
     * @return {@link ResponseEntity} с {@link ObjectResponseDto} объекта
     */
    @GetMapping("/{id}")
    public ResponseEntity<ObjectResponseDto> getObjectById(@PathVariable Long id) {
        log.info("Получение объекта с ID {}", id);
        ObjectResponseDto object = objectService.getObjectById(id);
        return ResponseEntity.ok(object);
    }

    /**
     * Получение объектов по типу.
     *
     * @param type Тип объекта ({@link ObjectType}); фильтр для выборки
     * @return {@link ResponseEntity} со списком объектов указанного типа
     */
    @GetMapping("/by-type")
    public ResponseEntity<List<ObjectResponseDto>> getObjectsByType(@RequestParam ObjectType type) {
        log.info("Запрос на получение объектов типа: {}", type);
        return ResponseEntity.ok(objectService.getObjectsByType(type));
    }

    /**
     * Получение дочерних объектов для указанного объекта.
     *
     * @param id ID родительского объекта; должен существовать в системе
     * @return {@link ResponseEntity} со списком дочерних объектов
     */
    @GetMapping("/{id}/children")
    public ResponseEntity<List<ObjectResponseDto>> getChildren(@PathVariable Long id) {
        log.info("Запрос на получение дочерних объектов для ID: {}", id);
        return ResponseEntity.ok(objectService.getChildren(id));
    }

    /**
     * Создание нового объекта недвижимости.
     *
     * @param objectDto DTO с данными нового объекта; все обязательные поля должны быть заполнены
     * @return {@link ResponseEntity} с {@link ObjectResponseDto} созданного объекта
     */
    @PostMapping
    public ResponseEntity<ObjectResponseDto> createObject(@RequestBody ObjectRequestDto objectDto) {
        log.info("Создание объекта: {}", objectDto);
        ObjectResponseDto createdObject = objectService.createObject(objectDto);
        return ResponseEntity.ok(createdObject);
    }

    /**
     * Обновление существующего объекта по идентификатору.
     *
     * @param id        ID объекта; должен существовать в системе
     * @param objectDto DTO с обновлёнными данными объекта
     * @return {@link ResponseEntity} с {@link ObjectResponseDto} обновлённого объекта
     */
    @PutMapping("/{id}")
    public ResponseEntity<ObjectResponseDto> updateObject(@PathVariable Long id, @RequestBody ObjectRequestDto objectDto) {
        log.info("Обновление объекта с ID {}: {}", id, objectDto);
        ObjectResponseDto updatedObject = objectService.updateObject(id, objectDto);
        return ResponseEntity.ok(updatedObject);
    }

    /**
     * Удаление объекта по идентификатору.
     *
     * @param id ID объекта; должен существовать в системе
     * @return {@link ResponseEntity} без содержимого (HTTP 204), если удаление прошло успешно
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteObject(@PathVariable Long id) {
        log.info("Удаление объекта с ID: {}", id);
        objectService.deleteObject(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Получение объектов текущего пользователя.
     *
     * @return {@link ResponseEntity} со списком объектов, за которые отвечает текущий пользователь
     */
    @GetMapping("/my-objects")
    public ResponseEntity<List<ObjectResponseDto>> getCurrentUserObjects() {
        log.info("Получение объектов текущего пользователя");
        return ResponseEntity.ok(objectService.getCurrentUserObjects());
    }

    /**
     * Получение объектов по ответственному пользователю.
     *
     * @param userId ID ответственного пользователя; должен существовать в системе
     * @return {@link ResponseEntity} со списком объектов, за которые отвечает указанный пользователь
     */
    @GetMapping("/by-responsible/{userId}")
    public ResponseEntity<List<ObjectResponseDto>> getObjectsByResponsibleUser(@PathVariable Long userId) {
        log.info("Получение объектов для ответственного пользователя: {}", userId);
        return ResponseEntity.ok(objectService.getObjectsByResponsibleUser(userId));
    }

    /**
     * Назначение ответственного пользователя для объекта.
     *
     * @param id     ID объекта; должен существовать в системе
     * @param userId ID пользователя; должен существовать в системе
     * @return {@link ResponseEntity} с {@link ObjectResponseDto} обновлённого объекта
     */
    @PutMapping("/{id}/assign-responsible/{userId}")
    public ResponseEntity<ObjectResponseDto> assignResponsibleUser(
            @PathVariable Long id,
            @PathVariable Long userId) {
        log.info("Назначение ответственного пользователя {} для объекта {}", userId, id);
        ObjectResponseDto updatedObject = objectService.assignResponsibleUser(id, userId);
        return ResponseEntity.ok(updatedObject);
    }

    /**
     * Удаление ответственного пользователя с объекта.
     *
     * @param id ID объекта; должен существовать в системе
     * @return {@link ResponseEntity} с {@link ObjectResponseDto} обновлённого объекта
     */
    @PutMapping("/{id}/remove-responsible")
    public ResponseEntity<ObjectResponseDto> removeResponsibleUser(@PathVariable Long id) {
        log.info("Удаление ответственного пользователя для объекта {}", id);
        ObjectResponseDto updatedObject = objectService.removeResponsibleUser(id);
        return ResponseEntity.ok(updatedObject);
    }

    /**
     * Получение пути (хлебных крошек) до объекта.
     *
     * @param id ID объекта; должен существовать в системе
     * @return {@link ResponseEntity} со списком объектов, представляющих путь от корня до указанного объекта
     */
    @GetMapping("/{id}/path")
    public ResponseEntity<List<ObjectResponseDto>> getObjectPath(@PathVariable Long id) {
        log.info("Получение пути (хлебных крошек) для объекта {}", id);
        return ResponseEntity.ok(objectService.getObjectPath(id));
    }
}
