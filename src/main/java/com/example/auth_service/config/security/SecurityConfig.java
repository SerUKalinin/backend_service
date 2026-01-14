package com.example.auth_service.config.security;

import com.example.auth_service.config.security.filter.JwtAuthFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Центральная конфигурация безопасности приложения аутентификации.
 * <p>
 * Определяет правила доступа к HTTP-эндпоинтам, стратегию аутентификации, работу с JWT,
 * политику управления сессиями и интеграцию с сервисами пользователей.
 * Применяется на уровне security-слоя Spring Security.
 * </p>
 * <p>
 * Включает поддержку аннотаций безопасности для методов с {@link EnableMethodSecurity}
 * и конфигурирует stateless-подход без использования HTTP-сессий.
 * </p>
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    /**
     * Сервис для загрузки информации о пользователях.
     * <p>
     * Используется провайдером аутентификации для проверки учётных данных.
     * Не допускает null, инъектируется через конструктор.
     */
    private final UserDetailsService userDetailsService;

    /**
     * Фильтр JWT-аутентификации.
     * <p>
     * Обрабатывает входящие HTTP-запросы, проверяя JWT-токены
     * и устанавливая контекст безопасности.
     */
    private final JwtAuthFilter jwtAuthFilter;

    /**
     * Формирует цепочку фильтров безопасности Spring Security.
     * <p>
     * Настраивает правила авторизации для HTTP-эндпоинтов, JWT-аутентификацию
     * и stateless-режим работы без HTTP-сессий.
     * </p>
     *
     * @param http объект конфигурации HTTP-безопасности; не может быть null
     * @return {@link SecurityFilterChain} с применёнными настройками безопасности
     * @throws Exception при ошибках конфигурации Spring Security
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Настройка SecurityFilterChain");

        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()

                        .requestMatchers(HttpMethod.GET, "/real-estate-objects").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/real-estate-objects").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/real-estate-objects/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/real-estate-objects/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/real-estate-objects/my-objects").authenticated()
                        .requestMatchers(HttpMethod.GET, "/real-estate-objects/by-responsible/{userId}").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/real-estate-objects/{id}/assign-responsible/{userId}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/real-estate-objects/{id}/remove-responsible").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/users").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users/info").authenticated()
                        .requestMatchers(HttpMethod.GET, "/users/info/all").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users/info/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/users/update/first-name").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/users/update/last-name").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/users/update/email").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/users/update/{userId}/role").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/users/{id}").hasRole("ADMIN")

                        .requestMatchers(HttpMethod.POST, "/tasks").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/tasks").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/tasks/{id}").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/tasks/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/tasks/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/tasks/status/{status}").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/tasks/object/{objectId}").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/tasks/status/{status}/object/{objectId}").hasAnyRole("USER", "ADMIN")

                        .requestMatchers(HttpMethod.POST, "/api/files/upload").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/files/download/{fileName:.+}").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/files/{fileName:.+}").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/files/task/{taskId}").hasAnyRole("USER", "ADMIN")

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("SecurityFilterChain настроен успешно");
        return http.build();
    }

    /**
     * Регистрирует {@link PasswordEncoder} для хеширования паролей пользователей.
     * <p>
     * Используется при аутентификации и сохранении учётных данных пользователей.
     *
     * @return экземпляр {@link PasswordEncoder} на основе BCrypt
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("Создание PasswordEncoder (BCrypt)");
        return new BCryptPasswordEncoder();
    }

    /**
     * Настраивает {@link AuthenticationProvider} на основе базы данных пользователей.
     * <p>
     * Использует {@link UserDetailsService} и {@link PasswordEncoder} для проверки
     * учётных данных при аутентификации.
     *
     * @return {@link AuthenticationProvider} для проверки пользователя
     * @throws IllegalStateException если {@link UserDetailsService} не инициализирован
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        if (userDetailsService == null) {
            log.error("UserDetailsService не может быть null");
            throw new IllegalStateException("UserDetailsService не может быть null");
        }

        log.info("Настройка AuthenticationProvider");
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Предоставляет {@link AuthenticationManager} для процессов аутентификации.
     * <p>
     * Используется, в том числе, в контроллерах аутентификации для выполнения входа пользователей.
     *
     * @param configuration конфигурация аутентификации Spring Security; не может быть null
     * @return {@link AuthenticationManager} для проверки учётных данных пользователей
     * @throws Exception при ошибке создания менеджера аутентификации
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        log.info("Создание AuthenticationManager");
        return configuration.getAuthenticationManager();
    }
}
