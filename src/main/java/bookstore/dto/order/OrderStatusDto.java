package bookstore.dto.order;

import bookstore.model.enumeration.OrderStatus;

public record OrderStatusDto(
        OrderStatus status
) {
}
