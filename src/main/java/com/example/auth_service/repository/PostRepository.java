package com.example.auth_service.repository;

import com.example.auth_service.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Репозиторий для работы с сущностью {@link Post}.
 * Предоставляет методы для взаимодействия с базой данных через JPA.
 */
@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    /**
     * Проверяет существование поста с указанным идентификатором и именем пользователя.
     *
     * @param id       Идентификатор поста.
     * @param username Имя пользователя.
     * @return true, если пост с данным id и пользователем существует; false в противном случае.
     * @throws IllegalArgumentException Если id или username равны null.
     */
    boolean existsByIdAndUser_Username(Long id, String username);
}
