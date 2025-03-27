package com.example.auth_service.controller;

import com.example.auth_service.model.ObjectEntity;
import com.example.auth_service.model.ObjectType;
import com.example.auth_service.service.ObjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/objects")
@RequiredArgsConstructor
public class ObjectController {

    private final ObjectService objectService;

    /**
     * Получить все объекты.
     */
    @GetMapping
    public ResponseEntity<List<ObjectEntity>> getAllObjects() {
        return ResponseEntity.ok(objectService.getAllObjects());
    }

    /**
     * Получить объект по ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ObjectEntity> getObjectById(@PathVariable Long id) {
        return objectService.getObjectsById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Получить объекты по типу.
     */
    @GetMapping("/by-type")
    public ResponseEntity<List<ObjectEntity>> getObjectsByType(@RequestParam ObjectType type) {
        return ResponseEntity.ok(objectService.getObjectsByType(type));
    }

    /**
     * Получить дочерние объекты.
     */
    @GetMapping("/{id}/children")
    public ResponseEntity<List<ObjectEntity>> getChildren(@PathVariable Long id) {
        return ResponseEntity.ok(objectService.getChildren(id));
    }

    /**
     * Создать новый объект.
     */
    @PostMapping
    public ResponseEntity<ObjectEntity> createObject(@RequestBody ObjectEntity object) {
        return ResponseEntity.ok(objectService.createObject(object));
    }

    /**
     * Обновить объект.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ObjectEntity> updateObject(@PathVariable Long id, @RequestBody ObjectEntity object) {
        return ResponseEntity.ok(objectService.updateObject(id, object));
    }

    /**
     * Удалить объект.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteObject(@PathVariable Long id) {
        objectService.deleteObject(id);
        return ResponseEntity.noContent().build();
    }

}
