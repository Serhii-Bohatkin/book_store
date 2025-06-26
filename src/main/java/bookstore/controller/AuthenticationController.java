package bookstore.controller;

import bookstore.controller.api.AuthenticationControllerApi;
import bookstore.dto.jwt.JwtResponseDto;
import bookstore.dto.jwt.RefreshJwtRequestDto;
import bookstore.dto.user.UserLoginRequestDto;
import bookstore.dto.user.UserRegistrationRequestDto;
import bookstore.dto.user.UserResponseDto;
import bookstore.security.AuthenticationService;
import bookstore.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthenticationController implements AuthenticationControllerApi {
    private final UserService userService;
    private final AuthenticationService authenticationService;

    @Override
    public JwtResponseDto login(UserLoginRequestDto request) {
        return authenticationService.login(request);
    }

    @Override
    public UserResponseDto register(UserRegistrationRequestDto request) {
        return userService.register(request);
    }

    @Override
    public JwtResponseDto getNewAccessToken(RefreshJwtRequestDto request) {
        return authenticationService.getAccessToken(request.refreshToken());
    }

    @Override
    public JwtResponseDto getNewRefreshToken(RefreshJwtRequestDto request) {
        return authenticationService.refresh(request.refreshToken());
    }
}
