package com.example.auth_service.repository;

import com.example.auth_service.model.Task;
import com.example.auth_service.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Репозиторий для работы с задачами ({@link Task}).
 * <p>
 * Предоставляет стандартные CRUD-операции через {@link JpaRepository} и
 * дополнительные методы для поиска задач по статусу, объекту недвижимости и заголовку.
 * </p>
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

    /**
     * Проверяет, существует ли задача с указанным заголовком для конкретного объекта недвижимости.
     *
     * @param title    заголовок задачи
     * @param objectId идентификатор объекта недвижимости
     * @return {@code true}, если задача существует, иначе {@code false}
     */
    boolean existsByTitleAndRealEstateObjectId(String title, Long objectId);

    /**
     * Находит задачи для списка объектов недвижимости.
     *
     * @param objectIds список идентификаторов объектов недвижимости
     * @return список задач, принадлежащих указанным объектам
     */
    List<Task> findByRealEstateObjectIdIn(List<Long> objectIds);
}
