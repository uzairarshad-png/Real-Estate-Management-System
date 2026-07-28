package security;

import java.util.regex.Pattern;

/**
 * Utility class for validating all user input fields.
 * All methods are static — no instantiation needed.
 *
 * Validates: email, password strength, phone, CNIC,
 *            names, prices, areas, and general empty checks.
 */
public class InputValidator {

    // ─── Regex Patterns ───────────────────────────────────────
    // Standard email format: user@domain.ext
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[a-zA-Z0-9._%+\\-]+@[a-zA-Z0-9.\\-]+\\.[a-zA-Z]{2,6}$");

    // Pakistani phone: 03XX-XXXXXXX or 03XXXXXXXXX
    private static final Pattern PHONE_PATTERN =
            Pattern.compile("^(03[0-9]{2}[-]?[0-9]{7})$");

    // Pakistani CNIC: XXXXX-XXXXXXX-X
    private static final Pattern CNIC_PATTERN =
            Pattern.compile("^[0-9]{5}-[0-9]{7}-[0-9]$");

    // License number: alphanumeric, 5–15 characters
    private static final Pattern LICENSE_PATTERN =
            Pattern.compile("^[A-Za-z0-9\\-]{5,15}$");

    // Password: min 8 chars, 1 uppercase, 1 lowercase, 1 digit, 1 special char
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$!%^&*()_+]).{8,}$");

    // Prevent instantiation
    private InputValidator() {}

    // ─── Empty / Null ─────────────────────────────────────────

    /**
     * Check that a string is not null or blank.
     */
    public static boolean isNotEmpty(String input) {
        return input != null && !input.trim().isEmpty();
    }

    // ─── Email ────────────────────────────────────────────────

    /**
     * Validate email format.
     * Example valid: user@example.com
     */
    public static boolean isValidEmail(String email) {
        if (!isNotEmpty(email)) return false;
        return EMAIL_PATTERN.matcher(email.trim()).matches();
    }

    /**
     * Returns a human-readable error message if email is invalid.
     */
    public static String getEmailError(String email) {
        if (!isNotEmpty(email))        return "Email cannot be empty.";
        if (!isValidEmail(email))      return "Invalid email format. Example: user@domain.com";
        return null;
    }

    // ─── Password ─────────────────────────────────────────────

    /**
     * Validate password strength:
     * - Minimum 8 characters
     * - At least 1 uppercase letter
     * - At least 1 lowercase letter
     * - At least 1 digit
     * - At least 1 special character (@#$!%^&*()_+)
     */
    public static boolean isStrongPassword(String password) {
        if (!isNotEmpty(password)) return false;
        return PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * Returns a human-readable error message if password is weak.
     */
    public static String getPasswordError(String password) {
        if (!isNotEmpty(password))
            return "Password cannot be empty.";
        if (password.length() < 8)
            return "Password must be at least 8 characters long.";
        if (!password.matches(".*[A-Z].*"))
            return "Password must contain at least one uppercase letter.";
        if (!password.matches(".*[a-z].*"))
            return "Password must contain at least one lowercase letter.";
        if (!password.matches(".*\\d.*"))
            return "Password must contain at least one digit.";
        if (!password.matches(".*[@#$!%^&*()_+].*"))
            return "Password must contain at least one special character (@#$!%^&*()_+).";
        return null;
    }

    // ─── Phone ────────────────────────────────────────────────

    /**
     * Validate Pakistani phone number format.
     * Accepted: 03001234567 or 0300-1234567
     */
    public static boolean isValidPhone(String phone) {
        if (!isNotEmpty(phone)) return false;
        return PHONE_PATTERN.matcher(phone.trim()).matches();
    }

    /**
     * Returns a human-readable error message if phone is invalid.
     */
    public static String getPhoneError(String phone) {
        if (!isNotEmpty(phone))   return "Phone number cannot be empty.";
        if (!isValidPhone(phone)) return "Invalid phone format. Example: 0300-1234567 or 03001234567";
        return null;
    }

    // ─── CNIC ─────────────────────────────────────────────────

    /**
     * Validate Pakistani CNIC format: XXXXX-XXXXXXX-X
     */
    public static boolean isValidCnic(String cnic) {
        if (!isNotEmpty(cnic)) return false;
        return CNIC_PATTERN.matcher(cnic.trim()).matches();
    }

    /**
     * Returns a human-readable error message if CNIC is invalid.
     */
    public static String getCnicError(String cnic) {
        if (!isNotEmpty(cnic))   return "CNIC cannot be empty.";
        if (!isValidCnic(cnic))  return "Invalid CNIC format. Example: 35201-1234567-9";
        return null;
    }

    // ─── Name ─────────────────────────────────────────────────

    /**
     * Validate a person's name:
     * - Not empty
     * - 2–50 characters
     * - Letters and spaces only
     */
    public static boolean isValidName(String name) {
        if (!isNotEmpty(name)) return false;
        String trimmed = name.trim();
        return trimmed.length() >= 2
            && trimmed.length() <= 50
            && trimmed.matches("[a-zA-Z ]+");
    }

    /**
     * Returns a human-readable error message if name is invalid.
     */
    public static String getNameError(String name) {
        if (!isNotEmpty(name))       return "Name cannot be empty.";
        if (name.trim().length() < 2) return "Name must be at least 2 characters.";
        if (name.trim().length() > 50) return "Name cannot exceed 50 characters.";
        if (!name.trim().matches("[a-zA-Z ]+")) return "Name can only contain letters and spaces.";
        return null;
    }

    // ─── License Number ───────────────────────────────────────

    /**
     * Validate an agent license number (5–15 alphanumeric chars).
     */
    public static boolean isValidLicense(String license) {
        if (!isNotEmpty(license)) return false;
        return LICENSE_PATTERN.matcher(license.trim()).matches();
    }

    /**
     * Returns a human-readable error message if license is invalid.
     */
    public static String getLicenseError(String license) {
        if (!isNotEmpty(license))     return "License number cannot be empty.";
        if (!isValidLicense(license)) return "Invalid license number. Must be 5–15 alphanumeric characters.";
        return null;
    }

    // ─── Numeric ──────────────────────────────────────────────

    /**
     * Validate a positive price value (must be > 0).
     */
    public static boolean isValidPrice(double price) {
        return price > 0;
    }

    /**
     * Validate a positive area value (must be > 0).
     */
    public static boolean isValidArea(double area) {
        return area > 0;
    }

    /**
     * Validate a commission rate (must be between 0 and 100).
     */
    public static boolean isValidCommissionRate(double rate) {
        return rate >= 0 && rate <= 100;
    }

    /**
     * Validate a salary (must be >= 0).
     */
    public static boolean isValidSalary(double salary) {
        return salary >= 0;
    }

    /**
     * Validate rental duration in months (must be >= 1).
     */
    public static boolean isValidDuration(int months) {
        return months >= 1;
    }

    // ─── Password Confirmation ────────────────────────────────

    /**
     * Check that two password entries match (for registration confirm field).
     */
    public static boolean passwordsMatch(String pass1, String pass2) {
        if (pass1 == null || pass2 == null) return false;
        return pass1.equals(pass2);
    }

    /**
     * Validate an image file path (must end with common image extensions).
     */
    public static boolean isValidImagePath(String path) {
        if (!isNotEmpty(path)) return false;
        String lower = path.toLowerCase();
        return lower.endsWith(".jpg") || lower.endsWith(".jpeg")
            || lower.endsWith(".png") || lower.endsWith(".gif");
    }
}
