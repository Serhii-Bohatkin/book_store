package bookstore.dto.book;

import bookstore.validation.Isbn;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

public record UpdateBookRequestDto(
        String title,
        String author,
        @Isbn
        String isbn,
        @Positive
        BigDecimal price,
        String description,
        String coverImage,
        List<Long> categoryIds
) {
}
