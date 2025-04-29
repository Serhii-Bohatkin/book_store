package org.example.bookstore.mapper;

import org.example.bookstore.config.MapperConfig;
import org.example.bookstore.dto.BookDto;
import org.example.bookstore.dto.BookSearchParametersDto;
import org.example.bookstore.dto.CreateBookRequestDto;
import org.example.bookstore.model.Book;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface BookMapper {
    BookDto toDto(Book book);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "isbn", source = "isbn", qualifiedByName = "formatIsbn")
    Book toModel(CreateBookRequestDto bookDto);

    @Mapping(target = "isbn", source = "isbn", qualifiedByName = "formatIsbn")
    BookSearchParametersDto formatParametersDto(BookSearchParametersDto parametersDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "isbn", source = "isbn", qualifiedByName = "formatIsbn")
    void updateBookFromDto(@MappingTarget Book book, CreateBookRequestDto requestDto);

    @Named("formatIsbn")
    default String formatIsbn(String rawIsbn) {
        return rawIsbn.toUpperCase().replaceAll("[^0-9X]", "");
    }
}
