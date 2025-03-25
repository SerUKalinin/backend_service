package com.example.auth_service.service;

import com.example.auth_service.exception.RoleNotFoundException;  // Кастомное исключение
import com.example.auth_service.model.Role;
import com.example.auth_service.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Сервис для работы с ролями пользователей.
 * Обрабатывает создание и поиск ролей.
 */
@Service
@RequiredArgsConstructor
public class RoleService {

    private static final Logger logger = LoggerFactory.getLogger(RoleService.class);

    private final RoleRepository roleRepository;

    /**
     * Находит роль по типу.
     *
     * @param roleType Тип роли.
     * @return Роль, если найдена.
     * @throws RoleNotFoundException Если роль не найдена.
     */
    public Role getRoleByType(Role.RoleType roleType) {
        logger.info("Попытка найти роль с типом {}", roleType);

        return roleRepository.findByRoleType(roleType)
                .orElseThrow(() -> {
                    logger.warn("Роль с типом {} не найдена", roleType);
                    return new RoleNotFoundException("Роль не найдена");
                });
    }
}
