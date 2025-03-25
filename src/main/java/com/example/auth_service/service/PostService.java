package com.example.auth_service.service;

import com.example.auth_service.dto.PostDto;
import com.example.auth_service.exception.PostNotFoundException;
import com.example.auth_service.exception.UserNotFoundException;
import com.example.auth_service.model.Post;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.PostRepository;
import com.example.auth_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

/**
 * Сервис для работы с постами пользователей.
 * Обрабатывает создание и получение постов.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    /**
     * Создает новый пост для указанного пользователя.
     *
     * @param postDto       Данные для создания поста.
     * @param authentication объект аутентификации, содержащий информацию о пользователе.
     * @throws UserNotFoundException Если пользователь с указанным именем не найден.
     */
    public void createPost(PostDto postDto, Authentication authentication) {
        String username = authentication.getName();
        log.info("Попытка создания поста для пользователя {}", username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Пользователь с именем {} не найден", username);
                    return new UserNotFoundException("Пользователь не найден");
                });

        Post post = new Post();
        post.setContent(postDto.getContent());
        post.setUser(user);
        postRepository.save(post);

        log.info("Пост для пользователя {} успешно создан", username);
    }

    /**
     * Получает пост по его идентификатору.
     * Доступ разрешен только владельцу поста.
     *
     * @param id идентификатор поста.
     * @param authentication объект аутентификации, содержащий информацию о пользователе.
     * @return DTO с данными поста.
     * @throws PostNotFoundException если пост с указанным идентификатором не найден.
     */
    public PostDto getPostById(Long id, Authentication authentication) {
        String username = authentication.getName();
        log.info("Попытка получить пост с идентификатором {} для пользователя {}", id, username);

        Post post = postRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Пост с идентификатором {} не найден", id);
                    return new PostNotFoundException("Пост не найден");
                });

        if (!post.getUser().getUsername().equals(username)) {
            log.warn("Пользователь {} не является владельцем поста с id {}", username, id);
            throw new SecurityException("У вас нет прав доступа к этому посту");
        }

        log.info("Пост с идентификатором {} найден для пользователя {}", id, username);
        return new PostDto(post.getContent());
    }
}
