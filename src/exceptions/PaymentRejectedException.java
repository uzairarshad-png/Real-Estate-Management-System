package exceptions;

/**
 * Thrown when Admin rejects a submitted payment,
 * preventing the transaction from completing.
 */
public class PaymentRejectedException extends Exception {

    private final int paymentId;

    public PaymentRejectedException(int paymentId) {
        super("Payment [ID: " + paymentId + "] was rejected by Admin. "
            + "Please contact Admin for details.");
        this.paymentId = paymentId;
    }

    public int getPaymentId() { return paymentId; }
}
