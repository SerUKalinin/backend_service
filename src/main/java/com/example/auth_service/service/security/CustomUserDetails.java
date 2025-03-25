package com.example.auth_service.service.security;

import com.example.auth_service.model.Role;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Кастомный класс для хранения данных пользователя, который реализует интерфейс {@link UserDetails}.
 * Используется для работы с Spring Security.
 */
@Slf4j
@AllArgsConstructor
public class CustomUserDetails implements UserDetails {

    private final String username;
    private final String password;
    private final Set<Role> roles;

    /**
     * Возвращает роли пользователя в виде коллекции {@link GrantedAuthority}.
     * Каждая роль преобразуется в {@link SimpleGrantedAuthority}.
     *
     * @return Коллекция {@link GrantedAuthority}, представляющая роли пользователя.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (roles == null || roles.isEmpty()) {
            log.error("Пользователь {} не имеет назначенных ролей!", username);
            throw new IllegalStateException("Пользователю должна быть назначена хотя бы одна роль.");
        }
        return roles.stream()
                .map(role -> new SimpleGrantedAuthority(role.getRoleType().toString()))
                .collect(Collectors.toList());
    }

    /**
     * Возвращает пароль пользователя.
     *
     * @return Пароль пользователя.
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Возвращает имя пользователя.
     *
     * @return Имя пользователя.
     */
    @Override
    public String getUsername() {
        return username;
    }

    /**
     * Проверяет, не истек ли срок действия учетной записи.
     *
     * @return true, если учетная запись не истекла.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Проверяет, не заблокирована ли учетная запись.
     *
     * @return true, если учетная запись не заблокирована.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Проверяет, не истек ли срок действия учетных данных (например, пароля).
     *
     * @return true, если учетные данные не истекли.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Проверяет, включена ли учетная запись пользователя.
     *
     * @return true, если учетная запись активна.
     */
    @Override
    public boolean isEnabled() {
        return true;
    }
}
