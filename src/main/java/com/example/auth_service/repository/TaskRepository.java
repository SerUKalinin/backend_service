package com.example.auth_service.repository;

import com.example.auth_service.model.Task;
import com.example.auth_service.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с задачами.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Находит все задачи с указанным статусом.
     *
     * @param status статус задачи
     * @return список задач с данным статусом
     */
    List<Task> findByStatus(TaskStatus status);

    /**
     * Находит все задачи, относящиеся к указанному объекту недвижимости.
     *
     * @param objectId идентификатор объекта недвижимости
     * @return список задач для данного объекта
     */
    List<Task> findByRealEstateObjectId(Long objectId);

    /**
     * Находит все задачи с указанным статусом для заданного объекта недвижимости.
     *
     * @param status   статус задачи
     * @param objectId идентификатор объекта недвижимости
     * @return список задач, соответствующих критериям
     */
    List<Task> findByStatusAndRealEstateObjectId(TaskStatus status, Long objectId);

    boolean existsByTitleAndRealEstateObjectId(String title, Long objectId);

}
