package security;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * Utility class for SHA-256 password hashing and verification.
 * All methods are static — no instantiation needed.
 */
public class PasswordUtils {

    // ─── Constants ────────────────────────────────────────────
    private static final String ALGORITHM       = "SHA-256";
    private static final String TEMP_PASS_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@#$!";
    private static final int    TEMP_PASS_LENGTH = 10;

    // Prevent instantiation
    private PasswordUtils() {}

    // ─── Hash ─────────────────────────────────────────────────

    /**
     * Hash a plain-text password using SHA-256.
     *
     * @param plainPassword the raw password entered by the user
     * @return hexadecimal SHA-256 hash string
     * @throws IllegalArgumentException if password is null or empty
     */
    public static String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.isEmpty())
            throw new IllegalArgumentException("Password cannot be null or empty.");

        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hashBytes     = digest.digest(plainPassword.getBytes());
            return bytesToHex(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available on this system.", e);
        }
    }

    // ─── Verify ───────────────────────────────────────────────

    /**
     * Verify a plain-text password against a stored SHA-256 hash.
     *
     * @param plainPassword the raw password entered at login
     * @param storedHash    the hash stored in the database
     * @return true if the password matches, false otherwise
     */
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        if (plainPassword == null || plainPassword.isEmpty()) return false;
        if (storedHash    == null || storedHash.isEmpty())    return false;

        String computedHash = hashPassword(plainPassword);
        return computedHash.equalsIgnoreCase(storedHash);
    }

    // ─── Generate Temporary Password ──────────────────────────

    /**
     * Generate a secure random temporary password.
     * Used by Admin when approving a password reset request.
     *
     * @return a random 10-character password string
     */
    public static String generateTempPassword() {
        SecureRandom random      = new SecureRandom();
        StringBuilder tempPass   = new StringBuilder(TEMP_PASS_LENGTH);

        for (int i = 0; i < TEMP_PASS_LENGTH; i++) {
            int index = random.nextInt(TEMP_PASS_CHARS.length());
            tempPass.append(TEMP_PASS_CHARS.charAt(index));
        }
        return tempPass.toString();
    }

    // ─── Private Helper ───────────────────────────────────────

    /**
     * Convert a byte array to a hexadecimal string.
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hex = new StringBuilder();
        for (byte b : bytes) {
            hex.append(String.format("%02x", b));
        }
        return hex.toString();
    }
}
