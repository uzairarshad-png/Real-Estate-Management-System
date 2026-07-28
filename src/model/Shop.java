package model;

import enums.PropertyMode;
import enums.PropertyStatus;
import interfaces.Transactable;
import interfaces.Searchable;
import exceptions.BudgetExceededException;
import exceptions.InvalidPropertyException;

/**
 * Represents a commercial shop property.
 * Supports: Purchase, Rent, Sell, Rent-Out
 */
public class Shop extends CommercialProperty
        implements Transactable, Searchable {

    // ─── Fields ───────────────────────────────────────────────
    private String  shopCategory;       // e.g. "Retail", "Food", "Electronics"
    private boolean hasStorageRoom;

    // ─── Constructor ──────────────────────────────────────────
    public Shop(int propertyId, String title, String address,
                String city, double area, double basePrice,
                PropertyMode mode, String businessZone,
                int parkingSpots, String shopCategory,
                boolean hasStorageRoom) {

        super(propertyId, title, address, city, area, basePrice,
              mode, businessZone, parkingSpots);
        validateCategory(shopCategory);

        this.shopCategory   = shopCategory.trim();
        this.hasStorageRoom = hasStorageRoom;
    }

    // ─── Validation ───────────────────────────────────────────
    private void validateCategory(String category) {
        if (category == null || category.trim().isEmpty())
            throw new IllegalArgumentException("Shop category cannot be empty.");
    }

    // ─── Property Abstract Implementations ────────────────────
    @Override
    public double calculatePrice() {
        double price = getBasePrice();
        if (hasStorageRoom)    price += price * 0.04;
        if (getParkingSpots() > 0) price += getParkingSpots() * 30000;
        return price;
    }

    @Override
    public String getPropertyType() {
        return "Shop";
    }

    // ─── Transactable Implementation ──────────────────────────
    @Override
    public void purchase(Client client)
            throws BudgetExceededException, InvalidPropertyException {

        if (client == null)
            throw new IllegalArgumentException("Client cannot be null.");
        if (getStatus() != PropertyStatus.AVAILABLE)
            throw new InvalidPropertyException("status",
                    "Shop is not available. Current status: " + getStatus());
        if (getMode() != PropertyMode.FOR_SALE)
            throw new InvalidPropertyException("mode", "Shop is not listed for sale.");

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
                    "Shop is not available. Current status: " + getStatus());
        if (getMode() != PropertyMode.FOR_RENT)
            throw new InvalidPropertyException("mode", "Shop is not listed for rent.");

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
            throw new InvalidPropertyException("owner", "Client does not own this shop.");

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
            throw new InvalidPropertyException("owner", "Client does not own this shop.");

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
            "Shop | %s, %s | Category: %s | %s | Parking: %d | PKR %.2f | %s",
            getAddress(), getCity(), shopCategory,
            hasStorageRoom ? "Storage Room" : "No Storage",
            getParkingSpots(), calculatePrice(), getStatus()
        );
    }

    // ─── Getters & Setters ────────────────────────────────────
    public String  getShopCategory()   { return shopCategory; }
    public boolean hasStorageRoom()    { return hasStorageRoom; }

    public void setShopCategory(String category) {
        validateCategory(category);
        this.shopCategory = category.trim();
    }

    public void setHasStorageRoom(boolean storage) { this.hasStorageRoom = storage; }
}
