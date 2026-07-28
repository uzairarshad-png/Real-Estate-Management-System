package model;

import enums.PropertyMode;
import enums.PropertyStatus;
import interfaces.Transactable;
import interfaces.Searchable;
import exceptions.BudgetExceededException;
import exceptions.InvalidPropertyException;

/**
 * Represents a commercial office property.
 * Supports: Purchase, Rent, Sell, Rent-Out
 */
public class Office extends CommercialProperty
        implements Transactable, Searchable {

    // ─── Fields ───────────────────────────────────────────────
    private int     workstations;
    private boolean hasConferenceRoom;

    // ─── Constructor ──────────────────────────────────────────
    public Office(int propertyId, String title, String address,
                  String city, double area, double basePrice,
                  PropertyMode mode, String businessZone,
                  int parkingSpots, int workstations,
                  boolean hasConferenceRoom) {

        super(propertyId, title, address, city, area, basePrice,
              mode, businessZone, parkingSpots);
        validateWorkstations(workstations);

        this.workstations       = workstations;
        this.hasConferenceRoom  = hasConferenceRoom;
    }

    // ─── Validation ───────────────────────────────────────────
    private void validateWorkstations(int ws) {
        if (ws < 1)
            throw new IllegalArgumentException("Office must have at least 1 workstation.");
    }

    // ─── Property Abstract Implementations ────────────────────
    @Override
    public double calculatePrice() {
        double price = getBasePrice();
        if (hasConferenceRoom) price += price * 0.06;
        if (getParkingSpots() > 0) price += getParkingSpots() * 50000;
        price += workstations * 10000;
        return price;
    }

    @Override
    public String getPropertyType() {
        return "Office";
    }

    // ─── Transactable Implementation ──────────────────────────
    @Override
    public void purchase(Client client)
            throws BudgetExceededException, InvalidPropertyException {

        if (client == null)
            throw new IllegalArgumentException("Client cannot be null.");
        if (getStatus() != PropertyStatus.AVAILABLE)
            throw new InvalidPropertyException("status",
                    "Office is not available. Current status: " + getStatus());
        if (getMode() != PropertyMode.FOR_SALE)
            throw new InvalidPropertyException("mode", "Office is not listed for sale.");

        double price = calculatePrice();
        if (client.getBudget() < price)
            throw new BudgetExceededException(client.getBudget(), price);

        setStatus(PropertyStatus.SOLD);
        setOwner(client);
        client.addOwnedProperty(this);
    }

    @Override
    public void rent(Client client, int months)
            throws BudgetExceededException, InvalidPropertyException {

        if (client == null)
            throw new IllegalArgumentException("Client cannot be null.");
        if (months <= 0)
            throw new IllegalArgumentException("Rental duration must be at least 1 month.");
        if (getStatus() != PropertyStatus.AVAILABLE)
            throw new InvalidPropertyException("status",
                    "Office is not available. Current status: " + getStatus());
        if (getMode() != PropertyMode.FOR_RENT)
            throw new InvalidPropertyException("mode", "Office is not listed for rent.");

        double totalRent = calculatePrice() * months;
        if (client.getBudget() < totalRent)
            throw new BudgetExceededException(client.getBudget(), totalRent);

        setStatus(PropertyStatus.RENTED);
        client.addRentedProperty(this);
    }

    @Override
    public void sellProperty(Client client) throws InvalidPropertyException {
        if (client == null)
            throw new IllegalArgumentException("Client cannot be null.");
        if (!client.getOwnedProperties().contains(this))
            throw new InvalidPropertyException("owner", "Client does not own this office.");

        setStatus(PropertyStatus.AVAILABLE);
        setMode(PropertyMode.FOR_SALE);
        client.removeOwnedProperty(this);
        setOwner(null);
    }

    @Override
    public void rentOut(Client client) throws InvalidPropertyException {
        if (client == null)
            throw new IllegalArgumentException("Client cannot be null.");
        if (!client.getOwnedProperties().contains(this))
            throw new InvalidPropertyException("owner", "Client does not own this office.");

        setStatus(PropertyStatus.RENTED_OUT);
        setMode(PropertyMode.FOR_RENT_OUT);
    }

    // ─── Searchable Implementation ────────────────────────────
    @Override
    public boolean matchesFilter(String type, double maxPrice) {
        return baseMatchesFilter(type, maxPrice)
               && getStatus() == PropertyStatus.AVAILABLE;
    }

    @Override
    public String getSummary() {
        return String.format(
            "Office | %s, %s | %d Workstations | %s | Parking: %d | PKR %.2f | %s",
            getAddress(), getCity(), workstations,
            hasConferenceRoom ? "Conf. Room" : "No Conf. Room",
            getParkingSpots(), calculatePrice(), getStatus()
        );
    }

    // ─── Getters & Setters ────────────────────────────────────
    public int     getWorkstations()    { return workstations; }
    public boolean hasConferenceRoom()  { return hasConferenceRoom; }

    public void setWorkstations(int ws) {
        validateWorkstations(ws);
        this.workstations = ws;
    }

    public void setHasConferenceRoom(boolean conf) { this.hasConferenceRoom = conf; }
}
