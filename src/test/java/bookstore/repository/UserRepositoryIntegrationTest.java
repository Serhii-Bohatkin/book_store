package bookstore.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import bookstore.TestObjectsFactory;
import bookstore.model.Role;
import bookstore.model.User;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {
        "classpath:database/clear-db.sql",
        "classpath:database/insert-users.sql",
        "classpath:database/insert-roles.sql",
        "classpath:database/link-users-to-roles.sql"}, executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/clear-db.sql", executionPhase = AFTER_TEST_METHOD)
class UserRepositoryIntegrationTest {
    private static final String VALID_EMAIL = "user@gmail.com";
    private static final String INVALID_EMAIL = "MNCkApOTZuKTczO";
    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should return true if user exists by email")
    void existsByEmail_ValidEmail_ReturnTrue() {
        boolean exists = userRepository.existsByEmail(VALID_EMAIL);
        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("Should return false if user does not exist by email")
    void existsByEmail_InvalidEmail_ReturnFalse() {
        boolean exists = userRepository.existsByEmail(INVALID_EMAIL);
        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("Should return user with roles by valid email")
    void findByEmail_ValidEmail_ReturnUserWithRoles() {
        Optional<User> expected = Optional.of(TestObjectsFactory.createUser());
        Optional<User> actual = userRepository.findByEmail(VALID_EMAIL);
        assertThat(actual).isEqualTo(expected);
        Set<Role> roles = actual.orElseThrow().getRoles();
        assertThat(roles).isNotEmpty();
    }

    @Test
    @DisplayName("Should return empty optional for invalid email")
    void findByEmail_InvalidEmail_ReturnEmptyOptional() {
        Optional<User> optionalUser = userRepository.findByEmail(INVALID_EMAIL);
        assertThat(optionalUser).isEmpty();
    }
}
