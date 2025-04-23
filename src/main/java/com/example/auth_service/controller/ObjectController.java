package com.example.auth_service.controller;

import com.example.auth_service.dto.ObjectResponseDto;
import com.example.auth_service.model.ObjectType;
import com.example.auth_service.service.ObjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Контроллер для управления объектами недвижимости.
 * <p>
 * Предоставляет CRUD-операции, а также функциональность назначения ответственных пользователей
 * и фильтрации объектов по различным критериям (тип, родитель, создатель).
 * </p>
 */
@Slf4j
@RestController
@RequestMapping("/real-estate-objects")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:63342")
public class ObjectController {

    private final ObjectService objectService;

    /**
     * Получить все объекты недвижимости.
     *
     * @return список всех объектов недвижимости
     */
    @GetMapping
    public ResponseEntity<List<ObjectResponseDto>> getAllObjects() {
        log.info("Получение всех объектов");
        List<ObjectResponseDto> objects = objectService.getAllObjects();
        return ResponseEntity.ok(objects);
    }

    /**
     * Получить объект недвижимости по его ID.
     *
     * @param id идентификатор объекта
     * @return объект недвижимости, если найден
     */
    @GetMapping("/{id}")
    public ResponseEntity<ObjectResponseDto> getObjectById(@PathVariable Long id) {
        return objectService.getObjectById(id);
    }

    /**
     * Получить объекты по их типу.
     *
     * @param type тип объекта недвижимости (например, ЭТАЖ, КВАРТИРА и т.д.)
     * @return список объектов данного типа
     */
    @GetMapping("/by-type")
    public ResponseEntity<List<ObjectResponseDto>> getObjectsByType(@RequestParam ObjectType type) {
        log.info("Запрос на получение объектов типа: {}", type);
        return ResponseEntity.ok(objectService.getObjectsByType(type));
    }

    /**
     * Получить дочерние объекты по ID родительского объекта.
     *
     * @param id идентификатор родительского объекта
     * @return список дочерних объектов
     */
    @GetMapping("/{id}/children")
    public ResponseEntity<List<ObjectResponseDto>> getChildren(@PathVariable Long id) {
        log.info("Запрос на получение дочерних объектов для ID: {}", id);
        return ResponseEntity.ok(objectService.getChildren(id));
    }

    /**
     * Создать новый объект недвижимости.
     *
     * @param objectDto данные нового объекта недвижимости
     * @return созданный объект
     */
    @PostMapping
    public ResponseEntity<ObjectResponseDto> createObject(@RequestBody ObjectResponseDto objectDto) {
        log.info("Запрос на создание объекта: {}", objectDto);
        ObjectResponseDto createdObject = objectService.createObject(objectDto);
        log.info("Объект создан: {}", createdObject);
        return ResponseEntity.ok(createdObject);
    }

    /**
     * Обновить существующий объект недвижимости.
     *
     * @param id     идентификатор обновляемого объекта
     * @param object новые данные объекта
     * @return обновленный объект недвижимости
     */
    @PutMapping("/{id}")
    public ResponseEntity<ObjectResponseDto> updateObject(@PathVariable Long id, @RequestBody ObjectResponseDto object) {
        log.info("Запрос на обновление объекта с ID {}: {}", id, object);
        ObjectResponseDto updatedObject = objectService.updateObject(id, object);
        log.info("Объект обновлен: {}", updatedObject);
        return ResponseEntity.ok(updatedObject);
    }

    /**
     * Удалить объект недвижимости по его ID.
     *
     * @param id идентификатор удаляемого объекта
     * @return HTTP 204 No Content в случае успешного удаления
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteObject(@PathVariable Long id) {
        log.info("Запрос на удаление объекта с ID: {}", id);
        objectService.deleteObject(id);
        log.info("Объект с ID {} удален", id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Получить все объекты, созданные текущим пользователем.
     *
     * @return список объектов, созданных текущим пользователем
     */
    @GetMapping("/my-objects")
    public ResponseEntity<List<ObjectResponseDto>> getCurrentUserObjects() {
        log.info("Запрос на получение объектов текущего пользователя");
        List<ObjectResponseDto> objects = objectService.getCurrentUserObjects();
        return ResponseEntity.ok(objects);
    }

    /**
     * Получить все объекты, за которые отвечает указанный пользователь.
     *
     * @param userId идентификатор пользователя
     * @return список объектов, за которые отвечает пользователь
     */
    @GetMapping("/by-responsible/{userId}")
    public ResponseEntity<List<ObjectResponseDto>> getObjectsByResponsibleUser(@PathVariable Long userId) {
        log.info("Запрос на получение объектов для ответственного пользователя: {}", userId);
        List<ObjectResponseDto> objects = objectService.getObjectsByResponsibleUser(userId);
        return ResponseEntity.ok(objects);
    }

    /**
     * Назначить ответственного пользователя за объект недвижимости.
     *
     * @param id     идентификатор объекта
     * @param userId идентификатор пользователя
     * @return обновлённый объект недвижимости
     */
    @PutMapping("/{id}/assign-responsible/{userId}")
    public ResponseEntity<ObjectResponseDto> assignResponsibleUser(
            @PathVariable Long id,
            @PathVariable Long userId) {
        log.info("Запрос на назначение ответственного пользователя {} для объекта {}", userId, id);
        ObjectResponseDto updatedObject = objectService.assignResponsibleUser(id, userId);
        return ResponseEntity.ok(updatedObject);
    }

    /**
     * Удалить назначенного ответственного пользователя из объекта.
     *
     * @param id идентификатор объекта
     * @return обновлённый объект недвижимости
     */
    @PutMapping("/{id}/remove-responsible")
    public ResponseEntity<ObjectResponseDto> removeResponsibleUser(@PathVariable Long id) {
        log.info("Запрос на удаление ответственного пользователя для объекта {}", id);
        ObjectResponseDto updatedObject = objectService.removeResponsibleUser(id);
        return ResponseEntity.ok(updatedObject);
    }
}
