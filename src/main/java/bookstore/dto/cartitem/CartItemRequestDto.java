package bookstore.dto.cartitem;

import jakarta.validation.constraints.Min;

public record CartItemRequestDto(
        @Min(1)
        Long bookId,
        @Min(1)
        Integer quantity
) {
}
