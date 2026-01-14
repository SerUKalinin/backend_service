package com.example.auth_service.repository;

import com.example.auth_service.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link Role}.
 * <p>
 * Предоставляет стандартные CRUD-операции через {@link JpaRepository} и
 * метод поиска роли по типу {@link Role.RoleType}.
 * </p>
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Находит роль по её типу.
     *
     * @param roleType тип роли ({@link Role.RoleType}), который необходимо найти
     * @return {@link Optional} с найденной ролью, либо пустой, если роль не существует
     * @throws IllegalArgumentException если {@code roleType} равен null
     */
    Optional<Role> findByRoleType(Role.RoleType roleType);
}
