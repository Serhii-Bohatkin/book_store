package bookstore.repository.book;

import bookstore.dto.book.BookSearchParametersDto;
import bookstore.model.Book;
import bookstore.repository.specification.SpecificationBuilder;
import lombok.AllArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class BookSpecificationBuilder implements SpecificationBuilder<Book> {
    private final BookSpecificationProviderManager bookSpecificationProviderManager;

    @Override
    public Specification<Book> build(BookSearchParametersDto searchParameters) {
        Specification<Book> spec = Specification.where(null);
        if (searchParameters.title() != null) {
            spec = spec.and(bookSpecificationProviderManager.getSpecificationProvider("title")
                    .getSpecification(searchParameters.title()));
        }
        if (searchParameters.author() != null) {
            spec = spec.and(bookSpecificationProviderManager.getSpecificationProvider("author")
                    .getSpecification(searchParameters.author()));
        }
        if (searchParameters.isbn() != null) {
            spec = spec.and(bookSpecificationProviderManager.getSpecificationProvider("isbn")
                    .getSpecification(searchParameters.isbn()));
        }
        if (searchParameters.minPrice() != null) {
            spec = spec.and(bookSpecificationProviderManager.getSpecificationProvider("min_price")
                    .getSpecification(searchParameters.minPrice().toString()));
        }
        if (searchParameters.maxPrice() != null) {
            spec = spec.and(bookSpecificationProviderManager.getSpecificationProvider("max_price")
                    .getSpecification(searchParameters.maxPrice().toString()));
        }
        return spec;
    }
}
