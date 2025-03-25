package com.example.auth_service.repository;

import com.example.auth_service.model.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    boolean existsByIdAndUser_Username(Long id, String username);
}
