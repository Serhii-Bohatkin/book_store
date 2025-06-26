package bookstore.controller;

import bookstore.controller.api.BookControllerApi;
import bookstore.dto.book.BookDto;
import bookstore.dto.book.BookSearchParametersDto;
import bookstore.dto.book.CreateBookRequestDto;
import bookstore.dto.book.UpdateBookRequestDto;
import bookstore.dto.page.PageDto;
import bookstore.mapper.PageMapper;
import bookstore.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class BookController implements BookControllerApi {
    private final BookService bookService;
    private final PageMapper pageMapper;

    @GetMapping
    @Override
    public PageDto<BookDto> getAll(Pageable pageable) {
        return pageMapper.toDto(bookService.findAll(pageable));
    }

    @Override
    public BookDto getBookById(Long bookId) {
        return bookService.findById(bookId);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Override
    public BookDto createBook(CreateBookRequestDto bookDto) {
        return bookService.save(bookDto);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Override
    public BookDto updateBook(UpdateBookRequestDto requestDto, Long bookId) {
        return bookService.update(requestDto, bookId);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Override
    public void deleteById(Long bookId) {
        bookService.deleteById(bookId);
    }

    @Override
    public PageDto<BookDto> searchBooks(BookSearchParametersDto parametersDto, Pageable pageable) {
        return pageMapper.toDto(bookService.search(parametersDto, pageable));
    }
}
