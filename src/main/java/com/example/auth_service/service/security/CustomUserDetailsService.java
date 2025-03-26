package com.example.auth_service.service.security;

import com.example.auth_service.model.User;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.exception.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import java.util.Optional;

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
     * Загружает пользователя по имени пользователя или email.
     *
     * @param identifier Логин или email пользователя.
     * @return Объект UserDetails.
     * @throws UserNotFoundException Если пользователь с таким идентификатором не найден.
     */
    @Override
    public UserDetails loadUserByUsername(String identifier) {
        log.info("Попытка загрузить пользователя с идентификатором: {}", identifier);

        // Проверяем, является ли идентификатор почтой
        Optional<User> userOptional;
        if (identifier.contains("@")) {
            userOptional = userRepository.findByEmail(identifier);  // поиск по email
        } else {
            userOptional = userRepository.findByUsername(identifier);  // поиск по username
        }

        User user = userOptional.orElseThrow(() -> {
            log.error("Пользователь с идентификатором {} не найден", identifier);
            return new UserNotFoundException("Пользователь не найден: " + identifier);
        });

        log.info("Пользователь с идентификатором {} найден", identifier);

        return new CustomUserDetails(user.getUsername(), user.getPassword(), user.getRoles());
    }
}
