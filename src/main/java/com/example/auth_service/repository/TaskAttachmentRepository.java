package com.example.auth_service.repository;

import com.example.auth_service.model.TaskAttachment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Репозиторий для управления сущностью {@link TaskAttachment}.
 * Предоставляет методы для получения, поиска и управления вложениями задач в базе данных.
 */
@Repository
public interface TaskAttachmentRepository extends JpaRepository<TaskAttachment, Long> {

    /**
     * Возвращает список всех вложений для указанной задачи.
     *
     * @param taskId идентификатор задачи, для которой нужно получить вложения. Не может быть null.
     * @return список {@link TaskAttachment} для задачи, список может быть пустым, если вложений нет.
     */
    List<TaskAttachment> findByTaskId(Long taskId);

    /**
     * Находит вложение по его пути в файловой системе.
     *
     * @param filePath полный путь к файлу. Не может быть null или пустым.
     * @return {@link Optional} с найденным вложением, либо пустой Optional, если вложение не найдено.
     */
    Optional<TaskAttachment> findByFilePath(String filePath);

    /**
     * Находит вложение по идентификатору задачи и пути к файлу.
     *
     * @param taskId   идентификатор задачи, к которой относится вложение. Не может быть null.
     * @param filePath полный путь к файлу. Не может быть null или пустым.
     * @return {@link Optional} с найденным вложением, либо пустой Optional, если совпадений нет.
     */
    Optional<TaskAttachment> findByTaskIdAndFilePath(Long taskId, String filePath);
}
