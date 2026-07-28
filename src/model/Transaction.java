package model;

import enums.TransactionStatus;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Abstract base class for all property transactions in REMS.
 * Subclasses: PurchaseTransaction, SaleTransaction,
 *             RentTransaction, RentOutTransaction
 */
public abstract class Transaction {

    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

    // ─── Fields ───────────────────────────────────────────────
    private int               transactionId;
    private Property          property;
    private Client            client;
    private Agent             agent;
    private double            amount;
    private String            date;
    private TransactionStatus status;

    // ─── Constructor ──────────────────────────────────────────
    public Transaction(int transactionId, Property property,
                       Client client, Agent agent, double amount) {

        validateId(transactionId);
        validateProperty(property);
        validateClient(client);
        validateAmount(amount);

        this.transactionId = transactionId;
        this.property      = property;
        this.client        = client;
        this.agent         = agent;
        this.amount        = amount;
        this.date          = LocalDateTime.now().format(FORMATTER);
        this.status        = TransactionStatus.PENDING;
    }

    // ─── Abstract Methods ─────────────────────────────────────

    /** Returns the type label of this transaction. */
    public abstract String getTransactionType();

    /** Executes the business logic of this transaction. */
    public abstract void processTransaction()
            throws exceptions.BudgetExceededException,
                   exceptions.InvalidPropertyException;

    /**
     * Calculate the agent's commission from this transaction.
     * @return commission amount in PKR
     */
    public abstract double calculateAgentCommission();

    // ─── Validation ───────────────────────────────────────────
    private void validateId(int id) {
        if (id < 0)
            throw new IllegalArgumentException("Transaction ID cannot be negative.");
    }

    private void validateProperty(Property p) {
        if (p == null)
            throw new IllegalArgumentException("Transaction must have a property.");
    }

    private void validateClient(Client c) {
        if (c == null)
            throw new IllegalArgumentException("Transaction must have a client.");
    }

    private void validateAmount(double amount) {
        if (amount <= 0)
            throw new IllegalArgumentException("Transaction amount must be greater than zero.");
    }

    // ─── Status Transitions ───────────────────────────────────
    public void markCompleted() {
        this.status = TransactionStatus.COMPLETED;
    }

    public void markCancelled() {
        this.status = TransactionStatus.CANCELLED;
    }

    // ─── Getters ──────────────────────────────────────────────
    public int               getTransactionId() { return transactionId; }
    public Property          getProperty()      { return property; }
    public Client            getClient()        { return client; }
    public Agent             getAgent()         { return agent; }
    public double            getAmount()        { return amount; }
    public String            getDate()          { return date; }
    public TransactionStatus getStatus()        { return status; }

    // ─── Setters ──────────────────────────────────────────────
    public void setAmount(double amount) {
        validateAmount(amount);
        this.amount = amount;
    }

    public void setStatus(TransactionStatus status) {
        if (status == null)
            throw new IllegalArgumentException("Status cannot be null.");
        this.status = status;
    }

    // ─── toString ─────────────────────────────────────────────
    @Override
    public String toString() {
        return String.format(
            "[%s] TxID: %d | Property: %s | Client: %s | Amount: PKR %.2f | Date: %s | Status: %s",
            getTransactionType(), transactionId,
            property.getTitle(), client.getName(),
            amount, date, status
        );
    }
}
