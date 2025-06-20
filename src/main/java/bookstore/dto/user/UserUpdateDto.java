package bookstore.dto.user;

import bookstore.validation.AtLeastOneFieldNotBlank;
import org.hibernate.validator.constraints.Length;

@AtLeastOneFieldNotBlank
public record UserUpdateDto(
        @Length(min = 3, max = 30)
        String firstName,
        @Length(min = 3, max = 30)
        String lastName,
        @Length(min = 10, max = 150)
        String shippingAddress
) {
}
