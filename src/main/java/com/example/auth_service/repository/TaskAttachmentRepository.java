package com.example.auth_service.repository;

import com.example.auth_service.model.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с вложениями задач.
 */
@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {
    
    /**
     * Находит все вложения для указанной задачи.
     *
     * @param taskId идентификатор задачи
     * @return список вложений для данной задачи
     */
    List<TaskAttachment> findByTaskId(Long taskId);

    /**
     * Находит вложение по пути к файлу.
     *
     * @param filePath путь к файлу
     * @return Optional с найденным вложением или пустой Optional, если вложение не найдено
     */
    Optional<TaskAttachment> findByFilePath(String filePath);
} 