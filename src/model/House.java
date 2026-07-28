package model;

import enums.PropertyMode;
import enums.PropertyStatus;
import interfaces.Transactable;
import interfaces.Searchable;
import exceptions.BudgetExceededException;
import exceptions.InvalidPropertyException;

/**
 * Represents a house property.
 * Supports: Purchase, Rent, Sell, Rent-Out
 */
public class House extends ResidentialProperty
        implements Transactable, Searchable {

    // ─── Fields ───────────────────────────────────────────────
    private boolean hasGarage;
    private double  gardenArea;   // in square feet, 0 if none
    private int     floors;

    // ─── Constructor ──────────────────────────────────────────
    public House(int propertyId, String title, String address,
                 String city, double area, double basePrice,
                 PropertyMode mode, int bedrooms, int bathrooms,
                 boolean isFurnished, boolean hasGarage,
                 double gardenArea, int floors) {

        super(propertyId, title, address, city, area, basePrice,
              mode, bedrooms, bathrooms, isFurnished);
        validateFloors(floors);
        validateGardenArea(gardenArea);

        this.hasGarage  = hasGarage;
        this.gardenArea = gardenArea;
        this.floors     = floors;
    }

    // ─── Validation ───────────────────────────────────────────
    private void validateFloors(int floors) {
        if (floors < 1)
            throw new IllegalArgumentException("House must have at least 1 floor.");
    }

    private void validateGardenArea(double area) {
        if (area < 0)
            throw new IllegalArgumentException("Garden area cannot be negative.");
    }

    // ─── Property Abstract Implementations ────────────────────
    @Override
    public double calculatePrice() {
        double price = getBasePrice();
        // Garage premium
        if (hasGarage)         price += price * 0.04;
        // Garden premium: +2% per 500 sqft of garden
        if (gardenArea > 0)    price += price * ((gardenArea / 500.0) * 0.02);
        // Multi-floor premium
        if (floors > 1)        price += price * ((floors - 1) * 0.05);
        // Furnished premium
        if (isFurnished())     price += price * 0.05;
        return price;
    }

    @Override
    public String getPropertyType() {
        return "House";
    }

    // ─── Transactable Implementation ──────────────────────────
    @Override
    public void purchase(Client client)
            throws BudgetExceededException, InvalidPropertyException {

        if (client == null)
            throw new IllegalArgumentException("Client cannot be null.");
        if (getStatus() != PropertyStatus.AVAILABLE)
            throw new InvalidPropertyException("status",
                    "House is not available for purchase. Current status: " + getStatus());
        if (getMode() != PropertyMode.FOR_SALE)
            throw new InvalidPropertyException("mode",
                    "House is not listed for sale.");

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
                    "House is not available for rent. Current status: " + getStatus());
        if (getMode() != PropertyMode.FOR_RENT)
            throw new InvalidPropertyException("mode", "House is not listed for rent.");

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
                    "Client does not own this house.");

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
                    "Client does not own this house.");

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
            "House | %s, %s | %d Floor(s) | %d Bed / %d Bath | %s | Garden: %.0f sqft | PKR %.2f | %s",
            getAddress(), getCity(), floors, getBedrooms(), getBathrooms(),
            isFurnished() ? "Furnished" : "Unfurnished",
            gardenArea, calculatePrice(), getStatus()
        );
    }

    // ─── Getters & Setters ────────────────────────────────────
    public boolean hasGarage()    { return hasGarage; }
    public double  getGardenArea() { return gardenArea; }
    public int     getFloors()    { return floors; }

    public void setHasGarage(boolean hasGarage)  { this.hasGarage = hasGarage; }

    public void setGardenArea(double area) {
        validateGardenArea(area);
        this.gardenArea = area;
    }

    public void setFloors(int floors) {
        validateFloors(floors);
        this.floors = floors;
    }
}
