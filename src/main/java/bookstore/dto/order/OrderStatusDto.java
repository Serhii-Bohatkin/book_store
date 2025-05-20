package bookstore.dto.order;

import bookstore.model.Order;

public record OrderStatusDto(
        Order.Status status
) {
}
