package com.example.auth_service.repository;

import com.example.auth_service.model.Task;
import com.example.auth_service.model.TaskStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    // Все задачи с пагинацией и сразу подтянутыми связями
    @EntityGraph(attributePaths = {"createdBy", "responsibleUser", "realEstateObject", "attachments"})
    Page<Task> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"createdBy", "responsibleUser", "realEstateObject", "attachments"})
    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {"createdBy", "responsibleUser", "realEstateObject", "attachments"})
    Page<Task> findByRealEstateObjectId(Long objectId, Pageable pageable);

    @EntityGraph(attributePaths = {"createdBy", "responsibleUser", "realEstateObject", "attachments"})
    Page<Task> findByStatusAndRealEstateObjectId(TaskStatus status, Long objectId, Pageable pageable);

    boolean existsByTitleAndRealEstateObjectId(String title, Long objectId);

    @EntityGraph(attributePaths = {"createdBy", "responsibleUser", "realEstateObject", "attachments"})
    Page<Task> findByRealEstateObjectIdIn(List<Long> objectIds, Pageable pageable);
}
