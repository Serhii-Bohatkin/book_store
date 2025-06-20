package bookstore.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import bookstore.TestObjectsFactory;
import bookstore.model.OrderItem;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
class OrderItemRepositoryIntegrationTest {
    private static final Long VALID_USER_ID = 1L;
    private static final Long INVALID_USER_ID = Long.MAX_VALUE;
    private static final Long VALID_ORDER_ID = 1L;
    private static final Long INVALID_ORDER_ID = Long.MAX_VALUE;
    private static final Long VALID_ORDER_ITEM_ID = 1L;
    private static final Long INVALID_ORDER_ITEM_ID = Long.MAX_VALUE;
    private static final PageRequest DEFAULT_PAGE_REQUEST = PageRequest.of(0, 20);
    @Autowired
    private OrderItemRepository itemRepository;

    @Test
    @DisplayName("Should return two order items for valid order ID and user ID")
    void findByOrderIdAndOrderUserId_ValidIds_ReturnOrderItem() {
        List<OrderItem> expected = List.of(TestObjectsFactory.createOrderItem(),
                TestObjectsFactory.createSecondOrderItem());
        List<OrderItem> actual =
                itemRepository.findByOrderIdAndOrderUserId(VALID_ORDER_ID, VALID_USER_ID,
                        DEFAULT_PAGE_REQUEST).getContent();
        assertThat(actual).hasSize(2).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should return empty page for invalid order ID or invalid user ID")
    void findByOrderIdAndOrderUserId_InvalidIds_ReturnEmptyPage() {
        Page<OrderItem> actual;
        actual = itemRepository.findByOrderIdAndOrderUserId(
                INVALID_ORDER_ID,
                VALID_USER_ID,
                DEFAULT_PAGE_REQUEST);
        assertThat(actual).isEmpty();

        actual = itemRepository.findByOrderIdAndOrderUserId(
                VALID_ORDER_ID,
                INVALID_USER_ID,
                DEFAULT_PAGE_REQUEST);
        assertThat(actual).isEmpty();

        actual = itemRepository.findByOrderIdAndOrderUserId(
                INVALID_ORDER_ID,
                INVALID_USER_ID,
                DEFAULT_PAGE_REQUEST);
        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("Should return order item for valid order item ID, order ID, and user ID")
    void findByIdAndOrderIdAndOrderUserId_ValidIds_ReturnOrderItem() {
        Optional<OrderItem> expected = Optional.of(TestObjectsFactory.createOrderItem());
        Optional<OrderItem> actual = itemRepository.findByIdAndOrderIdAndOrderUserId(
                VALID_ORDER_ITEM_ID,
                VALID_ORDER_ID,
                VALID_USER_ID);
        assertThat(actual).isEqualTo(expected);
    }

    @ParameterizedTest
    @MethodSource("provideInvalidIdCombinations")
    @DisplayName("Should return empty optional for invalid combinations of "
            + "order item ID, order ID, and user ID")
    void findByIdAndOrderIdAndOrderUserId_InvalidIds_ReturnOrderItem(
            Long orderItemId,
            Long orderId,
            Long userId
    ) {
        Optional<OrderItem> actual =
                itemRepository.findByIdAndOrderIdAndOrderUserId(orderItemId, orderId, userId);
        assertThat(actual).isEmpty();
    }

    private static Stream<Arguments> provideInvalidIdCombinations() {
        return Stream.of(
                Arguments.of(INVALID_ORDER_ITEM_ID, VALID_ORDER_ID, VALID_USER_ID),
                Arguments.of(VALID_ORDER_ITEM_ID, INVALID_ORDER_ID, VALID_USER_ID),
                Arguments.of(VALID_ORDER_ITEM_ID, VALID_ORDER_ID, INVALID_USER_ID),
                Arguments.of(INVALID_ORDER_ITEM_ID, INVALID_ORDER_ID, VALID_USER_ID),
                Arguments.of(INVALID_ORDER_ITEM_ID, VALID_ORDER_ID, INVALID_USER_ID),
                Arguments.of(VALID_ORDER_ITEM_ID, INVALID_ORDER_ID, INVALID_USER_ID),
                Arguments.of(INVALID_ORDER_ITEM_ID, INVALID_ORDER_ID, INVALID_USER_ID)
        );
    }
}
