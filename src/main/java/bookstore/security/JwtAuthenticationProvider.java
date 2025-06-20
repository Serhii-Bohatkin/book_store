package bookstore.security;

import bookstore.model.Role;
import bookstore.model.User;
import bookstore.repository.RoleRepository;
import bookstore.service.UserService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationProvider {
    private final RoleRepository roleRepository;
    private final ObjectMapper objectMapper;
    private final UserService userService;

    public JwtAuthentication createAuthentication(Claims claims) {
        return JwtAuthentication.builder()
                .roles(getRoles(claims))
                .firstName(claims.get("firstName", String.class))
                .email(claims.getSubject())
                .user((User) userService.loadUserByUsername(claims.getSubject()))
                .build();
    }

    private Set<Role> getRoles(Claims claims) {
        List<String> roles = objectMapper.convertValue(
                claims.get("roles"),
                new TypeReference<List<String>>() {
                }
        );
        return roles.stream()
                .map(Role.RoleName::valueOf)
                .map(roleRepository::getByName)
                .collect(Collectors.toSet());
    }
}
