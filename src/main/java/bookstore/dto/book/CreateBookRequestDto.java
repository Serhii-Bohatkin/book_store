package bookstore.dto.book;

import bookstore.validation.Isbn;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.util.List;

public record CreateBookRequestDto(
        @NotBlank
        String title,
        @NotBlank
        String author,
        @Isbn
        String isbn,
        @NotNull
        @Positive
        BigDecimal price,
        String description,
        String coverImage,
        List<Long> categoryIds
) {
}
