package bookstore.service;

import bookstore.dto.book.BookDto;
import bookstore.dto.book.BookDtoWithoutCategoryIds;
import bookstore.dto.book.BookSearchParametersDto;
import bookstore.dto.book.CreateBookRequestDto;
import bookstore.dto.book.UpdateBookRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {
    BookDto save(CreateBookRequestDto bookDto);

    BookDto findById(Long id);

    Page<BookDto> findAll(Pageable pageable);

    BookDto update(UpdateBookRequestDto requestDto, Long id);

    void deleteById(Long id);

    Page<BookDto> search(BookSearchParametersDto parametersDto, Pageable pageable);

    Page<BookDtoWithoutCategoryIds> findByCategoryId(Long id, Pageable pageable);
}
