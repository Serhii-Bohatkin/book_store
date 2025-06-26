package bookstore.service.impl;

import static bookstore.exception.EntityNotFoundException.entityNotFoundException;

import bookstore.dto.order.OrderAddressDto;
import bookstore.dto.order.OrderDto;
import bookstore.dto.order.OrderStatusDto;
import bookstore.dto.orderitem.OrderItemDto;
import bookstore.exception.EntityNotFoundException;
import bookstore.mapper.CartItemMapper;
import bookstore.mapper.OrderItemMapper;
import bookstore.mapper.OrderMapper;
import bookstore.model.CartItem;
import bookstore.model.Order;
import bookstore.model.OrderItem;
import bookstore.model.ShoppingCart;
import bookstore.model.User;
import bookstore.repository.CartItemRepository;
import bookstore.repository.OrderItemRepository;
import bookstore.repository.OrderRepository;
import bookstore.repository.ShoppingCartRepository;
import bookstore.service.OrderService;
import jakarta.transaction.Transactional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private static final String ORDER_NOT_FOUND_MESSAGE =
            "An order with orderId {0} and userId {1} does not exist";
    private static final String ORDER_DOES_NOT_EXIST_MESSAGE =
            "An order with orderId {0} does not exist";
    private static final String EMPTY_SHOPPING_CART_MESSAGE = "First add books to shopping cart";
    private static final String ORDER_ITEM_NOT_FOUND_MESSAGE =
            "An order item with itemId {0}, orderId {1} and userId {2} does not exist";
    private final ShoppingCartRepository cartRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final OrderMapper orderMapper;
    private final CartItemRepository cartItemRepository;
    private final OrderItemMapper orderItemMapper;
    private final CartItemMapper cartItemMapper;

    @Transactional
    @Override
    public OrderDto placeOrder(User user, OrderAddressDto addressDto) {
        ShoppingCart cart = cartRepository.getByUserId(user.getId());
        throwIfCartEmpty(cart);
        Set<CartItem> itemsFromCart = cart.getCartItems();
        Order order = cart.createOrder(addressDto.shippingAddress());
        Set<OrderItem> orderItems = mapToOrderItems(itemsFromCart);
        order.setOrderItems(orderItems);
        cartItemRepository.deleteAll(itemsFromCart);
        return orderMapper.toDto(orderRepository.save(order));
    }

    @Override
    public Page<OrderDto> getAllOrders(User user, Pageable pageable) {
        return orderRepository.findAllByUserId(user.getId(), pageable)
                .map(orderMapper::toDto);
    }

    @Transactional
    @Override
    public OrderDto updateStatus(Long orderId, OrderStatusDto statusDto) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(entityNotFoundException(ORDER_DOES_NOT_EXIST_MESSAGE, orderId));
        Order savedOrder = orderRepository.save(order.setStatus(statusDto.status()));
        return orderMapper.toDto(savedOrder);
    }

    @Override
    public Page<OrderItemDto> getOrderItems(Long orderId, User user, Pageable pageable) {
        Page<OrderItem> itemsPage = orderItemRepository
                .findByOrderIdAndOrderUserId(orderId, user.getId(), pageable);
        if (itemsPage.isEmpty()) {
            throw new EntityNotFoundException(ORDER_NOT_FOUND_MESSAGE, orderId, user.getId());
        }
        return itemsPage.map(orderItemMapper::toDto);
    }

    @Override
    public OrderDto getOrder(Long orderId, User user) {
        return orderMapper.toDto(getOrderOrThrow(orderId, user.getId()));
    }

    @Override
    public OrderItemDto getItemByOrderIdAndItemId(Long itemId, Long orderId, User user) {
        Long userId = user.getId();
        OrderItem item = orderItemRepository
                .findByIdAndOrderIdAndOrderUserId(itemId, orderId, userId)
                .orElseThrow(entityNotFoundException(
                        ORDER_ITEM_NOT_FOUND_MESSAGE,
                        itemId, orderId, userId));
        return orderItemMapper.toDto(item);
    }

    @Transactional
    @Override
    public OrderDto cancelOrder(Long orderId, User user) {
        Order order = getOrderOrThrow(orderId, user.getId());
        order.handleCancel();
        return orderMapper.toDto(orderRepository.save(order));
    }

    private Order getOrderOrThrow(Long orderId, Long userId) {
        return orderRepository.findByIdAndUserId(orderId, userId).orElseThrow(
                entityNotFoundException(ORDER_NOT_FOUND_MESSAGE, orderId, userId));
    }

    private void throwIfCartEmpty(ShoppingCart cart) {
        if (cart.getCartItems().isEmpty()) {
            throw new IllegalStateException(EMPTY_SHOPPING_CART_MESSAGE);
        }
    }

    private Set<OrderItem> mapToOrderItems(Set<CartItem> cartItems) {
        return cartItems.stream()
                .map(cartItemMapper::toOrderItem)
                .collect(Collectors.toSet());
    }
}
