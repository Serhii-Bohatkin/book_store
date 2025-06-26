package bookstore.controller.api;

import bookstore.dto.order.OrderAddressDto;
import bookstore.dto.order.OrderDto;
import bookstore.dto.order.OrderStatusDto;
import bookstore.dto.orderitem.OrderItemDto;
import bookstore.dto.page.PageDto;
import bookstore.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping("/orders")
@Tag(name = "Order management", description = "Endpoints for managing orders")
public interface OrderControllerApi {
    @Operation(summary = "Place order",
            description = "Enter your shipping address and place your order")
    @PostMapping
    OrderDto placeOrder(@AuthenticationPrincipal User user,
                        @RequestBody @Valid OrderAddressDto addressDto);

    @Operation(summary = "Cancel order",
            description = "You can cancel your order if it has not been processed yet")
    @PatchMapping("/cancel/{orderId}")
    OrderDto cancelOrder(@AuthenticationPrincipal User user,
                         @PathVariable @Min(1) Long orderId);

    @Operation(summary = "Get all orders", description = "Get all the current user's orders")
    @GetMapping
    PageDto<OrderDto> getHistory(@AuthenticationPrincipal User user, Pageable pageable);

    @Operation(summary = "Update order status", description =
            "Specify one of the following statuses: NEW, PROCESSED, SHIPPED, DELIVERED, CANCELED")
    @PatchMapping("/{orderId}")
    OrderDto updateStatus(@PathVariable @Min(1) Long orderId,
                          @RequestBody OrderStatusDto statusDto
    );

    @Operation(summary = "Get all items by order id", description = "Get all items by order id")
    @GetMapping("/{orderId}/items")
    PageDto<OrderItemDto> getOrderItems(@PathVariable @Min(1) Long orderId,
                                        @AuthenticationPrincipal User user,
                                        Pageable pageable);

    @Operation(summary = "Get order information", description = "Get order information by orderId")
    @GetMapping("/{orderId}")
    OrderDto getOrderDetails(@PathVariable @Min(1) Long orderId,
                             @AuthenticationPrincipal User user);

    @Operation(summary = "Get item by id", description = "Get item by order id and item id")
    @GetMapping("/{orderId}/items/{itemId}")
    OrderItemDto getItemById(@PathVariable @Min(1) Long itemId,
                             @PathVariable @Min(1) Long orderId,
                             @AuthenticationPrincipal User user

    );
}
