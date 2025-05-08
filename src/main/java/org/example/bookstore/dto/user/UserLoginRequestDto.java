package org.example.bookstore.dto.user;

import org.example.bookstore.validation.Email;
import org.example.bookstore.validation.Password;

public record UserLoginRequestDto(
        @Email
        String email,
        @Password
        String password
) {
}
