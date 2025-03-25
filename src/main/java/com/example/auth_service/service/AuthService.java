package com.example.auth_service.service;

import com.example.auth_service.dto.UserSigninDto;
import com.example.auth_service.dto.UserSignupDto;
import com.example.auth_service.model.Role;
import com.example.auth_service.model.User;
import com.example.auth_service.repository.RoleRepository;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.service.security.jwt.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;

    public void registerUser(UserSignupDto userSignupDto) {
        User user = new User();
        user.setUsername(userSignupDto.getUsername());
        user.setEmail(userSignupDto.getEmail());
        user.setPassword(passwordEncoder.encode(userSignupDto.getPassword()));
        Role role = roleRepository.findByRoleType(Role.RoleType.ROLE_USER)
                .orElseThrow();
        user.setRoles(Set.of(role));
        userRepository.save(user);
    }

    public void registerAdmin(UserSignupDto userSignupDto) {
        User user = new User();
        user.setUsername(userSignupDto.getUsername());
        user.setEmail(userSignupDto.getEmail());
        user.setPassword(passwordEncoder.encode(userSignupDto.getPassword()));
        Role role = roleRepository.findByRoleType(Role.RoleType.ROLE_ADMIN)
                .orElseThrow();
        user.setRoles(Set.of(role));
        userRepository.save(user);
    }

    public String login(UserSigninDto userSigninDto) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(userSigninDto.getUsername(), userSigninDto.getPassword())
        );
        return jwtUtil.generateToken(authentication.getName(), authentication.getAuthorities());
    }

    public void logout(String jwt) {
        jwtUtil.addJwtToBlacklist(jwt);
    }
}
