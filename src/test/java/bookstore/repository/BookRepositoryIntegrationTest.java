package bookstore.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import bookstore.TestObjectsFactory;
import bookstore.dto.book.BookSearchParametersDto;
import bookstore.model.Book;
import bookstore.repository.book.BookSpecificationBuilder;
import bookstore.repository.book.BookSpecificationProviderManager;
import bookstore.repository.book.spec.TitleSpecificationProvider;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {
        "classpath:database/clear-db.sql",
        "classpath:database/insert-books.sql",
        "classpath:database/insert-categories.sql",
        "classpath:database/link-books-to-categories.sql"}, executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/clear-db.sql", executionPhase = AFTER_TEST_METHOD)
@Import({BookSpecificationBuilder.class,
        BookSpecificationProviderManager.class,
        TitleSpecificationProvider.class})
class BookRepositoryIntegrationTest {
    private static final Long VALID_BOOK_ID = 1L;
    private static final Long VALID_CATEGORY_ID = 1L;
    private static final Long INVALID_CATEGORY_ID = Long.MAX_VALUE;
    private static final Long INVALID_BOOK_ID = Long.MAX_VALUE;
    private static final PageRequest DEFAULT_PAGE_REQUEST = PageRequest.of(0, 20);
    @Autowired
    private BookRepository bookRepository;
    @Autowired
    private BookSpecificationBuilder bookSpecificationBuilder;

    @Test
    @DisplayName("Should return two books for a valid category ID")
    void findAllByCategoryId_ValidId_ShouldReturnTwoBooks() {
        List<Book> expected = List.of(
                TestObjectsFactory.create1984Book(),
                TestObjectsFactory.createToKillMockingbirdBook());
        List<Book> actual =
                bookRepository.findAllByCategoryId(VALID_CATEGORY_ID, DEFAULT_PAGE_REQUEST)
                        .getContent();
        assertThat(actual).hasSize(2).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should return an empty list for an invalid category ID")
    void findAllByCategoryId_InvalidId_ShouldReturnEmptyList() {
        Page<Book> actual = bookRepository.findAllByCategoryId(INVALID_CATEGORY_ID,
                DEFAULT_PAGE_REQUEST);
        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("Should return one book for valid search specification")
    void findAll_ValidSpecification_ShouldReturnOneBook() {
        Book expectedBook = TestObjectsFactory.create1984Book();
        BookSearchParametersDto searchParameters =
                TestObjectsFactory.createSearchParameters("1984");
        Specification<Book> spec = bookSpecificationBuilder.build(searchParameters);
        Page<Book> actual = bookRepository.findAll(spec, DEFAULT_PAGE_REQUEST);
        assertThat(actual).hasSize(1);
        assertThat(actual.getContent()).containsExactly(expectedBook);
    }

    @Test
    @DisplayName("Should return three books for empty search parameters")
    void findAll_EmptySearch_ShouldReturnThreeBooks() {
        List<Book> threeBooksList = TestObjectsFactory.createThreeBooksList();
        BookSearchParametersDto emptySearchParameters =
                TestObjectsFactory.createEmptySearchParameters();
        Specification<Book> spec = bookSpecificationBuilder.build(emptySearchParameters);
        Page<Book> actual = bookRepository.findAll(spec, DEFAULT_PAGE_REQUEST);
        assertThat(actual).hasSize(3).containsExactlyElementsOf(threeBooksList);
    }

    @Test
    @DisplayName("Should return the correct book for a valid book ID")
    void findById_ValidId_ShouldReturnValidBook() {
        Book expected = TestObjectsFactory.create1984Book();
        Book actual = bookRepository.findById(VALID_BOOK_ID).orElseThrow();
        assertThat(actual).isEqualTo(expected);
        assertThat(actual.getCategories()).contains(TestObjectsFactory.createFictionCategory());
    }

    @Test
    @DisplayName("Should return an empty Optional for a non-existing book ID")
    void findById_WithNonExistingBookId_ShouldReturnEmptyOptional() {
        Optional<Book> actual = bookRepository.findById(INVALID_BOOK_ID);
        assertThat(actual).isEmpty();
    }
}
