package com.example.auth_service.service.user;

import org.springframework.stereotype.Service;

@Service
public class UserValidatorService {

    public boolean validateEmail(String email) {
        return email != null && email.matches("^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$");
    }

    public boolean validateName(String name) {
        return name != null && name.length() >=2;
    }
}
