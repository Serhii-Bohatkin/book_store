package bookstore.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import bookstore.TestObjectsFactory;
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
import bookstore.model.enumeration.OrderStatus;
import bookstore.repository.CartItemRepository;
import bookstore.repository.OrderItemRepository;
import bookstore.repository.OrderRepository;
import bookstore.repository.ShoppingCartRepository;
import bookstore.service.impl.OrderServiceImpl;
import java.text.MessageFormat;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {
    private static final String EMPTY_SHOPPING_CART_MESSAGE = "First add books to shopping cart";
    private static final String ORDER_DOES_NOT_EXIST_MESSAGE =
            "An order with orderId {0} does not exist";
    private static final String ORDER_NOT_FOUND_MESSAGE =
            "An order with orderId {0} and userId {1} does not exist";
    private static final String ORDER_ITEM_NOT_FOUND_MESSAGE =
            "An order item with itemId {0}, orderId {1} and userId {2} does not exist";
    private static final String ORDER_ALREADY_CANCELLED_MESSAGE =
            "Order with id {0} has already been cancelled";
    private static final String CONTACT_SUPPORT_MESSAGE = "Please contact our support";
    private static final PageRequest DEFAULT_PAGE_REQUEST = PageRequest.of(0, 20);
    private static final Long ORDER_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final Long ITEM_ID = 1L;

    private OrderItem firstOrderItem;
    private OrderItem secondOrderItem;
    private OrderItemDto firstOrderItemDto;
    private OrderItemDto secondOrderItemDto;
    private Order order;
    private OrderDto orderDto;
    private User user;

    @Mock
    private ShoppingCartRepository cartRepository;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private OrderItemRepository orderItemRepository;
    @Mock
    private OrderMapper orderMapper;
    @Mock
    private CartItemRepository cartItemRepository;
    @Mock
    private OrderItemMapper orderItemMapper;
    @Mock
    private CartItemMapper cartItemMapper;
    @InjectMocks
    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        firstOrderItem = TestObjectsFactory.createOrderItem();
        secondOrderItem = TestObjectsFactory.createSecondOrderItem();
        firstOrderItemDto = TestObjectsFactory.createOrderItemDto();
        secondOrderItemDto = TestObjectsFactory.createSecondOrderItemDto();
        order = TestObjectsFactory.createOrder();
        orderDto = TestObjectsFactory.createOrderDtoWithStatus(OrderStatus.NEW);
        user = TestObjectsFactory.createUser();
    }

    @AfterEach
    void verifyNoUnexpectedInteractions() {
        verifyNoMoreInteractions(cartRepository, orderRepository, orderItemRepository, orderMapper,
                cartItemRepository, orderItemMapper, cartItemMapper);
    }

    @Nested
    class PlaceOrderMethodTests {
        private OrderAddressDto orderAddressDto;

        @BeforeEach
        void setUp() {
            orderAddressDto = TestObjectsFactory.createOrderAddressDto();
        }

        @Test
        @DisplayName("Should return OrderDto when books are present in the shopping cart")
        void placeOrder_ThereAreBooksInShoppingCart_ShouldReturnOrderDto() {
            ShoppingCart shoppingCart = TestObjectsFactory.createShoppingCartWithTwoBooks();
            when(cartRepository.getByUserId(USER_ID)).thenReturn(shoppingCart);
            when(cartItemMapper.toOrderItem(any(CartItem.class)))
                    .thenReturn(firstOrderItem, secondOrderItem);
            doNothing().when(cartItemRepository).deleteAll(anyCollection());
            when(orderRepository.save(any(Order.class))).thenReturn(order);
            when(orderMapper.toDto(order)).thenReturn(orderDto);

            OrderDto actual = orderService.placeOrder(user, orderAddressDto);

            assertThat(actual).isEqualTo(orderDto);
            verify(cartRepository).getByUserId(USER_ID);
            verify(cartItemMapper, times(2)).toOrderItem(any(CartItem.class));
            verify(cartItemRepository).deleteAll(anyCollection());
            verify(orderRepository).save(any(Order.class));
            verify(orderMapper).toDto(order);
        }

        @Test
        @DisplayName("Should throw IllegalStateException when shopping cart is empty")
        void placeOrder_ThereIsNoBookInShoppingCart_ShouldThrowIllegalStateException() {
            ShoppingCart shoppingCart = TestObjectsFactory.createEmptyShoppingCart();
            when(cartRepository.getByUserId(USER_ID)).thenReturn(shoppingCart);

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> orderService.placeOrder(user, orderAddressDto));

            assertThat(ex.getMessage()).isEqualTo(EMPTY_SHOPPING_CART_MESSAGE);
            verify(cartRepository).getByUserId(USER_ID);
            verify(orderRepository, never()).save(any(Order.class));
        }
    }

    @Nested
    class GetAllOrdersMethodTests {
        @Test
        @DisplayName("Should return a page with one OrderDto")
        void getAllOrders_OrderFound_ShouldReturnPageWithOneOrderDto() {
            Page<Order> orderPage = TestObjectsFactory.createOneOrderPage();
            when(orderRepository.findAllByUserId(USER_ID, DEFAULT_PAGE_REQUEST))
                    .thenReturn(orderPage);
            when(orderMapper.toDto(order)).thenReturn(orderDto);

            Page<OrderDto> actual = orderService.getAllOrders(user, DEFAULT_PAGE_REQUEST);

            assertThat(actual).hasSize(1);
            assertThat(actual.getContent()).containsExactly(orderDto);
            verify(orderRepository).findAllByUserId(USER_ID, DEFAULT_PAGE_REQUEST);
            verify(orderMapper).toDto(order);
        }

        @Test
        @DisplayName("Should return an empty page when no orders are found")
        void getAllOrders_OrdersNotFound_ShouldReturnEmptyPage() {
            when(orderRepository.findAllByUserId(USER_ID, DEFAULT_PAGE_REQUEST))
                    .thenReturn(Page.empty());

            Page<OrderDto> actual = orderService.getAllOrders(user, DEFAULT_PAGE_REQUEST);

            assertThat(actual).isEmpty();
            verify(orderRepository).findAllByUserId(USER_ID, DEFAULT_PAGE_REQUEST);
        }
    }

    @Nested
    class UpdateStatusMethodTests {
        private OrderStatusDto orderStatusDto;

        @BeforeEach
        void setUp() {
            orderStatusDto = TestObjectsFactory.createOrderStatusDto(OrderStatus.PROCESSED);
        }

        @Test
        @DisplayName("Should return updated OrderDto when order ID is valid")
        void updateStatus_ValidOrderId_ShouldReturnOrderDto() {
            OrderDto expected = TestObjectsFactory.createOrderDtoWithStatus(OrderStatus.PROCESSED);
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.of(order));
            when(orderRepository.save(any(Order.class))).thenReturn(order);
            when(orderMapper.toDto(order)).thenReturn(expected);

            OrderDto actual = orderService.updateStatus(ORDER_ID, orderStatusDto);

            assertThat(actual).isEqualTo(expected);
            verify(orderRepository).findById(ORDER_ID);
            verify(orderRepository).save(any(Order.class));
            verify(orderMapper).toDto(order);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when order ID is invalid")
        void updateStatus_InvalidOrderId_ShouldThrowEntityNotFoundException() {
            when(orderRepository.findById(ORDER_ID)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> orderService.updateStatus(ORDER_ID, orderStatusDto));

            assertThat(ex.getMessage()).isEqualTo(
                    MessageFormat.format(ORDER_DOES_NOT_EXIST_MESSAGE, ORDER_ID));
            verify(orderRepository).findById(ORDER_ID);
            verify(orderRepository, never()).save(any(Order.class));
        }
    }

    @Nested
    class GetOrderItemsMethodTests {
        @Test
        @DisplayName("Should return a page with two OrderItemDto when order ID is valid")
        void getOrderItems_ValidOrderId_ShouldReturnPageWithTwoOrderItemDto() {
            Page<OrderItem> twoOrderItemsPage = TestObjectsFactory.createTwoOrderItemsPage();
            when(orderItemRepository.findByOrderIdAndOrderUserId(
                    ORDER_ID, USER_ID, DEFAULT_PAGE_REQUEST)).thenReturn(twoOrderItemsPage);
            when(orderItemMapper.toDto(any(OrderItem.class)))
                    .thenReturn(firstOrderItemDto, secondOrderItemDto);

            Page<OrderItemDto> actual =
                    orderService.getOrderItems(ORDER_ID, user, DEFAULT_PAGE_REQUEST);

            assertThat(actual.getContent()).containsExactly(firstOrderItemDto, secondOrderItemDto);
            verify(orderItemRepository).findByOrderIdAndOrderUserId(
                    ORDER_ID, USER_ID, DEFAULT_PAGE_REQUEST);
            verify(orderItemMapper, times(2)).toDto(any(OrderItem.class));
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when order ID is invalid")
        void getOrderItems_InvalidOrderId_ShouldThrowEntityNotFoundException() {
            when(orderItemRepository.findByOrderIdAndOrderUserId(
                    ORDER_ID, USER_ID, DEFAULT_PAGE_REQUEST)).thenReturn(Page.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> orderService.getOrderItems(ORDER_ID, user, DEFAULT_PAGE_REQUEST));

            assertThat(ex.getMessage()).isEqualTo(
                    MessageFormat.format(ORDER_NOT_FOUND_MESSAGE, ORDER_ID, USER_ID));
            verify(orderItemRepository).findByOrderIdAndOrderUserId(
                    ORDER_ID, USER_ID, DEFAULT_PAGE_REQUEST);
        }
    }

    @Nested
    class GetOrderMethodTests {
        @Test
        @DisplayName("Should return OrderDto when order ID is valid")
        void getOrder_ValidOrderId_ShouldReturnOrderDto() {
            when(orderRepository.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(
                    Optional.of(order));
            when(orderMapper.toDto(order)).thenReturn(orderDto);

            OrderDto actual = orderService.getOrder(ORDER_ID, user);

            assertThat(actual).isEqualTo(orderDto);
            verify(orderRepository).findByIdAndUserId(ORDER_ID, USER_ID);
            verify(orderMapper).toDto(order);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when order ID is invalid")
        void getOrder_InvalidOrderId_ShouldThrowEntityNotFoundException() {
            when(orderRepository.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> orderService.getOrder(ORDER_ID, user));
            assertThat(ex.getMessage()).isEqualTo(
                    MessageFormat.format(ORDER_NOT_FOUND_MESSAGE, ORDER_ID, USER_ID));
            verify(orderRepository).findByIdAndUserId(ORDER_ID, USER_ID);
        }
    }

    @Nested
    class GetItemByOrderIdAndItemIdMethodTests {
        @Test
        @DisplayName("Should return OrderItemDto when order ID and item ID are valid")
        void getItemByOrderIdAndItemId_ValidIds_ShouldReturnOrderItemDto() {
            when(orderItemRepository.findByIdAndOrderIdAndOrderUserId(ITEM_ID, ORDER_ID, USER_ID))
                    .thenReturn(Optional.of(firstOrderItem));
            when(orderItemMapper.toDto(firstOrderItem)).thenReturn(firstOrderItemDto);

            OrderItemDto actual = orderService.getItemByOrderIdAndItemId(ITEM_ID, ORDER_ID, user);

            assertThat(actual).isEqualTo(firstOrderItemDto);
            verify(orderItemRepository).findByIdAndOrderIdAndOrderUserId(ITEM_ID, ORDER_ID,
                    USER_ID);
            verify(orderItemMapper).toDto(firstOrderItem);
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when order ID or item ID is invalid")
        void getItemByOrderIdAndItemId_InvalidIds_ShouldThrowEntityNotFoundException() {
            when(orderItemRepository.findByIdAndOrderIdAndOrderUserId(ITEM_ID, ORDER_ID, USER_ID))
                    .thenReturn(Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> orderService.getItemByOrderIdAndItemId(ITEM_ID, ORDER_ID, user));

            assertThat(ex.getMessage()).isEqualTo(
                    MessageFormat.format(ORDER_ITEM_NOT_FOUND_MESSAGE, ITEM_ID, ORDER_ID, USER_ID));
            verify(orderItemRepository).findByIdAndOrderIdAndOrderUserId(ITEM_ID, ORDER_ID,
                    USER_ID);
        }
    }

    @Nested
    class CancelOrderMethodTests {
        @Test
        @DisplayName("Should cancel order when status is NEW")
        void cancelOrder_CurrentStatusIsNew_ShouldCancelOrder() {
            OrderDto expected = TestObjectsFactory.createOrderDtoWithStatus(OrderStatus.CANCELLED);
            when(orderRepository.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(
                    Optional.of(order));
            when(orderRepository.save(order)).thenReturn(order);
            when(orderMapper.toDto(order)).thenReturn(expected);

            OrderDto actual = orderService.cancelOrder(ORDER_ID, user);

            assertThat(actual.status()).isEqualTo(OrderStatus.CANCELLED);
            verify(orderRepository).findByIdAndUserId(ORDER_ID, USER_ID);
            verify(orderRepository).save(order);
            verify(orderMapper).toDto(order);
        }

        @Test
        @DisplayName("Should throw IllegalStateException when order is already CANCELLED")
        void cancelOrder_CurrentStatusIsCancelled_ShouldThrowIllegalStateException() {
            order.setStatus(OrderStatus.CANCELLED);
            when(orderRepository.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(
                    Optional.of(order));

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> orderService.cancelOrder(ORDER_ID, user));
            assertThat(ex.getMessage()).isEqualTo(MessageFormat.format(
                    ORDER_ALREADY_CANCELLED_MESSAGE, order.getId()));
            verify(orderRepository).findByIdAndUserId(ORDER_ID, USER_ID);
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw IllegalStateException when order status is PROCESSED")
        void cancelOrder_CurrentStatusIsProcessed_ShouldThrowIllegalStateException() {
            order.setStatus(OrderStatus.PROCESSED);
            when(orderRepository.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(
                    Optional.of(order));

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> orderService.cancelOrder(ORDER_ID, user));
            assertThat(ex.getMessage()).isEqualTo(CONTACT_SUPPORT_MESSAGE);
            verify(orderRepository).findByIdAndUserId(ORDER_ID, USER_ID);
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw IllegalStateException when order status is SHIPPED")
        void cancelOrder_CurrentStatusIsShipped_ShouldThrowIllegalStateException() {
            order.setStatus(OrderStatus.SHIPPED);
            when(orderRepository.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(
                    Optional.of(order));

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> orderService.cancelOrder(ORDER_ID, user));
            assertThat(ex.getMessage()).isEqualTo(CONTACT_SUPPORT_MESSAGE);
            verify(orderRepository).findByIdAndUserId(ORDER_ID, USER_ID);
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw IllegalStateException when order status is DELIVERED")
        void cancelOrder_CurrentStatusIsDelivered_ShouldThrowIllegalStateException() {
            order.setStatus(OrderStatus.DELIVERED);
            when(orderRepository.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(
                    Optional.of(order));

            IllegalStateException ex = assertThrows(IllegalStateException.class,
                    () -> orderService.cancelOrder(ORDER_ID, user));
            assertThat(ex.getMessage()).isEqualTo(CONTACT_SUPPORT_MESSAGE);
            verify(orderRepository).findByIdAndUserId(ORDER_ID, USER_ID);
            verify(orderRepository, never()).save(any(Order.class));
        }

        @Test
        @DisplayName("Should throw EntityNotFoundException when order ID is invalid for user")
        void cancelOrder_InvalidOrderId_ShouldThrowEntityNotFoundException() {
            when(orderRepository.findByIdAndUserId(ORDER_ID, USER_ID)).thenReturn(
                    Optional.empty());

            EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                    () -> orderService.cancelOrder(ORDER_ID, user));
            assertThat(ex.getMessage()).isEqualTo(MessageFormat.format(
                    ORDER_NOT_FOUND_MESSAGE, ORDER_ID, USER_ID));
            verify(orderRepository).findByIdAndUserId(ORDER_ID, USER_ID);
            verify(orderRepository, never()).save(any(Order.class));
        }
    }
}
