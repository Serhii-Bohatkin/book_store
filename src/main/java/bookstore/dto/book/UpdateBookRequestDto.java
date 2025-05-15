package bookstore.dto.book;

import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

public record UpdateBookRequestDto(
        String title,
        String author,
        String isbn,
        @Positive
        BigDecimal price,
        String description,
        String coverImage,
        List<Long> categoryIds
) {
}
