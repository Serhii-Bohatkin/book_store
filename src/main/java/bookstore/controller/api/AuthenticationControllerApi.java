package bookstore.controller.api;

import bookstore.dto.jwt.JwtResponseDto;
import bookstore.dto.jwt.RefreshJwtRequestDto;
import bookstore.dto.user.UserLoginRequestDto;
import bookstore.dto.user.UserRegistrationRequestDto;
import bookstore.dto.user.UserResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@RequestMapping("/auth")
@Tag(name = "Authentication management", description = "Endpoints for managing authentications")
public interface AuthenticationControllerApi {
    @Operation(summary = "Authentication",
            description = "Authentication users by email and password")
    @PostMapping("/login")
    JwtResponseDto login(@RequestBody @Valid UserLoginRequestDto request);

    @Operation(summary = "Registration", description = "Registration of new users")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    UserResponseDto register(@RequestBody @Valid UserRegistrationRequestDto request);

    @Operation(summary = "Getting a new access token",
            description = "Getting a new access token using a refresh token")
    @PostMapping("/token")
    JwtResponseDto getNewAccessToken(@RequestBody RefreshJwtRequestDto request);

    @Operation(summary = "Regeneration of refresh and access tokens",
            description = "Require a valid access token")
    @PostMapping("/refresh")
    JwtResponseDto getNewRefreshToken(@RequestBody RefreshJwtRequestDto request);
}
