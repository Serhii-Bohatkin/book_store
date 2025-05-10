package bookstore.dto.user;

import bookstore.validation.Email;
import bookstore.validation.Password;

public record UserLoginRequestDto(
        @Email
        String email,
        @Password
        String password
) {
}
