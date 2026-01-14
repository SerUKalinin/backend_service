package com.example.auth_service.repository;

import com.example.auth_service.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link Role}.
 * Обеспечивает доступ к данным ролей пользователей в базе данных и предоставляет методы для поиска и управления ролями.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    /**
     * Находит роль по её типу {@link Role.RoleType}.
     *
     * @param roleType Тип роли, который необходимо найти. Не может быть null.
     * @return {@link Optional} с найденной ролью, или пустой Optional, если роль с указанным типом отсутствует.
     * @throws IllegalArgumentException Если переданный {@code roleType} равен null.
     */
    Optional<Role> findByRoleType(Role.RoleType roleType);
}
