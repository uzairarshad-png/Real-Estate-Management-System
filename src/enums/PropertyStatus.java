package enums;

/**
 * Represents the current listing status of a property.
 */
public enum PropertyStatus {
    AVAILABLE,      // Listed and open for purchase or rent
    SOLD,           // Permanently purchased by a client
    RENTED,         // Currently rented by a client
    RENTED_OUT,     // Owner has rented it out via the system
    UNDER_REVIEW    // Pending admin verification before listing
}
