package org.example.bookstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.bookstore.dto.jwt.JwtResponseDto;
import org.example.bookstore.dto.jwt.RefreshJwtRequestDto;
import org.example.bookstore.dto.user.UserLoginRequestDto;
import org.example.bookstore.dto.user.UserRegistrationRequestDto;
import org.example.bookstore.dto.user.UserResponseDto;
import org.example.bookstore.exception.RegistrationException;
import org.example.bookstore.security.AuthenticationService;
import org.example.bookstore.service.UserService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication management", description = "Endpoints for managing authentications")
public class AuthenticationController {
    private final UserService userService;
    private final AuthenticationService authenticationService;

    @Operation(summary = "Authentication",
            description = "Authentication users by email and password")
    @PostMapping("/login")
    public JwtResponseDto login(@RequestBody @Valid UserLoginRequestDto request) {
        return authenticationService.login(request);
    }

    @Operation(summary = "Registration", description = "Registration of new users")
    @PostMapping("/register")
    public UserResponseDto register(@RequestBody @Valid UserRegistrationRequestDto request)
            throws RegistrationException {
        return userService.register(request);
    }

    @Operation(summary = "Getting a new access token",
            description = "Getting a new access token using a refresh token")
    @PostMapping("/token")
    public JwtResponseDto getNewAccessToken(@RequestBody RefreshJwtRequestDto request) {
        return authenticationService.getAccessToken(request.refreshToken());
    }

    @Operation(summary = "Regeneration of refresh and access tokens",
            description = "Require a valid access token ")
    @PostMapping("/refresh")
    public JwtResponseDto getNewRefreshToken(@RequestBody RefreshJwtRequestDto request) {
        return authenticationService.refresh(request.refreshToken());
    }
}
