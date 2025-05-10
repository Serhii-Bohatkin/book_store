package bookstore.repository.book.spec;

import bookstore.model.Book;
import bookstore.repository.specification.SpecificationProvider;
import java.math.BigDecimal;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class MaxPriceSpecificationProvider implements SpecificationProvider<Book> {
    @Override
    public String getKey() {
        return "max_price";
    }

    public Specification<Book> getSpecification(String param) {
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.lt(root.get("price"), new BigDecimal(param));
    }
}
