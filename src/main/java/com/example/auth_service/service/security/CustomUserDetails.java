package com.example.auth_service.service.security;

import com.example.auth_service.model.User;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

/**
 * Кастомная реализация {@link UserDetails} для интеграции с Spring Security.
 *
 * <p>Предоставляет информацию о пользователе и его ролях для механизма аутентификации и авторизации.
 * Используется внутри {@link org.springframework.security.core.userdetails.UserDetailsService}.</p>
 */
@Slf4j
public class CustomUserDetails implements UserDetails {

    /**
     * Сущность пользователя {@link User}, содержащая основную информацию о пользователе.
     */
    @Getter
    private final User user;

    /**
     * Коллекция ролей и привилегий пользователя в формате {@link GrantedAuthority}.
     */
    private final Collection<? extends GrantedAuthority> authorities;

    /**
     * Конструктор, инициализирующий пользователя и его полномочия.
     *
     * @param user        Сущность пользователя. Не может быть null.
     * @param authorities Коллекция ролей пользователя. Не может быть null или пустой.
     */
    public CustomUserDetails(User user, Collection<? extends GrantedAuthority> authorities) {
        this.user = user;
        this.authorities = authorities;
    }

    /**
     * Возвращает список полномочий пользователя.
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
     * @return Хэшированный пароль пользователя.
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * Возвращает имя пользователя для аутентификации.
     *
     * @return Логин пользователя.
     */
    @Override
    public String getUsername() {
        return user.getUsername();
    }

    /**
     * Проверяет, не истек ли срок действия учетной записи.
     *
     * @return true, если учетная запись действительна, false если просрочена.
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
     * Проверяет, не истек ли срок действия учетных данных.
     *
     * @return true, если учетные данные действительны.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Проверяет, активна ли учетная запись пользователя.
     *
     * @return true, если учетная запись включена, false если отключена.
     */
    @Override
    public boolean isEnabled() {
        return user.isActive();
    }
}
