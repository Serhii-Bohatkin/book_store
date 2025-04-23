package org.example.bookstore.repository.book.spec;

import java.math.BigDecimal;
import org.example.bookstore.model.Book;
import org.example.bookstore.repository.SpecificationProvider;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class MinPriceSpecificationProvider implements SpecificationProvider<Book> {
    @Override
    public String getKey() {
        return "min_price";
    }

    public Specification<Book> getSpecification(String param) {
        return (root, query, criteriaBuilder)
                -> criteriaBuilder.gt(root.get("price"), new BigDecimal(param));
    }
}
