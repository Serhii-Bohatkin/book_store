package bookstore.dto.cartitem;

import jakarta.validation.constraints.Min;

public record UpdateCartItemDto(
        @Min(1)
        Integer quantity
) {
}
