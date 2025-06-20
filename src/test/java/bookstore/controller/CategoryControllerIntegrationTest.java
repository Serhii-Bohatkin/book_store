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
import bookstore.dto.book.BookDtoWithoutCategoryIds;
import bookstore.dto.category.CategoryDto;
import bookstore.dto.category.CreateCategoryRequestDto;
import bookstore.dto.category.UpdateCategoryRequestDto;
import bookstore.dto.page.PageDto;
import bookstore.model.Category;
import bookstore.repository.CategoryRepository;
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
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql(scripts = {
        "classpath:database/clear-db.sql",
        "classpath:database/insert-categories.sql"}, executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/clear-db.sql", executionPhase = AFTER_TEST_METHOD)
class CategoryControllerIntegrationTest {
    private static final String CATEGORY_ALREADY_EXISTS_MESSAGE =
            "A category with name {0} already exists";
    private static final String CATEGORY_NOT_FOUND_MESSAGE =
            "A category with id {0} does not exist";
    private static final String NAME_MUST_NOT_BE_BLANK_MESSAGE = "name must not be blank";
    private static final String CATEGORY_ID_MUST_BE_POSITIVE_MESSAGE =
            "categoryId must be greater than or equal to 1";
    private static final String BASE_URL = "/categories";
    private static final String BOOKS_PART_URL = "/books";
    private static final String CATEGORY_ID_PARAM = "/{categoryId}";

    private static final Long VALID_CATEGORY_ID = 1L;
    private static final Long NEGATIVE_CATEGORY_ID = Long.MIN_VALUE;
    private static final Long NON_EXISTING_CATEGORY_ID = Long.MAX_VALUE;
    private UpdateCategoryRequestDto updateDto;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CategoryRepository categoryRepository;

    @BeforeEach
    void setUp() {
        updateDto = TestObjectsFactory.createUpdateCategoryRequestDto();
    }

    @Test
    @DisplayName("Should create category and return CategoryDto for valid request")
    @WithMockUser(value = "admin@gmail.com", authorities = "ADMIN")
    @Sql(scripts = "classpath:database/clear-db.sql", executionPhase = BEFORE_TEST_METHOD)
    void createCategory_ValidRequestDto_ShouldReturnCategoryDto() throws Exception {
        CreateCategoryRequestDto requestDto = TestObjectsFactory.createCategoryRequestDto();
        CategoryDto expected = TestObjectsFactory.createFictionCategoryDto();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        String jsonResponse = mockMvc.perform(post(BASE_URL)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CategoryDto actual = objectMapper.readValue(jsonResponse, CategoryDto.class);
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("categoryId")
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("Should return 409 Conflict when category with the same name already exists")
    @WithMockUser(value = "admin@gmail.com", authorities = "ADMIN")
    void createCategory_CategoryAlreadyExists_Conflict() throws Exception {
        CreateCategoryRequestDto requestDto = TestObjectsFactory.createCategoryRequestDto();
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        String jsonResponse = mockMvc.perform(post(BASE_URL)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(MessageFormat.format(
                CATEGORY_ALREADY_EXISTS_MESSAGE, requestDto.name()));
    }

    @Test
    @DisplayName("Should return 400 Bad Request for invalid category creation request")
    @WithMockUser(value = "admin@gmail.com", authorities = "ADMIN")
    void createCategory_InvalidRequestDto_BadRequest() throws Exception {
        CreateCategoryRequestDto requestDto = new CreateCategoryRequestDto("", null);
        String jsonRequest = objectMapper.writeValueAsString(requestDto);

        String jsonResponse = mockMvc.perform(post(BASE_URL)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(NAME_MUST_NOT_BE_BLANK_MESSAGE);
    }

    @Test
    @DisplayName("Should return two categories")
    @WithMockUser
    void getAll_ShouldReturnPageDtoWithTwoCategoryDtos() throws Exception {
        List<CategoryDto> expected = TestObjectsFactory.createTwoCategoryDtosList();

        String jsonResponse = mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();

        PageDto<CategoryDto> actual =
                objectMapper.readValue(jsonResponse, new TypeReference<>() {
                });
        assertThat(actual.content()).containsExactlyElementsOf(expected);
    }

    @Test
    @DisplayName("Should return an empty page when no categories are found")
    @WithMockUser
    @Sql(scripts = "classpath:database/clear-db.sql", executionPhase = BEFORE_TEST_METHOD)
    void getAll_CategoriesNotFound_ShouldReturnEmptyPageDto() throws Exception {
        String jsonResponse = mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PageDto<CategoryDto> actual =
                objectMapper.readValue(jsonResponse, new TypeReference<>() {
                });
        assertThat(actual.content()).isEmpty();
    }

    @Test
    @DisplayName("Should return category for a valid category ID")
    @WithMockUser
    void getCategoryById_ValidCategoryId_ShouldReturnCategoryDto() throws Exception {
        CategoryDto expected = TestObjectsFactory.createFictionCategoryDto();

        String jsonResponse = mockMvc.perform(get(
                        BASE_URL + CATEGORY_ID_PARAM, VALID_CATEGORY_ID))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CategoryDto actual = objectMapper.readValue(jsonResponse, CategoryDto.class);
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("categoryId")
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("Should return 400 Bad Request for a negative category ID")
    @WithMockUser
    void getCategoryById_InvalidCategoryId_BadRequest() throws Exception {
        String jsonResponse = mockMvc.perform(get(
                        BASE_URL + CATEGORY_ID_PARAM, NEGATIVE_CATEGORY_ID))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(CATEGORY_ID_MUST_BE_POSITIVE_MESSAGE);
    }

    @Test
    @DisplayName("Should return 404 Not Found for a non-existing category ID")
    @WithMockUser
    void getCategoryById_CategoryDoesNotExist_NotFound() throws Exception {
        String jsonResponse = mockMvc.perform(get(
                        BASE_URL + CATEGORY_ID_PARAM, NON_EXISTING_CATEGORY_ID))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(MessageFormat.format(
                CATEGORY_NOT_FOUND_MESSAGE, NON_EXISTING_CATEGORY_ID));
    }

    @Test
    @DisplayName("Should update category and return CategoryDto for valid ID and request")
    @WithMockUser(value = "admin@gmail.com", authorities = "ADMIN")
    void updateCategory_ValidIdAndValidRequestDto_ShouldReturnCategoryDto() throws Exception {
        String jsonRequest = objectMapper.writeValueAsString(updateDto);
        CategoryDto expected = TestObjectsFactory.createFictionCategoryDto();

        String jsonResponse = mockMvc.perform(patch(
                        BASE_URL + CATEGORY_ID_PARAM, VALID_CATEGORY_ID)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CategoryDto actual = objectMapper.readValue(jsonResponse, CategoryDto.class);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should return 404 Not Found when trying to update a non-existing category")
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    void updateCategory_CategoryDoesNotExist_NotFound() throws Exception {
        String jsonRequest = objectMapper.writeValueAsString(updateDto);

        String jsonResponse = mockMvc.perform(patch(
                        BASE_URL + CATEGORY_ID_PARAM, NON_EXISTING_CATEGORY_ID)
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
    void updateCategory_InvalidCategoryId_BadRequest() throws Exception {
        String jsonRequest = objectMapper.writeValueAsString(updateDto);

        String jsonResponse = mockMvc.perform(patch(
                        BASE_URL + CATEGORY_ID_PARAM, NEGATIVE_CATEGORY_ID)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(CATEGORY_ID_MUST_BE_POSITIVE_MESSAGE);
    }

    @Test
    @DisplayName("Should delete a category and return 204 No Content for a valid ID")
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    void deleteById_ValidCategoryId_NoContent() throws Exception {
        mockMvc.perform(delete(BASE_URL + CATEGORY_ID_PARAM, VALID_CATEGORY_ID))
                .andExpect(status().isNoContent());

        Optional<Category> actual = categoryRepository.findById(VALID_CATEGORY_ID);
        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("Should return 404 Not Found when trying to delete a non-existing category")
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    void deleteById_CategoryDoesNotExist_NotFound() throws Exception {
        String jsonResponse = mockMvc.perform(delete(
                        BASE_URL + CATEGORY_ID_PARAM, NON_EXISTING_CATEGORY_ID))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(MessageFormat.format(
                CATEGORY_NOT_FOUND_MESSAGE, NON_EXISTING_CATEGORY_ID));
    }

    @Test
    @DisplayName("Should return 400 Bad Request for a negative category ID on delete")
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    void deleteById_InvalidCategoryId_BadRequest() throws Exception {
        String jsonResponse = mockMvc.perform(delete(
                        BASE_URL + CATEGORY_ID_PARAM, NEGATIVE_CATEGORY_ID))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(CATEGORY_ID_MUST_BE_POSITIVE_MESSAGE);
    }

    @Test
    @DisplayName("Should return books assigned to category for valid category ID")
    @WithMockUser
    @Sql(scripts = {
            "classpath:database/insert-books.sql",
            "classpath:database/link-books-to-categories.sql"}, executionPhase = BEFORE_TEST_METHOD)
    void getBooksByCategoryId_ValidCategoryId_ShouldReturnTwoBookDto() throws Exception {
        List<BookDtoWithoutCategoryIds> expected =
                TestObjectsFactory.createTwoBookDtoWithoutCategoryIdsList();
        String jsonResponse = mockMvc.perform(get(
                        BASE_URL + CATEGORY_ID_PARAM + BOOKS_PART_URL, VALID_CATEGORY_ID))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PageDto<BookDtoWithoutCategoryIds> actual =
                objectMapper.readValue(jsonResponse, new TypeReference<>() {
                });
        assertThat(actual.content()).containsExactlyElementsOf(expected);
    }

    @Test
    @DisplayName("Should return 404 Not Found when retrieving books for a non-existing category ID")
    @WithMockUser
    void getBooksByCategoryId_CategoryDoesNotExist_NotFound() throws Exception {
        String jsonResponse = mockMvc.perform(get(
                        BASE_URL + CATEGORY_ID_PARAM + BOOKS_PART_URL, NON_EXISTING_CATEGORY_ID))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(MessageFormat.format(
                CATEGORY_NOT_FOUND_MESSAGE, NON_EXISTING_CATEGORY_ID));
    }

    @Test
    @DisplayName("Should return 400 Bad Request for a negative category ID when retrieving books")
    @WithMockUser
    void getBooksByCategoryId_InvalidCategoryId_BadRequest() throws Exception {
        String jsonResponse = mockMvc.perform(get(
                        BASE_URL + CATEGORY_ID_PARAM + BOOKS_PART_URL, NEGATIVE_CATEGORY_ID))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(CATEGORY_ID_MUST_BE_POSITIVE_MESSAGE);
    }
}
