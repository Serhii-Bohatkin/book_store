package org.example.bookstore.dto.jwt;

public record JwtResponseDto(
        String accessToken,
        String refreshToken
) {
}
