package bookstore.repository;

import bookstore.model.CartItem;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    boolean existsByShoppingCart_User_IdAndBook_Id(Long userId, Long bookId);

    Optional<CartItem> findByIdAndShoppingCart_User_Id(Long cartItemId, Long userId);

}
