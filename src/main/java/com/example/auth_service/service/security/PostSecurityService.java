package com.example.auth_service.service.security;

import com.example.auth_service.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostSecurityService {

    private final PostRepository postRepository;

    public boolean isPostOwner(Long postId, Authentication authentication) {
        String username = authentication.getName();
        return postRepository.existsByIdAndUser_Username(postId, username);
    }
}
