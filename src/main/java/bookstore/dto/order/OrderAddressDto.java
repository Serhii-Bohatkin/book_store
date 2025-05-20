package bookstore.dto.order;

import jakarta.validation.constraints.NotBlank;

public record OrderAddressDto(
        @NotBlank
        String shippingAddress
) {
}
