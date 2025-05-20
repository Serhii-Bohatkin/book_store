package bookstore.dto.book;

import java.math.BigDecimal;

public record BookDtoWithoutCategoryIds(
        Long bookId,
        String title,
        String author,
        String isbn,
        BigDecimal price,
        String description,
        String coverImage
) {
}
