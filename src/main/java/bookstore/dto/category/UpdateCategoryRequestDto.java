package bookstore.dto.category;

public record UpdateCategoryRequestDto(
        String name,
        String description
) {
}
