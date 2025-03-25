package com.example.auth_service.controller;

import com.example.auth_service.dto.PostDto;
import com.example.auth_service.exception.PostNotFoundException;
import com.example.auth_service.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * Контроллер для работы с постами.
 */
@Slf4j
@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    /**
     * Создает новый пост.
     *
     * @param postDto       данные нового поста.
     * @param authentication объект аутентификации, содержащий информацию о пользователе.
     */
    @PostMapping
    public void createPost(@Valid @RequestBody PostDto postDto, Authentication authentication) {
        log.info("Создание поста пользователем: {}", authentication.getName());
        postService.createPost(postDto, authentication);
    }

    /**
     * Получает пост по его идентификатору.
     * Доступ разрешен только владельцу поста.
     *
     * @param id идентификатор поста.
     * @return DTO с данными поста.
     * @throws PostNotFoundException если пост не найден.
     */
    @GetMapping("/{id}")
    @PreAuthorize("@postSecurityService.isPostOwner(#id, authentication)")
    public PostDto getPostById(@PathVariable Long id, Authentication authentication) {
        log.info("Запрос на получение поста с id: {}", id);
        return postService.getPostById(id, authentication);
    }
}
