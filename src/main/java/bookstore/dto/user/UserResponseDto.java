package bookstore.dto.user;

public record UserResponseDto(
        Long userId,
        String email,
        String firstName,
        String lastName,
        String shippingAddress
) {
}
