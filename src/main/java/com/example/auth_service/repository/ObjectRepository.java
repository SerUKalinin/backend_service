package com.example.auth_service.repository;

import com.example.auth_service.model.ObjectEntity;
import com.example.auth_service.model.ObjectType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с сущностями {@link ObjectEntity}.
 * <p>
 * Предоставляет стандартные CRUD-операции через {@link JpaRepository}
 * и дополнительные методы для поиска объектов по родителю, типу и пользователям.
 * </p>
 */
@Repository
public interface ObjectRepository extends JpaRepository<ObjectEntity, Long> {

    /**
     * Находит все объекты, у которых задан родительский объект.
     *
     * @param parentId ID родительского объекта
     * @return список дочерних объектов
     */
    List<ObjectEntity> findByParentId(Long parentId);

    /**
     * Находит все объекты заданного типа.
     *
     * @param objectType тип объекта
     * @return список объектов указанного типа
     */
    List<ObjectEntity> findByObjectType(ObjectType objectType);

    /**
     * Находит все объекты, созданные указанным пользователем.
     *
     * @param userId ID пользователя
     * @return список объектов, созданных пользователем
     */
    List<ObjectEntity> findByCreatedById(Long userId);

    /**
     * Находит все объекты, за которые отвечает указанный пользователь.
     *
     * @param userId ID пользователя
     * @return список объектов, за которые пользователь отвечает
     */
    List<ObjectEntity> findByResponsibleUserId(Long userId);

    /**
     * Проверяет, существуют ли объекты с указанным родительским ID.
     *
     * @param parentId ID родительского объекта
     * @return true, если дочерние объекты существуют, иначе false
     */
    boolean existsByParentId(Long parentId);
}
