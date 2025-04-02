package com.example.auth_service.controller;

import com.example.auth_service.model.ObjectEntity;
import com.example.auth_service.model.ObjectType;
import com.example.auth_service.service.ObjectService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * Контроллер для управления объектами недвижимости.
 * Предоставляет CRUD-операции для работы с объектами.
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
     * @return список всех объектов
     */
    @GetMapping
    public ResponseEntity<List<ObjectEntity>> getAllObjects() {
        log.info("Получение всех объектов");
        return ResponseEntity.ok(objectService.getAllObjects());
    }

    /**
     * Получить объект недвижимости по его ID.
     *
     * @param id идентификатор объекта
     * @return объект недвижимости, если найден, иначе 404 Not Found
     */
    @GetMapping("/{id}")
    public ResponseEntity<ObjectEntity> getObjectsById(@PathVariable Long id) {
        log.info("Запрос на получение объекта с ID: {}", id);
        return objectService.getObjectById(id)
                .map(object -> {
                    log.info("Объект найден: {}", object);
                    return ResponseEntity.ok(object);
                })
                .orElseGet(() -> {
                    log.warn("Объект с ID {} не найден", id);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Получить объекты по их типу.
     *
     * @param type тип объекта {@link ObjectType}
     * @return список объектов данного типа
     */
    @GetMapping("/by-type")
    public ResponseEntity<List<ObjectEntity>> getObjectsByType(@RequestParam ObjectType type) {
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
    public ResponseEntity<List<ObjectEntity>> getChildren(@PathVariable Long id) {
        log.info("Запрос на получение дочерних объектов для ID: {}", id);
        return ResponseEntity.ok(objectService.getChildren(id));
    }

    /**
     * Создать новый объект недвижимости.
     *
     * @param object данные нового объекта недвижимости
     * @return созданный объект
     */
    @PostMapping
    public ResponseEntity<ObjectEntity> createObject(@RequestBody ObjectEntity object) {
        log.info("Запрос на создание объекта: {}", object);
        ObjectEntity createdObject = objectService.createObject(object);
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
    public ResponseEntity<ObjectEntity> updateObject(@PathVariable Long id, @RequestBody ObjectEntity object) {
        log.info("Запрос на обновление объекта с ID {}: {}", id, object);
        ObjectEntity updatedObject = objectService.updateObject(id, object);
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
}
