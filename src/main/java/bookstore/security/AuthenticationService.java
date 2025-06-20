package bookstore.security;

import bookstore.dto.jwt.JwtResponseDto;
import bookstore.dto.user.UserLoginRequestDto;
import bookstore.exception.AuthenticationException;
import bookstore.model.User;
import bookstore.service.UserService;
import io.jsonwebtoken.Claims;
import java.util.HashMap;
import java.util.Map;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserService userService;
    private final Map<String, String> refreshStorage = new HashMap<>();
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public JwtResponseDto login(@NonNull UserLoginRequestDto requestDto) {
        final Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(requestDto.email(), requestDto.password()));
        User user = (User) authentication.getPrincipal();
        final String accessToken = jwtService.generateAccessToken(user);
        final String refreshToken = jwtService.generateRefreshToken(user);
        refreshStorage.put(user.getEmail(), refreshToken);
        return new JwtResponseDto(accessToken, refreshToken);
    }

    public JwtResponseDto getAccessToken(@NonNull String refreshToken) {
        if (jwtService.validateRefreshToken(refreshToken)) {
            final Claims claims = jwtService.getRefreshClaims(refreshToken);
            final String email = claims.getSubject();
            final String savedRefreshToken = refreshStorage.get(email);
            if (savedRefreshToken != null && savedRefreshToken.equals(refreshToken)) {
                final User user = (User) userService.loadUserByUsername(email);
                final String accessToken = jwtService.generateAccessToken(user);
                return new JwtResponseDto(accessToken, null);
            }
        }
        return new JwtResponseDto(null, null);
    }

    public JwtResponseDto refresh(@NonNull String refreshToken) {
        if (jwtService.validateRefreshToken(refreshToken)) {
            final Claims claims = jwtService.getRefreshClaims(refreshToken);
            final String email = claims.getSubject();
            final String saveRefreshToken = refreshStorage.get(email);
            if (saveRefreshToken != null && saveRefreshToken.equals(refreshToken)) {
                final User user = (User) userService.loadUserByUsername(email);
                final String accessToken = jwtService.generateAccessToken(user);
                final String newRefreshToken = jwtService.generateRefreshToken(user);
                refreshStorage.put(user.getEmail(), newRefreshToken);
                return new JwtResponseDto(accessToken, newRefreshToken);
            }
        }
        throw new AuthenticationException("Invalid JWT token");
    }
}
