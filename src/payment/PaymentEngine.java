package payment;

import enums.PaymentStatus;
import enums.PaymentType;
import model.Agent;
import model.Client;
import model.Transaction;
import notification.Notification;
import notification.NotificationCenter;
import enums.NotificationType;
import security.AuditLog;
import exceptions.InsufficientBalanceException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Singleton simulated payment engine.
 *
 * Manages all financial flows:
 *   - Client submits payment → PENDING_ADMIN
 *   - Admin approves → PaymentEngine disburses
 *   - Agent receives salary and commission from Admin
 *
 * Admin controls ALL payment approvals manually.
 * No real banking or gateway integration.
 */
public class PaymentEngine {

    // ─── Singleton ────────────────────────────────────────────
    private static PaymentEngine instance;

    // ─── State ────────────────────────────────────────────────
    private final List<Payment> allPayments;
    private double              systemBalance;

    // ─── Private Constructor ──────────────────────────────────
    private PaymentEngine() {
        this.allPayments   = new ArrayList<>();
        this.systemBalance = 50_000_000.0; // PKR 5 crore starting system balance
    }

    public static PaymentEngine getInstance() {
        if (instance == null) instance = new PaymentEngine();
        return instance;
    }

    // ─── Create Payment ───────────────────────────────────────

    /**
     * Create a new payment record and push a notification to Admin.
     * Initial status is always PENDING_ADMIN.
     *
     * @param payer           email of payer
     * @param payee           email of payee
     * @param amount          amount in PKR
     * @param type            PaymentType enum
     * @param relatedTxId     related transaction ID (-1 if none)
     * @return the created Payment object
     */
    public Payment createPayment(String payer, String payee,
                                 double amount, PaymentType type,
                                 int relatedTxId) {

        if (payer == null || payer.trim().isEmpty())
            throw new IllegalArgumentException("Payer cannot be empty.");
        if (payee == null || payee.trim().isEmpty())
            throw new IllegalArgumentException("Payee cannot be empty.");
        if (amount <= 0)
            throw new IllegalArgumentException("Amount must be greater than zero.");
        if (type == null)
            throw new IllegalArgumentException("Payment type cannot be null.");

        Payment payment = new Payment(type, amount, payer, payee, relatedTxId);
        allPayments.add(payment);

        // Notify Admin
        NotificationCenter.getInstance().push(new Notification(
            NotificationType.PAYMENT_REQUEST,
            String.format("New payment request [#%d] | Type: %s | Amount: PKR %.2f | From: %s | To: %s",
                payment.getPaymentId(), type, amount, payer, payee),
            payer
        ));

        AuditLog.getInstance().logPayment(payment, "CREATED");
        return payment;
    }

    // ─── Client Payment ───────────────────────────────────────

    /**
     * Process a payment from a Client for a property transaction.
     * Deducts from client wallet and creates PENDING_ADMIN payment.
     *
     * @param client     the paying client
     * @param amount     amount in PKR
     * @param type       payment type (PURCHASE / RENT / RENT_OUT)
     * @param relatedTxId related transaction ID
     * @return the created Payment object
     * @throws exceptions.InsufficientBalanceException if wallet insufficient
     */
    public Payment submitClientPayment(Client client, double amount,
                                       PaymentType type, int relatedTxId, String payeeEmail)
            throws exceptions.InsufficientBalanceException {

        if (client == null)
            throw new IllegalArgumentException("Client cannot be null.");
        if (client.getWalletBalance() < amount)
            throw new exceptions.InsufficientBalanceException(
                client.getWalletBalance(), amount);

        client.makePayment(amount);
        return createPayment(client.getEmail(), payeeEmail, amount, type, relatedTxId);
    }

    // ─── Admin: Approve Payment ───────────────────────────────

    /**
     * Admin approves a pending payment by its ID.
     *
     * @param paymentId ID of the payment to approve
     * @return true if found and approved, false if not found
     * @throws IllegalStateException if payment is not PENDING_ADMIN
     */
    public boolean approvePayment(int paymentId) {
        Payment payment = findById(paymentId);
        if (payment == null) return false;
        payment.approve();
        return true;
    }

    // ─── Admin: Reject Payment ────────────────────────────────

    /**
     * Admin rejects a pending payment by its ID.
     *
     * @param paymentId ID of the payment to reject
     * @param note      optional rejection reason
     * @return true if found and rejected, false if not found
     */
    public boolean rejectPayment(int paymentId, String note) {
        Payment payment = findById(paymentId);
        if (payment == null) return false;
        payment.reject(note);
        return true;
    }

    /** Reject without a note. */
    public boolean rejectPayment(int paymentId) {
        return rejectPayment(paymentId, "No reason provided.");
    }

    // ─── Agent Salary Disbursement ────────────────────────────

