package bookstore.controller.api;

import bookstore.dto.book.BookDto;
import bookstore.dto.book.BookSearchParametersDto;
import bookstore.dto.book.CreateBookRequestDto;
import bookstore.dto.book.UpdateBookRequestDto;
import bookstore.dto.page.PageDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;

@RequestMapping("/books")
@Tag(name = "Book management", description = "Endpoints for managing books")
public interface BookControllerApi {
    @Operation(summary = "Get all books", description = "Get a list of all available books. "
            + "Pagination: add a ? followed by the query {page}={value}&{size}={value} "
            + "For example: /books?page=0&size=10 "
            + "Sorting: add & followed by {sort}={field} or {sort}={field, DESC}")
    @GetMapping
    PageDto<BookDto> getAll(Pageable pageable);

    @Operation(summary = "Get a book by id", description = "Get a book by id")
    @GetMapping("/{bookId}")
    BookDto getBookById(@PathVariable @Min(1) Long bookId);

    @Operation(summary = "Create a book", description = "Create a book")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    BookDto createBook(@RequestBody @Valid CreateBookRequestDto bookDto);

    @Operation(summary = "Update book ", description = "Update the book by id")
    @PatchMapping("/{bookId}")
    BookDto updateBook(@RequestBody @Valid UpdateBookRequestDto requestDto,
                       @PathVariable @Min(1) Long bookId);

    @Operation(summary = "Delete book", description = "Delete the book by id")
    @DeleteMapping("/{bookId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteById(@PathVariable @Min(1) Long bookId);

    @Operation(summary = "Search book", description
            = "Search books by {title}, {author}, {isbn} or {minPrice}/{maxPrice}. "
            + "To start searching add a ? followed by the query {query}={value}. "
            + "If you want to chain several queries in the same call, use & followed by the query."
            + " Pagination: add a & followed by the query {page}={value}&{size}={value}. "
            + "For example: /books/search"
            + "?title=harry potter and the philosopher's stone&page=0&size=10 "
            + "Sorting: add & followed by {sort}={field} or {sort}={field, DESC}")
    @GetMapping("/search")
    PageDto<BookDto> searchBooks(@Valid BookSearchParametersDto parametersDto, Pageable pageable);
}
