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
 * Маппер для преобразования между сущностью {@link Task} и DTO.
 * Использует MapStruct для автоматического создания методов маппинга.
 */
@Mapper(componentModel = "spring")
public interface TaskMapper {

    /**
     * Преобразует {@link Task} в {@link TaskDTO}.
     *
     * @param task сущность задачи
     * @return DTO задачи
     */
    @Mapping(target = "realEstateObjectId", source = "realEstateObject.id")
    @Mapping(target = "createdByFirstName", source = "createdBy.firstName")
    @Mapping(target = "createdByLastName", source = "createdBy.lastName")
    @Mapping(target = "responsibleUserId", source = "responsibleUser.id")
    @Mapping(target = "responsibleUserFirstName", source = "responsibleUser.firstName")
    @Mapping(target = "responsibleUserLastName", source = "responsibleUser.lastName")
    TaskDTO toDto(Task task);

    /**
     * Преобразует {@link TaskCreateDTO} в {@link Task}.
     * Поля, которые устанавливаются в сервисе, игнорируются.
     *
     * @param dto DTO для создания задачи
     * @return сущность задачи
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "responsibleUser", ignore = true)
    @Mapping(target = "realEstateObject", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    Task toEntity(TaskCreateDTO dto);

    /**
     * Обновляет сущность {@link Task} на основе {@link TaskUpdateDTO}.
     * Игнорирует поля, которые не должны изменяться напрямую.
     *
     * @param dto DTO с обновлёнными полями
     * @param task сущность задачи для обновления
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
     * Преобразует список {@link Task} в список {@link TaskDTO}.
     *
     * @param tasks список задач
     * @return список DTO
     */
    List<TaskDTO> toDtoList(List<Task> tasks);
}
