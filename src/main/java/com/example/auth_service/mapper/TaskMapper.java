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
 * <p>Обеспечивает конвертацию сущностей в DTO, DTO в сущности и обновление существующих сущностей на основе DTO.</p>
 *
 * <p>Использует MapStruct для генерации методов маппинга.</p>
 */
@Mapper(componentModel = "spring")
public interface TaskMapper {

    /**
     * Преобразует сущность {@link Task} в DTO {@link TaskDTO}.
     *
     * <p>Маппинг включает поля, связанные с пользователями и объектом недвижимости.</p>
     *
     * @param task сущность задачи
     * @return DTO задачи, либо null, если task равен null
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
     *
     * <p>Поля, которые устанавливаются сервисом или автоматически (id, createdAt, updatedAt, связи с пользователями и объектом недвижимости),
     * игнорируются при маппинге.</p>
     *
     * @param dto DTO для создания задачи
     * @return сущность задачи, либо null, если dto равен null
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "responsibleUser", ignore = true)
    @Mapping(target = "realEstateObject", ignore = true)
    @Mapping(target = "attachments", ignore = true)
    @Mapping(target = "status", ignore = true)
    Task toEntity(TaskCreateDTO dto);

    /**
     * Обновляет существующую сущность {@link Task} данными из {@link TaskUpdateDTO}.
     *
     * <p>Игнорируются поля, которые не должны изменяться напрямую (id, созданные и обновлённые даты, связи с пользователями, объектом недвижимости и вложениями).</p>
     *
     * @param dto  DTO с обновлёнными данными
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
}
