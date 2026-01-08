package com.example.auth_service.repository;

import com.example.auth_service.model.ObjectEntity;
import com.example.auth_service.model.ObjectType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObjectRepository extends JpaRepository<ObjectEntity, Long> {

    List<ObjectEntity> findByParentId(Long parentId);

    List<ObjectEntity> findByObjectType(ObjectType objectType);

    List<ObjectEntity> findByCreatedById(Long userId);

    List<ObjectEntity> findByResponsibleUserId(Long userId);

    // Новый метод для проверки существования дочерних объектов
    boolean existsByParentId(Long parentId);
}
