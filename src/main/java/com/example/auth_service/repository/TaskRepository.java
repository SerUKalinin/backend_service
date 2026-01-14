package com.example.auth_service.repository;

import com.example.auth_service.model.Task;
import com.example.auth_service.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для управления сущностью {@link Task}.
 * Предоставляет методы для поиска, проверки существования и выборки задач по различным критериям.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    /**
     * Возвращает список всех задач с указанным статусом.
     *
     * @param status статус задачи. Не может быть null.
     * @return список {@link Task} с заданным статусом, может быть пустым.
     */
    List<Task> findByStatus(TaskStatus status);

    /**
     * Возвращает список всех задач, связанных с указанным объектом недвижимости.
     *
     * @param objectId идентификатор объекта недвижимости. Не может быть null.
     * @return список {@link Task} для данного объекта, может быть пустым.
     */
    List<Task> findByRealEstateObjectId(Long objectId);

    /**
     * Возвращает список задач с указанным статусом для конкретного объекта недвижимости.
     *
     * @param status   статус задачи. Не может быть null.
     * @param objectId идентификатор объекта недвижимости. Не может быть null.
     * @return список {@link Task}, соответствующих критериям, может быть пустым.
     */
    List<Task> findByStatusAndRealEstateObjectId(TaskStatus status, Long objectId);

    /**
     * Проверяет, существует ли задача с указанным названием для конкретного объекта недвижимости.
     *
     * @param title    название задачи. Не может быть null или пустым.
     * @param objectId идентификатор объекта недвижимости. Не может быть null.
     * @return true, если задача с данным названием существует для объекта, иначе false.
     */
    boolean existsByTitleAndRealEstateObjectId(String title, Long objectId);

    /**
     * Возвращает список задач, связанных с объектами недвижимости из заданного списка идентификаторов.
     *
     * @param objectIds список идентификаторов объектов недвижимости. Не может быть null.
     * @return список {@link Task} для указанных объектов, может быть пустым.
     */
    List<Task> findByRealEstateObjectIdIn(List<Long> objectIds);
}
