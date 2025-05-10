package bookstore.service.impl;

import static bookstore.exception.EntityNotFoundException.entityNotFoundException;

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
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {
    private static final String BOOK_NOT_FOUND_MESSAGE = "A book with id {0} does not exist";
    private static final String BOOK_ALREADY_EXISTS_MESSAGE =
            "A book with isbn {0} already exists";
    private static final String CATEGORY_NOT_FOUND_MESSAGE =
            "A category with id {0} does not exist";
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final BookSpecificationBuilder bookSpecificationBuilder;
    private final CategoryRepository categoryRepository;

    @Override
    public BookDto save(CreateBookRequestDto bookDto) {
        Book book = bookMapper.toModel(bookDto);
        if (bookRepository.existsByIsbn((book.getIsbn()))) {
            throw new EntityAlreadyExistsException(
                    BOOK_ALREADY_EXISTS_MESSAGE, book.getIsbn());
        }
        checkCategoriesForExistingOrThrow(bookDto.categoryIds());
        return bookMapper.toDto(bookRepository.save(book));
    }

    @Override
    public BookDto findById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(entityNotFoundException(BOOK_NOT_FOUND_MESSAGE, id));
        return bookMapper.toDto(book);
    }

    @Override
    public Page<BookDto> findAll(Pageable pageable) {
        return bookRepository.findAll(pageable)
                .map(bookMapper::toDto);
    }

    @Override
    public BookDto update(UpdateBookRequestDto requestDto, Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(entityNotFoundException(BOOK_NOT_FOUND_MESSAGE, id));
        checkCategoriesForExistingOrThrow(requestDto.categoryIds());
        bookMapper.updateBookFromDto(book, requestDto);
        return bookMapper.toDto(bookRepository.save(book));
    }

    @Override
    public void deleteById(Long id) {
        if (!bookRepository.existsById(id)) {
            throw new EntityNotFoundException(BOOK_NOT_FOUND_MESSAGE, id);
        }
        bookRepository.deleteById(id);
    }

    @Override
    public Page<BookDto> search(BookSearchParametersDto parametersDto, Pageable pageable) {
        if (isEmptySearch(parametersDto)) {
            return findAll(pageable);
        }
        BookSearchParametersDto formattedDto = bookMapper.formatParametersDto(parametersDto);
        Specification<Book> bookSpecification = bookSpecificationBuilder.build(formattedDto);
        return bookRepository.findAll(bookSpecification, pageable)
                .map(bookMapper::toDto);
    }

    @Override
    public Page<BookDtoWithoutCategoryIds> findByCategoryId(Long id, Pageable pageable) {
        return bookRepository.findAllByCategoryId(id, pageable)
                .map(bookMapper::toDtoWithoutCategories);
    }

    private boolean isEmptySearch(BookSearchParametersDto parametersDto) {
        return parametersDto.title() == null
                && parametersDto.author() == null
                && parametersDto.isbn() == null
                && parametersDto.minPrice() == null
                && parametersDto.maxPrice() == null;
    }

    private void checkCategoriesForExistingOrThrow(List<Long> categoryIds) {
        for (Long id : categoryIds) {
            if (!categoryRepository.existsById(id)) {
                throw new EntityNotFoundException(CATEGORY_NOT_FOUND_MESSAGE, id);
            }
        }
    }
}
