package enums;

/**
 * Represents the current state of a user account.
 */
public enum AccountStatus {
    ACTIVE,                 // Account is active and can log in
    LOCKED_PENDING_ADMIN,   // Locked after 3 failed attempts — requires Admin approval
    PENDING_REGISTRATION,   // Registered from Login Page — awaiting Admin approval
    SUSPENDED               // Manually suspended by Admin
}
