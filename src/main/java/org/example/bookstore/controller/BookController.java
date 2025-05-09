package org.example.bookstore.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.bookstore.dto.book.BookDto;
import org.example.bookstore.dto.book.BookSearchParametersDto;
import org.example.bookstore.dto.book.CreateBookRequestDto;
import org.example.bookstore.dto.page.PageDto;
import org.example.bookstore.mapper.PageMapper;
import org.example.bookstore.service.BookService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/books")
@RequiredArgsConstructor
@Tag(name = "Book management", description = "Endpoints for managing books")
public class BookController {
    private final BookService bookService;
    private final PageMapper pageMapper;

    @Operation(summary = "Get all books", description = "Get a list of all available books. "
            + "Pagination: add a ? followed by the query {page}={value}&{size}={value} "
            + "For example: /books?page=0&size=10 "
            + "Sorting: add & followed by {sort}={field} or {sort}={field, DESC}")
    @GetMapping
    public PageDto<BookDto> getAll(Pageable pageable) {
        return pageMapper.toDto(bookService.findAll(pageable));
    }

    @Operation(summary = "Get a book by id", description = "Get a book by id")
    @GetMapping("/{id}")
    public BookDto getBookById(@PathVariable Long id) {
        return bookService.findById(id);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Create a book", description = "Create a book")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookDto createBook(@RequestBody @Valid CreateBookRequestDto bookDto) {
        return bookService.save(bookDto);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Update book ", description = "Update the book by id")
    @PatchMapping("/{id}")
    public BookDto updateBook(@RequestBody @Valid CreateBookRequestDto requestDto,
                              @PathVariable Long id) {
        return bookService.update(requestDto, id);
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Delete book", description = "Delete the book by id")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteById(@PathVariable Long id) {
        bookService.deleteById(id);
    }

    @Operation(summary = "Search book", description
            = "Search books by {title}, {author}, {isbn} or {minPrice}/{maxPrice}. "
            + "To start searching add a ? followed by the query {query}={value}. "
            + "If you want to chain several queries in the same call, use & followed by the query."
            + " Pagination: add a & followed by the query {page}={value}&{size}={value}. "
            + "For example: /books/search"
            + "?titles=harry potter and the philosopher's stone&page=0&size=10 "
            + "Sorting: add & followed by {sort}={field} or {sort}={field, DESC}")
    @GetMapping("/search")
    public PageDto<BookDto> searchBooks(@Valid BookSearchParametersDto parametersDto,
                                        Pageable pageable) {
        return pageMapper.toDto(bookService.search(parametersDto, pageable));
    }
}
