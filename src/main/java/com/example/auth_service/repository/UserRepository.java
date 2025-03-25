package com.example.auth_service.repository;

import com.example.auth_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link User}.
 * Предоставляет методы для взаимодействия с базой данных через JPA.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Находит пользователя по имени пользователя {@link User}.
     *
     * @param username Имя пользователя, которое нужно найти.
     * @return {@link Optional} содержащий найденного пользователя, если он существует, или пустой {@link Optional}, если пользователь не найден.
     * @throws IllegalArgumentException Если {@code username} равен null или пустой строке.
     */
    Optional<User> findByUsername(String username);

    /**
     * Находит пользователя по адресу электронной почты {@link User}.
     *
     * @param email Адрес электронной почты, который нужно найти.
     * @return {@link Optional} содержащий найденного пользователя, если он существует, или пустой {@link Optional}, если пользователь не найден.
     * @throws IllegalArgumentException Если {@code email} равен null или пустой строке.
     */
    Optional<User> findByEmail(String email);
}
