package com.example.auth_service.service.security;

import com.example.auth_service.model.User;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

/**
 * Сервис для загрузки данных пользователя для аутентификации.
 * Реализует интерфейс {@link UserDetailsService} для интеграции с Spring Security.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Загружает пользователя по имени пользователя.
     *
     * @param username Имя пользователя, которое будет использовано для поиска.
     * @return Объект {@link UserDetails}, содержащий информацию о пользователе.
     * @throws UserNotFoundException Если пользователь с таким именем не найден.
     */
    @Override
    public UserDetails loadUserByUsername(String username) {
        log.info("Попытка загрузить пользователя с именем: {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("Пользователь с именем {} не найден", username);
                    return new UserNotFoundException("Пользователь не найден: " + username);
                });

        log.info("Пользователь с именем {} найден", username);

        return new CustomUserDetails(user.getUsername(), user.getPassword(), user.getRoles());
    }
}
