package bookstore.repository.book.spec;

import bookstore.model.Book;
import bookstore.repository.specification.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class IsbnSpecificationProvider implements SpecificationProvider<Book> {
    @Override
    public String getKey() {
        return "isbn";
    }

    @Override
    public Specification<Book> getSpecification(String param) {
        return (root, query, cb) -> cb.equal(root.get("isbn"), param);
    }
}
