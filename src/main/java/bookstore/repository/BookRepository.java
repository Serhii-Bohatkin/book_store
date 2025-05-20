package bookstore.repository;

import bookstore.model.Book;
import java.util.Optional;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;

public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    boolean existsByIsbn(String isbn);

    @Query("SELECT DISTINCT b FROM Book b JOIN b.categories c WHERE c.id = :categoryId")
    Page<Book> findAllByCategoryId(Long categoryId, Pageable pageable);

    @NonNull
    @EntityGraph(attributePaths = "categories")
    Page<Book> findAll(@NonNull Pageable pageable);

    @NonNull
    @EntityGraph(attributePaths = "categories")
    Page<Book> findAll(@Nullable Specification<Book> spec, @NonNull Pageable pageable);

    @NonNull
    @EntityGraph(attributePaths = "categories")
    Optional<Book> findById(@NonNull Long id);
}
