package exceptions;

/**
 * Thrown when a payment is attempted but the payer's wallet
 * balance is insufficient to cover the amount.
 */
public class InsufficientBalanceException extends Exception {

    private final double availableBalance;
    private final double requiredAmount;

    public InsufficientBalanceException(double availableBalance, double requiredAmount) {
        super(String.format(
            "Insufficient wallet balance! Available: PKR %.2f | Required: PKR %.2f | Shortfall: PKR %.2f",
            availableBalance, requiredAmount, (requiredAmount - availableBalance)
        ));
        this.availableBalance = availableBalance;
        this.requiredAmount   = requiredAmount;
    }

    public double getAvailableBalance() { return availableBalance; }
    public double getRequiredAmount()   { return requiredAmount; }
    public double getShortfall()        { return requiredAmount - availableBalance; }
}
