package com.example.auth_service.repository;

import com.example.auth_service.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Репозиторий для работы с сущностью {@link User}.
 * <p>
 * Предоставляет стандартные CRUD-операции через {@link JpaRepository} и дополнительные методы для поиска
 * пользователей по username и email, а также проверки их существования.
 * </p>
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Находит пользователя по имени пользователя.
     *
     * @param username имя пользователя
     * @return {@link Optional} с найденным пользователем или пустой, если пользователь не найден
     */
    Optional<User> findByUsername(String username);

    /**
     * Находит пользователя по email.
     *
     * @param email адрес электронной почты
     * @return {@link Optional} с найденным пользователем или пустой, если пользователь не найден
     */
    Optional<User> findByEmail(String email);

    /**
     * Проверяет существование пользователя по email.
     *
     * @param email адрес электронной почты
     * @return {@code true}, если пользователь с данным email существует, иначе {@code false}
     */
    boolean existsByEmail(String email);

    /**
     * Проверяет существование пользователя по username.
     *
     * @param username имя пользователя
     * @return {@code true}, если пользователь с данным username существует, иначе {@code false}
     */
    boolean existsByUsername(String username);
}
