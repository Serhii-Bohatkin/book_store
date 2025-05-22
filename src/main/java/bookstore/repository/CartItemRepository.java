package bookstore.repository;

import bookstore.model.CartItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    boolean existsByShoppingCart_IdAndBook_Id(Long shoppingCartId, Long bookId);

    Optional<CartItem> findByIdAndShoppingCartId(Long cartItemId, Long shoppingCartId);
}
