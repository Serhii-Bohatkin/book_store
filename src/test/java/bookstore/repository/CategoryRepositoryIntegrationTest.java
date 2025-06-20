package bookstore.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

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
        "classpath:database/insert-categories.sql"}, executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/clear-db.sql", executionPhase = AFTER_TEST_METHOD)
class CategoryRepositoryIntegrationTest {
    private static final String VALID_CATEGORY_NAME = "Fiction";
    private static final String INVALID_CATEGORY_NAME = "MNCkApOTZuKTczO";
    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    @DisplayName("Should return true when category exists")
    void existsByName_ValidName_ShouldReturnTrue() {
        boolean actual = categoryRepository.existsByNameIgnoreCase(VALID_CATEGORY_NAME);
        assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("Should return false when category does not exist")
    void existsByName_InvalidName_ShouldReturnFalse() {
        boolean actual = categoryRepository.existsByNameIgnoreCase(INVALID_CATEGORY_NAME);
        assertThat(actual).isFalse();
    }
}
