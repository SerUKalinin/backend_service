package com.example.auth_service.service;

import com.example.auth_service.model.ObjectEntity;
import com.example.auth_service.model.ObjectType;
import com.example.auth_service.repository.ObjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления объектами недвижимости.
 */
@Service
@RequiredArgsConstructor
public class ObjectService {

    private final ObjectRepository objectRepository;

    /**
     * Создать новый объект.
     */
    public ObjectEntity createObject(ObjectEntity object) {
        return objectRepository.save(object);
    }

    /**
     * Получить все объекты.
     */
    public List<ObjectEntity> getAllObjects() {
        return objectRepository.findAll();
    }

    /**
     * Получить объект по ID.
     */
    public Optional<ObjectEntity> getObjectsById(Long id) {
        return objectRepository.findById(id);
    }

    /**
     * Получить объекты по типу.
     */
    public List<ObjectEntity> getObjectsByType(ObjectType type) {
        return objectRepository.findByObjectType(type);
    }

    /**
     * Получить дочерние объекты.
     */
    public List<ObjectEntity> getChildren(Long parentId) {
        return objectRepository.findByParentId(parentId);
    }

    /**
     * Обновить объект.
     */
    public ObjectEntity updateObject(Long id, ObjectEntity updatedObject) {
        return objectRepository.findById(id).map(existing -> {
            existing.setName(updatedObject.getName());
            existing.setObjectType(updatedObject.getObjectType());
            existing.setParent(updatedObject.getParent());
            return objectRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Объект не найден"));
    }

    /**
     * Удалить объект.
     */
    public void deleteObject(Long id) {
        objectRepository.deleteById(id);
    }
}