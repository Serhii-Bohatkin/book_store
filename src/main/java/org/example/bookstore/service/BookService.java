package org.example.bookstore.service;

import org.example.bookstore.dto.BookDto;
import org.example.bookstore.dto.BookSearchParametersDto;
import org.example.bookstore.dto.CreateBookRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {
    BookDto save(CreateBookRequestDto bookDto);

    BookDto findById(Long id);

    Page<BookDto> findAll(Pageable pageable);

    BookDto update(CreateBookRequestDto requestDto, Long id);

    void deleteById(Long id);

    Page<BookDto> search(BookSearchParametersDto parametersDto, Pageable pageable);
}
