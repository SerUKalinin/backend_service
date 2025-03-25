package com.example.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSignupDto {

    @NotBlank
    @Size(min = 2, max = 255)
    private String username;

    @Email
    @NotBlank
    @Size(min = 2, max = 255)
    private String email;

    @NotBlank
    @Size(min = 6, max = 255)
    private String password;
}
