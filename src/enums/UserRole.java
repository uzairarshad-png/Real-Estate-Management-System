package enums;

/**
 * Defines the role of each user account in the system.
 */
public enum UserRole {
    ADMIN,    // Full system control — hardcoded credentials
    CLIENT,   // Can buy, sell, rent, and rent out properties
    AGENT     // Manages listings, earns salary + commission
}
