package security;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Singleton class that simulates REST API security alerts.
 *
 * In a production system this would make real HTTP POST calls
 * to an external endpoint. In this OOP lab project it simulates
 * the REST call in pure Java — printing a structured JSON-like
 * payload and logging the alert — demonstrating the concept
 * without requiring external dependencies.
 *
 * Fires alerts for:
 * 1. Known user account locked after 3 failed attempts
 * 2. Unknown/unrecognized ID attempting to log in
 */
public class RestApiService {

    // ─── Constants ────────────────────────────────────────────
    private static final String ADMIN_ALERT_ENDPOINT =
            "http://localhost:8080/api/security/alert";   // simulated endpoint

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    // ─── Singleton Instance ───────────────────────────────────
    private static RestApiService instance;

    // ─── Private Constructor ──────────────────────────────────
    private RestApiService() {}

    // ─── Singleton Accessor ───────────────────────────────────
    public static RestApiService getInstance() {
        if (instance == null) {
            instance = new RestApiService();
        }
        return instance;
    }

    // ─── Alert: Known Account Locked ──────────────────────────

    /**
     * Fire a REST alert when a known user's account gets locked
     * after exceeding the maximum failed login attempts.
     *
     * @param email the email of the locked account
     */
    public void alertLockedAccount(String email) {
        if (email == null || email.trim().isEmpty())
            throw new IllegalArgumentException("Email cannot be null or empty.");

        String timestamp = LocalDateTime.now().format(FORMATTER);
        String payload   = buildPayload("ACCOUNT_LOCKED", email, timestamp,
                "Account locked after " + LoginAttemptTracker.MAX_ATTEMPTS
                + " consecutive failed login attempts. Admin unlock required.");

        sendAlert(payload, "ACCOUNT_LOCKED");
        AuditLog.getInstance().logAccountLocked(email);
    }

    // ─── Alert: Unknown ID ────────────────────────────────────

    /**
     * Fire a REST alert when an unrecognized email/ID attempts to log in.
     *
     * @param unknownId the unrecognized email or ID string
     * @param timestamp the time of the attempt
     */
    public void alertUnknownId(String unknownId, String timestamp) {
        if (unknownId == null || unknownId.trim().isEmpty())
            throw new IllegalArgumentException("Unknown ID cannot be null or empty.");
        if (timestamp == null || timestamp.trim().isEmpty())
            timestamp = LocalDateTime.now().format(FORMATTER);

        String payload = buildPayload("UNKNOWN_ID_ATTEMPT", unknownId, timestamp,
                "Login attempted with unrecognized ID. No matching account found in system.");

        sendAlert(payload, "UNKNOWN_ID_ATTEMPT");
        AuditLog.getInstance().logSuspiciousId(unknownId);
    }

    // ─── Payload Builder ──────────────────────────────────────

    /**
     * Build a structured JSON-like alert payload string.
     *
     * @param alertType  type label for the alert
     * @param identifier the email or ID involved
     * @param timestamp  when the event occurred
     * @param message    descriptive message
     * @return formatted payload string
     */
    public String buildPayload(String alertType, String identifier,
                               String timestamp, String message) {
        return String.format(
            "{\n" +
            "  \"alert_type\"  : \"%s\",\n" +
            "  \"identifier\"  : \"%s\",\n" +
            "  \"timestamp\"   : \"%s\",\n" +
            "  \"message\"     : \"%s\",\n" +
            "  \"endpoint\"    : \"%s\",\n" +
            "  \"system\"      : \"REMS v4.0\"\n" +
            "}",
            alertType, identifier, timestamp, message, ADMIN_ALERT_ENDPOINT
        );
    }

    // ─── Send Alert (Simulated) ───────────────────────────────

    /**
     * Simulate sending the REST alert payload.
     * In production: would use HttpURLConnection or HttpClient.
     * In this prototype: prints the payload with a clear simulation label.
     *
     * @param payload   the JSON-like payload string
     * @param alertType type label for console display
     */
    private void sendAlert(String payload, String alertType) {
        System.out.println("\n╔══════════════════════════════════════════════╗");
        System.out.println("║      REST API SECURITY ALERT (SIMULATED)     ║");
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println("║  Endpoint : " + ADMIN_ALERT_ENDPOINT);
        System.out.println("║  Type     : " + alertType);
        System.out.println("╠══════════════════════════════════════════════╣");
        System.out.println(payload);
        System.out.println("╚══════════════════════════════════════════════╝\n");
    }

    // ─── Getter ───────────────────────────────────────────────
    public String getAlertEndpoint() {
        return ADMIN_ALERT_ENDPOINT;
    }
}
