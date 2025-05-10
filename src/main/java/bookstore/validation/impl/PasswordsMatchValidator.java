package bookstore.validation.impl;

import bookstore.validation.PasswordsMatch;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Objects;
import org.springframework.beans.BeanWrapperImpl;

public class PasswordsMatchValidator implements ConstraintValidator<PasswordsMatch, Object> {
    private String password;
    private String repeatPassword;

    @Override
    public void initialize(PasswordsMatch annotation) {
        this.password = annotation.password();
        this.repeatPassword = annotation.repeatPassword();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        BeanWrapperImpl wrapper = new BeanWrapperImpl(value);
        Object firstValue = wrapper.getPropertyValue(password);
        Object secondValue = wrapper.getPropertyValue(repeatPassword);
        return Objects.equals(firstValue, secondValue);
    }
}
