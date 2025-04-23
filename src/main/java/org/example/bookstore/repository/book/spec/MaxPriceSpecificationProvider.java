package org.example.bookstore.repository.book.spec;

import java.math.BigDecimal;
import org.example.bookstore.model.Book;
import org.example.bookstore.repository.SpecificationProvider;
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
