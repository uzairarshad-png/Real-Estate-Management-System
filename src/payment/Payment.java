package payment;

import enums.PaymentStatus;
import enums.PaymentType;
import security.AuditLog;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents a single payment record in the simulated payment system.
 *
 * Payment Flow:
 *   Client submits → PENDING_ADMIN
 *   → Admin approves → APPROVED
 *   → PaymentEngine disburses → DISBURSED
 *   OR
 *   → Admin rejects → REJECTED
 *
 * All state transitions are logged to AuditLog.
 */
public class Payment {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    private static int idCounter = 1;

    // ─── Fields ───────────────────────────────────────────────
    private final int           paymentId;
    private final PaymentType   type;
    private final double        amount;
    private final String        payer;
    private final String        payee;
    private final String        timestamp;
    private final int           relatedTransactionId;
    private PaymentStatus       status;
    private String              adminNote;

    // ─── Constructor ──────────────────────────────────────────
    public Payment(PaymentType type, double amount,
                   String payer, String payee,
                   int relatedTransactionId) {

        if (type == null)
            throw new IllegalArgumentException("Payment type cannot be null.");
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be greater than zero.");
        if (payer == null || payer.trim().isEmpty())
            throw new IllegalArgumentException("Payer cannot be empty.");
        if (payee == null || payee.trim().isEmpty())
            throw new IllegalArgumentException("Payee cannot be empty.");

        this.paymentId            = idCounter++;
        this.type                 = type;
        this.amount               = amount;
        this.payer                = payer.trim();
        this.payee                = payee.trim();
        this.relatedTransactionId = relatedTransactionId;
        this.timestamp            = LocalDateTime.now().format(FORMATTER);
        this.status               = PaymentStatus.PENDING_ADMIN;
        this.adminNote            = "";
    }

    // ─── State Transitions ────────────────────────────────────

    /**
     * Admin approves this payment.
     * @throws IllegalStateException if not in PENDING_ADMIN state
     */
    public void approve() {
        if (status != PaymentStatus.PENDING_ADMIN)
            throw new IllegalStateException(
                "Cannot approve Payment [#" + paymentId + "]. Status: " + status);
        this.status = PaymentStatus.APPROVED;
        AuditLog.getInstance().logPayment(this, "APPROVED");
    }

    /**
     * Admin rejects this payment with an optional reason.
     * @throws IllegalStateException if not in PENDING_ADMIN state
     */
    public void reject(String note) {
        if (status != PaymentStatus.PENDING_ADMIN)
            throw new IllegalStateException(
                "Cannot reject Payment [#" + paymentId + "]. Status: " + status);
        this.status    = PaymentStatus.REJECTED;
        this.adminNote = (note != null && !note.trim().isEmpty())
                         ? note.trim() : "No reason provided.";
        AuditLog.getInstance().logPayment(this, "REJECTED");
    }

    /** Reject without a note. */
    public void reject() { reject("No reason provided."); }

    /**
     * Mark as disbursed once funds transferred.
     * @throws IllegalStateException if not APPROVED
     */
    public void disburse() {
        if (status != PaymentStatus.APPROVED)
            throw new IllegalStateException(
                "Cannot disburse Payment [#" + paymentId
                + "]. Must be APPROVED first. Status: " + status);
        this.status = PaymentStatus.DISBURSED;
        AuditLog.getInstance().logPayment(this, "DISBURSED");
    }

    // ─── Receipt ──────────────────────────────────────────────
    public String getReceipt() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔══════════════════════════════════════════════╗\n");
        sb.append(  "║           REMS PAYMENT RECEIPT               ║\n");
        sb.append(  "╠══════════════════════════════════════════════╣\n");
        sb.append(String.format("  Payment ID   : %d%n",         paymentId));
        sb.append(String.format("  Type         : %s%n",         type));
        sb.append(String.format("  Amount       : PKR %.2f%n",   amount));
        sb.append(String.format("  Payer        : %s%n",         payer));
        sb.append(String.format("  Payee        : %s%n",         payee));
        sb.append(String.format("  Status       : %s%n",         status));
        sb.append(String.format("  Timestamp    : %s%n",         timestamp));
        if (relatedTransactionId >= 0)
            sb.append(String.format("  Transaction  : #%d%n",   relatedTransactionId));
        if (!adminNote.isEmpty())
            sb.append(String.format("  Admin Note   : %s%n",    adminNote));
        sb.append(  "╚══════════════════════════════════════════════╝");
        return sb.toString();
    }

    // ─── Getters ──────────────────────────────────────────────
    public int           getPaymentId()            { return paymentId; }
    public PaymentType   getType()                 { return type; }
    public double        getAmount()               { return amount; }
    public String        getPayer()                { return payer; }
    public String        getPayee()                { return payee; }
    public String        getTimestamp()            { return timestamp; }
    public int           getRelatedTransactionId() { return relatedTransactionId; }
    public PaymentStatus getStatus()               { return status; }
    public String        getAdminNote()            { return adminNote; }

    @Override
    public String toString() {
        return String.format(
            "[Payment #%d] %s | PKR %.2f | %s → %s | %s | %s",
            paymentId, type, amount, payer, payee, status, timestamp);
    }
}
