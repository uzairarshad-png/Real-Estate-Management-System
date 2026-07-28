package exceptions;

/**
 * Thrown when a property has invalid or missing data
 * (e.g. negative price, empty address, zero area).
 */
public class InvalidPropertyException extends Exception {

    private final String fieldName;

    public InvalidPropertyException(String message) {
        super(message);
        this.fieldName = "Unknown";
    }

    public InvalidPropertyException(String fieldName, String message) {
        super("Invalid value for [" + fieldName + "]: " + message);
        this.fieldName = fieldName;
    }

    public String getFieldName() { return fieldName; }
}
