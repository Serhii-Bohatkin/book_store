package bookstore.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import bookstore.TestObjectsFactory;
import bookstore.dto.cartitem.CartItemDto;
import bookstore.dto.cartitem.CartItemRequestDto;
import bookstore.dto.cartitem.UpdateCartItemDto;
import bookstore.dto.shoppingcart.ShoppingCartDto;
import bookstore.model.CartItem;
import bookstore.repository.CartItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.text.MessageFormat;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlMergeMode;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@SqlMergeMode(SqlMergeMode.MergeMode.MERGE)
@Sql(scripts = {"classpath:database/clear-db.sql",
        "classpath:database/insert-books.sql",
        "classpath:database/insert-users.sql",
        "classpath:database/insert-shopping_cart.sql",
        "classpath:database/insert-cart_items.sql"}, executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/clear-db.sql", executionPhase = AFTER_TEST_METHOD)
class ShoppingCartControllerIntegrationTest {
    private static final String BOOK_IS_ALREADY_IN_SHOPPING_CART_MESSAGE =
            "The book with the id {0} is already in shopping cart";
    private static final String BOOK_NOT_FOUND_MESSAGE = "A book with id {0} does not exist";
    private static final String CART_ITEM_NOT_FOUND_MESSAGE =
            "A cart item with id {0} does not exist";
    private static final String QUANTITY_MUST_BE_POSITIVE_MESSAGE =
            "quantity must be greater than or equal to 1";
    private static final String BOOK_ID_MUST_BE_POSITIVE_MESSAGE =
            "bookId must be greater than or equal to 1";
    private static final String ITEM_ID_MUST_BE_POSITIVE_MESSAGE =
            "itemId must be greater than or equal to 1";
    private static final String BASE_URL = "/cart";
    private static final String ITEMS_PART_URL = "/items";
    private static final String ITEM_ID_PARAM = "/{itemId}";

    private static final Long VALID_BOOK_ID = 1L;
    private static final Long VALID_ITEM_ID = 1L;
    private static final Long NEGATIVE_BOOK_ID = Long.MIN_VALUE;
    private static final Long NEGATIVE_ITEM_ID = Long.MIN_VALUE;
    private static final Long NON_EXISTING_BOOK_ID = Long.MAX_VALUE;
    private static final Long NON_EXISTING_ITEM_ID = Long.MAX_VALUE;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CartItemRepository cartItemRepository;

    @Test
    @DisplayName("Should return ShoppingCartDto for current user")
    @WithUserDetails("user@gmail.com")
    void getShoppingCart_ShouldReturnShoppingCartDto() throws Exception {
        ShoppingCartDto expected = TestObjectsFactory.createShoppingCartDto();

        String jsonResponse = mockMvc.perform(get(BASE_URL))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ShoppingCartDto actual = objectMapper.readValue(jsonResponse, ShoppingCartDto.class);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should add book to cart and return CartItemDto when valid request is sent")
    @Sql(scripts = "classpath:database/clear-cart_items.sql", executionPhase = BEFORE_TEST_METHOD)
    @WithUserDetails("user@gmail.com")
    void addBook_ValidRequestDto_ShouldReturnCartItemDto() throws Exception {
        CartItemDto expected = TestObjectsFactory.createCartItemDto();
        CartItemRequestDto cartItemRequestDto =
                TestObjectsFactory.createCartItemRequestDto(VALID_BOOK_ID, 1);
        String jsonRequest = objectMapper.writeValueAsString(cartItemRequestDto);

        String jsonResponse = mockMvc.perform(post(BASE_URL)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CartItemDto actual = objectMapper.readValue(jsonResponse, CartItemDto.class);
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("cartItemId")
                .isEqualTo(expected);
    }

    @Test
    @DisplayName("Should return 409 Conflict when book is already in shopping cart")
    @WithUserDetails("user@gmail.com")
    void addBook_BookIsAlreadyInCart_Conflict() throws Exception {
        CartItemRequestDto cartItemRequestDto =
                TestObjectsFactory.createCartItemRequestDto(VALID_BOOK_ID, 1);
        String jsonRequest = objectMapper.writeValueAsString(cartItemRequestDto);

        String jsonResponse = mockMvc.perform(post(BASE_URL)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isConflict())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(MessageFormat.format(
                BOOK_IS_ALREADY_IN_SHOPPING_CART_MESSAGE, cartItemRequestDto.bookId()));
    }

    @Test
    @DisplayName("Should return 404 Not Found when adding non-existing book to cart")
    @WithUserDetails("user@gmail.com")
    void addBook_BookDoesNotExist_NotFound() throws Exception {
        CartItemRequestDto cartItemRequestDto =
                TestObjectsFactory.createCartItemRequestDto(NON_EXISTING_BOOK_ID, 1);
        String jsonRequest = objectMapper.writeValueAsString(cartItemRequestDto);

        String jsonResponse = mockMvc.perform(post(BASE_URL)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(MessageFormat.format(
                BOOK_NOT_FOUND_MESSAGE, NON_EXISTING_BOOK_ID));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when request contains invalid bookId and quantity")
    @WithUserDetails("user@gmail.com")
    void addBook_InvalidRequestDto_BadRequest() throws Exception {
        CartItemRequestDto cartItemRequestDto =
                TestObjectsFactory.createCartItemRequestDto(NEGATIVE_BOOK_ID, -1);
        String jsonRequest = objectMapper.writeValueAsString(cartItemRequestDto);

        String jsonResponse = mockMvc.perform(post(BASE_URL)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(
                QUANTITY_MUST_BE_POSITIVE_MESSAGE, BOOK_ID_MUST_BE_POSITIVE_MESSAGE);
    }

    @Test
    @DisplayName("Should update cart item quantity when valid itemId and quantity are provided")
    @WithUserDetails("user@gmail.com")
    void updateCartItemQuantity_ValidItemId_ShouldReturnCartItemDto() throws Exception {
        UpdateCartItemDto updateCartItemDto = TestObjectsFactory.createUpdateCartItemDto(10);
        String jsonRequest = objectMapper.writeValueAsString(updateCartItemDto);

        String jsonResponse = mockMvc.perform(patch(
                        BASE_URL + ITEMS_PART_URL + ITEM_ID_PARAM, VALID_ITEM_ID)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        CartItemDto actual = objectMapper.readValue(jsonResponse, CartItemDto.class);
        assertThat(actual.quantity()).isEqualTo(updateCartItemDto.quantity());
    }

    @Test
    @DisplayName("Should return 404 Not Found when updating non-existing cart item")
    @WithUserDetails("user@gmail.com")
    void updateCartItemQuantity_ItemDoesNotExist_NotFound() throws Exception {
        UpdateCartItemDto updateCartItemDto = TestObjectsFactory.createUpdateCartItemDto(10);
        String jsonRequest = objectMapper.writeValueAsString(updateCartItemDto);

        String jsonResponse = mockMvc.perform(patch(
                        BASE_URL + ITEMS_PART_URL + ITEM_ID_PARAM, NON_EXISTING_ITEM_ID)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(MessageFormat.format(
                CART_ITEM_NOT_FOUND_MESSAGE, NON_EXISTING_ITEM_ID));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when updating cart item with negative itemId")
    @WithUserDetails("user@gmail.com")
    void updateCartItemQuantity_InvalidItemId_BadRequest() throws Exception {
        UpdateCartItemDto updateCartItemDto = TestObjectsFactory.createUpdateCartItemDto(10);
        String jsonRequest = objectMapper.writeValueAsString(updateCartItemDto);

        String jsonResponse = mockMvc.perform(patch(
                        BASE_URL + ITEMS_PART_URL + ITEM_ID_PARAM, NEGATIVE_ITEM_ID)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(ITEM_ID_MUST_BE_POSITIVE_MESSAGE);
    }

    @Test
    @DisplayName("Should return 400 Bad Request when updating cart item with negative quantity")
    @WithUserDetails("user@gmail.com")
    void updateCartItemQuantity_InvalidRequestDto_BadRequest() throws Exception {
        UpdateCartItemDto updateCartItemDto = TestObjectsFactory.createUpdateCartItemDto(-10);
        String jsonRequest = objectMapper.writeValueAsString(updateCartItemDto);

        String jsonResponse = mockMvc.perform(patch(
                        BASE_URL + ITEMS_PART_URL + ITEM_ID_PARAM, VALID_ITEM_ID)
                        .content(jsonRequest)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(QUANTITY_MUST_BE_POSITIVE_MESSAGE);
    }

    @Test
    @DisplayName("Should delete cart item and return 204 No Content for valid itemId")
    @WithUserDetails("user@gmail.com")
    void delete_ValidCartItemId_NoContent() throws Exception {
        mockMvc.perform(delete(BASE_URL + ITEMS_PART_URL + ITEM_ID_PARAM, VALID_ITEM_ID))
                .andExpect(status().isNoContent());

        Optional<CartItem> actual = cartItemRepository.findById(VALID_ITEM_ID);
        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("Should return 404 Not Found when deleting non-existing cart item")
    @WithUserDetails("user@gmail.com")
    void delete_CartItemDoesNotExist_NotFound() throws Exception {
        String jsonResponse = mockMvc.perform(delete(
                        BASE_URL + ITEMS_PART_URL + ITEM_ID_PARAM, NON_EXISTING_ITEM_ID))
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(MessageFormat.format(
                CART_ITEM_NOT_FOUND_MESSAGE, NON_EXISTING_ITEM_ID));
    }

    @Test
    @DisplayName("Should return 400 Bad Request when deleting cart item with negative itemId")
    @WithUserDetails("user@gmail.com")
    void delete_InvalidCartItemId_BadRequest() throws Exception {
        String jsonResponse = mockMvc.perform(delete(
                        BASE_URL + ITEMS_PART_URL + ITEM_ID_PARAM, NEGATIVE_ITEM_ID))
                .andExpect(status().isBadRequest())
                .andReturn()
                .getResponse()
                .getContentAsString();

        assertThat(jsonResponse).contains(ITEM_ID_MUST_BE_POSITIVE_MESSAGE);
    }
}
