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
 * Конфигурация безопасности приложения.
 * Настраивает фильтрацию запросов, аутентификацию и управление сессиями.
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthFilter jwtAuthFilter;

    /**
     * Настраивает цепочку фильтров безопасности.
     *
     * @param http объект для конфигурации HTTP безопасности.
     * @return настроенная цепочка безопасности.
     * @throws Exception если произошла ошибка конфигурации.
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        log.info("Настройка SecurityFilterChain");

        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // Доступ к аутентификации и регистрации открыт
                        .requestMatchers(
                                "/auth/**"
                        ).permitAll()
                        // Доступ к объектам недвижимости
                        .requestMatchers(HttpMethod.GET, "/real-estate-objects").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.POST, "/real-estate-objects").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/real-estate-objects/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/real-estate-objects/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/real-estate-objects/my-objects").authenticated()
                        .requestMatchers(HttpMethod.GET, "/real-estate-objects/by-responsible/{userId}").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/real-estate-objects/{id}/assign-responsible/{userId}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/real-estate-objects/{id}/remove-responsible").hasRole("ADMIN")

                        // Доступ к пользователям
                        .requestMatchers(HttpMethod.POST, "/users").hasRole("ADMIN") // создание пользователя
                        .requestMatchers(HttpMethod.GET, "/users/info").authenticated() // инфо о себе
                        .requestMatchers(HttpMethod.GET, "/users/info/all").hasRole("ADMIN") // инфо обо всех
                        .requestMatchers(HttpMethod.GET, "/users/info/{id}").hasRole("ADMIN") // инфо по ID
                        .requestMatchers(HttpMethod.PUT, "/users/update/first-name").authenticated() // обновление имени
                        .requestMatchers(HttpMethod.PUT, "/users/update/last-name").authenticated() // обновление фамилии
                        .requestMatchers(HttpMethod.PUT, "/users/update/email").authenticated() // обновление почты
                        .requestMatchers(HttpMethod.PUT, "/users/update/{userId}/role").hasRole("ADMIN") // обновление роли
                        .requestMatchers(HttpMethod.DELETE, "/users/{id}").hasRole("ADMIN") // удаление пользователей

                        // Доступ к задачам
                        .requestMatchers(HttpMethod.POST, "/tasks").hasRole("ADMIN") // создание задачи
                        .requestMatchers(HttpMethod.GET, "/tasks").hasAnyRole("USER", "ADMIN") // просмотр всех задач
                        .requestMatchers(HttpMethod.GET, "/tasks/{id}").hasAnyRole("USER", "ADMIN") // просмотр конкретной задачи
                        .requestMatchers(HttpMethod.PUT, "/tasks/{id}").hasRole("ADMIN") // обновление задачи
                        .requestMatchers(HttpMethod.DELETE, "/tasks/{id}").hasRole("ADMIN") // удаление задачи

                        // Получение задач по статусу
                        .requestMatchers(HttpMethod.GET, "/tasks/status/{status}").hasAnyRole("USER", "ADMIN")
                        // Получение задач по объекту недвижимости
                        .requestMatchers(HttpMethod.GET, "/tasks/object/{objectId}").hasAnyRole("USER", "ADMIN")
                        // Получение задач по статусу и объекту недвижимости
                        .requestMatchers(HttpMethod.GET, "/tasks/status/{status}/object/{objectId}").hasAnyRole("USER", "ADMIN")

                        // Доступ к файлам
                        .requestMatchers(HttpMethod.POST, "/api/files/upload").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/files/download/{fileName:.+}").hasAnyRole("USER", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/files/{fileName:.+}").hasAnyRole("USER", "ADMIN")

                        // Любые другие запросы требуют авторизации
                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        log.info("SecurityFilterChain настроен успешно");
        return http.build();
    }

    /**
     * Создает и возвращает {@link PasswordEncoder} для хеширования паролей.
     *
     * @return экземпляр {@link BCryptPasswordEncoder}.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("Создание PasswordEncoder (BCrypt)");
        return new BCryptPasswordEncoder();
    }

    /**
     * Настраивает провайдера аутентификации.
     * Использует {@link DaoAuthenticationProvider} с кастомным сервисом загрузки пользователей.
     *
     * @return экземпляр {@link AuthenticationProvider}.
     * @throws IllegalStateException если userDetailsService не инициализирован.
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
     * Создает менеджер аутентификации.
     *
     * @param configuration объект конфигурации аутентификации.
     * @return экземпляр {@link AuthenticationManager}.
     * @throws Exception если произошла ошибка при создании менеджера аутентификации.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        log.info("Создание AuthenticationManager");
        return configuration.getAuthenticationManager();
    }
}
