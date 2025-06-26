package bookstore.controller;

import bookstore.controller.api.OrderControllerApi;
import bookstore.dto.order.OrderAddressDto;
import bookstore.dto.order.OrderDto;
import bookstore.dto.order.OrderStatusDto;
import bookstore.dto.orderitem.OrderItemDto;
import bookstore.dto.page.PageDto;
import bookstore.mapper.PageMapper;
import bookstore.model.User;
import bookstore.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Validated
public class OrderController implements OrderControllerApi {
    private final OrderService orderService;
    private final PageMapper pageMapper;

    @Override
    public OrderDto placeOrder(User user, OrderAddressDto addressDto) {
        return orderService.placeOrder(user, addressDto);
    }

    @Override
    public OrderDto cancelOrder(User user, Long orderId) {
        return orderService.cancelOrder(orderId, user);
    }

    @Override
    public PageDto<OrderDto> getHistory(User user, Pageable pageable) {
        return pageMapper.toDto(orderService.getAllOrders(user, pageable));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Override
    public OrderDto updateStatus(Long orderId, OrderStatusDto statusDto) {
        return orderService.updateStatus(orderId, statusDto);
    }

    @Override
    public PageDto<OrderItemDto> getOrderItems(Long orderId, User user, Pageable pageable) {
        return pageMapper.toDto(orderService.getOrderItems(orderId, user, pageable));
    }

    @Override
    public OrderDto getOrderDetails(Long orderId, User user) {
        return orderService.getOrder(orderId, user);
    }

    @Override
    public OrderItemDto getItemById(Long itemId, Long orderId, User user) {
        return orderService.getItemByOrderIdAndItemId(itemId, orderId, user);
    }
}
