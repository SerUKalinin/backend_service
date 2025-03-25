package com.example.auth_service.service;

import com.example.auth_service.dto.PostDto;
import com.example.auth_service.model.Post;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.PostRepository;
import com.example.auth_service.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public void createPost(PostDto postDto, String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        Post post = new Post();
        post.setContent(postDto.getContent());
        post.setUser(user);
        postRepository.save(post);
    }

    public PostDto getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Post not found"));
        return new PostDto(post.getContent());
    }
}
