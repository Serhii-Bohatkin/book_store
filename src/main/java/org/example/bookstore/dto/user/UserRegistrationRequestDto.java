package org.example.bookstore.dto.user;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.bookstore.validation.Email;
import org.example.bookstore.validation.Password;
import org.example.bookstore.validation.PasswordsMatch;

@PasswordsMatch(password = "password", repeatPassword = "repeatPassword",
        message = "password and repeatPassword fields are not matching")
public record UserRegistrationRequestDto(
        @Email
        String email,
        @Password
        String password,
        String repeatPassword,
        @NotBlank
        String firstName,
        @NotBlank
        String lastName,
        @NotBlank
        String shippingAddress,
        @Size(min = 0, max = 100)
        String inviteCode
) {
}
