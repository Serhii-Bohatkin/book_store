package bookstore.dto.user;

import bookstore.validation.Email;
import bookstore.validation.Password;
import bookstore.validation.PasswordsMatch;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

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
        @Size(max = 100)
        String inviteCode
) {
}
