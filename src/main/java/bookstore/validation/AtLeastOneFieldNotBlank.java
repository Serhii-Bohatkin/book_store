package bookstore.validation;

import bookstore.validation.impl.AtLeastOneFieldNotBlankValidator;
import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = AtLeastOneFieldNotBlankValidator.class)
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
public @interface AtLeastOneFieldNotBlank {
    String message() default "Please fill in at least one field";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
