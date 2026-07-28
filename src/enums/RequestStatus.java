package enums;

/**
 * Status of a registration or password reset request.
 */
public enum RequestStatus {
    PENDING,    // Submitted and awaiting Admin action
    APPROVED,   // Admin approved the request
    REJECTED    // Admin rejected the request
}
