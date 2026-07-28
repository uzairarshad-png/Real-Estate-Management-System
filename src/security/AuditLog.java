package security;

import payment.Payment;
import database.DatabaseManager;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Singleton class that records all significant system events:
 * - Successful logins
 * - Failed logins
 * - Unknown ID attempts
 * - Logouts
 * - Payment events
 * - Account lock/unlock events
 *
 * All logs are timestamped and stored in memory,
 * and persisted to the database via DatabaseManager.
 */
public class AuditLog {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    // ─── Singleton Instance ───────────────────────────────────
    private static AuditLog instance;

    // ─── State ────────────────────────────────────────────────
    private final List<String> logs;

    // ─── Private Constructor ──────────────────────────────────
    private AuditLog() {
        this.logs = new ArrayList<>();
    }

    // ─── Singleton Accessor ───────────────────────────────────
    public static AuditLog getInstance() {
        if (instance == null) {
            instance = new AuditLog();
        }
        return instance;
    }

    // ─── Timestamp Helper ─────────────────────────────────────
    private String now() {
        return LocalDateTime.now().format(FORMATTER);
    }

    private void addLog(String entry) {
        logs.add(entry);
        // Forward to DatabaseManager for persistence
        DatabaseManager.getInstance().saveAuditLog(entry);
    }

    // ─── Login Events ─────────────────────────────────────────

    /**
     * Log a successful login.
     */
    public void logSuccess(String email, String role) {
        if (email == null || role == null) return;
        addLog(String.format("[%s] LOGIN SUCCESS | Email: %s | Role: %s",
                now(), email, role));
    }

    /**
     * Log a failed login attempt with remaining attempts count.
     */
    public void logFailure(String email, int remainingAttempts) {
        if (email == null) return;
        addLog(String.format("[%s] LOGIN FAILED | Email: %s | Remaining Attempts: %d",
                now(), email, remainingAttempts));
    }

    /**
     * Log a login attempt with an unknown/unrecognized ID.
     */
    public void logSuspiciousId(String unknownId) {
        if (unknownId == null) return;
        addLog(String.format("[%s] SUSPICIOUS ATTEMPT | Unknown ID: [%s] | REST Alert Fired",
                now(), unknownId));
    }

    /**
     * Log when an account gets locked after max failed attempts.
     */
    public void logAccountLocked(String email) {
        if (email == null) return;
        addLog(String.format("[%s] ACCOUNT LOCKED | Email: %s | Reason: Max attempts reached | Admin approval required",
                now(), email));
    }

    /**
     * Log when Admin unlocks a previously locked account.
     */
    public void logAccountUnlocked(String email, String adminEmail) {
        if (email == null) return;
        addLog(String.format("[%s] ACCOUNT UNLOCKED | Email: %s | Unlocked by Admin: %s",
                now(), email, adminEmail));
    }

    // ─── Logout ───────────────────────────────────────────────

    /**
     * Log a successful logout.
     */
    public void logLogout(String email) {
        if (email == null) return;
        addLog(String.format("[%s] LOGOUT | Email: %s",
                now(), email));
    }

    // ─── Registration Events ──────────────────────────────────

    /**
     * Log a new registration request submitted from Login Page.
     */
    public void logRegistrationRequest(String email, String role) {
        if (email == null) return;
        addLog(String.format("[%s] REGISTRATION REQUEST | Email: %s | Role: %s | Status: Pending Admin approval",
                now(), email, role));
    }

    /**
     * Log Admin approving a registration request.
     */
    public void logRegistrationApproved(String email) {
        if (email == null) return;
        addLog(String.format("[%s] REGISTRATION APPROVED | Email: %s | Account Activated",
                now(), email));
    }

    /**
     * Log Admin rejecting a registration request.
     */
    public void logRegistrationRejected(String email) {
        if (email == null) return;
        addLog(String.format("[%s] REGISTRATION REJECTED | Email: %s",
                now(), email));
    }

    // ─── Password Events ──────────────────────────────────────

    /**
     * Log a password reset request submission.
     */
    public void logPasswordResetRequest(String email) {
        if (email == null) return;
        addLog(String.format("[%s] PASSWORD RESET REQUEST | Email: %s | Status: Pending Admin",
                now(), email));
    }

    /**
     * Log Admin approving a password reset.
     */
    public void logPasswordResetApproved(String email) {
        if (email == null) return;
        addLog(String.format("[%s] PASSWORD RESET APPROVED | Email: %s | New password set",
                now(), email));
    }

    // ─── Payment Events ───────────────────────────────────────

    /**
     * Log a payment event (creation, approval, rejection, disbursement).
     */
    public void logPayment(Payment payment, String event) {
        if (payment == null) return;
        addLog(String.format("[%s] PAYMENT %s | ID: %d | Amount: PKR %.2f | Payer: %s | Payee: %s",
                now(), event,
                payment.getPaymentId(),
                payment.getAmount(),
                payment.getPayer(),
                payment.getPayee()));
    }

    // ─── Profile Events ───────────────────────────────────────

    /**
     * Log Admin creating a new profile directly from Admin Panel.
     */
    public void logProfileCreated(String email, String role) {
        if (email == null) return;
        addLog(String.format("[%s] PROFILE CREATED | Email: %s | Role: %s | Created by Admin",
                now(), email, role));
    }

    /**
     * Log a manual account status change by Admin.
     */
    public void logAccountStatusChanged(String email, String status) {
        if (email == null) return;
        addLog(String.format("[%s] ACCOUNT STATUS CHANGE | Email: %s | New Status: %s | Updated by Admin",
                now(), email, status));
    }

    /**
     * Log a profile deletion.
     */
    public void logProfileDeleted(String email, String role) {
        if (email == null) return;
        addLog(String.format("[%s] PROFILE DELETED | Email: %s | Role: %s | Action by Admin",
                now(), email, role));
    }

    // ─── Getters ──────────────────────────────────────────────

    /**
     * Get all audit log entries (unmodifiable).
     */
    public List<String> getLogs() {
        return Collections.unmodifiableList(logs);
    }

    /**
     * Get total number of log entries.
     */
    public int getLogCount() {
        return logs.size();
    }

    /**
     * Load previously saved log entries from database (called on startup).
     */
    public void loadLogs(List<String> savedLogs) {
        if (savedLogs != null) {
            logs.addAll(savedLogs);
        }
    }

    /**
     * Print all logs to console (for debugging).
     */
    public void printAll() {
        System.out.println("═══════════════ AUDIT LOG ═══════════════");
        for (String log : logs) {
            System.out.println(log);
        }
        System.out.println("═════════════════════════════════════════");
    }
}
