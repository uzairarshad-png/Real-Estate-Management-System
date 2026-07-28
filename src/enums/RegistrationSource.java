package enums;

/**
 * Indicates where a registration request originated.
 */
public enum RegistrationSource {
    LOGIN_PAGE,    // Self-registered from Sign Up tab — requires Admin approval
    ADMIN_PANEL    // Created directly by Admin — instantly activated
}
