package com.example.auth_service.repository;

import com.example.auth_service.model.Task;
import com.example.auth_service.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByRealEstateObjectId(Long objectId);

    List<Task> findByStatusAndRealEstateObjectId(TaskStatus status, Long objectId);
}