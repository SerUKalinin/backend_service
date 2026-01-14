package com.example.auth_service.mapper;

import com.example.auth_service.dto.UserDto;
import com.example.auth_service.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper для преобразования между сущностью {@link User} и DTO {@link UserDto}.
 *
 * <p>Обеспечивает:
 * - конвертацию сущности пользователя в DTO для передачи в API,
 * - конвертацию списка пользователей в список DTO,
 * - обновление сущности пользователя на основе DTO.</p>
 *
 * <p>Используется сервисным слоем для обеспечения отделения модели данных от представления
 * и поддержки чистой архитектуры.</p>
 */
@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Преобразует сущность {@link User} в DTO {@link UserDto}.
     * Если роли отсутствуют, устанавливается роль по умолчанию "ROLE_USER".
     *
     * @param user сущность пользователя; может быть null
     * @return DTO пользователя с заполненными полями, включая список ролей и активность; возвращает null, если входной объект null
     */
    default UserDto toDto(User user) {
        if (user == null) return null;

        List<String> roles = user.getRoles() == null || user.getRoles().isEmpty()
                ? List.of("ROLE_USER")
                : user.getRoles().stream()
                .map(r -> r.getRoleType().name())
                .collect(Collectors.toList());

        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setFirstName(user.getFirstName());
        dto.setLastName(user.getLastName());
        dto.setRoles(roles);
        dto.setActive(user.isActive());

        return dto;
    }

    /**
     * Преобразует список сущностей {@link User} в список DTO {@link UserDto}.
     *
     * @param users список пользователей; может быть null или пустым
     * @return список DTO; если входной список null, возвращается пустой список
     */
    default List<UserDto> toDtoList(List<User> users) {
        if (users == null) return List.of();
        return users.stream().map(this::toDto).collect(Collectors.toList());
    }

    /**
     * Обновляет существующую сущность {@link User} данными из {@link UserDto}.
     * Игнорирует управление ролями, которые должны управляться через сервис.
     *
     * @param dto  DTO с новыми данными; не может быть null
     * @param user сущность пользователя для обновления; не может быть null
     */
    default void updateUserFromDto(UserDto dto, @MappingTarget User user) {
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) user.setLastName(dto.getLastName());
        user.setActive(dto.isActive());
        // роли оставляем управляться через сервис (UserUpdateService)
    }
}
