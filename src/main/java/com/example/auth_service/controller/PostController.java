package com.example.auth_service.controller;

import com.example.auth_service.dto.PostDto;
import com.example.auth_service.service.PostService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;

    @PostMapping
    public void createPost(@Valid @RequestBody PostDto postDto, Authentication authentication) {
        postService.createPost(postDto, authentication.getName());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@postSecurityService.isPostOwner(#id, authentication)")
//    @PostAuthorize("returnObject.user.username == authentication.name")
    public PostDto getPostById(@PathVariable Long id) {
        return postService.getPostById(id);
    }
}
