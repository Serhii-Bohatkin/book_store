package bookstore.dto.order;

import bookstore.dto.orderitem.OrderItemDto;
import bookstore.model.enumeration.OrderStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Set;

public record OrderDto(
        Long orderId,
        Long userId,
        OrderStatus status,
        BigDecimal total,
        LocalDateTime orderDate,
        String shippingAddress,
        Set<OrderItemDto> orderItems
) {
}
