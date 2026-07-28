package model;

import enums.PropertyMode;
import enums.PropertyStatus;
import interfaces.Transactable;
import interfaces.Searchable;
import exceptions.BudgetExceededException;
import exceptions.InvalidPropertyException;

/**
 * Represents an apartment/flat property.
 * Supports: Purchase, Rent, Sell, Rent-Out
 */
public class Apartment extends ResidentialProperty
        implements Transactable, Searchable {

    // ─── Fields ───────────────────────────────────────────────
    private int     floorNumber;
    private boolean hasElevator;
    private boolean hasParking;

    // ─── Constructor ──────────────────────────────────────────
    public Apartment(int propertyId, String title, String address,
                     String city, double area, double basePrice,
                     PropertyMode mode, int bedrooms, int bathrooms,
                     boolean isFurnished, int floorNumber,
                     boolean hasElevator, boolean hasParking) {

        super(propertyId, title, address, city, area, basePrice,
              mode, bedrooms, bathrooms, isFurnished);
        validateFloor(floorNumber);

        this.floorNumber = floorNumber;
        this.hasElevator = hasElevator;
        this.hasParking  = hasParking;
    }

    // ─── Validation ───────────────────────────────────────────
    private void validateFloor(int floor) {
        if (floor < 0)
            throw new IllegalArgumentException("Floor number cannot be negative.");
    }

    // ─── Property Abstract Implementations ────────────────────
    @Override
    public double calculatePrice() {
        double price = getBasePrice();
        // Floor premium: +1% per floor above ground
        price += price * (floorNumber * 0.01);
        // Elevator premium
        if (hasElevator)  price += price * 0.03;
        // Parking premium
        if (hasParking)   price += price * 0.02;
        // Furnished premium
        if (isFurnished()) price += price * 0.05;
        return price;
    }

    @Override
    public String getPropertyType() {
        return "Apartment";
    }

    // ─── Transactable Implementation ──────────────────────────
    @Override
    public void purchase(Client client)
            throws BudgetExceededException, InvalidPropertyException {

        if (client == null)
            throw new IllegalArgumentException("Client cannot be null.");
        if (getStatus() != PropertyStatus.AVAILABLE)
            throw new InvalidPropertyException("status",
                    "Apartment is not available for purchase. Current status: " + getStatus());
        if (getMode() != PropertyMode.FOR_SALE)
            throw new InvalidPropertyException("mode",
                    "Apartment is not listed for sale.");
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
                    "Apartment is not available for rent. Current status: " + getStatus());
        if (getMode() != PropertyMode.FOR_RENT)
            throw new InvalidPropertyException("mode",
                    "Apartment is not listed for rent.");

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
            throw new InvalidPropertyException("owner",
                    "Client does not own this apartment.");

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
            throw new InvalidPropertyException("owner",
                    "Client does not own this apartment.");

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
            "Apartment | %s, %s | Floor %d | %d Bed / %d Bath | %s | PKR %.2f | %s",
            getAddress(), getCity(), floorNumber,
            getBedrooms(), getBathrooms(),
            isFurnished() ? "Furnished" : "Unfurnished",
            calculatePrice(), getStatus()
        );
    }

    // ─── Getters & Setters ────────────────────────────────────
    public int     getFloorNumber() { return floorNumber; }
    public boolean hasElevator()    { return hasElevator; }
    public boolean hasParking()     { return hasParking; }

    public void setFloorNumber(int floor) {
        validateFloor(floor);
        this.floorNumber = floor;
    }

    public void setHasElevator(boolean hasElevator) { this.hasElevator = hasElevator; }
    public void setHasParking(boolean hasParking)   { this.hasParking  = hasParking; }
}
