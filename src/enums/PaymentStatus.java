package enums;

/**
 * Tracks the lifecycle state of a payment.
 */
public enum PaymentStatus {
    PENDING_ADMIN,  // Submitted — waiting for Admin approval
    APPROVED,       // Admin approved — ready for disbursement
    REJECTED,       // Admin rejected this payment
    DISBURSED       // Funds have been transferred to the payee
}
