package bookstore.dto.book;

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
        List<Long> categoryIds
) {
}
