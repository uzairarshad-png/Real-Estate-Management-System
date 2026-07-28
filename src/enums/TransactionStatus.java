package enums;

/**
 * Tracks the current state of a property transaction.
 */
public enum TransactionStatus {
    PENDING,    // Transaction initiated — awaiting payment approval
    COMPLETED,  // Transaction fully processed and payment disbursed
    CANCELLED   // Transaction was cancelled before completion
}
