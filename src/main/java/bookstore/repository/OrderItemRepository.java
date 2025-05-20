package bookstore.repository;

import bookstore.model.OrderItem;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    @EntityGraph(attributePaths = "book")
    Page<OrderItem> findByOrderIdAndOrderUserId(Long orderId, Long userId, Pageable pageable);

    @EntityGraph(attributePaths = "book")
    Optional<OrderItem> findByIdAndOrderIdAndOrderUserId(Long orderItemId,
                                                         Long orderId,
                                                         Long userId);
}
