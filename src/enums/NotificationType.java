package enums;

/**
 * Classifies what kind of event triggered an admin notification.
 */
public enum NotificationType {
    FAILED_LOGIN_KNOWN,   // Known user failed login 3 times — account locked
    UNKNOWN_ID_ALERT,     // Unrecognized ID attempted login — REST alert fired
    REG_REQUEST,          // New registration submitted from Login Page
    PASSWORD_RESET,       // Locked user requested a password reset
    PAYMENT_REQUEST,      // Client submitted a payment pending Admin approval
    SYSTEM                // General system alerts (e.g. new property listings)
}
