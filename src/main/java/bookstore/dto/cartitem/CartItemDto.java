package bookstore.dto.cartitem;

public record CartItemDto(
        Long cartItemId,
        Long bookId,
        String bookTitle,
        Integer quantity
) {
}
