package com.example.auth_service.service;

import com.example.auth_service.dto.UserDto;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

//    @PreAuthorize("hasRole('ADMIN') or #id == principal.user.id")
    public UserDto getUserInfoByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        return new UserDto(user.getUsername(), user.getEmail());
    }

    public List<UserDto> getAllUsersInfo() {
        return userRepository.findAll().stream()
                .map(user -> new UserDto(user.getUsername(), user.getEmail()))
                .collect(Collectors.toList());
    }
}
