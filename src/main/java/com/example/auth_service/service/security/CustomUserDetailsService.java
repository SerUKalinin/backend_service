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
     * @param username Логин или email пользователя.
     * @return Объект UserDetails.
     * @throws UsernameNotFoundException Если пользователь с таким идентификатором не найден.
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
