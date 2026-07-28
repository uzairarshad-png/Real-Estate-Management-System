package model;

import enums.PropertyStatus;
import exceptions.BudgetExceededException;
import exceptions.InvalidPropertyException;

/**
 * Represents a sale transaction — client sells a property they own to another client.
 * The buyer's budget is checked, seller's ownership is transferred.
 */
public class SaleTransaction extends Transaction {

    private double salePrice;
    private Client seller;

    // ─── Constructor ──────────────────────────────────────────
    public SaleTransaction(int id, Property property,
                           Client buyer, Client seller,
                           Agent agent, double salePrice) {
        super(id, property, buyer, agent, salePrice);
        if (seller == null)
            throw new IllegalArgumentException("Seller cannot be null.");
        if (salePrice <= 0)
            throw new IllegalArgumentException("Sale price must be greater than zero.");
        this.seller    = seller;
        this.salePrice = salePrice;
    }

    // ─── Abstract Implementations ─────────────────────────────
    @Override
    public String getTransactionType() { return "SALE"; }

    @Override
    public void processTransaction()
            throws BudgetExceededException, InvalidPropertyException {

        if (getClient().getBudget() < salePrice)
            throw new BudgetExceededException(getClient().getBudget(), salePrice);

        String payee = getAgent() != null ? getAgent().getEmail() : seller.getEmail();

        try {
            payment.PaymentEngine.getInstance().submitClientPayment(
                getClient(), salePrice, enums.PaymentType.SALE, getTransactionId(), payee);
        } catch (exceptions.InsufficientBalanceException e) {
            throw new BudgetExceededException(getClient().getWalletBalance(), salePrice);
        }

        // Remove from seller, transfer to buyer
        seller.removeOwnedProperty(getProperty());
        getProperty().setStatus(PropertyStatus.SOLD);
        getProperty().setOwner(getClient());
        getClient().addOwnedProperty(getProperty());

        // Record for both parties
        getClient().addTransaction(this);
        seller.addTransaction(this);
        if (getAgent() != null) getAgent().addClosedDeal(this);
        markCompleted();
    }

    @Override
    public double calculateAgentCommission() {
        return getAgent() != null
               ? getAgent().calculateCommission(salePrice)
               : 0.0;
    }

    // ─── Getters ──────────────────────────────────────────────
    public double getSalePrice() { return salePrice; }
    public Client getSeller()    { return seller; }
}
