package com.example.auth_service.service.security;

import com.example.auth_service.model.User;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Кастомный класс для хранения данных пользователя, который реализует интерфейс {@link UserDetails}.
 * Используется для работы с Spring Security.
 */
@Slf4j
public class CustomUserDetails implements UserDetails {

    @Getter
    private final User user;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user, Collection<? extends GrantedAuthority> authorities) {
        this.user = user;
        this.authorities = authorities;
    }

    /**
     * Возвращает роли пользователя в виде коллекции {@link GrantedAuthority}.
     * Каждая роль преобразуется в {@link SimpleGrantedAuthority}.
     *
     * @return Коллекция {@link GrantedAuthority}, представляющая роли пользователя.
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    /**
     * Возвращает пароль пользователя.
     *
     * @return Пароль пользователя.
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Возвращает имя пользователя.
     *
     * @return Имя пользователя.
     */
    @Override
    public String getUsername() {
        return user.getUsername();
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
        return user.isActive();
    }
}
