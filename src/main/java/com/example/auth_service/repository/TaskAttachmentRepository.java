package com.example.auth_service.repository;

import com.example.auth_service.model.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для работы с вложениями задач ({@link TaskAttachment}).
 * <p>
 * Предоставляет стандартные CRUD-операции через {@link JpaRepository} и
 * дополнительные методы для поиска вложений по задаче или пути к файлу.
 * </p>
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
     * @return {@link Optional} с найденным вложением или пустой, если вложение не найдено
     */
    Optional<TaskAttachment> findByFilePath(String filePath);

    /**
     * Находит вложение по идентификатору задачи и пути к файлу.
     *
     * @param taskId идентификатор задачи
     * @param filePath путь к файлу
     * @return {@link Optional} с найденным вложением или пустой, если вложение не найдено
     */
    Optional<TaskAttachment> findByTaskIdAndFilePath(Long taskId, String filePath);
}
