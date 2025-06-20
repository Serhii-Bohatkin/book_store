package bookstore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import bookstore.TestObjectsFactory;
import bookstore.dto.category.CategoryDto;
import bookstore.dto.category.CreateCategoryRequestDto;
import bookstore.dto.category.UpdateCategoryRequestDto;
import bookstore.exception.EntityAlreadyExistsException;
import bookstore.exception.EntityNotFoundException;
import bookstore.mapper.CategoryMapper;
import bookstore.model.Category;
import bookstore.repository.CategoryRepository;
import bookstore.service.impl.CategoryServiceImpl;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class CategoryServiceImplTest {
    private static final String CATEGORY_NOT_FOUND_MESSAGE =
            "A category with id {0} does not exist";
    private static final String CATEGORY_ALREADY_EXISTS_MESSAGE =
            "A category with name {0} already exists";
    private static final String DEFAULT_CATEGORY_NAME = "Fiction";
    private static final String MISTAKEN_CATEGORY_NAME = "oops!";
    private static final PageRequest DEFAULT_PAGE_REQUEST = PageRequest.of(0, 20);
    private static final int EXPECTED_CATEGORIES_COUNT = 2;
    private static final Long CATEGORY_ID = 1L;

    private CategoryDto fictionCategoryDto;
    private Category fictionCategory;

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private CategoryMapper categoryMapper;
    @InjectMocks
    private CategoryServiceImpl categoryService;

    @BeforeEach
    void setUp() {
        fictionCategory = TestObjectsFactory.createFictionCategory();
        fictionCategoryDto = TestObjectsFactory.createFictionCategoryDto();
    }

    @AfterEach
    void verifyNoUnexpectedInteractions() {
        verifyNoMoreInteractions(categoryRepository, categoryMapper);
    }

    @Nested
    class FindAllMethodTests {
        @Test
        @DisplayName("Should return a page with two categories when data exists")
        void findAll_ValidPageableRequest_ShouldReturnTwoCategories() {
            Page<Category> twoCategoriesPage = TestObjectsFactory.createTwoCategoriesPage();
            List<CategoryDto> twoCategoryDtosList =
                    List.of(TestObjectsFactory.createFictionCategoryDto(),
                            TestObjectsFactory.createFantasyCategoryDto());
            when(categoryRepository.findAll(DEFAULT_PAGE_REQUEST)).thenReturn(twoCategoriesPage);
            when(categoryMapper.toDto(any(Category.class)))
                    .thenReturn(twoCategoryDtosList.get(0), twoCategoryDtosList.get(1));

            Page<CategoryDto> actual = categoryService.findAll(DEFAULT_PAGE_REQUEST);

            assertThat(actual).hasSize(EXPECTED_CATEGORIES_COUNT);
            assertThat(actual.getContent()).containsExactlyElementsOf(twoCategoryDtosList);
            verify(categoryRepository).findAll(DEFAULT_PAGE_REQUEST);
            verify(categoryMapper, times(EXPECTED_CATEGORIES_COUNT)).toDto(any(Category.class));
        }

        @Test
        @DisplayName("Should return an empty page when no categories are found")
        void findAll_NoCategoriesFound_ShouldReturnEmptyPage() {
            when(categoryRepository.findAll(DEFAULT_PAGE_REQUEST)).thenReturn(Page.empty());

            Page<CategoryDto> actual = categoryService.findAll(DEFAULT_PAGE_REQUEST);

            assertThat(actual.getContent()).isEmpty();
            verify(categoryRepository).findAll(DEFAULT_PAGE_REQUEST);
            verify(categoryMapper, never()).toDto(any(Category.class));
        }
    }

    @Nested
    class GetByIdMethodTests {
        @Test
        @DisplayName("Should return CategoryDto when a valid ID is provided")
        void getById_ValidCategoryId_ShouldReturnCategoryDto() {
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(fictionCategory));
            when(categoryMapper.toDto(fictionCategory)).thenReturn(fictionCategoryDto);

            CategoryDto actual = categoryService.getById(CATEGORY_ID);

            assertThat(actual).isEqualTo(fictionCategoryDto);
            verify(categoryRepository).findById(CATEGORY_ID);
            verify(categoryMapper).toDto(fictionCategory);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException for an invalid category ID")
        void getById_InvalidCategoryId_ShouldThrowEntityNotFoundException() {
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> categoryService.getById(CATEGORY_ID));

            assertThat(ex.getMessage()).isEqualTo(
                    MessageFormat.format(CATEGORY_NOT_FOUND_MESSAGE, CATEGORY_ID));
            verify(categoryRepository).findById(CATEGORY_ID);
            verify(categoryMapper, never()).toDto(any(Category.class));
        }
    }

    @Nested
    class SaveMethodTests {
        private CreateCategoryRequestDto createCategoryRequestDto;

        @BeforeEach
        void setUp() {
            createCategoryRequestDto = TestObjectsFactory.createCategoryRequestDto();
        }

        @Test
        @DisplayName("Should save and return CategoryDto when the name is unique")
        void save_ValidRequestDto_ShouldReturnCategoryDto() {
            when(categoryRepository.existsByNameIgnoreCase(DEFAULT_CATEGORY_NAME)).thenReturn(
                    false);
            when(categoryMapper.toModel(createCategoryRequestDto)).thenReturn(fictionCategory);
            when(categoryRepository.save(fictionCategory)).thenReturn(fictionCategory);
            when(categoryMapper.toDto(fictionCategory)).thenReturn(fictionCategoryDto);

            CategoryDto actual = categoryService.save(createCategoryRequestDto);

            assertThat(actual).isEqualTo(fictionCategoryDto);
            verify(categoryRepository).existsByNameIgnoreCase(DEFAULT_CATEGORY_NAME);
            verify(categoryMapper).toModel(createCategoryRequestDto);
            verify(categoryRepository).save(fictionCategory);
            verify(categoryMapper).toDto(fictionCategory);
        }

        @Test
        @DisplayName("Should throw EntityAlreadyExistsException for existing name")
        void save_CategoryAlreadyExists_ShouldThrowEntityAlreadyExistsException() {
            when(categoryRepository.existsByNameIgnoreCase(DEFAULT_CATEGORY_NAME)).thenReturn(true);

            EntityAlreadyExistsException ex = assertThrows(EntityAlreadyExistsException.class,
                    () -> categoryService.save(createCategoryRequestDto));

            assertThat(ex.getMessage()).isEqualTo(
                    MessageFormat.format(CATEGORY_ALREADY_EXISTS_MESSAGE, DEFAULT_CATEGORY_NAME));
            verify(categoryRepository).existsByNameIgnoreCase(DEFAULT_CATEGORY_NAME);
            verify(categoryRepository, never()).save(any(Category.class));
        }
    }

    @Nested
    class UpdateMethodTests {
        private UpdateCategoryRequestDto updateCategoryRequestDto;

        @BeforeEach
        void setUp() {
            updateCategoryRequestDto = TestObjectsFactory.createUpdateCategoryRequestDto();
        }

        @Test
        @DisplayName("Should return CategoryDto for a valid ID and same category name")
        void update_ValidCategoryIdAndSameName_ShouldNotCheckForExistence() {
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(fictionCategory));
            doNothing().when(categoryMapper)
                    .updateCategory(fictionCategory, updateCategoryRequestDto);
            when(categoryRepository.save(fictionCategory)).thenReturn(fictionCategory);
            when(categoryMapper.toDto(fictionCategory)).thenReturn(fictionCategoryDto);

            CategoryDto actual = categoryService.update(CATEGORY_ID, updateCategoryRequestDto);

            assertThat(actual).isEqualTo(fictionCategoryDto);
            verify(categoryRepository).findById(CATEGORY_ID);
            verify(categoryRepository, never()).existsByNameIgnoreCase(anyString());
            verify(categoryMapper).updateCategory(fictionCategory, updateCategoryRequestDto);
            verify(categoryRepository).save(fictionCategory);
            verify(categoryMapper).toDto(fictionCategory);
        }

        @Test
        @DisplayName("Should return CategoryDto for a valid ID and new category name")
        void update_ValidIdAndNewCategoryName_ShouldReturnCategoryDto() {
            fictionCategory.setName(MISTAKEN_CATEGORY_NAME);
            String newCategoryName = updateCategoryRequestDto.name();

            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.of(fictionCategory));
            when(categoryRepository.existsByNameIgnoreCase(newCategoryName)).thenReturn(false);
            doNothing().when(categoryMapper)
                    .updateCategory(fictionCategory, updateCategoryRequestDto);
            when(categoryRepository.save(fictionCategory)).thenReturn(fictionCategory);
            when(categoryMapper.toDto(fictionCategory)).thenReturn(fictionCategoryDto);

            CategoryDto actual = categoryService.update(CATEGORY_ID, updateCategoryRequestDto);

            assertThat(actual).isEqualTo(fictionCategoryDto);
            verify(categoryRepository).findById(CATEGORY_ID);
            verify(categoryRepository).existsByNameIgnoreCase(newCategoryName);
            verify(categoryMapper).updateCategory(fictionCategory, updateCategoryRequestDto);
            verify(categoryRepository).save(fictionCategory);
            verify(categoryMapper).toDto(fictionCategory);
        }

        @Test
        @DisplayName("Should throw EntityAlreadyExistsException if name already exists")
        void update_CategoryWithSuchNameAlreadyExists_ShouldThrowEntityAlreadyExistsException() {
            String newCategoryName = updateCategoryRequestDto.name();
            Category nonFictionCategory = TestObjectsFactory.createNonFictionCategory();
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(
                    Optional.of(nonFictionCategory));
            when(categoryRepository.existsByNameIgnoreCase(newCategoryName)).thenReturn(true);

            EntityAlreadyExistsException ex = assertThrows(EntityAlreadyExistsException.class,
                    () -> categoryService.update(CATEGORY_ID, updateCategoryRequestDto));
            assertThat(ex.getMessage()).isEqualTo(
                    MessageFormat.format(CATEGORY_ALREADY_EXISTS_MESSAGE, newCategoryName));
            verify(categoryRepository).findById(CATEGORY_ID);
            verify(categoryRepository).existsByNameIgnoreCase(newCategoryName);
            verify(categoryRepository, never()).save(any(Category.class));
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException for an invalid category ID")
        void update_InvalidCategoryId_ShouldThrowEntityNotFoundException() {
            when(categoryRepository.findById(CATEGORY_ID)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> categoryService.update(CATEGORY_ID, updateCategoryRequestDto));

            assertThat(ex.getMessage()).isEqualTo(
                    MessageFormat.format(CATEGORY_NOT_FOUND_MESSAGE, CATEGORY_ID));
            verify(categoryRepository).findById(CATEGORY_ID);
            verify(categoryRepository, never()).save(any(Category.class));
        }
    }

    @Nested
    class DeleteByIdMethodTests {
        @Test
        @DisplayName("Should delete category when a valid ID is provided")
        void deleteById_ValidCategoryId_ShouldDeleteCategory() {
            when(categoryRepository.existsById(CATEGORY_ID)).thenReturn(true);
            doNothing().when(categoryRepository).deleteById(CATEGORY_ID);

            categoryService.deleteById(CATEGORY_ID);

            verify(categoryRepository).existsById(CATEGORY_ID);
            verify(categoryRepository).deleteById(CATEGORY_ID);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException for an invalid category ID")
        void deleteById_InvalidCategoryId_ShouldThrowEntityNotFoundException() {
            when(categoryRepository.existsById(CATEGORY_ID)).thenReturn(false);

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> categoryService.deleteById(CATEGORY_ID));

            assertThat(ex.getMessage()).isEqualTo(
                    MessageFormat.format(CATEGORY_NOT_FOUND_MESSAGE, CATEGORY_ID));
            verify(categoryRepository).existsById(CATEGORY_ID);
            verify(categoryRepository, never()).deleteById(anyLong());
        }
    }
}
