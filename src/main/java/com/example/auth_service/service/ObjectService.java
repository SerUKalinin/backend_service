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

@Slf4j
@Service
@RequiredArgsConstructor
public class ObjectService {

    private final ObjectRepository objectRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    // -------------------- Current User --------------------
    private User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            throw new UserNotFoundException("Пользователь не авторизован");
        }
        return userRepository.findByUsername(auth.getName())
                .orElseThrow(() -> new UserNotFoundException("Текущий пользователь не найден"));
    }

    // -------------------- Helpers --------------------
    private ObjectEntity resolveParent(Long parentId) {
        if (parentId == null) return null;
        return objectRepository.findById(parentId)
                .orElseThrow(() -> new ObjectNotFoundException("Родительский объект не найден"));
    }

    private User resolveResponsibleUser(Long userId) {
        if (userId == null) return null;
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("Ответственный пользователь не найден"));
    }

    // -------------------- CRUD --------------------
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

    public ObjectResponseDto updateObject(Long id, ObjectRequestDto dto) {
        Assert.hasText(dto.getName(), "Имя объекта не должно быть пустым");
        Assert.notNull(dto.getObjectType(), "Тип объекта не должен быть null");

        ObjectEntity updated = objectRepository.findById(id)
                .map(existing -> {
                    existing.setName(dto.getName());
                    existing.setObjectType(dto.getObjectType());
                    existing.setParent(resolveParent(dto.getParentId()));
                    existing.setResponsibleUser(resolveResponsibleUser(dto.getResponsibleUserId()));
                    return objectRepository.save(existing);
                })
                .orElseThrow(() -> new ObjectNotFoundException("Объект не найден"));

        log.info("Объект с ID {} обновлён", id);
        return objectMapper.toDto(updated);
    }

    public void deleteObject(Long id) {
        ObjectEntity object = objectRepository.findById(id)
                .orElseThrow(() -> new ObjectNotFoundException("Объект не найден"));

        // Эффективная проверка наличия дочерних объектов
        if (objectRepository.existsByParentId(id)) {
            throw new IllegalStateException("Объект имеет дочерние элементы и не может быть удален");
        }

        objectRepository.delete(object);
        log.info("Объект с ID {} удалён", id);
    }

    // -------------------- Queries --------------------
    public ObjectResponseDto getObjectById(Long id) {
        return objectRepository.findById(id)
                .map(objectMapper::toDto)
                .orElseThrow(() -> new ObjectNotFoundException("Объект не найден"));
    }

    public List<ObjectResponseDto> getAllObjects() {
        return objectRepository.findAll()
                .stream()
                .map(objectMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ObjectResponseDto> getObjectsByType(ObjectType type) {
        return objectRepository.findByObjectType(type)
                .stream()
                .map(objectMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ObjectResponseDto> getChildren(Long parentId) {
        return objectRepository.findByParentId(parentId)
                .stream()
                .map(objectMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ObjectResponseDto> getCurrentUserObjects() {
        User user = getCurrentUser();
        return objectRepository.findByCreatedById(user.getId())
                .stream()
                .map(objectMapper::toDto)
                .collect(Collectors.toList());
    }

    public List<ObjectResponseDto> getObjectsByResponsibleUser(Long userId) {
        return objectRepository.findByResponsibleUserId(userId)
                .stream()
                .map(objectMapper::toDto)
                .collect(Collectors.toList());
    }

    // -------------------- Responsible Assignment --------------------
    public ObjectResponseDto assignResponsibleUser(Long objectId, Long userId) {
        ObjectEntity object = objectRepository.findById(objectId)
                .orElseThrow(() -> new ObjectNotFoundException("Объект не найден"));
        object.setResponsibleUser(resolveResponsibleUser(userId));
        ObjectEntity saved = objectRepository.save(object);
        log.info("Ответственный пользователь {} назначен для объекта {}", userId, objectId);
        return objectMapper.toDto(saved);
    }

    public ObjectResponseDto removeResponsibleUser(Long objectId) {
        ObjectEntity object = objectRepository.findById(objectId)
                .orElseThrow(() -> new ObjectNotFoundException("Объект не найден"));
        object.setResponsibleUser(null);
        ObjectEntity saved = objectRepository.save(object);
        log.info("Ответственный пользователь удалён для объекта {}", objectId);
        return objectMapper.toDto(saved);
    }

    // -------------------- Path (breadcrumbs) --------------------
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
