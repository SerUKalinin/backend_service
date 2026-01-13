package com.example.auth_service.mapper;

import com.example.auth_service.dto.UserDto;
import com.example.auth_service.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface UserMapper {

    // Маппинг User > UserDto
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

    // Маппинг списка User > List<UserDto>
    default List<UserDto> toDtoList(List<User> users) {
        if (users == null) return List.of();
        return users.stream().map(this::toDto).collect(Collectors.toList());
    }

    // Обновление User из UserDto
    default void updateUserFromDto(UserDto dto, @MappingTarget User user) {
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getFirstName() != null) user.setFirstName(dto.getFirstName());
        if (dto.getLastName() != null) user.setLastName(dto.getLastName());
        user.setActive(dto.isActive());
        // роли оставляем управляться через сервис (UserUpdateService)
    }
}
