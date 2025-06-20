package bookstore.repository;

import bookstore.model.ShoppingCart;
import lombok.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShoppingCartRepository extends JpaRepository<ShoppingCart, Long> {
    @NonNull
    @EntityGraph(attributePaths = "cartItems.book")
    ShoppingCart getByUserId(@NonNull Long userId);
}
