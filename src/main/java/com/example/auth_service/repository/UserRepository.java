package com.example.auth_service.repository;

import com.example.auth_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для управления сущностью {@link User}.
 * Предоставляет методы для поиска пользователей по различным критериям и проверки их существования.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Находит пользователя по уникальному имени пользователя.
     *
     * @param username имя пользователя. Не может быть null или пустой строкой.
     * @return {@link Optional} с найденным пользователем, если он существует, иначе пустой {@link Optional}.
     * @throws IllegalArgumentException если {@code username} null или пустой.
     */
    Optional<User> findByUsername(String username);

    /**
     * Находит пользователя по уникальному адресу электронной почты.
     *
     * @param email адрес электронной почты. Не может быть null или пустой строкой.
     * @return {@link Optional} с найденным пользователем, если он существует, иначе пустой {@link Optional}.
     * @throws IllegalArgumentException если {@code email} null или пустой.
     */
    Optional<User> findByEmail(String email);

    /**
     * Проверяет существование пользователя с указанным email.
     *
     * @param email адрес электронной почты. Не может быть null.
     * @return true, если пользователь с данным email существует, иначе false.
     */
    boolean existsByEmail(String email);

    /**
     * Проверяет существование пользователя с указанным именем пользователя.
     *
     * @param username имя пользователя. Не может быть null.
     * @return true, если пользователь с данным username существует, иначе false.
     */
    boolean existsByUsername(String username);
}
