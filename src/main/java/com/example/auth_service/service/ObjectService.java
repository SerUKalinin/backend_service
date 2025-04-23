package com.example.auth_service.service;

import com.example.auth_service.dto.ObjectResponseDto;
import com.example.auth_service.exception.ObjectNotFoundException;
import com.example.auth_service.mapper.ObjectMapper;
import com.example.auth_service.model.ObjectEntity;
import com.example.auth_service.model.ObjectType;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.ObjectRepository;
import com.example.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Сервис для управления объектами недвижимости.
 * Предоставляет методы для создания, обновления, удаления и получения объектов недвижимости,
 * а также для назначения и удаления ответственного пользователя.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ObjectService {

    private final ObjectRepository objectRepository;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    /**
     * Создает новый объект недвижимости.
     *
     * @param objectDto DTO с данными для создания объекта
     * @return DTO созданного объекта
     * @throws IllegalArgumentException если объект или его поля невалидны
     */
    public ObjectResponseDto createObject(ObjectResponseDto objectDto) {
        Assert.notNull(objectDto, "Объект не должен быть null");
        Assert.hasText(objectDto.getName(), "Имя объекта не должно быть пустым");
        Assert.notNull(objectDto.getObjectType(), "Тип объекта не должен быть null");

        log.info("Создание объекта: {}", objectDto);

        // Преобразуем DTO в сущность
        ObjectEntity entity = objectMapper.toEntity(objectDto);

        // Если родительский объект задан (parentId != null), то связываем с родителем
        if (objectDto.getParentId() != null) {
            Optional<ObjectEntity> parentEntity = objectRepository.findById(objectDto.getParentId());
            parentEntity.ifPresent(entity::setParent);  // Устанавливаем родителя, если он найден
        }

        // Устанавливаем создателя объекта
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<User> currentUser = userRepository.findByUsername(username);
            currentUser.ifPresent(entity::setCreatedBy);
        }

        // Устанавливаем ответственного пользователя, если указан
        if (objectDto.getResponsibleUserId() != null) {
            User responsibleUser = userRepository.findById(objectDto.getResponsibleUserId())
                    .orElseThrow(() -> new RuntimeException("Ответственный пользователь не найден"));
            entity.setResponsibleUser(responsibleUser);
        }

        // Сохраняем объект в базу данных
        ObjectEntity savedEntity = objectRepository.save(entity);
        log.info("Объект успешно создан с ID: {}", savedEntity.getId());

        // Преобразуем сущность обратно в DTO и возвращаем
        return objectMapper.toDto(savedEntity);
    }

    /**
     * Получает все объекты недвижимости.
     *
     * @return Список всех объектов недвижимости в формате DTO
     */
    public List<ObjectResponseDto> getAllObjects() {
        log.info("Получение всех объектов");
        List<ObjectEntity> entities = objectRepository.findAll();
        // Преобразуем список сущностей в список DTO
        return entities.stream()
                .map(objectMapper::toDto)  // Преобразуем каждую сущность в DTO
                .collect(Collectors.toList());
    }

    /**
     * Получает объект недвижимости по его идентификатору.
     *
     * @param id идентификатор объекта
     * @return ResponseEntity с объектом недвижимости в формате DTO
     * @throws ObjectNotFoundException если объект не найден
     */
    public ResponseEntity<ObjectResponseDto> getObjectById(Long id) {
        log.info("Запрос на получение объекта с ID: {}", id);
        return objectRepository.findById(id)
                .map(object -> {
                    ObjectResponseDto objectDto = objectMapper.toDto(object); // Преобразуем сущность в DTO
                    log.info("Объект найден: {}", objectDto);
                    return ResponseEntity.ok(objectDto);
                })
                .orElseGet(() -> {
                    log.warn("Объект с ID {} не найден", id);
                    return ResponseEntity.notFound().build();
                });
    }

    /**
     * Получает объекты недвижимости по их типу.
     *
     * @param type тип объекта недвижимости
     * @return Список объектов данного типа в формате DTO
     */
    public List<ObjectResponseDto> getObjectsByType(ObjectType type) {
        log.info("Запрос на получение объектов типа: {}", type);
        List<ObjectEntity> entities = objectRepository.findByObjectType(type);
        return entities.stream()
                .map(objectMapper::toDto)  // Преобразуем сущности в DTO
                .collect(Collectors.toList());
    }

    /**
     * Получает дочерние объекты для указанного родительского объекта.
     *
     * @param parentId идентификатор родительского объекта
     * @return Список дочерних объектов в формате DTO
     */
    public List<ObjectResponseDto> getChildren(Long parentId) {
        log.info("Запрос на получение дочерних объектов для ID: {}", parentId);
        List<ObjectEntity> entities = objectRepository.findByParentId(parentId);
        return entities.stream()
                .map(objectMapper::toDto)  // Преобразуем сущности в DTO
                .collect(Collectors.toList());
    }

    /**
     * Обновляет объект недвижимости.
     *
     * @param id идентификатор объекта для обновления
     * @param updatedObject DTO с новыми данными объекта
     * @return Обновленный объект недвижимости в формате DTO
     * @throws ObjectNotFoundException если объект с указанным ID не найден
     */
    public ObjectResponseDto updateObject(Long id, ObjectResponseDto updatedObject) {
        Assert.notNull(updatedObject, "Обновленный объект не должен быть null");
        Assert.hasText(updatedObject.getName(), "Имя объекта не должно быть пустым");
        Assert.notNull(updatedObject.getObjectType(), "Тип объекта не должен быть null");

        log.info("Обновление объекта с ID {}: {}", id, updatedObject);

        return objectRepository.findById(id)
                .map(existing -> {
                    existing.setName(updatedObject.getName());
                    existing.setObjectType(updatedObject.getObjectType());

                    // Обновляем родительский объект
                    if (updatedObject.getParentId() != null) {
                        ObjectEntity parent = objectRepository.findById(updatedObject.getParentId())
                                .orElseThrow(() -> new ObjectNotFoundException("Родительский объект не найден"));
                        existing.setParent(parent);
                    } else {
                        existing.setParent(null);
                    }

                    // Обновляем ответственного пользователя
                    if (updatedObject.getResponsibleUserId() != null) {
                        User responsibleUser = userRepository.findById(updatedObject.getResponsibleUserId())
                                .orElseThrow(() -> new RuntimeException("Ответственный пользователь не найден"));
                        existing.setResponsibleUser(responsibleUser);
                    } else {
                        existing.setResponsibleUser(null);
                    }

                    ObjectEntity savedObject = objectRepository.save(existing);
                    log.info("Объект обновлен: {}", savedObject);
                    return objectMapper.toDto(savedObject);
                })
                .orElseThrow(() -> {
                    log.warn("Объект с ID {} не найден", id);
                    return new ObjectNotFoundException("Объект не найден");
                });
    }

    /**
     * Удаляет объект недвижимости.
     *
     * @param id идентификатор объекта для удаления
     * @throws ObjectNotFoundException если объект с указанным ID не найден
     * @throws IllegalStateException если объект имеет дочерние объекты и не может быть удален
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

    /**
     * Получает все объекты, созданные текущим пользователем.
     *
     * @return Список объектов, созданных текущим пользователем
     */
    public List<ObjectResponseDto> getCurrentUserObjects() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            String username = authentication.getName();
            Optional<User> currentUser = userRepository.findByUsername(username);

            if (currentUser.isPresent()) {
                log.info("Получение объектов, созданных пользователем: {}", currentUser.get().getId());
                List<ObjectEntity> entities = objectRepository.findByCreatedById(currentUser.get().getId());
                return entities.stream()
                        .map(objectMapper::toDto)
                        .collect(Collectors.toList());
            }
        }
        return List.of();
    }

    /**
     * Получает все объекты, назначенные ответственным пользователем.
     *
     * @param userId идентификатор пользователя
     * @return Список объектов, назначенных ответственным пользователем
     */
    public List<ObjectResponseDto> getObjectsByResponsibleUser(Long userId) {
        log.info("Получение объектов для ответственного пользователя с ID: {}", userId);
        List<ObjectEntity> entities = objectRepository.findByResponsibleUserId(userId);
        return entities.stream()
                .map(objectMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Назначает ответственным пользователем для указанного объекта.
     *
     * @param objectId идентификатор объекта
     * @param userId идентификатор пользователя
     * @return Обновленный объект с назначенным ответственным пользователем
     * @throws ObjectNotFoundException если объект не найден
     * @throws RuntimeException если пользователь не найден
     */
    public ObjectResponseDto assignResponsibleUser(Long objectId, Long userId) {
        log.info("Назначение ответственного пользователя {} для объекта {}", userId, objectId);

        ObjectEntity object = objectRepository.findById(objectId)
                .orElseThrow(() -> new ObjectNotFoundException("Объект не найден"));

        User responsibleUser = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Ответственный пользователь не найден"));

        object.setResponsibleUser(responsibleUser);
        ObjectEntity savedObject = objectRepository.save(object);

        return objectMapper.toDto(savedObject);
    }

    /**
     * Удаляет ответственного пользователя для объекта недвижимости.
     *
     * @param objectId идентификатор объекта
     * @return Обновленный объект с удаленным ответственным пользователем
     * @throws ObjectNotFoundException если объект не найден
     */
    public ObjectResponseDto removeResponsibleUser(Long objectId) {
        log.info("Удаление ответственного пользователя для объекта {}", objectId);

        ObjectEntity object = objectRepository.findById(objectId)
                .orElseThrow(() -> new ObjectNotFoundException("Объект не найден"));

        object.setResponsibleUser(null);
        ObjectEntity savedObject = objectRepository.save(object);

        return objectMapper.toDto(savedObject);
    }
}
