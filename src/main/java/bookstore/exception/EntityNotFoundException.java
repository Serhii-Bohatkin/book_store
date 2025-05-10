package bookstore.exception;

import java.text.MessageFormat;
import java.util.function.Supplier;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(String message) {
        super(message);
    }

    public EntityNotFoundException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }

    public static Supplier<EntityNotFoundException> entityNotFoundException(
            String message, Object... args) {
        return () -> new EntityNotFoundException(message, args);
    }

    public static Supplier<EntityNotFoundException> entityNotFoundException(String message) {
        return () -> new EntityNotFoundException(message);
    }
}
