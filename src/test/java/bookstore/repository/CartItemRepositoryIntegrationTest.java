package bookstore.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import bookstore.TestObjectsFactory;
import bookstore.model.CartItem;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@Sql(scripts = {
        "classpath:database/clear-db.sql",
        "classpath:database/insert-books.sql",
        "classpath:database/insert-users.sql",
        "classpath:database/insert-shopping_cart.sql",
        "classpath:database/insert-cart_items.sql"}, executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/clear-db.sql", executionPhase = AFTER_TEST_METHOD)
class CartItemRepositoryIntegrationTest {
    private static final Long VALID_USER_ID = 1L;
    private static final Long VALID_BOOK_ID = 1L;
    private static final Long INVALID_BOOK_ID = Long.MAX_VALUE;
    private static final Long INVALID_USER_ID = Long.MAX_VALUE;
    private static final Long VALID_CART_ITEM_ID = 1L;
    private static final Long INVALID_CART_ITEM_ID = Long.MAX_VALUE;
    @Autowired
    private CartItemRepository itemRepository;

    @Test
    @DisplayName("Should return true for existing cart item with valid cart ID and book ID")
    void existsByShoppingCart_IdAndBook_Id_ValidUserIdAndBookId_ReturnTrue() {
        boolean actual = itemRepository.existsByShoppingCart_User_IdAndBook_Id(VALID_USER_ID,
                VALID_BOOK_ID);
        assertThat(actual).isTrue();
    }

    @Test
    @DisplayName(
            "Should return false for existing cart item with valid cart ID and invalid book ID")
    void existsByShoppingCart_IdAndBook_Id_ValidUserIdAndInvalidBookId_ReturnFalse() {
        boolean actual = itemRepository.existsByShoppingCart_User_IdAndBook_Id(VALID_USER_ID,
                INVALID_BOOK_ID);
        assertThat(actual).isFalse();
    }

    @Test
    @DisplayName(
            "Should return false for existing cart item with invalid cart ID and valid book ID")
    void existsByShoppingCart_IdAndBook_Id_InvalidUserIdAndValidBookId_ReturnFalse() {
        boolean actual = itemRepository.existsByShoppingCart_User_IdAndBook_Id(INVALID_USER_ID,
                VALID_BOOK_ID);
        assertThat(actual).isFalse();
    }

    @Test
    @DisplayName(
            "Should return false for existing cart item with invalid cart ID and invalid book ID")
    void existsByShoppingCart_IdAndBook_Id_InvalidUserIdAndInvalidBookId_ReturnFalse() {
        boolean actual = itemRepository.existsByShoppingCart_User_IdAndBook_Id(INVALID_USER_ID,
                INVALID_BOOK_ID);
        assertThat(actual).isFalse();
    }

    @Test
    @DisplayName("Should return valid cart item optional for existing item ID and cart ID")
    void findByIdAndShoppingCartId_ValidIdAndValidUserId_ReturnValidOptionalCartUserItem() {
        Optional<CartItem> expected = Optional.of(TestObjectsFactory.createCartItem());
        Optional<CartItem> actual =
                itemRepository.findByIdAndShoppingCart_User_Id(VALID_CART_ITEM_ID,
                        VALID_USER_ID);
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should return empty optional for valid item ID and invalid cart ID")
    void findByIdAndShoppingCartId_ValidIdAndInvalidUserId_ReturnEmptyOptional() {
        Optional<CartItem> actual =
                itemRepository.findByIdAndShoppingCart_User_Id(VALID_CART_ITEM_ID,
                        INVALID_USER_ID);
        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("Should return empty optional for invalid item ID and invalid cart ID")
    void findByIdAndShoppingCartId_InvalidIdAndInvalidUserId_ReturnEmptyOptional() {
        Optional<CartItem> actual =
                itemRepository.findByIdAndShoppingCart_User_Id(INVALID_CART_ITEM_ID,
                        INVALID_USER_ID);
        assertThat(actual).isEmpty();
    }

    @Test
    @DisplayName("Should return empty optional for invalid item ID and valid cart ID")
    void findByIdAndShoppingCartId_InvalidIdAndValidUserId_ReturnEmptyOptional() {
        Optional<CartItem> actual =
                itemRepository.findByIdAndShoppingCart_User_Id(INVALID_CART_ITEM_ID,
                        VALID_USER_ID);
        assertThat(actual).isEmpty();
    }
}
