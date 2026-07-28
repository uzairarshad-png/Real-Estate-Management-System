package model;

import enums.TransactionStatus;
import exceptions.BudgetExceededException;
import exceptions.InvalidPropertyException;

/**
 * Represents a purchase transaction — client buys a property.
 * Tax is applied on top of the property price.
 */
public class PurchaseTransaction extends Transaction {

    private double  taxRate;      // e.g. 5.0 = 5%
    private boolean loanApplied;

    // ─── Constructor ──────────────────────────────────────────
    public PurchaseTransaction(int id, Property property,
                               Client client, Agent agent,
                               double taxRate, boolean loanApplied) {
        super(id, property, client, agent, property.calculatePrice());
        validateTaxRate(taxRate);
        this.taxRate     = taxRate;
        this.loanApplied = loanApplied;
    }

    // ─── Validation ───────────────────────────────────────────
    private void validateTaxRate(double rate) {
        if (rate < 0 || rate > 100)
            throw new IllegalArgumentException("Tax rate must be between 0 and 100.");
    }

    // ─── Calculations ─────────────────────────────────────────
    public double calculateTax() {
        return getAmount() * (taxRate / 100.0);
    }

    public double getTotalWithTax() {
        return getAmount() + calculateTax();
    }

    // ─── Abstract Implementations ─────────────────────────────
    @Override
    public String getTransactionType() { return "PURCHASE"; }

    @Override
    public void processTransaction()
            throws BudgetExceededException, InvalidPropertyException {

        double total = getTotalWithTax();
        if (getClient().getBudget() < total)
            throw new BudgetExceededException(getClient().getBudget(), total);

        String payee = getAgent() != null ? getAgent().getEmail() : 
                      (getProperty().getOwner() != null ? getProperty().getOwner().getEmail() : "SYSTEM");

        try {
            payment.PaymentEngine.getInstance().submitClientPayment(
                getClient(), total, enums.PaymentType.PURCHASE, getTransactionId(), payee);
        } catch (exceptions.InsufficientBalanceException e) {
            throw new BudgetExceededException(getClient().getWalletBalance(), total);
        }

        getProperty().purchase(getClient());
        getClient().addTransaction(this);
        if (getAgent() != null) getAgent().addClosedDeal(this);
        markCompleted();
    }

    @Override
    public double calculateAgentCommission() {
        return getAgent() != null
               ? getAgent().calculateCommission(getAmount())
               : 0.0;
    }

    // ─── Getters ──────────────────────────────────────────────
    public double  getTaxRate()    { return taxRate; }
    public boolean isLoanApplied() { return loanApplied; }
}
