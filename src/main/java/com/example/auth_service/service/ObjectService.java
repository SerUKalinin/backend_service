package com.example.auth_service.service;

import com.example.auth_service.exception.ObjectNotFoundException;
import com.example.auth_service.model.ObjectEntity;
import com.example.auth_service.model.ObjectType;
import com.example.auth_service.repository.ObjectRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления объектами недвижимости.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ObjectService {

    private final ObjectRepository objectRepository;

    /**
     * Создать новый объект.
     *
     * @param object объект для создания
     * @return созданный объект
     * @throws IllegalArgumentException если объект null или некорректен
     */
    public ObjectEntity createObject(ObjectEntity object) {
        Assert.notNull(object, "Объект не должен быть null");
        Assert.hasText(object.getName(), "Имя объекта не должно быть пустым");
        Assert.notNull(object.getObjectType(), "Тип объекта не должен быть null");

        log.info("Создание объекта: {}", object);
        return objectRepository.save(object);
    }

    /**
     * Получить все объекты.
     *
     * @return список объектов
     */
    public List<ObjectEntity> getAllObjects() {
        log.info("Получение всех объектов");
        return objectRepository.findAll();
    }

    /**
     * Получить объект по ID.
     *
     * @param id идентификатор объекта
     * @return объект, если найден
     * @throws ObjectNotFoundException если объект не найден
     */
    public ObjectEntity getObjectById(Long id) {
        log.info("Запрос на получение объекта с ID: {}", id);
        return objectRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Объект с ID {} не найден", id);
                    return new ObjectNotFoundException("Объект не найден");
                });
    }

    /**
     * Получить объекты по типу.
     *
     * @param type тип объекта
     * @return список объектов данного типа
     */
    public List<ObjectEntity> getObjectsByType(ObjectType type) {
        log.info("Запрос на получение объектов типа: {}", type);
        return objectRepository.findByObjectType(type);
    }

    /**
     * Получить дочерние объекты.
     *
     * @param parentId ID родительского объекта
     * @return список дочерних объектов
     */
    public List<ObjectEntity> getChildren(Long parentId) {
        log.info("Запрос на получение дочерних объектов для ID: {}", parentId);
        return objectRepository.findByParentId(parentId);
    }

    /**
     * Обновить объект.
     *
     * @param id            идентификатор объекта
     * @param updatedObject обновленные данные объекта
     * @return обновленный объект
     * @throws ObjectNotFoundException если объект не найден
     * @throws IllegalArgumentException если обновленные данные некорректны
     */
    public ObjectEntity updateObject(Long id, ObjectEntity updatedObject) {
        Assert.notNull(updatedObject, "Обновленный объект не должен быть null");
        Assert.hasText(updatedObject.getName(), "Имя объекта не должно быть пустым");
        Assert.notNull(updatedObject.getObjectType(), "Тип объекта не должен быть null");

        log.info("Обновление объекта с ID {}: {}", id, updatedObject);

        return objectRepository.findById(id)
                .map(existing -> {
                    existing.setName(updatedObject.getName());
                    existing.setObjectType(updatedObject.getObjectType());
                    existing.setParent(updatedObject.getParent());
                    ObjectEntity savedObject = objectRepository.save(existing);
                    log.info("Объект обновлен: {}", savedObject);
                    return savedObject;
                })
                .orElseThrow(() -> {
                    log.warn("Объект с ID {} не найден", id);
                    return new ObjectNotFoundException("Объект не найден");
                });
    }

    /**
     * Удалить объект.
     *
     * @param id идентификатор удаляемого объекта
     * @throws ObjectNotFoundException если объект не найден
     */
    public void deleteObject(Long id) {
        log.info("Запрос на удаление объекта с ID: {}", id);

        ObjectEntity object = objectRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Попытка удаления несуществующего объекта с ID {}", id);
                    return new ObjectNotFoundException("Объект не найден");
                });

        // Проверяем, есть ли у объекта дочерние элементы
        List<ObjectEntity> children = objectRepository.findByParentId(id);
        if (!children.isEmpty()) {
            log.warn("Объект с ID {} имеет дочерние объекты и не может быть удален", id);
            throw new IllegalStateException("Удаление невозможно: у объекта есть дочерние элементы");
        }

        objectRepository.delete(object);
        log.info("Объект с ID {} успешно удален", id);
    }
}
