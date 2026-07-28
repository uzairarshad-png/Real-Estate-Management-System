package exceptions;

/**
 * Thrown when a client attempts a transaction that exceeds their available budget.
 */
public class BudgetExceededException extends Exception {

    private final double clientBudget;
    private final double requiredAmount;

    public BudgetExceededException(double clientBudget, double requiredAmount) {
        super(String.format(
            "Budget exceeded! Client budget: PKR %.2f | Required: PKR %.2f | Shortfall: PKR %.2f",
            clientBudget, requiredAmount, (requiredAmount - clientBudget)
        ));
        this.clientBudget   = clientBudget;
        this.requiredAmount = requiredAmount;
    }

    public double getClientBudget()   { return clientBudget; }
    public double getRequiredAmount() { return requiredAmount; }
    public double getShortfall()      { return requiredAmount - clientBudget; }
}
