package bookstore.service;

import bookstore.dto.order.OrderAddressDto;
import bookstore.dto.order.OrderDto;
import bookstore.dto.order.OrderStatusDto;
import bookstore.dto.orderitem.OrderItemDto;
import bookstore.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {
    OrderDto placeOrder(User user, OrderAddressDto addressDto);

    Page<OrderDto> getAllOrders(User user, Pageable pageable);

    OrderDto updateStatus(Long orderId, OrderStatusDto statusDto);

    Page<OrderItemDto> getOrderItems(Long orderId, User user, Pageable pageable);

    OrderDto getOrder(Long orderId, User user);

    OrderItemDto getItemByOrderIdAndItemId(Long itemId, Long orderId, User user);
}
