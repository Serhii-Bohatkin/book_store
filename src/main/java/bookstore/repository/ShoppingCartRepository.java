package bookstore.repository;

import bookstore.model.ShoppingCart;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    @NonNull
    @EntityGraph(attributePaths = "cartItems.book")
    Optional<ShoppingCart> findById(@NonNull Long userId);
}
