package bookstore.controller;

import bookstore.dto.order.OrderAddressDto;
import bookstore.dto.order.OrderDto;
import bookstore.dto.order.OrderStatusDto;
import bookstore.dto.orderitem.OrderItemDto;
import bookstore.dto.page.PageDto;
import bookstore.mapper.PageMapper;
import bookstore.model.User;
import bookstore.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
@RequiredArgsConstructor
@Tag(name = "Order management", description = "Endpoints for managing orders")
@Validated
public class OrderController {
    private final OrderService orderService;
    private final PageMapper pageMapper;

    @Operation(summary = "Place order",
            description = "Enter your shipping address and place your order")
    @PostMapping
    public OrderDto placeOrder(@AuthenticationPrincipal User user,
                               @RequestBody @Valid OrderAddressDto addressDto) {
        return orderService.placeOrder(user, addressDto);
    }

    @Operation(summary = "Get all orders", description = "Get all the current user's orders")
    @GetMapping
    public PageDto<OrderDto> getHistory(@AuthenticationPrincipal User user, Pageable pageable) {
        return pageMapper.toDto(orderService.getAllOrders(user, pageable));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "Update order status", description =
            "Specify one of the following statuses: NEW, PROCESSED, SHIPPED, DELIVERED, CANCELED")
    @PatchMapping("/{orderId}")
    public OrderDto updateStatus(@PathVariable @Min(1) Long orderId,
                                 @RequestBody OrderStatusDto statusDto
    ) {
        return orderService.updateStatus(orderId, statusDto);
    }

    @Operation(summary = "Get all items by order id", description = "Get all items by order id")
    @GetMapping("/{orderId}/items")
    public PageDto<OrderItemDto> getOrderItems(@PathVariable @Min(1) Long orderId,
                                               @AuthenticationPrincipal User user,
                                               Pageable pageable) {
        return pageMapper.toDto(orderService.getOrderItems(orderId, user, pageable));
    }

    @Operation(summary = "Get order information", description = "Get order information by orderId")
    @GetMapping("/{orderId}")
    public OrderDto getOrderDetails(@PathVariable @Min(1) Long orderId,
                                    @AuthenticationPrincipal User user) {
        return orderService.getOrder(orderId, user);
    }

    @Operation(summary = "Get item by id", description = "Get item by order id and item id")
    @GetMapping("/{orderId}/items/{itemId}")
    public OrderItemDto getItemById(@PathVariable @Min(1) Long itemId,
                                    @PathVariable @Min(1) Long orderId,
                                    @AuthenticationPrincipal User user

    ) {
        return orderService.getItemByOrderIdAndItemId(itemId, orderId, user);
    }
}
