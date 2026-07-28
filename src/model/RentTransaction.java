package model;

import exceptions.BudgetExceededException;
import exceptions.InvalidPropertyException;

/**
 * Represents a rent transaction — client rents a property for N months.
 * Total = (monthly rent × months) + security deposit.
 */
public class RentTransaction extends Transaction {

    private int    durationMonths;
    private double monthlyRent;
    private double securityDeposit;

    // ─── Constructor ──────────────────────────────────────────
    public RentTransaction(int id, Property property,
                           Client client, Agent agent,
                           int durationMonths,
                           double securityDepositMonths) {
        super(id, property, client, agent, property.calculatePrice());
        validateDuration(durationMonths);

        this.durationMonths  = durationMonths;
        this.monthlyRent     = property.calculatePrice();
        this.securityDeposit = monthlyRent * securityDepositMonths;
    }

    // ─── Validation ───────────────────────────────────────────
    private void validateDuration(int months) {
        if (months < 1)
            throw new IllegalArgumentException(
                "Rental duration must be at least 1 month.");
    }

    // ─── Calculations ─────────────────────────────────────────
    public double getTotalRent() {
        return (monthlyRent * durationMonths) + securityDeposit;
    }

    // ─── Abstract Implementations ─────────────────────────────
    @Override
    public String getTransactionType() { return "RENT"; }

    @Override
    public void processTransaction()
            throws BudgetExceededException, InvalidPropertyException {

        double total = getTotalRent();
        if (getClient().getBudget() < total)
            throw new BudgetExceededException(getClient().getBudget(), total);
            
        String payee = getAgent() != null ? getAgent().getEmail() : 
                      (getProperty().getOwner() != null ? getProperty().getOwner().getEmail() : "SYSTEM");

        try {
            payment.PaymentEngine.getInstance().submitClientPayment(
                getClient(), total, enums.PaymentType.RENT, getTransactionId(), payee);
        } catch (exceptions.InsufficientBalanceException e) {
            throw new BudgetExceededException(getClient().getWalletBalance(), total);
        }

        getProperty().rent(getClient(), durationMonths);
        getClient().addTransaction(this);
        if (getAgent() != null) getAgent().addClosedDeal(this);
        markCompleted();
    }

    @Override
    public double calculateAgentCommission() {
        return getAgent() != null
               ? getAgent().calculateCommission(getTotalRent())
               : 0.0;
    }

    // ─── Getters ──────────────────────────────────────────────
    public int    getDurationMonths()  { return durationMonths; }
    public double getMonthlyRent()     { return monthlyRent; }
    public double getSecurityDeposit() { return securityDeposit; }
}
