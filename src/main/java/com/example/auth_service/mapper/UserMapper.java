package com.example.auth_service.mapper;

import com.example.auth_service.dto.UserDto;
import com.example.auth_service.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;

@Mapper(componentModel = "spring")
public interface UserMapper {

    /**
     * Преобразует сущность {@link User} в DTO {@link UserDto}.
     * Если у пользователя нет ролей, по умолчанию присваивается роль "ROLE_USER".
     */
    @Mapping(target = "roles", expression = "java(user.getRoles() != null && !user.getRoles().isEmpty() ? user.getRoles().stream().map(r -> r.getRoleType().name()).toList() : java.util.List.of(\"ROLE_USER\"))")
    UserDto toDto(User user);

    /**
     * Преобразует список сущностей {@link User} в список DTO {@link UserDto}.
     */
    List<UserDto> toDtoList(List<User> users);
}
