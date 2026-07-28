package notification;

import enums.RegistrationSource;
import enums.RequestStatus;
import enums.UserRole;
import security.AuditLog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a registration request submitted from the Login Page.
 *
 * Two sources:
 *   LOGIN_PAGE  — user self-registers, Admin must approve before activation
 *   ADMIN_PANEL — Admin creates directly, immediately ACTIVE (handled in Admin class)
 *
 * Lifecycle:
 *   PENDING → APPROVED (Admin activates account) or REJECTED
 */
public class RegistrationRequest {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private static int idCounter = 1;

    // ─── Fields ───────────────────────────────────────────────
    private final int                requestId;
    private final String             name;
    private final String             email;
    private final String             phone;
    private final String             cnic;
    private final String             passwordHash;
    private final UserRole           requestedRole;
    private final RegistrationSource source;
    private final String             submittedAt;
    private RequestStatus            status;

    // ─── Constructor ──────────────────────────────────────────
    public RegistrationRequest(String name, String email,
                                String phone, String cnic,
                                String passwordHash,
                                UserRole requestedRole,
                                RegistrationSource source) {

        validateField(name,         "Name");
        validateField(email,        "Email");
        validateField(phone,        "Phone");
        validateField(cnic,         "CNIC");
        validateField(passwordHash, "Password hash");

        if (requestedRole == null)
            throw new IllegalArgumentException("Requested role cannot be null.");
        if (requestedRole == UserRole.ADMIN)
            throw new IllegalArgumentException(
                "Cannot register as Admin. Admin credentials are hardcoded.");
        if (source == null)
            throw new IllegalArgumentException("Registration source cannot be null.");

        this.requestId     = idCounter++;
        this.name          = name.trim();
        this.email         = email.trim().toLowerCase();
        this.phone         = phone.trim();
        this.cnic          = cnic.trim();
        this.passwordHash  = passwordHash;
        this.requestedRole = requestedRole;
        this.source        = source;
        this.submittedAt   = LocalDateTime.now().format(FORMATTER);
        this.status        = RequestStatus.PENDING;

        // Log the registration request
        AuditLog.getInstance().logRegistrationRequest(this.email, requestedRole.name());
    }

    // ─── Validation ───────────────────────────────────────────
    private void validateField(String value, String fieldName) {
        if (value == null || value.trim().isEmpty())
            throw new IllegalArgumentException(fieldName + " cannot be null or empty.");
    }

    // ─── State Transitions ────────────────────────────────────

    /**
     * Admin approves this registration request.
     * Account will be activated by Admin.approveRegistration().
     * @throws IllegalStateException if not in PENDING state
     */
    public void approve() {
        if (status != RequestStatus.PENDING)
            throw new IllegalStateException(
                "Cannot approve request [#" + requestId
                + "]. Current status: " + status);
        this.status = RequestStatus.APPROVED;
        AuditLog.getInstance().logRegistrationApproved(email);
    }

    /**
     * Admin rejects this registration request.
     * @throws IllegalStateException if not in PENDING state
     */
    public void reject() {
        if (status != RequestStatus.PENDING)
            throw new IllegalStateException(
                "Cannot reject request [#" + requestId
                + "]. Current status: " + status);
        this.status = RequestStatus.REJECTED;
        AuditLog.getInstance().logRegistrationRejected(email);
    }

    // ─── Getters ──────────────────────────────────────────────
    public int                getRequestId()     { return requestId; }
    public String             getName()          { return name; }
    public String             getEmail()         { return email; }
    public String             getPhone()         { return phone; }
    public String             getCnic()          { return cnic; }
    public String             getPasswordHash()  { return passwordHash; }
    public UserRole           getRequestedRole() { return requestedRole; }
    public RegistrationSource getSource()        { return source; }
    public String             getSubmittedAt()   { return submittedAt; }
    public RequestStatus      getStatus()        { return status; }

    @Override
    public String toString() {
        return String.format(
            "[REG REQUEST #%d] %s | %s | Role: %s | Source: %s | Status: %s | At: %s",
            requestId, name, email, requestedRole, source, status, submittedAt);
    }
}
