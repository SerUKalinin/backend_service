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

@Slf4j
@RestController
@RequestMapping("/real-estate-objects")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:63342")
public class ObjectController {

    private final ObjectService objectService;

    @GetMapping
    public ResponseEntity<List<ObjectResponseDto>> getAllObjects() {
        log.info("Получение всех объектов");
        List<ObjectResponseDto> objects = objectService.getAllObjects();
        return ResponseEntity.ok(objects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ObjectResponseDto> getObjectById(@PathVariable Long id) {
        log.info("Получение объекта с ID {}", id);
        ObjectResponseDto object = objectService.getObjectById(id);
        return ResponseEntity.ok(object);
    }

    @GetMapping("/by-type")
    public ResponseEntity<List<ObjectResponseDto>> getObjectsByType(@RequestParam ObjectType type) {
        log.info("Запрос на получение объектов типа: {}", type);
        return ResponseEntity.ok(objectService.getObjectsByType(type));
    }

    @GetMapping("/{id}/children")
    public ResponseEntity<List<ObjectResponseDto>> getChildren(@PathVariable Long id) {
        log.info("Запрос на получение дочерних объектов для ID: {}", id);
        return ResponseEntity.ok(objectService.getChildren(id));
    }

    @PostMapping
    public ResponseEntity<ObjectResponseDto> createObject(@RequestBody ObjectRequestDto objectDto) {
        log.info("Создание объекта: {}", objectDto);
        ObjectResponseDto createdObject = objectService.createObject(objectDto);
        return ResponseEntity.ok(createdObject);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ObjectResponseDto> updateObject(@PathVariable Long id, @RequestBody ObjectRequestDto objectDto) {
        log.info("Обновление объекта с ID {}: {}", id, objectDto);
        ObjectResponseDto updatedObject = objectService.updateObject(id, objectDto);
        return ResponseEntity.ok(updatedObject);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteObject(@PathVariable Long id) {
        log.info("Удаление объекта с ID: {}", id);
        objectService.deleteObject(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my-objects")
    public ResponseEntity<List<ObjectResponseDto>> getCurrentUserObjects() {
        log.info("Получение объектов текущего пользователя");
        return ResponseEntity.ok(objectService.getCurrentUserObjects());
    }

    @GetMapping("/by-responsible/{userId}")
    public ResponseEntity<List<ObjectResponseDto>> getObjectsByResponsibleUser(@PathVariable Long userId) {
        log.info("Получение объектов для ответственного пользователя: {}", userId);
        return ResponseEntity.ok(objectService.getObjectsByResponsibleUser(userId));
    }

    @PutMapping("/{id}/assign-responsible/{userId}")
    public ResponseEntity<ObjectResponseDto> assignResponsibleUser(
            @PathVariable Long id,
            @PathVariable Long userId) {
        log.info("Назначение ответственного пользователя {} для объекта {}", userId, id);
        ObjectResponseDto updatedObject = objectService.assignResponsibleUser(id, userId);
        return ResponseEntity.ok(updatedObject);
    }

    @PutMapping("/{id}/remove-responsible")
    public ResponseEntity<ObjectResponseDto> removeResponsibleUser(@PathVariable Long id) {
        log.info("Удаление ответственного пользователя для объекта {}", id);
        ObjectResponseDto updatedObject = objectService.removeResponsibleUser(id);
        return ResponseEntity.ok(updatedObject);
    }

    @GetMapping("/{id}/path")
    public ResponseEntity<List<ObjectResponseDto>> getObjectPath(@PathVariable Long id) {
        log.info("Получение пути (хлебных крошек) для объекта {}", id);
        return ResponseEntity.ok(objectService.getObjectPath(id));
    }
}
