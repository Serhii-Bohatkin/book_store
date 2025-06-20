package bookstore.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import bookstore.TestObjectsFactory;
import bookstore.model.Order;
import bookstore.model.OrderItem;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {
        "classpath:database/clear-db.sql",
        "classpath:database/insert-users.sql",
        "classpath:database/insert-order.sql",
        "classpath:database/insert-books.sql",
        "classpath:database/insert-order_items.sql"}, executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/clear-db.sql", executionPhase = AFTER_TEST_METHOD)
class OrderRepositoryIntegrationTest {
    private static final Long VALID_USER_ID = 1L;
    private static final Long INVALID_USER_ID = Long.MAX_VALUE;
    private static final Long INVALID_ORDER_ID = Long.MAX_VALUE;
    private static final Long VALID_ORDER_ID = 1L;
    private static final PageRequest DEFAULT_PAGE_REQUEST = PageRequest.of(0, 20);
    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("Should return list of orders for valid user ID")
    void findAllByUserId_ValidUserId_ReturnOrder() {
        List<Order> expected = List.of(TestObjectsFactory.createOrder());
        List<Order> actual =
                orderRepository.findAllByUserId(VALID_USER_ID, DEFAULT_PAGE_REQUEST).getContent();
        assertThat(actual).isEqualTo(expected);
        Set<OrderItem> orderItems = actual.getFirst().getOrderItems();
        assertThat(orderItems).isNotEmpty();
    }

    @Test
    @DisplayName("Should return empty page for invalid user ID")
    void findAllByUserId_InvalidUserId_ReturnEmptyPage() {
        Page<Order> actual = orderRepository.findAllByUserId(INVALID_USER_ID, DEFAULT_PAGE_REQUEST);
        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("Should return order for valid order ID and user ID")
    void findByIdAndUserId_ValidOrderIdAndUserId_ReturnOrderOptional() {
        Optional<Order> expected = Optional.of(TestObjectsFactory.createOrder());
        Optional<Order> actual = orderRepository.findByIdAndUserId(VALID_ORDER_ID, VALID_USER_ID);
        assertThat(actual).isEqualTo(expected);
        Set<OrderItem> orderItems = actual.orElseThrow().getOrderItems();
        assertThat(orderItems).isNotEmpty();
    }

    @Test
    @DisplayName("Should return empty optional for valid order ID and invalid user ID")
    void findByIdAndUserId_ValidOrderIdAndInvalidUserId_ReturnEmptyOptional() {
        Optional<Order> actual = orderRepository.findByIdAndUserId(VALID_ORDER_ID, INVALID_USER_ID);
        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("Should return empty optional for invalid order ID and valid user ID")
    void findByIdAndUserId_InvalidOrderIdAndValidUserId_ReturnEmptyOptional() {
        Optional<Order> actual = orderRepository.findByIdAndUserId(INVALID_ORDER_ID, VALID_USER_ID);
        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("Should return empty optional for invalid order ID and invalid user ID")
    void findByIdAndUserId_InvalidOrderIdAndInvalidUserId_ReturnEmptyOptional() {
        Optional<Order> actual =
                orderRepository.findByIdAndUserId(INVALID_ORDER_ID, INVALID_USER_ID);
        assertThat(actual).isEmpty();
    }
}