    /**
     * Admin disburses monthly salary to an Agent.
     * Creates a payment, approves it, disburses it immediately.
     *
     * @param agent  the agent receiving salary
     * @param amount salary amount in PKR
     * @throws IllegalArgumentException if agent is null or amount invalid
     */
    public void disburseSalary(Agent agent, double amount) {
        if (agent == null)
            throw new IllegalArgumentException("Agent cannot be null.");
        if (amount <= 0)
            throw new IllegalArgumentException("Salary amount must be greater than zero.");
        if (systemBalance < amount)
            throw new IllegalStateException(
                "Insufficient system balance to disburse salary. "
                + "System: PKR " + systemBalance + " | Required: PKR " + amount);

        Payment p = new Payment(PaymentType.AGENT_SALARY, amount,
                                "SYSTEM", agent.getEmail(), -1);
        allPayments.add(p);
        p.approve();
        p.disburse();
        agent.receiveSalary(amount);
        systemBalance -= amount;

        AuditLog.getInstance().logPayment(p, "SALARY DISBURSED");
    }

    // ─── Agent Commission Disbursement ────────────────────────

    /**
     * Disburse commission to an Agent after a deal closes.
     * Commission calculated from the transaction.
     *
     * @param agent       the agent to pay
     * @param transaction the completed transaction
     */
    public void disburseCommission(Agent agent, Transaction transaction) {
        if (agent == null)
            throw new IllegalArgumentException("Agent cannot be null.");
        if (transaction == null)
            throw new IllegalArgumentException("Transaction cannot be null.");

        double commission = transaction.calculateAgentCommission();
        if (commission <= 0) return;

        if (systemBalance < commission)
            throw new IllegalStateException(
                "Insufficient system balance to disburse commission.");

        Payment p = new Payment(PaymentType.AGENT_COMMISSION, commission,
                                "SYSTEM", agent.getEmail(),
                                transaction.getTransactionId());
        allPayments.add(p);
        p.approve();
        p.disburse();
        agent.receiveCommission(commission);
        systemBalance -= commission;

        AuditLog.getInstance().logPayment(p, "COMMISSION DISBURSED");
    }

    // ─── Query Methods ────────────────────────────────────────

    /**
     * Get all payments with PENDING_ADMIN status.
     */
    public List<Payment> getPendingPayments() {
        List<Payment> pending = new ArrayList<>();
        for (Payment p : allPayments)
            if (p.getStatus() == PaymentStatus.PENDING_ADMIN)
                pending.add(p);
        return Collections.unmodifiableList(pending);
    }

    /**
     * Get all payments with APPROVED status (ready for disbursement).
     */
    public List<Payment> getApprovedPayments() {
        List<Payment> approved = new ArrayList<>();
        for (Payment p : allPayments)
            if (p.getStatus() == PaymentStatus.APPROVED)
                approved.add(p);
        return Collections.unmodifiableList(approved);
    }

    /**
     * Get payments for a specific payer email.
     */
    public List<Payment> getPaymentsByPayer(String email) {
        if (email == null) return new ArrayList<>();
        List<Payment> result = new ArrayList<>();
        for (Payment p : allPayments)
            if (p.getPayer().equalsIgnoreCase(email))
                result.add(p);
        return Collections.unmodifiableList(result);
    }

    /**
     * Get payments for a specific payee email.
     */
    public List<Payment> getPaymentsByPayee(String email) {
        if (email == null) return new ArrayList<>();
        List<Payment> result = new ArrayList<>();
        for (Payment p : allPayments)
            if (p.getPayee().equalsIgnoreCase(email))
                result.add(p);
        return Collections.unmodifiableList(result);
    }

    /**
     * Find a payment by its ID.
     * @return the Payment or null if not found
     */
    public Payment findById(int paymentId) {
        for (Payment p : allPayments)
            if (p.getPaymentId() == paymentId)
                return p;
        return null;
    }

    /**
     * Get full summary of system payment statistics.
     */
    public String getPaymentSummary() {
        int pending = 0, approved = 0, rejected = 0, disbursed = 0;
        double totalDisbursed = 0;
        for (Payment p : allPayments) {
            switch (p.getStatus()) {
                case PENDING_ADMIN: pending++;   break;
                case APPROVED:      approved++;  break;
                case REJECTED:      rejected++;  break;
                case DISBURSED:
                    disbursed++;
                    totalDisbursed += p.getAmount();
                    break;
            }
        }
        return String.format(
            "Payment Summary | Total: %d | Pending: %d | Approved: %d | "
          + "Rejected: %d | Disbursed: %d | Total Disbursed: PKR %.2f | System Balance: PKR %.2f",
            allPayments.size(), pending, approved, rejected,
            disbursed, totalDisbursed, systemBalance);
    }

    // ─── Getters ──────────────────────────────────────────────
    public List<Payment> getAllPayments()   { return Collections.unmodifiableList(allPayments); }
    public double        getSystemBalance() { return systemBalance; }

    public void setSystemBalance(double balance) {
        if (balance < 0)
            throw new IllegalArgumentException("System balance cannot be negative.");
        this.systemBalance = balance;
    }

    /** Load saved payments from DB on startup. */
    public void loadPayments(List<Payment> saved) {
        if (saved != null) allPayments.addAll(saved);
    }
}
