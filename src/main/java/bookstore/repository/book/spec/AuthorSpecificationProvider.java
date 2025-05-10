package bookstore.repository.book.spec;

import bookstore.model.Book;
import bookstore.repository.specification.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class AuthorSpecificationProvider implements SpecificationProvider<Book> {
    @Override
    public String getKey() {
        return "author";
    }

    public Specification<Book> getSpecification(String param) {
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.like(root.get("author"), "%" + param + "%");
    }
}
