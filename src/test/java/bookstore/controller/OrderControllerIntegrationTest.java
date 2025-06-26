package bookstore.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bookstore.TestObjectsFactory;
import bookstore.dto.order.OrderAddressDto;
import bookstore.dto.order.OrderDto;
import bookstore.dto.order.OrderStatusDto;
import bookstore.dto.orderitem.OrderItemDto;
import bookstore.dto.page.PageDto;
import bookstore.model.enumeration.OrderStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.MessageFormat;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql(scripts = {
        "classpath:database/clear-db.sql",
        "classpath:database/insert-books.sql",
        "classpath:database/insert-users.sql",
        "classpath:database/insert-order.sql",
        "classpath:database/insert-order_items.sql",
        "classpath:database/insert-shopping_cart.sql",
        "classpath:database/insert-cart_items.sql"}, executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/clear-db.sql", executionPhase = AFTER_TEST_METHOD)
class OrderControllerIntegrationTest {
    private static final String ORDER_DOES_NOT_EXIST_MESSAGE =
            "An order with orderId {0} does not exist";
    private static final String ORDER_NOT_FOUND_MESSAGE =
            "An order with orderId {0} and userId {1} does not exist";
    private static final String BASE_URL = "/orders";
    private static final String FIRST_ADD_BOOKS_TO_SHOPPING_CART_MESSAGE =
            "First add books to shopping cart";
    private static final String ORDER_ITEM_NOT_FOUND_MESSAGE =
            "An order item with itemId {0}, orderId {1} and userId {2} does not exist";
    private static final String ORDER_ID_MUST_BE_POSITIVE_MESSAGE =
            "orderId must be greater than or equal to 1";
    private static final String STATUS_CANNOT_BE_NULL_MESSAGE = "Column 'status' cannot be null";
    private static final String ITEM_ID_MUST_BE_POSITIVE_MESSAGE =
            "itemId must be greater than or equal to 1";
    private static final String ORDER_ALREADY_CANCELLED_MESSAGE =
            "Order with id {0} has already been cancelled";
    private static final String CONTACT_SUPPORT_MESSAGE = "Please contact our support";
    private static final String ORDER_ID_PARAM = "/{orderId}";
    private static final String ITEMS_PART_URL = "/items";
    private static final String ITEM_ID_PARAM = "/{itemId}";

    private static final Long VALID_ORDER_ID = 1L;
    private static final Long VALID_ITEM_ID = 1L;
    private static final Long USER_ID = 1L;
    private static final Long NON_EXISTING_ORDER_ID = Long.MAX_VALUE;
    private static final Long NON_EXISTING_ITEM_ID = Long.MAX_VALUE;
    private static final Long NEGATIVE_ORDER_ID = Long.MIN_VALUE;
    private static final Long NEGATIVE_ITEM_ID = Long.MIN_VALUE;
    private static final String CANCEL_PART_URL = "/cancel";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("Should place order and return OrderDto when books are in cart")
    @WithUserDetails("user@gmail.com")
    void placeOrder_ThereAreBooksInCart_ShouldReturnOrderDto() throws Exception {
        OrderDto expected = TestObjectsFactory.createOrderDtoWithStatus(OrderStatus.NEW);
        OrderAddressDto addressDto = TestObjectsFactory.createOrderAddressDto();
        String jsonRequest = objectMapper.writeValueAsString(addressDto);

        String jsonResponse = mockMvc.perform(post(BASE_URL)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderDto actual = objectMapper.readValue(jsonResponse, OrderDto.class);
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("orderId", "orderDate", "orderItems")
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("Should return 409 Conflict when placing order with empty cart")
    @Sql(scripts = "classpath:database/clear-cart_items.sql", executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails("user@gmail.com")
    void placeOrder_ThereIsNoBookInCart_Conflict() throws Exception {
        OrderAddressDto addressDto = TestObjectsFactory.createOrderAddressDto();
        String jsonRequest = objectMapper.writeValueAsString(addressDto);

        String jsonResponse = mockMvc.perform(post(BASE_URL)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(FIRST_ADD_BOOKS_TO_SHOPPING_CART_MESSAGE);
    }

    @Test
    @DisplayName("Should return order history with one order")
    @WithUserDetails("user@gmail.com")
    void getHistory_ShouldReturnPageDtoWithOneOrderDto() throws Exception {
        OrderDto expected = TestObjectsFactory.createOrderDtoWithStatus(OrderStatus.NEW);

        String jsonResponse = mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PageDto<OrderDto> actual = objectMapper.readValue(jsonResponse, new TypeReference<>() {
        });
        assertThat(actual.content().getFirst())
                .usingRecursiveComparison()
                .ignoringFields("orderId", "orderDate", "orderItems")
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("Should return empty order history when no orders exist")
    @Sql(scripts = {"classpath:database/clear-order_items.sql",
            "classpath:database/clear-orders.sql"}, executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails("user@gmail.com")
    void getHistory_OrdersNotFound_ShouldReturnEmptyPageDto() throws Exception {
        String jsonResponse = mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PageDto<OrderDto> actual = objectMapper.readValue(jsonResponse, new TypeReference<>() {
        });
        assertThat(actual.content()).isEmpty();
    }

    @Test
    @DisplayName("Should update order status and return OrderDto for valid orderId")
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    void updateStatus_ValidOrderId_ShouldReturnOrderDto() throws Exception {
        OrderDto expected = TestObjectsFactory.createOrderDtoWithStatus(OrderStatus.PROCESSED);
        OrderStatusDto orderStatusDto =
                TestObjectsFactory.createOrderStatusDto(OrderStatus.PROCESSED);
        String jsonRequest = objectMapper.writeValueAsString(orderStatusDto);

        String jsonResponse = mockMvc.perform(patch(BASE_URL + ORDER_ID_PARAM, VALID_ORDER_ID)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderDto actual = objectMapper.readValue(jsonResponse, OrderDto.class);
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("orderId", "orderDate", "orderItems")
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("Should return 404 Not Found when updating status of non-existing order")
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    void updateStatus_OrderDoesNotExist_NotFound() throws Exception {
        OrderStatusDto orderStatusDto =
                TestObjectsFactory.createOrderStatusDto(OrderStatus.PROCESSED);
        String jsonRequest = objectMapper.writeValueAsString(orderStatusDto);

        String jsonResponse = mockMvc.perform(patch(
                        BASE_URL + ORDER_ID_PARAM, NON_EXISTING_ORDER_ID)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(MessageFormat.format(
                ORDER_DOES_NOT_EXIST_MESSAGE, NON_EXISTING_ORDER_ID));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when updating status with negative orderId")
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    void updateStatus_InvalidOrderId_BadRequest() throws Exception {
        OrderStatusDto orderStatusDto =
                TestObjectsFactory.createOrderStatusDto(OrderStatus.PROCESSED);
        String jsonRequest = objectMapper.writeValueAsString(orderStatusDto);

        String jsonResponse = mockMvc.perform(patch(BASE_URL + ORDER_ID_PARAM, NEGATIVE_ORDER_ID)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(ORDER_ID_MUST_BE_POSITIVE_MESSAGE);
    }

    @Test
    @DisplayName("Should return 409 Conflict when updating status with null status")
    @WithMockUser(username = "admin@gmail.com", authorities = "ADMIN")
    void updateStatus_InvalidRequestDto_Conflict() throws Exception {
        OrderStatusDto orderStatusDto = TestObjectsFactory.createOrderStatusDto(null);
        String jsonRequest = objectMapper.writeValueAsString(orderStatusDto);

        String jsonResponse = mockMvc.perform(patch(BASE_URL + ORDER_ID_PARAM, VALID_ORDER_ID)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(STATUS_CANNOT_BE_NULL_MESSAGE);
    }

    @Test
    @DisplayName("Should return order items for valid orderId")
    @WithUserDetails("user@gmail.com")
    void getOrderItems_ValidOrderId_ShouldReturnPageDtoWithTwoOrderItemDtos() throws Exception {
        List<OrderItemDto> expectedItems = List.of(
                TestObjectsFactory.createOrderItemDto(),
                TestObjectsFactory.createSecondOrderItemDto());

        String jsonResponse = mockMvc.perform(get(
                        BASE_URL + ORDER_ID_PARAM + ITEMS_PART_URL, VALID_ORDER_ID))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        PageDto<OrderItemDto> actual = objectMapper.readValue(jsonResponse, new TypeReference<>() {
        });

        for (int i = 0; i < expectedItems.size(); i++) {
            assertThat(actual.content().get(i))
                    .usingRecursiveComparison()
                    .ignoringFields("orderItemId")
                    .isEqualTo(expectedItems.get(i));
        }
    }

    @Test
    @DisplayName("Should return 404 Not Found for non-existing order when getting items")
    @WithUserDetails("user@gmail.com")
    void getOrderItems_OrderDoesNotExist_NotFound() throws Exception {
        String jsonResponse = mockMvc.perform(get(
                        BASE_URL + ORDER_ID_PARAM + ITEMS_PART_URL, NON_EXISTING_ORDER_ID))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(MessageFormat.format(
                ORDER_NOT_FOUND_MESSAGE, NON_EXISTING_ORDER_ID, USER_ID));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when orderId is negative while getting items")
    @WithUserDetails("user@gmail.com")
    void getOrderItems_InvalidOrderId_BadRequest() throws Exception {
        String jsonResponse = mockMvc.perform(get(
                        BASE_URL + ORDER_ID_PARAM + ITEMS_PART_URL, NEGATIVE_ORDER_ID))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(ORDER_ID_MUST_BE_POSITIVE_MESSAGE);
    }

    @Test
    @DisplayName("Should return order details for valid orderId")
    @WithUserDetails("user@gmail.com")
    void getOrderDetails_ValidOrderId_ShouldReturnOrderDto() throws Exception {
        OrderDto expected = TestObjectsFactory.createOrderDtoWithStatus(OrderStatus.NEW);

        String jsonResponse = mockMvc.perform(get(BASE_URL + ORDER_ID_PARAM, VALID_ORDER_ID))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderDto actual = objectMapper.readValue(jsonResponse, OrderDto.class);
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("orderId", "orderDate", "orderItems")
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("Should return 404 Not Found for non-existing order details")
    @WithUserDetails("user@gmail.com")
    void getOrderDetails_OrderDoesNotExist_NotFound() throws Exception {
        String jsonResponse = mockMvc.perform(get(
                        BASE_URL + ORDER_ID_PARAM, NON_EXISTING_ORDER_ID))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(MessageFormat.format(
                ORDER_NOT_FOUND_MESSAGE, NON_EXISTING_ORDER_ID, USER_ID));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when getting order details with negative orderId")
    @WithUserDetails("user@gmail.com")
    void getOrderDetails_InvalidOrderId_BadRequest() throws Exception {
        String jsonResponse = mockMvc.perform(get(BASE_URL + ORDER_ID_PARAM, NEGATIVE_ORDER_ID))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(ORDER_ID_MUST_BE_POSITIVE_MESSAGE);
    }

    @Test
    @DisplayName("Should return order item for valid orderItemId")
    @WithUserDetails("user@gmail.com")
    void getItemById_ValidOrderItemId_ShouldReturnOrderItemDto() throws Exception {
        OrderItemDto expected = TestObjectsFactory.createOrderItemDto();

        String jsonResponse = mockMvc.perform(get(
                        BASE_URL + ORDER_ID_PARAM + ITEMS_PART_URL + ITEM_ID_PARAM,
                        VALID_ORDER_ID, VALID_ITEM_ID))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderItemDto actual = objectMapper.readValue(jsonResponse, OrderItemDto.class);
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("orderItemId")
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("Should return 404 Not Found when order item does not exist")
    @WithUserDetails("user@gmail.com")
    void getItemById_OrderItemDoesNotExist_NotFound() throws Exception {
        String jsonResponse = mockMvc.perform(get(
                        BASE_URL + ORDER_ID_PARAM + ITEMS_PART_URL + ITEM_ID_PARAM,
                        VALID_ORDER_ID, NON_EXISTING_ITEM_ID))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(MessageFormat.format(
                ORDER_ITEM_NOT_FOUND_MESSAGE, NON_EXISTING_ITEM_ID, VALID_ORDER_ID, USER_ID));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when itemId is negative")
    @WithUserDetails("user@gmail.com")
    void getItemById_InvalidOrderItemId_BadRequest() throws Exception {
        String jsonResponse = mockMvc.perform(get(
                        BASE_URL + ORDER_ID_PARAM + ITEMS_PART_URL + ITEM_ID_PARAM,
                        VALID_ORDER_ID, NEGATIVE_ITEM_ID))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(ITEM_ID_MUST_BE_POSITIVE_MESSAGE);
    }

    @Test
    @DisplayName("Should cancel order and return 200 OK when current status is NEW")
    @WithUserDetails("user@gmail.com")
    void cancelOrder_CurrentStatusIsNew_ShouldCancelOrder() throws Exception {
        String jsonResponse = mockMvc.perform(patch(
                        BASE_URL + CANCEL_PART_URL + ORDER_ID_PARAM, VALID_ORDER_ID))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        OrderDto actual = objectMapper.readValue(jsonResponse, OrderDto.class);
        assertThat(actual.status()).isEqualTo(OrderStatus.CANCELLED);
    }

    @ParameterizedTest
    @DisplayName("Should return 409 Conflict and contact support "
            + "message for non-cancellable order statuses")
    @ValueSource(longs = {2L, 3L, 4L})
    @WithUserDetails("user@gmail.com")
    void cancelOrder_NonCancellableStatuses_ShouldReturnConflict(Long orderId) throws Exception {
        String jsonResponse = mockMvc.perform(patch(
                        BASE_URL + CANCEL_PART_URL + ORDER_ID_PARAM, orderId))
                .andExpect(status().isConflict())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(CONTACT_SUPPORT_MESSAGE);
    }

    @Test
    @DisplayName("Should return 409 Conflict when current status is CANCELLED")
    @WithUserDetails("user@gmail.com")
    void cancelOrder_CurrentStatusIsCancelled_Conflict() throws Exception {
        String jsonResponse = mockMvc.perform(patch(
                        BASE_URL + CANCEL_PART_URL + ORDER_ID_PARAM, 5))
                .andExpect(status().isConflict())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(MessageFormat.format(
                ORDER_ALREADY_CANCELLED_MESSAGE, 5));
    }

    @Test
    @DisplayName("Should return 404 Not Found when order does not exist")
    @WithUserDetails("user@gmail.com")
    void cancelOrder_OrderDoesNotExist_NotFound() throws Exception {
        String jsonResponse = mockMvc.perform(patch(
                        BASE_URL + CANCEL_PART_URL + ORDER_ID_PARAM, NON_EXISTING_ORDER_ID))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(MessageFormat.format(
                ORDER_NOT_FOUND_MESSAGE, NON_EXISTING_ORDER_ID, USER_ID));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when order ID is invalid")
    @WithUserDetails("user@gmail.com")
    void cancelOrder_InvalidOrderId_BadRequest() throws Exception {
        String jsonResponse = mockMvc.perform(patch(
                        BASE_URL + CANCEL_PART_URL + ORDER_ID_PARAM, NEGATIVE_ORDER_ID))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(ORDER_ID_MUST_BE_POSITIVE_MESSAGE);
    }
}
