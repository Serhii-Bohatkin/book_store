package bookstore.validation.impl;

import bookstore.dto.user.UserUpdateDto;
import bookstore.validation.AtLeastOneFieldNotBlank;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.stream.Stream;

public class AtLeastOneFieldNotBlankValidator implements
        ConstraintValidator<AtLeastOneFieldNotBlank, UserUpdateDto> {

    @Override
    public boolean isValid(UserUpdateDto updateDto,
                           ConstraintValidatorContext constraintValidatorContext) {
        if (updateDto == null) {
            return false;
        }

        return Stream.of(updateDto.firstName(), updateDto.lastName(), updateDto.shippingAddress())
                .anyMatch(fieldValue -> fieldValue != null && !fieldValue.isBlank());
    }
}
