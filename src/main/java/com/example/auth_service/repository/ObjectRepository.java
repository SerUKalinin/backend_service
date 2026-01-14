package com.example.auth_service.repository;

import com.example.auth_service.model.ObjectEntity;
import com.example.auth_service.model.ObjectType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для управления объектами недвижимости и другими универсальными объектами в системе.
 * Предоставляет методы для поиска объектов по различным критериям и проверки существования дочерних объектов.
 */
@Repository
public interface ObjectRepository extends JpaRepository<ObjectEntity, Long> {

    /**
     * Получает список объектов по идентификатору родительского объекта.
     *
     * @param parentId Идентификатор родителя. Не может быть null.
     * @return Список дочерних объектов. Пустой список, если дочерние объекты отсутствуют.
     */
    List<ObjectEntity> findByParentId(Long parentId);

    /**
     * Получает список объектов определённого типа.
     *
     * @param objectType Тип объекта. Не может быть null.
     * @return Список объектов указанного типа. Пустой список, если объекты отсутствуют.
     */
    List<ObjectEntity> findByObjectType(ObjectType objectType);

    /**
     * Получает список объектов, созданных конкретным пользователем.
     *
     * @param userId Идентификатор пользователя. Не может быть null.
     * @return Список объектов, созданных пользователем. Пустой список, если объекты отсутствуют.
     */
    List<ObjectEntity> findByCreatedById(Long userId);

    /**
     * Получает список объектов, за которые отвечает конкретный пользователь.
     *
     * @param userId Идентификатор пользователя. Не может быть null.
     * @return Список объектов, за которые отвечает пользователь. Пустой список, если объекты отсутствуют.
     */
    List<ObjectEntity> findByResponsibleUserId(Long userId);

    /**
     * Проверяет существование дочерних объектов для указанного родителя.
     *
     * @param parentId Идентификатор родительского объекта. Не может быть null.
     * @return true, если существуют дочерние объекты; false, если их нет.
     */
    boolean existsByParentId(Long parentId);
}
