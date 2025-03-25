package com.example.auth_service.repository;

import com.example.auth_service.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link Role}.
 * Предоставляет методы для взаимодействия с базой данных через JPA.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Находит роль по типу роли {@link Role.RoleType}.
     *
     * @param roleType Тип роли, которую нужно найти.
     * @return {@link Optional} содержащий найденную роль, если она существует, или пустой {@link Optional}, если роль не найдена.
     * @throws IllegalArgumentException Если {@code roleType} равен null.
     */
    Optional<Role> findByRoleType(Role.RoleType roleType);
}
