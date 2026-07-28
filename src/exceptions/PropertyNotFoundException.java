package exceptions;

/**
 * Thrown when a property cannot be found in the system by its ID.
 */
public class PropertyNotFoundException extends Exception {

    private final int propertyId;

    public PropertyNotFoundException(int propertyId) {
        super("Property with ID [" + propertyId + "] was not found in the system.");
        this.propertyId = propertyId;
    }

    public int getPropertyId() { return propertyId; }
}
