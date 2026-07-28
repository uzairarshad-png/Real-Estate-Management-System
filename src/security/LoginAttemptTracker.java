package security;

import enums.AccountStatus;
import model.Person;
import notification.NotificationCenter;
import notification.Notification;
import enums.NotificationType;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Tracks failed login attempts per user email.
 *
 * Smart Lockout Logic:
 * - After MAX_ATTEMPTS (3) failed logins:
 *   → Account status set to LOCKED_PENDING_ADMIN
 *   → Admin notified via NotificationCenter
 *   → REST API alert fired via RestApiService
 *   → User CANNOT log in until Admin manually unlocks
 *
 * Unknown ID Logic:
 * - If email not found in system:
 *   → REST API alert fired with unknown ID
 *   → Admin notified via UNKNOWN_ID_ALERT notification
 */
public class LoginAttemptTracker {

    // ─── Constants ────────────────────────────────────────────
    public static final int MAX_ATTEMPTS = 3;

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    // ─── State Maps ───────────────────────────────────────────
    // email → number of consecutive failed attempts
    private final Map<String, Integer> attemptCount;

    // emails that are locked AND require Admin approval to unlock
    private final Set<String> adminApprovalRequired;

    // ─── Constructor ──────────────────────────────────────────
    public LoginAttemptTracker() {
        this.attemptCount          = new HashMap<>();
        this.adminApprovalRequired = new HashSet<>();
    }

    // ─── Record Failed Attempt ────────────────────────────────

    /**
     * Record a failed login attempt for a known user.
     * Locks the account after MAX_ATTEMPTS and notifies Admin.
     *
     * @param email  the email that failed login
     * @param user   the Person object (must not be null — caller verified ID exists)
     */
    public void recordFailedAttempt(String email, Person user) {
        if (email == null || email.trim().isEmpty())
            throw new IllegalArgumentException("Email cannot be null or empty.");
        if (user == null)
            throw new IllegalArgumentException("User cannot be null.");

        String normalizedEmail = email.trim().toLowerCase();
        int current = attemptCount.getOrDefault(normalizedEmail, 0) + 1;
        attemptCount.put(normalizedEmail, current);

        // Lock account after MAX_ATTEMPTS
        if (current >= MAX_ATTEMPTS) {
            user.setAccountStatus(AccountStatus.LOCKED_PENDING_ADMIN);
            adminApprovalRequired.add(normalizedEmail);

            // Fire REST API alert
            RestApiService.getInstance().alertLockedAccount(normalizedEmail);

            // Push notification to Admin
            String timestamp = LocalDateTime.now().format(FORMATTER);
            Notification notif = new Notification(
                NotificationType.FAILED_LOGIN_KNOWN,
                "Account LOCKED: [" + normalizedEmail + "] failed login "
                + MAX_ATTEMPTS + " times. Manual Admin unlock required. Time: " + timestamp,
                normalizedEmail
            );
            NotificationCenter.getInstance().push(notif);
        }
    }

    // ─── Handle Unknown ID ────────────────────────────────────

    /**
     * Called when the entered email does not exist in the system.
     * Fires REST alert and pushes UNKNOWN_ID_ALERT to Admin.
     *
     * @param unknownId the email/ID that was not found
     */
    public void handleUnknownId(String unknownId) {
        if (unknownId == null || unknownId.trim().isEmpty())
            throw new IllegalArgumentException("Unknown ID cannot be null or empty.");

        String timestamp = LocalDateTime.now().format(FORMATTER);

        // Fire REST API alert
        RestApiService.getInstance().alertUnknownId(unknownId, timestamp);

        // Push notification to Admin
        Notification notif = new Notification(
            NotificationType.UNKNOWN_ID_ALERT,
            "SUSPICIOUS LOGIN ATTEMPT: Unknown ID [" + unknownId
            + "] attempted to log in at " + timestamp + ". REST alert sent.",
            unknownId
        );
        NotificationCenter.getInstance().push(notif);
    }

    // ─── Check Lock Status ────────────────────────────────────

    /**
     * Check if this email is currently locked (reached max attempts).
     */
    public boolean isLocked(String email) {
        if (email == null) return false;
        return adminApprovalRequired.contains(email.trim().toLowerCase());
    }

    /**
     * Check if this locked account requires Admin approval to unlock.
     */
    public boolean requiresAdminApproval(String email) {
        if (email == null) return false;
        return adminApprovalRequired.contains(email.trim().toLowerCase());
    }

    /**
     * Get how many attempts remain before lockout.
     */
    public int getRemainingAttempts(String email) {
        if (email == null) return MAX_ATTEMPTS;
        int used = attemptCount.getOrDefault(email.trim().toLowerCase(), 0);
        return Math.max(0, MAX_ATTEMPTS - used);
    }

    /**
     * Get total failed attempts for this email.
     */
    public int getFailedAttempts(String email) {
        if (email == null) return 0;
        return attemptCount.getOrDefault(email.trim().toLowerCase(), 0);
    }

    // ─── Admin Unlock ─────────────────────────────────────────

    /**
     * Admin unlocks a previously locked account.
     * Resets attempt counter and removes from approval-required set.
     *
     * @param email the email to unlock
     * @param user  the Person object to set back to ACTIVE
     */
    public void adminUnlock(String email, Person user) {
        if (email == null || email.trim().isEmpty())
            throw new IllegalArgumentException("Email cannot be null or empty.");
        if (user == null)
            throw new IllegalArgumentException("User cannot be null.");

        String normalized = email.trim().toLowerCase();
        attemptCount.remove(normalized);
        adminApprovalRequired.remove(normalized);
        user.setAccountStatus(AccountStatus.ACTIVE);
    }

    // ─── Reset Attempts ───────────────────────────────────────

    /**
     * Reset the attempt counter after a successful login.
     */
    public void resetAttempts(String email) {
        if (email == null) return;
        attemptCount.remove(email.trim().toLowerCase());
    }
}
