package model;

import exceptions.BudgetExceededException;
import exceptions.InvalidPropertyException;

/**
 * Represents a rent-out transaction — a client who owns a property
 * lists it for rent to a tenant through the system.
 * Commission is calculated on annual rent value.
 */
public class RentOutTransaction extends Transaction {

    private Client landlord;
    private double monthlyRent;

    // ─── Constructor ──────────────────────────────────────────
    public RentOutTransaction(int id, Property property,
                              Client tenant, Client landlord,
                              Agent agent, double monthlyRent) {
        super(id, property, tenant, agent, monthlyRent);
        if (landlord == null)
            throw new IllegalArgumentException("Landlord cannot be null.");
        if (monthlyRent <= 0)
            throw new IllegalArgumentException(
                "Monthly rent must be greater than zero.");
        this.landlord    = landlord;
        this.monthlyRent = monthlyRent;
    }

    // ─── Abstract Implementations ─────────────────────────────
    @Override
    public String getTransactionType() { return "RENT_OUT"; }

    @Override
    public void processTransaction()
            throws BudgetExceededException, InvalidPropertyException {

        getProperty().rentOut(landlord);
        getClient().addRentedProperty(getProperty());
        landlord.addTransaction(this);
        getClient().addTransaction(this);
        if (getAgent() != null) getAgent().addClosedDeal(this);
        markCompleted();
    }

    @Override
    public double calculateAgentCommission() {
        // Commission based on annual rent value
        return getAgent() != null
               ? getAgent().calculateCommission(monthlyRent * 12)
               : 0.0;
    }

    // ─── Getters ──────────────────────────────────────────────
    public Client getLandlord()    { return landlord; }
    public double getMonthlyRent() { return monthlyRent; }
}
