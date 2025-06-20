package bookstore.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bookstore.TestObjectsFactory;
import bookstore.dto.book.BookDto;
import bookstore.dto.book.CreateBookRequestDto;
import bookstore.dto.book.UpdateBookRequestDto;
import bookstore.dto.page.PageDto;
import bookstore.model.Book;
import bookstore.repository.BookRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Sql(scripts = {
        "classpath:database/clear-db.sql",
        "classpath:database/insert-books.sql",
        "classpath:database/insert-categories.sql",
        "classpath:database/link-books-to-categories.sql"}, executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/clear-db.sql", executionPhase = AFTER_TEST_METHOD)
class BookControllerIntegrationTest {
    private static final String BOOK_ALREADY_EXISTS_MESSAGE = "A book with isbn {0} already exists";
    private static final String CATEGORY_NOT_FOUND_MESSAGE =
            "A category with id {0} does not exist";
    private static final String BOOK_NOT_FOUND_MESSAGE = "A book with id {0} does not exist";
    private static final String BOOK_ID_MUST_BE_POSITIVE_MESSAGE =
            "bookId must be greater than or equal to 1";
    private static final String PRICE_MUST_BE_POSITIVE_MESSAGE = "price must be greater than 0";
    private static final String INVALID_FORMAT_ISBN_MESSAGE = "isbn Invalid format Isbn";
    private static final String AUTHOR_MUST_NOT_BE_BLANK_MESSAGE = "author must not be blank";
    private static final String TITLE_MUST_NOT_BE_BLANK_MESSAGE = "title must not be blank";
    private static final String CATEGORY_ID_MUST_BE_POSITIVE_MESSAGE =
            "categoryIds[%d] must be greater than or equal to 1";
    private static final String BASE_URL = "/books";
    private static final String BOOK_ID_PARAM = "/{bookId}";
    private static final String SEARCH_PART_URL = "/search";

    private static final Long VALID_BOOK_ID = 1L;
    private static final Long NEGATIVE_BOOK_ID = Long.MIN_VALUE;
    private static final Long NEGATIVE_CATEGORY_ID = Long.MIN_VALUE;
    private static final Long NON_EXISTING_BOOK_ID = Long.MAX_VALUE;
    private static final Long NON_EXISTING_CATEGORY_ID = Long.MAX_VALUE;
    private static final int CATEGORY_INDEX = 0;
    private CreateBookRequestDto createBookRequestDto;
    private UpdateBookRequestDto updateBookRequestDto;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private BookRepository bookRepository;

    @BeforeEach
    void setUp() {
        createBookRequestDto = TestObjectsFactory.createBookRequestDto();
        updateBookRequestDto = TestObjectsFactory.createUpdateBookRequestDto("newTitle");
    }

    @Test
    @DisplayName("Should return a page with three books for default pageable request")
    @WithMockUser
    void getAll_DefaultPageableRequest_ShouldReturnThreeBooks() throws Exception {
        List<BookDto> expected = TestObjectsFactory.createThreeBookDtosList();

        String jsonResponse = mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PageDto<BookDto> actual = objectMapper.readValue(jsonResponse, new TypeReference<>() {
        });
        assertThat(actual.content()).hasSize(3);
        assertThat(actual.content()).containsExactlyElementsOf(expected);
    }

    @Test
    @DisplayName("Should return an empty page when no books are found in the database")
    @WithMockUser
    @Sql(scripts = "classpath:database/clear-db.sql", executionPhase = BEFORE_TEST_METHOD)
    void getAll_BooksNotFound_ShouldReturnEmptyPageDto() throws Exception {
        String jsonResponse = mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PageDto<BookDto> actual = objectMapper.readValue(jsonResponse, new TypeReference<>() {
        });
        assertThat(actual.content()).isEmpty();
    }

    @Test
    @DisplayName("Should return a book for a valid book ID")
    @WithMockUser
    void getBookById_ValidBookId_ShouldReturnBookDto() throws Exception {
        BookDto expected = TestObjectsFactory.create1984BookDto("1984");

        String jsonResponse = mockMvc.perform(get(BASE_URL + BOOK_ID_PARAM, VALID_BOOK_ID))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        BookDto actual = objectMapper.readValue(jsonResponse, BookDto.class);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should return 404 Not Found for a non-existing book ID")
    @WithMockUser
    void getBookById_BookDoesNotExist_NotFound() throws Exception {
        String jsonResponse = mockMvc.perform(get(
                        BASE_URL + BOOK_ID_PARAM, NON_EXISTING_BOOK_ID))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(MessageFormat.format(
                BOOK_NOT_FOUND_MESSAGE, NON_EXISTING_BOOK_ID));
    }

    @Test
    @DisplayName("Should return 400 Bad Request for a negative book ID")
    @WithMockUser
    void getBookById_InvalidBookId_BadRequest() throws Exception {
        String jsonResponse = mockMvc.perform(get(BASE_URL + BOOK_ID_PARAM, NEGATIVE_BOOK_ID))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(BOOK_ID_MUST_BE_POSITIVE_MESSAGE);
    }

    @Test
    @DisplayName("Should create a new book and return BookDto for a valid request")
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    @Sql(scripts = {"classpath:database/clear-db.sql",
            "classpath:database/insert-categories.sql"},
            executionPhase = BEFORE_TEST_METHOD)
    void createBook_ValidRequestDto_ShouldReturnBookDto() throws Exception {
        BookDto expected = TestObjectsFactory.create1984BookDto("1984");
        String jsonRequest = objectMapper.writeValueAsString(createBookRequestDto);

        String jsonResponse = mockMvc.perform(post(BASE_URL)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        BookDto actual = objectMapper.readValue(jsonResponse, BookDto.class);
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("bookId")
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("Should return 409 Conflict when book with the same ISBN already exists")
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    void createBook_BookAlreadyExist_Conflict() throws Exception {
        String jsonRequest = objectMapper.writeValueAsString(createBookRequestDto);

        String jsonResponse = mockMvc.perform(post(BASE_URL)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(MessageFormat.format(
                BOOK_ALREADY_EXISTS_MESSAGE, createBookRequestDto.isbn()));
    }

    @Test
    @DisplayName("Should return 404 Not Found when the specified category does not exist")
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    @Sql(scripts = "classpath:database/clear-db.sql", executionPhase = BEFORE_TEST_METHOD)
    void createBook_CategoryDoesNotExist_NotFound() throws Exception {
        String jsonRequest = objectMapper.writeValueAsString(createBookRequestDto);

        String jsonResponse = mockMvc.perform(post(BASE_URL)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(MessageFormat.format(
                CATEGORY_NOT_FOUND_MESSAGE, createBookRequestDto.categoryIds().getFirst()));
    }

    @Test
    @DisplayName("Should return 400 Bad Request for invalid book creation request")
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    @Sql(scripts = "classpath:database/clear-db.sql", executionPhase = BEFORE_TEST_METHOD)
    void createBook_InvalidRequestDto_BadRequest() throws Exception {
        createBookRequestDto = TestObjectsFactory.createInvalidBookRequestDto();
        String jsonRequest = objectMapper.writeValueAsString(createBookRequestDto);

        String jsonResponse = mockMvc.perform(post(BASE_URL)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(
                PRICE_MUST_BE_POSITIVE_MESSAGE, INVALID_FORMAT_ISBN_MESSAGE,
                AUTHOR_MUST_NOT_BE_BLANK_MESSAGE, TITLE_MUST_NOT_BE_BLANK_MESSAGE);
    }

    @Test
    @DisplayName("Should update a book and return BookDto for a valid ID and request")
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    void updateBook_ValidRequestDtoAndValidId_ShouldReturnBookDto() throws Exception {
        String jsonRequest = objectMapper.writeValueAsString(updateBookRequestDto);
        BookDto expected = TestObjectsFactory.create1984BookDto("newTitle");

        String jsonResponse = mockMvc.perform(patch(BASE_URL + BOOK_ID_PARAM, VALID_BOOK_ID)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        BookDto actual = objectMapper.readValue(jsonResponse, BookDto.class);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should return 400 Bad Request for invalid book update request")
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    void updateBook_InvalidRequestDto_BadRequest() throws Exception {
        updateBookRequestDto = TestObjectsFactory.createInvalidUpdateBookRequestDto();
        String jsonRequest = objectMapper.writeValueAsString(updateBookRequestDto);

        String jsonResponse = mockMvc.perform(patch(BASE_URL + BOOK_ID_PARAM, VALID_BOOK_ID)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(
                PRICE_MUST_BE_POSITIVE_MESSAGE, INVALID_FORMAT_ISBN_MESSAGE);
    }

    @Test
    @DisplayName("Should return 404 Not Found when trying to update a non-existing book")
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    void updateBook_BookDoesNotExist_NotFound() throws Exception {
        String jsonRequest = objectMapper.writeValueAsString(updateBookRequestDto);

        String jsonResponse = mockMvc.perform(patch(
                        BASE_URL + BOOK_ID_PARAM, NON_EXISTING_BOOK_ID)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(MessageFormat.format(
                BOOK_NOT_FOUND_MESSAGE, NON_EXISTING_BOOK_ID));
    }

    @Test
    @DisplayName("Should return 400 Bad Request for a negative book ID on update")
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    void updateBook_InvalidBookId_BadRequest() throws Exception {
        String jsonRequest = objectMapper.writeValueAsString(updateBookRequestDto);

        String jsonResponse = mockMvc.perform(patch(BASE_URL + BOOK_ID_PARAM, NEGATIVE_BOOK_ID)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(BOOK_ID_MUST_BE_POSITIVE_MESSAGE);
    }

    @Test
    @DisplayName("Should return 404 Not Found when update contains a non-existing category ID")
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    void updateBook_CategoryDoesNotExist_NotFound() throws Exception {
        updateBookRequestDto.categoryIds().addFirst(NON_EXISTING_CATEGORY_ID);
        String jsonRequest = objectMapper.writeValueAsString(updateBookRequestDto);

        String jsonResponse = mockMvc.perform(patch(BASE_URL + BOOK_ID_PARAM, VALID_BOOK_ID)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(MessageFormat.format(
                CATEGORY_NOT_FOUND_MESSAGE, NON_EXISTING_CATEGORY_ID));
    }

    @Test
    @DisplayName("Should return 400 Bad Request for a negative category ID on update")
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    void updateBook_InvalidCategoryId_BadRequest() throws Exception {
        updateBookRequestDto.categoryIds().addFirst(NEGATIVE_CATEGORY_ID);
        String jsonRequest = objectMapper.writeValueAsString(updateBookRequestDto);

        String jsonResponse = mockMvc.perform(patch(BASE_URL + BOOK_ID_PARAM, VALID_BOOK_ID)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(String.format(
                CATEGORY_ID_MUST_BE_POSITIVE_MESSAGE, CATEGORY_INDEX));
    }

    @Test
    @DisplayName("Should delete a book and return 204 No Content for a valid ID")
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    void deleteById_ValidBookId_NoContent() throws Exception {
        mockMvc.perform(delete(BASE_URL + BOOK_ID_PARAM, VALID_BOOK_ID))
                .andExpect(status().isNoContent());

        Optional<Book> actual = bookRepository.findById(VALID_BOOK_ID);
        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("Should return 404 Not Found when trying to delete a non-existing book")
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    void deleteById_BookDoesNotExist_NotFound() throws Exception {
        String jsonResponse = mockMvc.perform(delete(
                        BASE_URL + BOOK_ID_PARAM, NON_EXISTING_BOOK_ID))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(MessageFormat.format(
                BOOK_NOT_FOUND_MESSAGE, NON_EXISTING_BOOK_ID));
    }

    @Test
    @DisplayName("Should return 400 Bad Request for a negative book ID on delete")
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    void deleteById_InvalidBookId_BadRequest() throws Exception {
        String jsonResponse = mockMvc.perform(delete(
                        BASE_URL + BOOK_ID_PARAM, NEGATIVE_BOOK_ID))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(BOOK_ID_MUST_BE_POSITIVE_MESSAGE);
    }

    @Test
    @DisplayName("Should return one book matching the given title in search")
    @WithMockUser
    void searchBooks_SearchByTitle_ShouldReturnPageDtoWithOneBook() throws Exception {
        BookDto expected = TestObjectsFactory.create1984BookDto("1984");

        String jsonResponse = mockMvc.perform(get(
                        BASE_URL + SEARCH_PART_URL).param("title", "1984"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PageDto<BookDto> actual = objectMapper.readValue(jsonResponse, new TypeReference<>() {
        });
        assertThat(actual.content()).hasSize(1);
        assertThat(actual.content().getFirst()).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should return all books when search parameters are empty")
    @WithMockUser
    void searchBooks_EmptySearchParameters_ShouldReturnAllBooks() throws Exception {
        List<BookDto> expected = TestObjectsFactory.createThreeBookDtosList();

        String jsonResponse = mockMvc.perform(get(BASE_URL + SEARCH_PART_URL))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PageDto<BookDto> actual = objectMapper.readValue(jsonResponse, new TypeReference<>() {
        });
        assertThat(actual.content()).hasSize(3);
        assertThat(actual.content()).containsExactlyElementsOf(expected);
    }
}
