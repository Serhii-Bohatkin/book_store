package bookstore.dto.book;

import jakarta.validation.constraints.Min;
import java.math.BigDecimal;
import java.util.List;

public record BookDto(
        Long bookId,
        String title,
        String author,
        String isbn,
        BigDecimal price,
        String description,
        String coverImage,
        List<@Min(1) Long> categoryIds
) {
}
