package notification;

import enums.RequestStatus;
import security.AuditLog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a password reset request submitted by a locked user.
 *
 * Lifecycle:
 *   User locked → submits request → PENDING
 *   → Admin approves with new hash → APPROVED → account unlocked
 *   OR
 *   → Admin rejects → REJECTED → account stays locked
 */
public class PasswordResetRequest {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private static int idCounter = 1;

    // ─── Fields ───────────────────────────────────────────────
    private final int         resetId;
    private final String      email;
    private final String      submittedAt;
    private RequestStatus     status;
    private String            newPasswordHash;
    private String            adminNote;

    // ─── Constructor ──────────────────────────────────────────
    public PasswordResetRequest(String email) {
        if (email == null || email.trim().isEmpty())
            throw new IllegalArgumentException("Email cannot be null or empty.");

        this.resetId         = idCounter++;
        this.email           = email.trim().toLowerCase();
        this.submittedAt     = LocalDateTime.now().format(FORMATTER);
        this.status          = RequestStatus.PENDING;
        this.newPasswordHash = null;
        this.adminNote       = "";

        AuditLog.getInstance().logPasswordResetRequest(this.email);
    }

    // ─── State Transitions ────────────────────────────────────

    /**
     * Admin approves the reset and provides a new password hash.
     * The Admin.approvePasswordReset() method will set this on the Person.
     *
     * @param newHash SHA-256 hash of the new password
     * @throws IllegalArgumentException if hash is null or empty
     * @throws IllegalStateException    if not in PENDING state
     */
    public void approve(String newHash) {
        if (newHash == null || newHash.trim().isEmpty())
            throw new IllegalArgumentException("New password hash cannot be null or empty.");
        if (status != RequestStatus.PENDING)
            throw new IllegalStateException(
                "Cannot approve reset request [#" + resetId
                + "]. Current status: " + status);

        this.newPasswordHash = newHash.trim();
        this.status          = RequestStatus.APPROVED;
        AuditLog.getInstance().logPasswordResetApproved(email);
    }

    /**
     * Admin rejects the reset request with an optional note.
     * @throws IllegalStateException if not in PENDING state
     */
    public void reject(String note) {
        if (status != RequestStatus.PENDING)
            throw new IllegalStateException(
                "Cannot reject reset request [#" + resetId
                + "]. Current status: " + status);

        this.status    = RequestStatus.REJECTED;
        this.adminNote = (note != null && !note.trim().isEmpty())
                         ? note.trim() : "No reason provided.";
    }

    /** Reject without a note. */
    public void reject() { reject("No reason provided."); }

    // ─── Getters ──────────────────────────────────────────────
    public int           getResetId()         { return resetId; }
    public String        getEmail()           { return email; }
    public String        getSubmittedAt()     { return submittedAt; }
    public RequestStatus getStatus()          { return status; }
    public String        getNewPasswordHash() { return newPasswordHash; }
    public String        getAdminNote()       { return adminNote; }

    @Override
    public String toString() {
        return String.format(
            "[PWD RESET #%d] Email: %s | Status: %s | Submitted: %s%s",
            resetId, email, status, submittedAt,
            adminNote.isEmpty() ? "" : " | Note: " + adminNote);
    }
}
