package org.example.bookstore.repository.book;

import java.util.List;
import lombok.AllArgsConstructor;
import org.example.bookstore.model.Book;
import org.example.bookstore.repository.SpecificationProvider;
import org.example.bookstore.repository.SpecificationProviderManager;
import org.springframework.stereotype.Component;

@AllArgsConstructor
@Component
public class BookSpecificationProviderManager implements SpecificationProviderManager<Book> {
    private final List<SpecificationProvider<Book>> bookSpecificationProviders;

    @Override
    public SpecificationProvider<Book> getSpecificationProvider(String key) {
        return bookSpecificationProviders.stream()
                .filter(provider -> provider.getKey().equals(key))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Can't find a correct specification provider for the key " + key));
    }
}
