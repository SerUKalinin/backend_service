package com.example.auth_service.mapper;

import com.example.auth_service.dto.TaskCreateDTO;
import com.example.auth_service.dto.TaskDTO;
import com.example.auth_service.dto.TaskUpdateDTO;
import com.example.auth_service.model.Task;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;

/**
 * Mapper для преобразования между сущностью {@link Task} и DTO ({@link TaskDTO}, {@link TaskCreateDTO}, {@link TaskUpdateDTO}).
 *
 * <p>Отвечает за преобразование данных между слоями приложения:
 * - конвертация сущности задачи в DTO для передачи клиенту,
 * - создание сущности задачи на основе DTO для сохранения,
 * - обновление существующей сущности данными из DTO.</p>
 *
 * <p>Используется сервисным слоем для обеспечения чистой архитектуры и отделения модели данных от представления.</p>
 */
@Mapper(componentModel = "spring")
public interface TaskMapper {

    /**
     * Преобразует сущность {@link Task} в DTO {@link TaskDTO}.
     * Выполняется маппинг связанных сущностей пользователя и объекта недвижимости.
     *
     * @param task сущность задачи; не может быть null
     * @return DTO задачи с заполненными полями, включая id пользователей и информацию о создателе и ответственном пользователе
     */
    @Mapping(target = "realEstateObjectId", source = "realEstateObject.id")
    @Mapping(target = "createdByFirstName", source = "createdBy.firstName")
    @Mapping(target = "createdByLastName", source = "createdBy.lastName")
    @Mapping(target = "responsibleUserId", source = "responsibleUser.id")
    @Mapping(target = "responsibleUserFirstName", source = "responsibleUser.firstName")
    @Mapping(target = "responsibleUserLastName", source = "responsibleUser.lastName")
    TaskDTO toDto(Task task);

    /**
     * Преобразует DTO {@link TaskCreateDTO} в сущность {@link Task}.
     * Игнорируются поля, которые устанавливаются сервисом автоматически
     * (id, timestamps, связанные сущности, вложения).
     *
     * @param dto DTO для создания задачи; не может быть null
     * @return новая сущность задачи с полями, указанными в DTO
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "responsibleUser", ignore = true)
    @Mapping(target = "realEstateObject", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "status", constant = "NEW")
    Task toEntity(TaskCreateDTO dto);

    /**
     * Обновляет существующую сущность {@link Task} на основе данных из {@link TaskUpdateDTO}.
     * Игнорируются поля, которые не должны изменяться напрямую:
     * id, timestamps, создатель, ответственный, объект недвижимости, вложения.
     *
     * @param dto  DTO с обновлёнными данными; не может быть null
     * @param task сущность задачи для обновления; не может быть null
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "realEstateObject", ignore = true)
    @Mapping(target = "responsibleUser", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    void updateTaskFromDto(TaskUpdateDTO dto, @MappingTarget Task task);

    /**
     * Преобразует список сущностей {@link Task} в список DTO {@link TaskDTO}.
     *
     * @param tasks список задач; может быть пустым или null
     * @return список DTO; если входной список null, возвращается пустой список
     */
    List<TaskDTO> toDtoList(List<Task> tasks);
}
