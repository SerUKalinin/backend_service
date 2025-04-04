package com.example.auth_service.repository;

import com.example.auth_service.model.ObjectEntity;
import com.example.auth_service.model.ObjectType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с объектами недвижимости.
 */
@Repository
public interface ObjectRepository extends JpaRepository<ObjectEntity, Long> {

    /**
     * Найти все объекты по-родительскому ID.
     */
    List<ObjectEntity> findByParentId(Long parentId);

    /**
     * Найти все объекты по типу.
     */
    List<ObjectEntity> findByObjectType(ObjectType objectType);

    /**
     * Найти все объекты, созданные пользователем с указанным ID.
     */
    List<ObjectEntity> findByCreatedById(Long userId);
}
