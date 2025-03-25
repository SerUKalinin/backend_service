package com.example.auth_service.service.security;

import com.example.auth_service.repository.PostRepository;
import com.example.auth_service.exception.PostNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Сервис для проверки прав доступа к постам.
 * Проверяет, является ли текущий пользователь владельцем поста.
 */
@Service
@RequiredArgsConstructor
@Slf4j  // Использование аннотации для логирования
public class PostSecurityService {

    private final PostRepository postRepository;

    /**
     * Проверяет, является ли текущий пользователь владельцем поста.
     *
     * @param postId Идентификатор поста.
     * @param authentication Объект аутентификации текущего пользователя.
     * @return {@code true}, если пользователь является владельцем поста, {@code false} в противном случае.
     * @throws PostNotFoundException Если пост с указанным идентификатором не найден.
     */
    public boolean isPostOwner(Long postId, Authentication authentication) {
        log.info("Проверка прав доступа пользователя {} для поста с ID {}", authentication.getName(), postId);

        // Проверка существования поста
        boolean isOwner = postRepository.existsByIdAndUser_Username(postId, authentication.getName());

        if (!isOwner) {
            log.warn("Пользователь {} не является владельцем поста с ID {}", authentication.getName(), postId);
            throw new PostNotFoundException("Пост с ID " + postId + " не найден или не принадлежит пользователю " + authentication.getName());
        }

        log.info("Пользователь {} является владельцем поста с ID {}", authentication.getName(), postId);
        return true;
    }
}
