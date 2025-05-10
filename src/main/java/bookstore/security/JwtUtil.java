package bookstore.security;

import bookstore.model.Role;
import bookstore.repository.RoleRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public final class JwtUtil {
    private final RoleRepository roleRepository;
    private final ObjectMapper objectMapper;

    public JwtAuthentication createAuthentication(Claims claims) {
        final JwtAuthentication authentication = new JwtAuthentication();
        authentication.setRoles(getRoles(claims));
        authentication.setFirstName(claims.get("firstName", String.class));
        authentication.setEmail(claims.getSubject());
        return authentication;
    }

    private Set<Role> getRoles(Claims claims) {
        List<String> roles = objectMapper.convertValue(
                claims.get("roles"),
                new TypeReference<List<String>>() {
                }
        );
        return roles.stream()
                .map(Role.RoleName::valueOf)
                .map(roleRepository::findByName)
                .map(Optional::orElseThrow)
                .collect(Collectors.toSet());
    }
}
