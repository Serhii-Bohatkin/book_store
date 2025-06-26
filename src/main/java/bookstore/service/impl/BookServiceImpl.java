package bookstore.service.impl;

import static bookstore.exception.EntityNotFoundException.entityNotFoundException;
import static bookstore.service.impl.CategoryServiceImpl.CATEGORY_NOT_FOUND_MESSAGE;

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
import bookstore.service.BookService;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    protected static final String BOOK_NOT_FOUND_MESSAGE = "A book with id {0} does not exist";
    private static final String BOOK_ALREADY_EXISTS_MESSAGE =
            "A book with isbn {0} already exists";
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final BookSpecificationBuilder bookSpecificationBuilder;
    private final CategoryRepository categoryRepository;

    @Transactional
    @Override
    public BookDto save(CreateBookRequestDto bookDto) {
        Book book = bookMapper.toModel(bookDto);
        if (bookRepository.existsByIsbn((book.getIsbn()))) {
            throw new EntityAlreadyExistsException(BOOK_ALREADY_EXISTS_MESSAGE, book.getIsbn());
        }
        throwExceptionIfCategoriesNotExist(bookDto.categoryIds());
        return bookMapper.toDto(bookRepository.save(book));
    }

    @Override
    public BookDto findById(Long bookId) {
        Book book = getBookOrThrow(bookId);
        return bookMapper.toDto(book);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<BookDto> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable)
                .map(bookMapper::toDto);
    }

    @Transactional
    @Override
    public BookDto update(UpdateBookRequestDto requestDto, Long bookId) {
        throwExceptionIfCategoriesNotExist(requestDto.categoryIds());
        Book book = getBookOrThrow(bookId);
        bookMapper.updateBookFromDto(book, requestDto);
        return bookMapper.toDto(bookRepository.save(book));
    }

    @Transactional
    @Override
    public void deleteById(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new EntityNotFoundException(BOOK_NOT_FOUND_MESSAGE, bookId);
        }
        bookRepository.deleteById(bookId);
    }

    @Transactional(readOnly = true)
    @Override
    public Page<BookDto> search(BookSearchParametersDto parametersDto, Pageable pageable) {
        if (isEmptySearchParameters(parametersDto)) {
            return bookRepository.findAll(pageable)
                    .map(bookMapper::toDto);
        }
        BookSearchParametersDto formattedDto = bookMapper.formatParametersDto(parametersDto);
        Specification<Book> bookSpecification = bookSpecificationBuilder.build(formattedDto);
        return bookRepository.findAll(bookSpecification, pageable)
                .map(bookMapper::toDto);
    }

    @Override
    public Page<BookDtoWithoutCategoryIds> findByCategoryId(Long categoryId, Pageable pageable) {
        throwExceptionIfCategoriesNotExist(Collections.singletonList(categoryId));
        return bookRepository.findAllByCategoryId(categoryId, pageable)
                .map(bookMapper::toDtoWithoutCategories);
    }

    private boolean isEmptySearchParameters(BookSearchParametersDto parametersDto) {
        return parametersDto.title() == null
                && parametersDto.author() == null
                && parametersDto.isbn() == null
                && parametersDto.minPrice() == null
                && parametersDto.maxPrice() == null;
    }

    private void throwExceptionIfCategoriesNotExist(List<Long> categoryIds) {
        for (Long id : categoryIds) {
            if (!categoryRepository.existsById(id)) {
                throw new EntityNotFoundException(CATEGORY_NOT_FOUND_MESSAGE, id);
            }
        }
    }

    private Book getBookOrThrow(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(entityNotFoundException(BOOK_NOT_FOUND_MESSAGE, bookId));
    }
}
