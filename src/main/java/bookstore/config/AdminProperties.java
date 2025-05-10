package bookstore.config;

import bookstore.validation.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@ConfigurationProperties(prefix = "admin")
@Validated
@Getter
@Setter
public class AdminProperties {
    @NotBlank(message = "Admin email is required")
    @Email
    private String email;

    @NotBlank(message = "Admin invite code is required")
    private String inviteCode;
}
