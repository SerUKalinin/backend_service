package com.example.auth_service.service.security;

import com.example.auth_service.model.User;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

/**
 * Сервис для загрузки данных пользователя и его ролей для аутентификации и авторизации.
 * <p>
 * Реализует интерфейс {@link UserDetailsService} для интеграции с Spring Security.
 * Поддерживает кеширование {@link Cacheable} для оптимизации повторных запросов по username.
 * </p>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * Репозиторий для доступа к данным пользователя.
     * Используется для поиска пользователя по username или email.
     */
    private final UserRepository userRepository;

    /**
     * Загружает пользователя по имени пользователя или email для аутентификации.
     * <p>
     * Метод извлекает пользователя из базы данных, преобразует его роли в
     * {@link SimpleGrantedAuthority} и возвращает объект {@link CustomUserDetails}.
     * Поддерживается кеширование для ускорения повторных вызовов.
     * </p>
     *
     * @param username Логин или email пользователя. Не может быть null или пустым.
     * @return {@link UserDetails} с информацией о пользователе и его ролях.
     * @throws UsernameNotFoundException Если пользователь с указанным именем не найден.
     */
    @Override
    @Cacheable(value = "userDetails", key = "#username")
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Попытка загрузить пользователя с идентификатором: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

        log.info("Пользователь с идентификатором {} найден", username);

        var authorities = user.getRoles().stream()
                .map(role -> {
                    log.info("Добавление роли {} для пользователя {}", role.getRoleType(), username);
                    return new SimpleGrantedAuthority(role.getRoleType().name());
                })
                .collect(Collectors.toList());

        log.info("Пользователь {} имеет роли: {}", username, authorities);

        return new CustomUserDetails(user, authorities);
    }
}
