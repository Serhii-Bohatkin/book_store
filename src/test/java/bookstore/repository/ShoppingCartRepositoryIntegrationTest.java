package bookstore.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.AFTER_TEST_METHOD;
import static org.springframework.test.context.jdbc.Sql.ExecutionPhase.BEFORE_TEST_METHOD;

import bookstore.TestObjectsFactory;
import bookstore.model.Book;
import bookstore.model.CartItem;
import bookstore.model.ShoppingCart;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Sql(scripts = {
        "classpath:database/clear-db.sql",
        "classpath:database/insert-books.sql",
        "classpath:database/insert-users.sql",
        "classpath:database/insert-shopping_cart.sql",
        "classpath:database/insert-cart_items.sql"}, executionPhase = BEFORE_TEST_METHOD)
@Sql(scripts = "classpath:database/clear-db.sql", executionPhase = AFTER_TEST_METHOD)
class ShoppingCartRepositoryIntegrationTest {
    private static final Long VALID_USER_ID = 1L;
    @Autowired
    private ShoppingCartRepository cartRepository;

    @Test
    @DisplayName("Should return shopping cart for valid user ID")
    void getByUserId_ValidUserId_ReturnShoppingCart() {
        ShoppingCart expected = TestObjectsFactory.createShoppingCartWithTwoBooks();
        ShoppingCart actual = cartRepository.getByUserId(VALID_USER_ID);
        assertThat(actual).isEqualTo(expected);
        assertThat(actual.getCartItems()).isNotEmpty();
        Optional<Book> bookOptional = actual.getCartItems().stream()
                .map(CartItem::getBook)
                .findFirst();
        assertThat(bookOptional).isPresent();
    }
}
