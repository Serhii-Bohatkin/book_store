package org.example.bookstore.repository.book;

import lombok.AllArgsConstructor;
import org.example.bookstore.dto.BookSearchParametersDto;
import org.example.bookstore.model.Book;
import org.example.bookstore.repository.SpecificationBuilder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class BookSpecificationBuilder implements SpecificationBuilder<Book> {
    private final BookSpecificationProviderManager bookSpecificationProviderManager;

    @Override
    public Specification<Book> build(BookSearchParametersDto searchParameters) {
        Specification<Book> spec = Specification.where(null);
        if (searchParameters.getTitle() != null) {
            spec = spec.and(bookSpecificationProviderManager.getSpecificationProvider("title")
                    .getSpecification(searchParameters.getTitle()));
        }
        if (searchParameters.getAuthor() != null) {
            spec = spec.and(bookSpecificationProviderManager.getSpecificationProvider("author")
                    .getSpecification(searchParameters.getAuthor()));
        }
        if (searchParameters.getIsbn() != null) {
            spec = spec.and(bookSpecificationProviderManager.getSpecificationProvider("isbn")
                    .getSpecification(searchParameters.getIsbn()));
        }
        if (searchParameters.getMinPrice() != null) {
            spec = spec.and(bookSpecificationProviderManager.getSpecificationProvider("min_price")
                    .getSpecification(searchParameters.getMinPrice().toString()));
        }
        if (searchParameters.getMaxPrice() != null) {
            spec = spec.and(bookSpecificationProviderManager.getSpecificationProvider("max_price")
                    .getSpecification(searchParameters.getMaxPrice().toString()));
        }
        return spec;
    }
}
