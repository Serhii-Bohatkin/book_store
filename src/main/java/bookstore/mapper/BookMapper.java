package bookstore.mapper;

import bookstore.config.MapperConfig;
import bookstore.dto.book.BookDto;
import bookstore.dto.book.BookDtoWithoutCategoryIds;
import bookstore.dto.book.BookSearchParametersDto;
import bookstore.dto.book.CreateBookRequestDto;
import bookstore.dto.book.UpdateBookRequestDto;
import bookstore.model.Book;
import bookstore.model.Category;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

@Mapper(config = MapperConfig.class)
public interface BookMapper {
    @Mapping(target = "categoryIds", source = "categories", qualifiedByName = "setCategoryIds")
    BookDto toDto(Book book);

    @Named("setCategoryIds")
    default List<Long> setCategoryIds(Set<Category> categories) {
        return categories.stream()
                .map(Category::getId)
                .toList();
    }

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "isbn", source = "isbn", qualifiedByName = "formatIsbn")
    Book toModel(CreateBookRequestDto requestDto);

    @Mapping(target = "isbn", source = "isbn", qualifiedByName = "formatIsbn")
    BookSearchParametersDto formatParametersDto(BookSearchParametersDto parametersDto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deleted", ignore = true)
    @Mapping(target = "categories", ignore = true)
    @Mapping(target = "isbn", ignore = true)
    void updateBookFromDto(@MappingTarget Book book, UpdateBookRequestDto requestDto);

    @AfterMapping
    default void setCategories(@MappingTarget Book book, CreateBookRequestDto requestDto) {
        Set<Category> categories = requestDto.categoryIds().stream()
                .map(Category::new)
                .collect(Collectors.toSet());
        book.setCategories(categories);
    }

    @AfterMapping
    default void setCategories(@MappingTarget Book book, UpdateBookRequestDto requestDto) {
        Set<Category> categories = requestDto.categoryIds().stream()
                .map(Category::new)
                .collect(Collectors.toSet());
        book.setCategories(categories);
    }

    BookDtoWithoutCategoryIds toDtoWithoutCategories(Book book);

    @Named("formatIsbn")
    default String formatIsbn(String rawIsbn) {
        return rawIsbn.toUpperCase().replaceAll("[^0-9X]", "");
    }
}
