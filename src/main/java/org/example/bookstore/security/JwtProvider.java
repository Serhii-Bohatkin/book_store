package org.example.bookstore.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.NonNull;
import org.example.bookstore.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {
    private static final int MILLISECONDS_IN_A_MINUTE = 60000;
    private static final int MILLISECONDS_IN_A_DAY = 86400000;
    private final SecretKey jwtAccessSecret;
    private final SecretKey jwtRefreshSecret;
    @Value("${jwt.accessTokenExpirationInMinutes}")
    private long accessExpirationInMinutes;
    @Value("${jwt.refreshTokenExpirationInDays}")
    private long refreshExpirationInDays;

    public JwtProvider(
            @Value("${jwt.accessSecret}") String jwtAccessSecret,
            @Value("${jwt.refreshSecret}") String jwtRefreshSecret
    ) {
        this.jwtAccessSecret =
                Keys.hmacShaKeyFor(jwtAccessSecret.getBytes(StandardCharsets.UTF_8));
        this.jwtRefreshSecret =
                Keys.hmacShaKeyFor(jwtRefreshSecret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(@NonNull User user) {
        return Jwts.builder().subject(user.getEmail())
                .expiration(new Date(System.currentTimeMillis()
                                + accessExpirationInMinutes * MILLISECONDS_IN_A_MINUTE
                ))
                .signWith(jwtAccessSecret)
                .claim("roles", user.getAuthorityList())
                .claim("firstName", user.getFirstName())
                .compact();
    }

    public String generateRefreshToken(@NonNull User user) {
        return Jwts.builder()
                .subject(user.getEmail())
                .expiration(new Date(System.currentTimeMillis()
                                + refreshExpirationInDays * MILLISECONDS_IN_A_DAY
                ))
                .signWith(jwtRefreshSecret)
                .compact();
    }

    public boolean validateAccessToken(@NonNull String accessToken) {
        return validateToken(accessToken, jwtAccessSecret);
    }

    public boolean validateRefreshToken(@NonNull String refreshToken) {
        return validateToken(refreshToken, jwtRefreshSecret);
    }

    public Claims getAccessClaims(@NonNull String token) {
        return getClaims(token, jwtAccessSecret);
    }

    public Claims getRefreshClaims(@NonNull String token) {
        return getClaims(token, jwtRefreshSecret);
    }

    private boolean validateToken(@NonNull String token, @NonNull SecretKey secret) {
        try {
            Jwts.parser()
                    .verifyWith(secret)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException ex) {
            throw new JwtException("Token expired", ex);
        } catch (UnsupportedJwtException ex) {
            throw new JwtException("Unsupported jwt", ex);
        } catch (MalformedJwtException ex) {
            throw new JwtException("Malformed jwt", ex);
        } catch (SignatureException ex) {
            throw new JwtException("Invalid signature", ex);
        } catch (Exception ex) {
            throw new JwtException("Invalid token", ex);
        }
    }

    private Claims getClaims(@NonNull String token, @NonNull SecretKey secret) {
        return Jwts.parser()
                .verifyWith(secret)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
