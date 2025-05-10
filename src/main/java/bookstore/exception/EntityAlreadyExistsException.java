package bookstore.exception;

import java.text.MessageFormat;

public class EntityAlreadyExistsException extends RuntimeException {
    public EntityAlreadyExistsException(String message) {
        super(message);
    }

    public EntityAlreadyExistsException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }
}
