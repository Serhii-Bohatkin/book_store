package bookstore.dto.category;

public record CategoryDto(
        Long categoryId,
        String name,
        String description
) {
}
