package com.example.auth_service.service;

import com.example.auth_service.dto.ObjectRequestDto;
import com.example.auth_service.dto.ObjectResponseDto;
import com.example.auth_service.exception.ObjectNotFoundException;
import com.example.auth_service.exception.UserNotFoundException;
import com.example.auth_service.mapper.ObjectMapper;
import com.example.auth_service.model.ObjectEntity;
import com.example.auth_service.model.ObjectType;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.ObjectRepository;
import com.example.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Сервис для управления объектами доменной модели недвижимости.
 *
 * <p>Оркестрирует создание, обновление, удаление и получение объектов,
 * а также работу с иерархией объектов и ответственными пользователями.</p>
 *
 * <p><strong>Ответственность:</strong></p>
 * <ul>
 *   <li>CRUD-операции над объектами</li>
 *   <li>Поддержка иерархии объектов (parent / children)</li>
 *   <li>Назначение и снятие ответственных пользователей</li>
 *   <li>Формирование пути объекта (breadcrumbs)</li>
 *   <li>Работа с текущим аутентифицированным пользователем</li>
 * </ul>
 *
 * <p>Сервис не содержит логики представления и не управляет
 * авторизацией на уровне ролей — только доменные правила.</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ObjectService {

    private final ObjectRepository objectRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    /**
     * Возвращает текущего аутентифицированного пользователя.
     *
     * @return текущий пользователь
     * @throws UserNotFoundException если пользователь не авторизован
     *                               или не найден в базе данных
     */
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new UserNotFoundException("Пользователь не авторизован");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new UserNotFoundException("Текущий пользователь не найден"));
    }

    /**
     * Разрешает родительский объект по идентификатору.
     *
     * @param parentId идентификатор родительского объекта
     * @return родительский объект или {@code null}, если parentId не задан
     * @throws ObjectNotFoundException если родительский объект не найден
     */
    private ObjectEntity resolveParent(Long parentId) {
        if (parentId == null) return null;
        return objectRepository.findById(parentId)
                .orElseThrow(() -> new ObjectNotFoundException("Родительский объект не найден"));
    }

    /**
     * Разрешает ответственного пользователя по идентификатору.
     *
     * @param userId идентификатор пользователя
     * @return пользователь или {@code null}, если userId не задан
     * @throws UserNotFoundException если пользователь не найден
     */
    private User resolveResponsibleUser(Long userId) {
        if (userId == null) return null;
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Ответственный пользователь не найден"));
    }

    /**
     * Создаёт новый объект.
     *
     * <p>В процессе создания:</p>
     * <ol>
     *   <li>Проверяется корректность входных данных</li>
     *   <li>Разрешается родительский объект (если указан)</li>
     *   <li>Назначается ответственный пользователь (если указан)</li>
     *   <li>Фиксируется пользователь, создавший объект</li>
     * </ol>
     *
     * @param dto данные для создания объекта
     * @return созданный объект
     */
    public ObjectResponseDto createObject(ObjectRequestDto dto) {
        Assert.hasText(dto.getName(), "Имя объекта не должно быть пустым");
        Assert.notNull(dto.getObjectType(), "Тип объекта не должен быть null");

        ObjectEntity entity = objectMapper.toEntity(dto);
        entity.setParent(resolveParent(dto.getParentId()));
        entity.setResponsibleUser(resolveResponsibleUser(dto.getResponsibleUserId()));
        entity.setCreatedBy(getCurrentUser());

        ObjectEntity saved = objectRepository.save(entity);
        log.info("Объект создан с ID {}", saved.getId());

        return objectMapper.toDto(saved);
    }

    /**
     * Обновляет существующий объект.
     *
     * @param id идентификатор объекта
     * @param dto новые данные объекта
     * @return обновлённый объект
     * @throws ObjectNotFoundException если объект не найден
     */
    public ObjectResponseDto updateObject(Long id, ObjectRequestDto dto) {
        Assert.hasText(dto.getName(), "Имя объекта не должно быть пустым");
        Assert.notNull(dto.getObjectType(), "Тип объекта не должен быть null");

        ObjectEntity updated = objectRepository.findById(id)
                .map(existing -> {
                    objectMapper.updateEntityFromDto(dto, existing); // MapStruct обновляет name и type
                    existing.setParent(resolveParent(dto.getParentId()));
                    existing.setResponsibleUser(resolveResponsibleUser(dto.getResponsibleUserId()));
                    return objectRepository.save(existing);
                })
                .orElseThrow(() -> new ObjectNotFoundException("Объект не найден"));

        log.info("Объект с ID {} обновлён", id);
        return objectMapper.toDto(updated);
    }

    /**
     * Удаляет объект.
     *
     * <p>Удаление невозможно, если объект содержит дочерние элементы.</p>
     *
     * @param id идентификатор объекта
     * @throws ObjectNotFoundException если объект не найден
     * @throws IllegalStateException если объект имеет дочерние элементы
     */
    public void deleteObject(Long id) {
        ObjectEntity object = objectRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Объект не найден"));

        if (objectRepository.existsByParentId(id)) {
            throw new IllegalStateException("Объект имеет дочерние элементы и не может быть удален");
        }

        objectRepository.delete(object);
        log.info("Объект с ID {} удалён", id);
    }

    /**
     * Возвращает объект по идентификатору.
     *
     * @param id идентификатор объекта
     * @return объект
     * @throws ObjectNotFoundException если объект не найден
     */
    public ObjectResponseDto getObjectById(Long id) {
        return objectRepository.findById(id)
                .map(objectMapper::toDto)
                .orElseThrow(() -> new ObjectNotFoundException("Объект не найден"));
    }

    /**
     * Возвращает список всех объектов.
     *
     * @return список объектов
     */
    public List<ObjectResponseDto> getAllObjects() {
        return objectRepository.findAll()
                .stream()
                .map(objectMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Возвращает объекты по типу.
     *
     * @param type тип объекта
     * @return список объектов указанного типа
     */
    public List<ObjectResponseDto> getObjectsByType(ObjectType type) {
        return objectRepository.findByObjectType(type)
                .stream()
                .map(objectMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Возвращает дочерние объекты.
     *
     * @param parentId идентификатор родительского объекта
     * @return список дочерних объектов
     */
    public List<ObjectResponseDto> getChildren(Long parentId) {
        return objectRepository.findByParentId(parentId)
                .stream()
                .map(objectMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Возвращает объекты, созданные текущим пользователем.
     *
     * @return список объектов пользователя
     */
    public List<ObjectResponseDto> getCurrentUserObjects() {
        User user = getCurrentUser();
        return objectRepository.findByCreatedById(user.getId())
                .stream()
                .map(objectMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Возвращает объекты, за которые отвечает указанный пользователь.
     *
     * @param userId идентификатор пользователя
     * @return список объектов
     */
    public List<ObjectResponseDto> getObjectsByResponsibleUser(Long userId) {
        return objectRepository.findByResponsibleUserId(userId)
                .stream()
                .map(objectMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * Назначает ответственного пользователя для объекта.
     *
     * @param objectId идентификатор объекта
     * @param userId идентификатор пользователя
     * @return обновлённый объект
     */
    public ObjectResponseDto assignResponsibleUser(Long objectId, Long userId) {
        ObjectEntity object = objectRepository.findById(objectId)
                .orElseThrow(() -> new ObjectNotFoundException("Объект не найден"));

        object.setResponsibleUser(resolveResponsibleUser(userId));
        ObjectEntity saved = objectRepository.save(object);

        log.info("Ответственный пользователь {} назначен для объекта {}", userId, objectId);
        return objectMapper.toDto(saved);
    }

    /**
     * Удаляет ответственного пользователя у объекта.
     *
     * @param objectId идентификатор объекта
     * @return обновлённый объект
     */
    public ObjectResponseDto removeResponsibleUser(Long objectId) {
        ObjectEntity object = objectRepository.findById(objectId)
                .orElseThrow(() -> new ObjectNotFoundException("Объект не найден"));

        object.setResponsibleUser(null);
        ObjectEntity saved = objectRepository.save(object);

        log.info("Ответственный пользователь удалён для объекта {}", objectId);
        return objectMapper.toDto(saved);
    }

    /**
     * Возвращает путь объекта от корневого элемента до текущего.
     *
     * @param id идентификатор объекта
     * @return список объектов, представляющих путь
     */
    public List<ObjectResponseDto> getObjectPath(Long id) {
        List<ObjectResponseDto> path = new ArrayList<>();

        ObjectEntity current = objectRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Объект не найден"));

        while (current != null) {
            path.add(0, objectMapper.toDto(current));
            current = current.getParent();
        }

        return path;
    }
}
