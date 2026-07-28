package interfaces;

import payment.Payment;
import exceptions.InsufficientBalanceException;
import exceptions.PaymentRejectedException;
import java.util.List;

/**
 * Defines payment behaviour for users who send or receive money.
 * Implemented by: Agent, Client
 */
public interface Payable {

    /**
     * Process an incoming or outgoing payment for this user.
     * @param payment the payment object to process
     * @throws InsufficientBalanceException if wallet balance is too low
     * @throws PaymentRejectedException     if Admin rejects the payment
     */
    void processPayment(Payment payment)
            throws InsufficientBalanceException, PaymentRejectedException;

    /**
     * Get the current wallet balance of this user.
     * @return wallet balance in PKR
     */
    double getBalance();

    /**
     * Get the full payment history of this user.
     * @return list of all payments (approved, rejected, disbursed)
     */
    List<Payment> getPaymentHistory();
}
