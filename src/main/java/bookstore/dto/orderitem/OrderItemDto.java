package bookstore.dto.orderitem;

public record OrderItemDto(
        Long orderItemId,
        Long bookId,
        Integer quantity
) {
}
