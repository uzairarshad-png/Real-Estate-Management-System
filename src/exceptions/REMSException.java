package exceptions;

/**
 * Base exception class for all REMS application errors.
 * Provides a consistent way to handle business logic errors.
 */
public class REMSException extends Exception {

    private final String errorCode;

    public REMSException(String message) {
        super(message);
        this.errorCode = "GENERAL_ERROR";
    }

    public REMSException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String toString() {
        return String.format("[%s] %s", errorCode, getMessage());
    }
}
