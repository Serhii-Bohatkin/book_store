package bookstore.exception;

import java.text.MessageFormat;

public class RegistrationException extends RuntimeException {
    public RegistrationException(String message) {
        super(message);
    }

    public RegistrationException(String message, Object... args) {
        super(MessageFormat.format(message, args));
    }
}
