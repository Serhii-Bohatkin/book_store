package bookstore.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import bookstore.TestObjectsFactory;
import bookstore.model.Role;
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
        "classpath:database/insert-roles.sql"}, executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/clear-db.sql", executionPhase = AFTER_TEST_METHOD)
class RoleRepositoryIntegrationTest {
    @Autowired
    private RoleRepository roleRepository;

    @Test
    @DisplayName("Should return role by valid name")
    void getByName_ValidRoleName_ReturnRole() {
        Role expected = TestObjectsFactory.createUserRole();
        Role actual = roleRepository.getByName(Role.RoleName.USER);
        assertThat(actual).isEqualTo(expected);
    }
}
