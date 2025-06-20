package bookstore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import bookstore.TestObjectsFactory;
import bookstore.dto.book.BookDto;
import bookstore.dto.book.BookDtoWithoutCategoryIds;
import bookstore.dto.book.BookSearchParametersDto;
import bookstore.dto.book.CreateBookRequestDto;
import bookstore.dto.book.UpdateBookRequestDto;
import bookstore.exception.EntityAlreadyExistsException;
import bookstore.exception.EntityNotFoundException;
import bookstore.mapper.BookMapper;
import bookstore.model.Book;
import bookstore.repository.BookRepository;
import bookstore.repository.CategoryRepository;
import bookstore.repository.book.BookSpecificationBuilder;
import bookstore.service.impl.BookServiceImpl;
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
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {
    private static final String BOOK_ALREADY_EXISTS_MESSAGE = "A book with isbn {0} already exists";
    private static final String CATEGORY_NOT_FOUND_MESSAGE =
            "A category with id {0} does not exist";
    private static final String BOOK_NOT_FOUND_MESSAGE = "A book with id {0} does not exist";
    private static final int EXPECTED_BOOKS_COUNT = 2;
    private static final Long CATEGORY_ID = 1L;
    private static final PageRequest DEFAULT_PAGE_REQUEST = PageRequest.of(0, 20);
    private static final Long BOOK_ID = 1L;

    private BookDto book1984Dto;
    private Book book1984;
    private Page<Book> twoBooksPage;
    private List<BookDto> twoBookDtoList;

    @Mock
    private BookRepository bookRepository;
    @Mock
    private BookMapper bookMapper;
    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private BookSpecificationBuilder bookSpecificationBuilder;
    @Mock
    private Specification<Book> specification;
    @InjectMocks
    private BookServiceImpl bookService;

    @BeforeEach
    void setUp() {
        book1984Dto = TestObjectsFactory.create1984BookDto("1984");
        book1984 = TestObjectsFactory.create1984Book();
        twoBooksPage = TestObjectsFactory.createTwoBooksPage();
        twoBookDtoList = List.of(TestObjectsFactory.create1984BookDto("1984"),
                TestObjectsFactory.createToKillMockingbirdBookDto());
    }

    @AfterEach
    void verifyNoUnexpectedInteractions() {
        verifyNoMoreInteractions(bookRepository, bookMapper, categoryRepository,
                bookSpecificationBuilder);
    }

    @Nested
    class SaveMethodTests {
        private CreateBookRequestDto createBookRequestDto;

        @BeforeEach
        void setUp() {
            createBookRequestDto = TestObjectsFactory.createBookRequestDto();
        }

        @Test
        @DisplayName("Should return valid BookDto when CreateBookRequestDto is valid")
        void save_ValidCreateBookRequestDto_ShouldReturnValidBookDto() {
            when(bookMapper.toModel(createBookRequestDto)).thenReturn(book1984);
            when(bookRepository.existsByIsbn(createBookRequestDto.isbn())).thenReturn(false);
            when(categoryRepository.existsById(anyLong())).thenReturn(true);
            when(bookRepository.save(book1984)).thenReturn(book1984);
            when(bookMapper.toDto(book1984)).thenReturn(book1984Dto);

            BookDto actual = bookService.save(createBookRequestDto);

            assertThat(actual).isEqualTo(book1984Dto);
            verify(bookMapper).toModel(createBookRequestDto);
            verify(bookRepository).existsByIsbn(createBookRequestDto.isbn());
            verify(categoryRepository).existsById(anyLong());
            verify(bookRepository).save(book1984);
            verify(bookMapper).toDto(book1984);
        }

        @Test
        @DisplayName("Should return valid BookDto when categoryIds is empty")
        void save_EmptyCategoryList_ShouldReturnValidBookDto() {
            createBookRequestDto = TestObjectsFactory.createBookRequestDtoWithoutCategory();
            when(bookMapper.toModel(createBookRequestDto)).thenReturn(book1984);
            when(bookRepository.existsByIsbn(createBookRequestDto.isbn())).thenReturn(false);
            when(bookRepository.save(book1984)).thenReturn(book1984);
            when(bookMapper.toDto(book1984)).thenReturn(book1984Dto);

            BookDto actual = bookService.save(createBookRequestDto);

            assertThat(actual).isEqualTo(book1984Dto);
            verify(bookMapper).toModel(createBookRequestDto);
            verify(bookRepository).existsByIsbn(createBookRequestDto.isbn());
            verify(bookRepository).save(book1984);
            verify(bookMapper).toDto(book1984);
        }

        @Test
        @DisplayName("Should throw EntityAlreadyExistsException when ISBN already exists")
        void save_WhenIsbnAlreadyExists_ShouldThrowEntityAlreadyExistsException() {
            when(bookMapper.toModel(createBookRequestDto)).thenReturn(book1984);
            when(bookRepository.existsByIsbn(createBookRequestDto.isbn())).thenReturn(true);

            EntityAlreadyExistsException ex = assertThrows(EntityAlreadyExistsException.class,
                    () -> bookService.save(createBookRequestDto));

            assertThat(ex.getMessage()).isEqualTo(
                    MessageFormat.format(BOOK_ALREADY_EXISTS_MESSAGE, createBookRequestDto.isbn()));
            verify(bookMapper).toModel(createBookRequestDto);
            verify(bookRepository).existsByIsbn(createBookRequestDto.isbn());
            verify(bookRepository, never()).save(any(Book.class));
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when category ID is invalid")
        void save_InvalidCategoryId_ShouldThrowEntityNotFoundException() {
            when(bookMapper.toModel(createBookRequestDto)).thenReturn(book1984);
            when(bookRepository.existsByIsbn(createBookRequestDto.isbn())).thenReturn(false);
            when(categoryRepository.existsById(
                    createBookRequestDto.categoryIds().getFirst())).thenReturn(false);

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> bookService.save(createBookRequestDto));

            assertThat(ex.getMessage()).isEqualTo(
                    MessageFormat.format(CATEGORY_NOT_FOUND_MESSAGE,
                            createBookRequestDto.categoryIds().getFirst()));
            verify(bookMapper).toModel(createBookRequestDto);
            verify(bookRepository).existsByIsbn(createBookRequestDto.isbn());
            verify(categoryRepository).existsById(createBookRequestDto.categoryIds().getFirst());
            verify(bookRepository, never()).save(any(Book.class));
        }
    }

    @Nested
    class FindByIdMethodTests {
        @Test
        @DisplayName("Should return valid BookDto when book ID exists")
        void findById_ValidBookId_ShouldReturnValidBookDto() {
            when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(book1984));
            when(bookMapper.toDto(book1984)).thenReturn(book1984Dto);

            BookDto actual = bookService.findById(BOOK_ID);

            assertThat(actual).isEqualTo(book1984Dto);
            verify(bookRepository).findById(BOOK_ID);
            verify(bookMapper).toDto(book1984);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when book ID does not exist")
        void findById_InvalidBookId_ShouldThrowEntityNotFoundException() {
            when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> bookService.findById(BOOK_ID));

            assertThat(ex.getMessage()).isEqualTo(
                    MessageFormat.format(BOOK_NOT_FOUND_MESSAGE, BOOK_ID));
            verify(bookRepository).findById(BOOK_ID);
        }
    }

    @Nested
    class FindAllMethodTests {
        @Test
        @DisplayName("Should return two BookDto when books are found")
        void findAll_ShouldReturnTwoBooks() {
            when(bookRepository.findAll(DEFAULT_PAGE_REQUEST)).thenReturn(twoBooksPage);
            when(bookMapper.toDto(any(Book.class))).thenReturn(twoBookDtoList.get(0),
                    twoBookDtoList.get(1));

            Page<BookDto> actual = bookService.findAll(DEFAULT_PAGE_REQUEST);

            assertThat(actual.getContent()).containsExactlyElementsOf(twoBookDtoList);
            assertThat(actual).hasSize(EXPECTED_BOOKS_COUNT);
            verify(bookRepository).findAll(DEFAULT_PAGE_REQUEST);
            verify(bookMapper, times(EXPECTED_BOOKS_COUNT)).toDto(any(Book.class));
        }

        @Test
        @DisplayName("Should return empty page when no books are found")
        void findAll_NoBooksFound_ShouldReturnEmptyPage() {
            when(bookRepository.findAll(DEFAULT_PAGE_REQUEST)).thenReturn(Page.empty());

            Page<BookDto> actual = bookService.findAll(DEFAULT_PAGE_REQUEST);

            assertThat(actual).isEmpty();
            verify(bookRepository).findAll(DEFAULT_PAGE_REQUEST);
        }
    }

    @Nested
    class UpdateMethodTests {
        private UpdateBookRequestDto updateBookRequestDto;

        @BeforeEach
        void setUp() {
            updateBookRequestDto = TestObjectsFactory.createUpdateBookRequestDto("1984");
        }

        @Test
        @DisplayName("Should return updated BookDto when request and book ID are valid")
        void update_ValidRequestDtoAndValidBookId_ShouldReturnBookDto() {
            Long categoryId = updateBookRequestDto.categoryIds().getFirst();
            when(categoryRepository.existsById(categoryId)).thenReturn(true);
            when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.of(book1984));
            doNothing().when(bookMapper).updateBookFromDto(book1984, updateBookRequestDto);
            when(bookRepository.save(book1984)).thenReturn(book1984);
            when(bookMapper.toDto(book1984)).thenReturn(book1984Dto);

            BookDto actual = bookService.update(updateBookRequestDto, BOOK_ID);

            assertThat(actual).isEqualTo(book1984Dto);
            verify(categoryRepository).existsById(categoryId);
            verify(bookRepository).findById(BOOK_ID);
            verify(bookMapper).updateBookFromDto(book1984, updateBookRequestDto);
            verify(bookRepository).save(book1984);
            verify(bookMapper).toDto(book1984);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when category ID is invalid")
        void update_InvalidCategoryId_ShouldThrowEntityNotFoundException() {
            Long categoryId = updateBookRequestDto.categoryIds().getFirst();
            when(categoryRepository.existsById(categoryId)).thenReturn(false);

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> bookService.update(updateBookRequestDto, BOOK_ID));

            assertThat(ex.getMessage()).isEqualTo(
                    MessageFormat.format(CATEGORY_NOT_FOUND_MESSAGE, categoryId));
            verify(categoryRepository).existsById(categoryId);
            verify(bookRepository, never()).save(any(Book.class));
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when book ID is invalid")
        void update_InvalidBookId_ShouldThrowEntityNotFoundException() {
            when(categoryRepository.existsById(CATEGORY_ID)).thenReturn(true);
            when(bookRepository.findById(BOOK_ID)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> bookService.update(updateBookRequestDto, BOOK_ID));

            assertThat(ex.getMessage()).isEqualTo(
                    MessageFormat.format(BOOK_NOT_FOUND_MESSAGE, BOOK_ID));
            verify(categoryRepository).existsById(CATEGORY_ID);
            verify(bookRepository).findById(BOOK_ID);
            verify(bookRepository, never()).save(any(Book.class));
        }
    }

    @Nested
    class DeleteByIdMethodTests {
        @Test
        @DisplayName("Should delete book when book ID is valid")
        void deleteById_ValidBookId_ShouldDeleteBook() {
            when(bookRepository.existsById(BOOK_ID)).thenReturn(true);

            bookService.deleteById(BOOK_ID);

            verify(bookRepository).existsById(BOOK_ID);
            verify(bookRepository).deleteById(BOOK_ID);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when book ID is invalid")
        void deleteById_InvalidBookId_ShouldThrowEntityNotFoundException() {
            when(bookRepository.existsById(BOOK_ID)).thenReturn(false);

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> bookService.deleteById(BOOK_ID));

            assertThat(ex.getMessage()).isEqualTo(
                    MessageFormat.format(BOOK_NOT_FOUND_MESSAGE, BOOK_ID));
            verify(bookRepository).existsById(BOOK_ID);
            verify(bookRepository, never()).deleteById(anyLong());
        }
    }

    @Nested
    class SearchMethodTests {
        @Test
        @DisplayName("Should return results from findAll() when search parameters are empty")
        void search_EmptyParameters_ShouldReturnResultFromFindAll() {
            BookSearchParametersDto emptyParametersDto =
                    TestObjectsFactory.createEmptySearchParameters();
            when(bookRepository.findAll(DEFAULT_PAGE_REQUEST)).thenReturn(twoBooksPage);
            when(bookMapper.toDto(any(Book.class))).thenReturn(twoBookDtoList.get(0),
                    twoBookDtoList.get(1));

            Page<BookDto> actual =
                    bookService.search(emptyParametersDto, DEFAULT_PAGE_REQUEST);

            assertThat(actual.getContent()).hasSize(EXPECTED_BOOKS_COUNT);
            assertThat(actual.getContent()).containsExactlyElementsOf(twoBookDtoList);
            verify(bookRepository).findAll(DEFAULT_PAGE_REQUEST);
            verify(bookMapper, times(EXPECTED_BOOKS_COUNT)).toDto(any(Book.class));
        }

        @Test
        @DisplayName("Should use specification and return page with one BookDto")
        void search_ValidParameters_ShouldReturnPageWithOneBook() {
            BookSearchParametersDto searchParametersDto =
                    TestObjectsFactory.createSearchParameters("1984");
            Page<Book> bookPage = TestObjectsFactory.createOneBookPage();
            when(bookMapper.formatParametersDto(searchParametersDto)).thenReturn(
                    searchParametersDto);
            when(bookSpecificationBuilder.build(searchParametersDto)).thenReturn(specification);
            when(bookRepository.findAll(specification, DEFAULT_PAGE_REQUEST)).thenReturn(bookPage);
            when(bookMapper.toDto(bookPage.getContent().getFirst())).thenReturn(book1984Dto);

            Page<BookDto> actual = bookService.search(searchParametersDto, DEFAULT_PAGE_REQUEST);

            assertThat(actual.getContent()).containsExactly(book1984Dto);
            verify(bookMapper).formatParametersDto(searchParametersDto);
            verify(bookSpecificationBuilder).build(searchParametersDto);
            verify(bookRepository).findAll(specification, DEFAULT_PAGE_REQUEST);
            verify(bookMapper).toDto(bookPage.getContent().getFirst());
        }
    }

    @Nested
    class FindByCategoryIdMethodTests {
        @Test
        @DisplayName("Should return page with two BookDto for valid category ID")
        void findByCategoryId_ValidCategoryId_ShouldReturnMappedPage() {
            List<BookDtoWithoutCategoryIds> dtoList = List.of(
                    TestObjectsFactory.create1984BookDtoWithoutCategory(),
                    TestObjectsFactory.createToKillMockingbirdDtoWithoutCategory());

            when(categoryRepository.existsById(CATEGORY_ID)).thenReturn(true);
            when(bookRepository.findAllByCategoryId(CATEGORY_ID, DEFAULT_PAGE_REQUEST)).thenReturn(
                    twoBooksPage);
            when(bookMapper.toDtoWithoutCategories(any(Book.class)))
                    .thenReturn(dtoList.get(0), dtoList.get(1));

            Page<BookDtoWithoutCategoryIds> actual =
                    bookService.findByCategoryId(CATEGORY_ID, DEFAULT_PAGE_REQUEST);

            assertThat(actual).hasSize(EXPECTED_BOOKS_COUNT);
            assertThat(actual.getContent()).containsExactlyElementsOf(dtoList);
            verify(categoryRepository).existsById(CATEGORY_ID);
            verify(bookRepository).findAllByCategoryId(CATEGORY_ID, DEFAULT_PAGE_REQUEST);
            verify(bookMapper, times(EXPECTED_BOOKS_COUNT)).toDtoWithoutCategories(any(Book.class));
        }

        @Test
        @DisplayName("Should return page with two BookDto for valid category ID")
        void findByCategoryId_InvalidCategoryId_ShouldThrowEntityNotFoundException() {
            when(categoryRepository.existsById(CATEGORY_ID)).thenReturn(false);

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> bookService.findByCategoryId(CATEGORY_ID, DEFAULT_PAGE_REQUEST));

            assertThat(ex.getMessage()).isEqualTo(
                    MessageFormat.format(CATEGORY_NOT_FOUND_MESSAGE, CATEGORY_ID));
            verify(categoryRepository).existsById(CATEGORY_ID);
        }

        @Test
        @DisplayName("Should return empty page when no books are found")
        void findByCategoryId_NoBooksFound_ShouldReturnEmptyPage() {
            when(categoryRepository.existsById(CATEGORY_ID)).thenReturn(true);
            when(bookRepository.findAllByCategoryId(CATEGORY_ID, DEFAULT_PAGE_REQUEST))
                    .thenReturn(Page.empty());

            Page<BookDtoWithoutCategoryIds> actual =
                    bookService.findByCategoryId(CATEGORY_ID, DEFAULT_PAGE_REQUEST);

            assertThat(actual).isEmpty();
            verify(categoryRepository).existsById(CATEGORY_ID);
            verify(bookRepository).findAllByCategoryId(CATEGORY_ID, DEFAULT_PAGE_REQUEST);
            verify(bookMapper, never()).toDtoWithoutCategories(any(Book.class));
        }
    }
}
